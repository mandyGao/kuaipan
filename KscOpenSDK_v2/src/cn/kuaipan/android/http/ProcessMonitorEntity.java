package cn.kuaipan.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

public class ProcessMonitorEntity extends HttpEntityWrapper {
  private final KscSpeedMonitor mMonitor;
  private final IKscTransferListener mListener;
  private final boolean mSendMode;

  private boolean mMonitorUsed;

  public ProcessMonitorEntity(HttpEntity wrapped, KscSpeedMonitor monitor,
      IKscTransferListener listener, boolean sendMode) {
    super(wrapped);
    mMonitor = monitor;
    mListener = listener;
    mSendMode = sendMode;
    mMonitorUsed = false;
  }

  @Override
  public InputStream getContent() throws IOException {
    InputStream result = super.getContent();
    if (!mMonitorUsed) {
      if (mListener != null) {
        if (mSendMode) {
          mListener.setSendTotal(this.getContentLength());
        } else {
          mListener.setReceiveTotal(this.getContentLength());
        }
      }
      result = new ProcessMonitorInputStream(result, mMonitor, mListener,
          mSendMode);
      mMonitorUsed = true;
    }
    return result;
  }

  @Override
  public void writeTo(OutputStream outstream) throws IOException {
    if (!mMonitorUsed) {
      outstream = new ProcessMonitorOutputStream(outstream, mMonitor,
          mListener, mSendMode);

      mMonitorUsed = true;
    }
    super.writeTo(outstream);
  }
}
