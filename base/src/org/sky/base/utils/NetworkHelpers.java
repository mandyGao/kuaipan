/*
 * Copyright (C) 2010-2011 Android Cache Library Project,
 * All rights reserved by PCR(Wanzheng Ma)
 * Version 0.9
 * Date 2011-May-16
 * Support: pcrxjxj@gmail.com
 */

package org.sky.base.utils;

import org.apache.http.HttpHost;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

/**
 * Some helper functions for the download manager
 */
public class NetworkHelpers {
  private static final boolean LOGD = false;
  private static final String LOG_TAG = "NetworkHelpers";

  public static final int TYPE_INVALID = -1;
  public static final int TYPE_MOBILE = ConnectivityManager.TYPE_MOBILE;
  public static final int TYPE_WIFI = ConnectivityManager.TYPE_WIFI;

  private static final int TYPE_MOBILE_HIPRI = 5;

  private NetworkHelpers() {}

  /**
   * Returns whether the network is available
   */
  public static boolean isNetworkAvailable(Context context,
      boolean includeMobile, boolean includeRoaming) {
    if (context == null) {
      return true;
    }
    ConnectivityManager connectivity = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivity == null) {
      Log.w(LOG_TAG, "Couldn't get connectivity manager");
      return false;
    }

    boolean result = false;
    NetworkInfo info = connectivity.getActiveNetworkInfo();

    result = info != null && info.isConnected();
    result = result && (includeRoaming || !info.isRoaming());
    result = result && (includeMobile || !isMobile(info));

    if (LOGD) {
      Log.d(LOG_TAG, result ? "Network is available"
          : "Network is not available");
    }
    return result;
  }

  public static int getCurrentNetType(Context context) {
    ConnectivityManager cm = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    int result = TYPE_INVALID;
    if (cm != null) {
      NetworkInfo info = cm.getActiveNetworkInfo();
      if (info != null) {
        result = info.getType();
      }
    }
    return result;
  }

  public static HttpHost getCurrentProxy() {
    Context context = ContextUtils.getContext();
    if (getCurrentNetType(context) != TYPE_MOBILE) {
      return null;
    }

    HttpHost result = null;
    String host = android.net.Proxy.getDefaultHost();// æ­¤å??Proxyæº????android.net
    int port = android.net.Proxy.getDefaultPort();// ???ä¸?
    if (!TextUtils.isEmpty(host)) {
      result = new HttpHost(host, port);
    }
    return result;
  }

  private static boolean isMobile(NetworkInfo info) {
    int type = info.getType();

    return type == ConnectivityManager.TYPE_MOBILE
        || (type > ConnectivityManager.TYPE_WIFI && type <= TYPE_MOBILE_HIPRI);
  }

  /**
   * Returns whether the network is available
   */
  public static boolean isNetworkAvailable(Context context) {
    ConnectivityManager connectivity = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivity == null) {
      Log.w(LOG_TAG, "couldn't get connectivity manager");
    } else {
      NetworkInfo[] info = connectivity.getAllNetworkInfo();
      if (info != null) {
        for (int i = 0; i < info.length; i++) {
          if (info[i].isConnected()) {
            if (LOGD) {
              Log.d(LOG_TAG, "network is available");
            }
            return true;
          }
        }
      }
    }
    if (LOGD) {
      Log.d(LOG_TAG, "network is not available");
    }
    return false;
  }

  /**
   * Returns whether the network is roaming
   */
  public static boolean isNetworkRoaming(Context context) {
    ConnectivityManager connectivity = (ConnectivityManager) context
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity == null) {
      Log.w(LOG_TAG, "couldn't get connectivity manager");
    } else {
      NetworkInfo info = connectivity.getActiveNetworkInfo();
      if (info != null
          && info.getType() == ConnectivityManager.TYPE_MOBILE) {
        // TelephonyManager tm = (TelephonyManager) context
        // .getSystemService(Context.TELEPHONY_SERVICE);
        // if (tm != null && tm.isNetworkRoaming()) {
        if (info.isRoaming()) {
          if (LOGD) {
            Log.d(LOG_TAG, "network is roaming");
          }
          return true;
        } else {
          if (LOGD) {
            Log.d(LOG_TAG, "network is not roaming");
          }
        }
      } else {
        if (LOGD) {
          Log.d(LOG_TAG, "not using mobile network");
        }
      }
    }
    return false;
  }
}
