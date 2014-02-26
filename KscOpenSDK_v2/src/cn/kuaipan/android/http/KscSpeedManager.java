package cn.kuaipan.android.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import android.os.SystemClock;
import android.util.SparseArray;

public class KscSpeedManager {
  private static final int MAX_RECODE_DURATION = 60 * 60;
  private static final int MIN_RECODE_DURATION = 5 * 60;
  private static final int DEF_RECODE_DURATION = 10 * 60;

  private static final int RECODE_MASK = MAX_RECODE_DURATION * 1000;
  private static final long ERASE_DURATION = MIN_RECODE_DURATION * 1000;

  private final int mRecodeDuration;
  private final HashMap<String, SparseArray<Float>> mRecordMap;
  private long mLatestEraseTime = 0;

  public KscSpeedManager(int recodeDuration) {
    mRecordMap = new HashMap<String, SparseArray<Float>>();
    if (recodeDuration < 0) {
      recodeDuration = DEF_RECODE_DURATION;
    }
    mRecodeDuration = Math.min(MAX_RECODE_DURATION,
        Math.max(MIN_RECODE_DURATION, recodeDuration));
  }

  public KscSpeedMonitor getMoniter(String host) {
    return new KscSpeedMonitor(this, host);
  }

  public synchronized int getTransmitSize(String host, int duration,
      int timeOffset) {
    if (timeOffset >= mRecodeDuration) {
      return -2;
    }
    SparseArray<Float> recodes = mRecordMap.get(host);
    if (recodes == null || recodes.size() <= 0) {
      return -1;
    }

    final long current = current();
    duration = Math.min(duration, mRecodeDuration - timeOffset);

    final int currentKey = computeKey(current);
    int endKey = currentKey - timeOffset;
    int startKey = endKey - duration;

    List<Float> validRecoders = findValidRecoders(recodes, startKey, endKey);
    if (validRecoders == null || validRecoders.isEmpty()) {
      return -1;
    }

    float sum = 0;
    for (Float recoder : validRecoders) {
      sum += recoder;
    }
    return (int) sum;
  }

  public int getAverageSpeed(int duration) {
    return getAverageSpeed(null, duration, 0);
  }

  public int getAverageSpeed(String host, int duration) {
    return getAverageSpeed(host, duration, 0);
  }

  /**
   * Returns the average speed in a duration time. Time unit is second. Speed
   * unit is byte/second. Return -2 if timeOffset is too large. Return -1 if
   * no speed recodes
   * 
   * @param host
   * @param duration
   * @param timeOffset
   * @return
   */
  public synchronized int getAverageSpeed(String host, int duration,
      int timeOffset) {
    if (timeOffset >= mRecodeDuration) {
      return -2;
    }
    SparseArray<Float> recodes = mRecordMap.get(host);
    if (recodes == null || recodes.size() <= 0) {
      return -1;
    }

    final long current = current();
    duration = Math.min(duration, mRecodeDuration - timeOffset);

    final int currentKey = computeKey(current);
    int endKey = currentKey - timeOffset;
    int startKey = endKey - duration;

    List<Float> validRecoders = findValidRecoders(recodes, startKey, endKey);
    if (validRecoders == null || validRecoders.isEmpty()) {
      return -1;
    }

    int size = validRecoders.size();
    float sum = 0;
    for (Float recoder : validRecoders) {
      sum += recoder;
    }
    return (int) (sum / size);
  }

  private static List<Float> findValidRecoders(SparseArray<Float> recodes,
      final int startKey, final int endKey) {
    LinkedList<Float> validRecoders = new LinkedList<Float>();
    final boolean broken = endKey < startKey;
    final int size = recodes.size();
    if (broken) {
      for (int index = 0; index < size; index++) {
        int key = recodes.keyAt(index);
        if (key <= endKey || key >= startKey) {
          validRecoders.add(recodes.valueAt(index));
        }
      }
    } else {
      for (int index = 0; index < size; index++) {
        int key = recodes.keyAt(index);
        if (key <= endKey && key >= startKey) {
          validRecoders.add(recodes.valueAt(index));
        } else if (key > endKey) {
          break;
        }
      }
    }

    return validRecoders;
  }

  public synchronized void recoder(String host, long start, long end,
      float size) {
    if (end < start || size < 0) {
      return;
    }

    final long sSec = start / 1000;
    final long eSec = end / 1000;
    if (eSec == sSec) {
      int key = computeKey(start);
      appendRecoder(host, key, size);
    } else if (eSec - sSec <= 1) {
      long dur = end - start;
      float header = size * (1000 - (start % 1000)) / dur;
      float foot = size * (end % 1000) / dur;

      int headerKey = computeKey(start);
      int footKey = computeKey(end);

      appendRecoder(host, headerKey, header);
      appendRecoder(host, footKey, foot);
    } else {
      long dur = end - start;
      float header = size * (1000 - (start % 1000)) / dur;
      float foot = size * (end % 1000) / dur;
      float body = (size - header - foot) / (eSec - sSec - 1);

      int headerKey = computeKey(start);
      int footKey = computeKey(end);

      appendRecoder(host, headerKey, header);
      appendRecoder(host, footKey, foot);

      int bodyStart = headerKey + 1;
      int bodyEnd = footKey - 1;

      appendRecoders(host, bodyStart, bodyEnd, body);
    }

    eraseExpired();
  }

  private void eraseExpired() {
    final long current = current();
    if ((current - mLatestEraseTime) <= ERASE_DURATION) {
      return;
    }

    final int endKey = computeKey(current);
    final int startKey = endKey - mRecodeDuration;
    final boolean broken = endKey < startKey;

    LinkedList<String> removeList = new LinkedList<String>();

    for (Entry<String, SparseArray<Float>> entity : mRecordMap.entrySet()) {
      String host = entity.getKey();
      SparseArray<Float> recodes = entity.getValue();

      if (broken) {
        int index = 0;
        while (index < recodes.size()) {
          int key = recodes.keyAt(index);
          if (key > endKey && key < startKey) {
            recodes.delete(key);
          } else if (key >= startKey) {
            break;
          } else {
            index++;
          }
        }
      } else {
        int index = 0;
        while (index < recodes.size()) {
          int key = recodes.keyAt(index);
          if (key > endKey || key < startKey) {
            recodes.delete(key);
          } else {
            index++;
          }
        }
      }

      if (recodes.size() <= 0) {
        removeList.add(host);
      }
    }

    for (String host : removeList) {
      mRecordMap.remove(host);
    }
    mLatestEraseTime = current;
  }

  private static int computeKey(long start) {
    return (int) ((start / 1000) % RECODE_MASK);
  }

  private void appendRecoders(String host, int startKey, int endKey,
      float size) {
    if (endKey >= startKey) {
      for (int key = startKey; key <= endKey; key++) {
        appendRecoder(host, key, size);
      }
    } else {
      for (int key = startKey; key < RECODE_MASK; key++) {
        appendRecoder(host, key, size);
      }

      for (int key = 0; key <= endKey; key++) {
        appendRecoder(host, key, size);
      }
    }
  }

  private void appendRecoder(String host, int key, float size) {
    SparseArray<Float> recoders = mRecordMap.get(host);
    if (recoders == null) {
      recoders = new SparseArray<Float>();
      mRecordMap.put(host, recoders);
    }
    float old = recoders.get(key, 0f);
    recoders.put(key, old + size);

    if (host != null) {
      appendRecoder(null, key, size);
    }
  }

  public static long current() {
    return SystemClock.elapsedRealtime();
  }
}
