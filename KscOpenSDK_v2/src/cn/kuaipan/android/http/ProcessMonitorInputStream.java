package cn.kuaipan.android.http;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProcessMonitorInputStream extends FilterInputStream {
  private final KscSpeedMonitor mMonitor;
  private final IKscTransferListener mListener;
  private final boolean mSendMode;

  private long mCurrent = 0;
  private long mMarkPos = 0;

  protected ProcessMonitorInputStream(InputStream in,
      KscSpeedMonitor monitor, IKscTransferListener listener,
      boolean sendMode) {
    super(in);
    mMonitor = monitor;
    mListener = listener;
    mSendMode = sendMode;
  }

  private void process(long len) {
    if (len >= 0) {
      mCurrent += len;
      if (mMonitor != null) {
        mMonitor.recode(len);
      }
      if (mListener != null) {
        if (mSendMode) {
          mListener.sended(len);
        } else {
          mListener.received(len);
        }
      }
    }
  }

  @Override
  public int read() throws IOException {
    int result = super.read();
    if (result != -1) {
      process(1);
    }
    return result;
  }

  @Override
  public int read(byte[] buffer, int offset, int count) throws IOException {
    int len = super.read(buffer, offset, count);
    if (len > 0) {
      process(len);
    }
    return len;
  }

  @Override
  public long skip(long count) throws IOException {
    long len = super.skip(count);
    if (len > 0) {
      process(len);
    }
    return len;
  }

  @Override
  public synchronized void mark(int readlimit) {
    super.mark(readlimit);
    mMarkPos = mCurrent;
  }

  @Override
  public synchronized void reset() throws IOException {
    super.reset();
    mCurrent = mMarkPos;

    if (mListener != null) {
      if (mSendMode) {
        mListener.setSendPos(mCurrent);
      } else {
        mListener.setReceivePos(mCurrent);
      }
    }
  }

}
