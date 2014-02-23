package org.sky.kuaipan;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class KuaipanApplication extends Application {
  
  private static KuaipanApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    // Create global configuration and initialize ImageLoader with this configuration
    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
    ImageLoader.getInstance().init(config);
  }
  
  public static KuaipanApplication getInstance() {
    return instance;
  }
}
