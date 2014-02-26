package org.sky.base.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class CollectionMap<K, V, E extends Collection<V>> {
  private HashMap<K, E> mMap = new HashMap<K, E>();

  public synchronized void put(K key, V value) {
    E collection = mMap.get(key);
    if (collection == null) {
      collection = createCollection();
      mMap.put(key, collection);
    }
    collection.add(value);
  }

  public synchronized E remove(K key) {
    return mMap.remove(key);
  }

  public synchronized boolean removeValue(K key, V value) {
    E collection = mMap.get(key);
    if (collection == null) {
      return false;
    }
    boolean result = collection.remove(value);
    if (result) {
      if (collection.isEmpty()) {
        mMap.remove(key);
      }
    }

    return result;
  }

  public synchronized boolean removeValue(V value) {
    Iterator<Map.Entry<K, E>> iterator = mMap.entrySet().iterator();

    boolean result = false;
    while (iterator.hasNext()) {
      Map.Entry<K, E> entry = iterator.next();
      E collection = entry.getValue();
      if (collection == null) {
        iterator.remove();
        continue;
      }
      if (collection.remove(value)) {
        result = true;
        if (collection.isEmpty()) {
          iterator.remove();
        }
        break;
      }
    }

    return result;
  }

  abstract protected E createCollection();

  public synchronized void clear() {
    mMap.clear();
  }
}
