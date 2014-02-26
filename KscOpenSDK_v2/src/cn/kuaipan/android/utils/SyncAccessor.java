package cn.kuaipan.android.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class SyncAccessor extends Handler {
  public static class Args {
    public volatile boolean handled = false;
    public Object[] params;
    public Object result;
    public RuntimeException err;

    private static Object mPoolSync = new Object();
    private static Args mPool;
    private static int mPoolSize = 0;

    private static final int MAX_POOL_SIZE = 10;

    private Args next;

    public static Args obtain() {
      synchronized (mPoolSync) {
        if (mPool != null) {
          Args m = mPool;
          mPool = m.next;
          m.next = null;
          return m;
        }
      }
      return new Args();
    }

    public void recycle() {
      synchronized (mPoolSync) {
        if (mPoolSize < MAX_POOL_SIZE) {
          clearForRecycle();

          next = mPool;
          mPool = this;
        }
      }
    }

    private void clearForRecycle() {
      params = null;
      result = null;
      err = null;
      handled = false;
    }
  }

  public SyncAccessor(Looper looper) {
    super(looper);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T> T access(int what, Object... objects)
      throws InterruptedException {
    Args args = Args.obtain();
    args.params = objects;

    if (sendMessage(obtainMessage(what, args))) {
      while (!args.handled) {
        if (!isAlive()) {
          throw new RuntimeException("SyncAccessor has dead.");
        }

        synchronized (args) {
          args.wait(50);
        }
      }
    } else {
      throw new RuntimeException("SyncAccessor has dead.");
    }

    Object result = args.result;
    RuntimeException err = args.err;
    args.recycle();

    if (err != null) {
      throw err;
    }

    return (T) result;
  }

  private boolean isAlive() {
    Looper looper = getLooper();
    if (looper == null) {
      return false;
    }
    Thread t = looper.getThread();
    if (t == null || !t.isAlive()) {
      return false;
    }
    return true;
  }

  public Object handleAccess(int what, Object... objects) {
    return null;
  }

  public void dispatchMessage(Message msg) {
    Object obj = msg.obj;
    if (obj != null && obj instanceof Args) {
      Args args = (Args) obj;

      try {
        args.result = handleAccess(msg.what, args.params);
      } catch (RuntimeException e) {
        args.err = e;
      } finally {
        args.handled = true;
        synchronized (args) {
          args.notifyAll();
        }
      }
    } else {
      super.dispatchMessage(msg);
    }
  }
}
