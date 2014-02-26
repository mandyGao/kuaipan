package cn.kuaipan.android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

import android.util.Log;

public class Encode {

  private static final String LOG_TAG = "Encode";

  public static byte[] hexStringToByteArray(String string) {
    byte[] bytes = new byte[string.length() / 2];
    for (int i = 0; i < bytes.length; ++i) {
      bytes[i] = (byte) (Character.digit(string.charAt(i * 2), 16) * 16 + Character
          .digit(string.charAt(i * 2 + 1), 16));
    }
    return bytes;
  }

  // ��存�拌浆���涓�16杩���跺��绗�涓茬����跺��锛�缁�涓�浣跨�ㄥ�����瀛�姣�
  final static String[] HEXDIGITS = {
      "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c",
      "d", "e", "f"
  };

  public static String byteArrayToHexString(byte[] b) {
    if (b == null) {
      return null;
    }

    StringBuffer resultSB = new StringBuffer(b.length * 2);
    for (int i = 0; i < b.length; i++) {
      resultSB.append(HEXDIGITS[b[i] >>> 4 & 0xf]);
      resultSB.append(HEXDIGITS[b[i] & 0xf]);
    }
    return resultSB.toString();
  }

  public static String byteToHexString(byte b) {
    return (HEXDIGITS[b >>> 4 & 0xf] + HEXDIGITS[b & 0xf]);
  }

  public static String intToHexString(int num) {
    byte[] arr = new byte[4];
    arr[0] = (byte) ((num >> 24) & 0xFF);
    arr[1] = (byte) ((num >> 16) & 0xFF);
    arr[2] = (byte) ((num >> 8) & 0xFF);
    arr[3] = (byte) (num & 0xFF);
    return byteArrayToHexString(arr);
  }

  public static String longToHexString(long num) {
    byte[] arr = new byte[8];
    for (int i = 0; i < 8; i++) {
      arr[i] = (byte) (num >> (8 * (7 - i)) & 0xFF);
    }
    return byteArrayToHexString(arr);
  }

  public static short byteArrayToShort(byte[] arr, int startIdx) {
    short r = (short) ((arr[startIdx] << 8) | (arr[startIdx + 1] & 0xFF));
    return r;
  }

  public static int byteArrayToInt(byte[] arr, int startIdx) {
    // 涓�妫���ヨ�����
    int r = (arr[startIdx] & 0xFF) << 24 | (arr[startIdx + 1] & 0xFF) << 16
        | (arr[startIdx + 2] & 0xFF) << 8 | (arr[startIdx + 3] & 0xFF);
    return r;
  }

  public static long byteArrayToLong(byte[] arr, int startIdx) {
    // 涓�妫���ヨ�����
    int endIdx = startIdx + 8;

    long r = arr[startIdx];
    for (int i = startIdx + 1; i < endIdx; i++) {
      r = r << 8;
      r = r | (arr[i] & 0xFF);
    }
    return r;
  }

  // MD5
  public static String MD5Encode(byte[] oriData) {
    String md5 = null;
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md5 = byteArrayToHexString(md.digest(oriData));
    } catch (Exception e) {
      Log.e(LOG_TAG, "MD5Encode failed.", e);
      return null;
    }
    return md5;
  }

  // SHA1
  public static String SHA1Encode(byte[] oriData) {
    String sha1 = null;
    try {
      MessageDigest md = MessageDigest.getInstance("sha1");
      sha1 = byteArrayToHexString(md.digest(oriData));
    } catch (Exception e) {
      Log.e(LOG_TAG, "SHA1Encode failed.", e);
      return null;
    }
    return sha1;
  }

  public static String SHA1Encode(InputStream in) {
    try {
      MessageDigest md = MessageDigest.getInstance("sha1");
      byte[] buf = new byte[16 * 1024];
      int len = -1;
      while ((len = in.read(buf)) >= 0) {
        md.update(buf, 0, len);
      }

      return byteArrayToHexString(md.digest());
    } catch (Exception e) {
      Log.e(LOG_TAG, "SHA1Encode failed.", e);
      return null;
    }
  }

  public static String SHA1Encode(InputStream in, int size) {
    try {
      MessageDigest md = MessageDigest.getInstance("sha1");
      byte[] buf = new byte[16 * 1024];
      int len = -1;
      int pos = 0;
      while ((len = in.read(buf, 0, Math.min(buf.length, size - pos))) >= 0) {
        md.update(buf, 0, len);
        pos += len;
        if (size - pos <= 0) {
          break;
        }
      }

      return byteArrayToHexString(md.digest());
    } catch (Exception e) {
      Log.e(LOG_TAG, "SHA1Encode failed.", e);
      return null;
    }
  }

  public static String SHA1Encode(File file) {
    if (!file.exists() || !file.isFile()) {
      return "";
    }

    String r = null;
    InputStream in = null;
    try {
      // in = new BufferedInputStream(new FileInputStream(file));
      in = new FileInputStream(file);
      r = SHA1Encode(in);
    } catch (IOException e) {
      Log.e(LOG_TAG, "Failed compute SHA1 for file: " + file);
    } finally {
      try {
        in.close();
      } catch (Throwable t) {
        // ignore
      }
    }

    return r;
  }

  public static String SHA1Encode(RandomAccessFile file, long start, long len) {
    try {
      MessageDigest md = MessageDigest.getInstance("sha1");
      byte[] buf = new byte[8 * 1024];

      long end = start + len;
      long pos = start;
      file.seek(pos);

      int l = -1;
      while ((l = file
          .read(buf, 0, (int) Math.min(buf.length, end - pos))) >= 0) {
        md.update(buf, 0, l);
        pos += l;
        if (pos >= end) {
          break;
        }
      }

      if (pos < end) {
        Log.w(LOG_TAG, "File size may not enough for sha1.");
        return null;
      }

      return byteArrayToHexString(md.digest());
    } catch (Exception e) {
      Log.e(LOG_TAG, "SHA1Encode failed.", e);
      return null;
    }
  }
}
