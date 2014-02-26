package cn.kuaipan.android.http;

public class KscSpeedMonitor {
  private final KscSpeedManager mManager;
  private final String mHost;

  private long mLatestUpdate;

  public KscSpeedMonitor(KscSpeedManager speedManager, String host) {
    mManager = speedManager;
    mHost = host;
    mLatestUpdate = KscSpeedManager.current();
  }

  public void recode(long start, long end, long size) {
    if (mManager != null) {
      mManager.recoder(mHost, start, end, size);
      if (end > mLatestUpdate) {
        mLatestUpdate = end;
      }
    }
  }

  public void recode(long size) {
    if (mManager != null) {
      long current = KscSpeedManager.current();
      mManager.recoder(mHost, mLatestUpdate, current, size);
      mLatestUpdate = current;
    }
  }

  public String getHost() {
    return mHost;
  }
}
