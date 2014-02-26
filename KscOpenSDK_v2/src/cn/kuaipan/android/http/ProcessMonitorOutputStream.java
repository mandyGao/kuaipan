package cn.kuaipan.android.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ProcessMonitorOutputStream extends FilterOutputStream {
  private final KscSpeedMonitor mMonitor;
  private final IKscTransferListener mListener;
  private final boolean mSendMode;

  public ProcessMonitorOutputStream(OutputStream out,
      KscSpeedMonitor monitor, IKscTransferListener listener,
      boolean sendMode) {
    super(out);
    mMonitor = monitor;
    mListener = listener;
    mSendMode = sendMode;
  }

  private void process(long len) {
    if (len >= 0) {
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
  public void write(byte[] buffer, int offset, int count) throws IOException {
    out.write(buffer, offset, count);
    process(count);
  }

  @Override
  public void write(int oneByte) throws IOException {
    super.write(oneByte);
    process(1);
  }

}
