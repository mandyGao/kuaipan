package cn.kuaipan.android.utils;

import java.util.Collection;
import java.util.LinkedList;

public class ObtainabelList<E> extends LinkedList<E> implements IObtainable {

  private static final long serialVersionUID = 6483198895359712723L;

  private static final int MAX_POOL_SIZE = 80;
  private static Object mPoolSync = new Object();
  private static ObtainabelList<?> mPool;
  private static int mPoolSize = 0;
  private ObtainabelList<?> next;

  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  public static <E> ObtainabelList<E> obtain() {
    synchronized (mPoolSync) {
      if (mPool != null) {
        ObtainabelList m = mPool;
        mPool = m.next;
        m.next = null;
        mPoolSize--;
        m.clear();
        return m;
      }
    }
    return new ObtainabelList<E>();
  }

  @Override
  public void recycle() {
    synchronized (mPoolSync) {
      if (mPoolSize < MAX_POOL_SIZE) {
        mPoolSize++;
        next = mPool;
        mPool = this;
      }

      for (E value : this) {
        if (value instanceof IObtainable) {
          ((IObtainable) value).recycle();
        }
      }
      clear();
    }
  }

  private ObtainabelList() {
    super();
  }

  private ObtainabelList(Collection<? extends E> collection) {
    super(collection);
  }

}
