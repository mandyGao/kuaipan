package org.sky.kuaipan;

public interface Callback<T> {
  
  void onSuccess(T result);
  
  void onFailed(Throwable throwable);
}
