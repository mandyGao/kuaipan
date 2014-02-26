package org.sky.kuaipan;

import android.app.Application;
import cn.kuaipan.android.sdk.internal.OAuthApi;
import cn.kuaipan.android.sdk.oauth.Session;
import cn.kuaipan.android.sdk.oauth.Session.Root;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class KuaipanApplication extends Application {

  private static final String KSC_OAUTH_KEY = "xcInFxiv9tnMmS5a";
  private static final String KSC_OAUTH_SECRET = "D7JvQn0wTR5rP9D9";

  private static KuaipanApplication instance;

  private OAuthApi kscApi;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
    // config kuaipan oauth kscApi
    kscApi = new OAuthApi(this, new Session(KSC_OAUTH_KEY, KSC_OAUTH_SECRET, Root.KUAIPAN));
    // Create global configuration and initialize ImageLoader with this configuration
    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
    ImageLoader.getInstance().init(config);

  }

  public static KuaipanApplication getInstance() {
    return instance;
  }

  public OAuthApi getKscApi() {
    return kscApi;
  }

}
