package cn.kuaipan.android.http;

import android.util.Log;

public interface IKscTransferListener {

  void setSendTotal(long total);

  void setReceiveTotal(long total);

  void setSendPos(long pos);

  void setReceivePos(long pos);

  void sended(long len);

  void received(long len);

  public static abstract class KscTransferListener implements
      IKscTransferListener {
    private static final String LOG_TAG = "KscTransferListener";
    private long sendTotal = -1;
    private long receiveTotal = -1;
    private long sended = 0;
    private long received = 0;

    @Override
    public final void setSendTotal(long total) {
      this.sendTotal = total;
    }

    @Override
    public final void setReceiveTotal(long total) {
      this.receiveTotal = total;
    }

    @Override
    public final void sended(long len) {
      sended += len;
      try {
        onDataSended(sended, sendTotal);
      } catch (Error e) {
        throw e;
      } catch (Throwable t) {
        Log.e(LOG_TAG, "Meet exception in onDataSended()", t);
      }
    }

    @Override
    public final void received(long len) {
      received += len;
      try {
        onDataReceived(received, receiveTotal);
      } catch (Error e) {
        throw e;
      } catch (Throwable t) {
        Log.e(LOG_TAG, "Meet exception in onDataReceived()", t);
      }
    }

    @Override
    public final void setSendPos(long pos) {
      if (pos == sended) {
        return;
      }

      sended = pos;
      onDataSended(sended, sendTotal);
    }

    @Override
    public final void setReceivePos(long pos) {
      if (pos == received) {
        return;
      }
      received = pos;
      onDataReceived(received, receiveTotal);
    }

    abstract public void onDataSended(long pos, long total);

    abstract public void onDataReceived(long pos, long total);

  }

}
