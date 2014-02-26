package cn.kuaipan.android.utils;

import java.util.HashMap;
import java.util.Map;

public class ObtainabelHashMap<K, V> extends HashMap<K, V> implements
    IObtainable {

  private static final long serialVersionUID = 5201260832948788096L;

  private static final int MAX_POOL_SIZE = 500;
  private static Object mPoolSync = new Object();
  private static ObtainabelHashMap<?, ?> mPool;
  private static int mPoolSize = 0;
  private ObtainabelHashMap<?, ?> next;

  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  public static <K, V> ObtainabelHashMap<K, V> obtain() {
    synchronized (mPoolSync) {
      if (mPool != null) {
        ObtainabelHashMap m = mPool;
        mPool = m.next;
        m.next = null;
        mPoolSize--;
        m.clear();
        return m;
      }
    }
    return new ObtainabelHashMap<K, V>();
  }

  @Override
  public void recycle() {
    synchronized (mPoolSync) {
      if (mPoolSize < MAX_POOL_SIZE) {
        mPoolSize++;
        next = mPool;
        mPool = this;
      }

      for (V value : values()) {
        if (value instanceof IObtainable) {
          ((IObtainable) value).recycle();
        }
      }
      clear();
    }
  }

  private ObtainabelHashMap() {
    super();
  }

  private ObtainabelHashMap(int capacity, float loadFactor) {
    super(capacity, loadFactor);
  }

  private ObtainabelHashMap(int capacity) {
    super(capacity);
  }

  private ObtainabelHashMap(Map<? extends K, ? extends V> map) {
    super(map);
  }

}
