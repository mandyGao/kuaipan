package org.sky.base.utils;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

public class ConstInfo {
  private static final String LOG_TAG = "ConstInfo";

  private static final HashMap<ConstKey, String> sValueMap = new HashMap<ConstKey, String>();

  public enum ConstKey {
    SDK_VERSION, APP_VERSION, APP_VERSION_CODE, APP_NAME, APP_PACKAGE, SCREEN_SIZE, DEVICE_MODEL,
    DEVICE_ID, OPERATORS, CHANNEL, PROCESS_NAME, PHONE_NUMBER, PHONE_IMEI, PHONE_IMSI
  }

  public static String getValue(Context context, ConstKey key) {
    if (key == null) {
      return null;
    }

    String result = sValueMap.get(key);
    if (result != null) {
      return result;
    }

    result = loadValue(context, key);
    sValueMap.put(key, result);
    return result;
  }

  private static String loadValue(Context context, ConstKey key) {
    String result = null;
    switch (key) {
      case SDK_VERSION:
        result = getSdkVersion(context);
        break;
      case APP_VERSION:
        result = getAppVersion(context);
        break;
      case APP_VERSION_CODE:
        result = getAppVersionCode(context);
        break;
      case APP_NAME:
        result = getAppName(context);
        break;
      case APP_PACKAGE:
        result = getAppPackage(context);
        break;
      case SCREEN_SIZE:
        result = getScreenSize(context);
        break;
      case DEVICE_MODEL:
        result = getDeviceModel(context);
        break;
      case DEVICE_ID:
        result = getDeviceId(context);
        break;
      case OPERATORS:
        result = getOperators(context);
        break;
      case CHANNEL:
        result = getChannel(context);
        break;
      case PROCESS_NAME:
        result = getProcessName(context);
        break;
      case PHONE_NUMBER:
        result = getPhoneNumber(context);
        break;
      case PHONE_IMEI:
        result = getPhoneIMEI(context);
        break;
      case PHONE_IMSI:
        result = getPhoneIMSI(context);
        break;
      default:
        result = null;
    }

    return result;
  }

  private static String getSdkVersion(Context context) {
    return Build.VERSION.RELEASE;
  }

  private static String getAppVersion(Context context) {
    loadPackageInfo(context);
    return sValueMap.get(ConstKey.APP_VERSION);
  }

  private static String getAppVersionCode(Context context) {
    loadPackageInfo(context);
    return sValueMap.get(ConstKey.APP_VERSION_CODE);
  }

  private static String getAppName(Context context) {
    loadAppInfo(context);
    return sValueMap.get(ConstKey.APP_NAME);
  }

  private static String getAppPackage(Context context) {
    return context.getPackageName();
  }

  private static void loadPackageInfo(Context context) {
    String packageName = context.getPackageName();
    sValueMap.put(ConstKey.APP_PACKAGE, packageName);

    PackageManager pm = context.getPackageManager();
    try {
      PackageInfo info = pm.getPackageInfo(packageName, 0);
      String version = info.versionName;
      if (version == null) {
        version = "code(" + info.versionCode + ")";
      }
      sValueMap.put(ConstKey.APP_VERSION, version);
      sValueMap.put(ConstKey.APP_VERSION_CODE,
          String.valueOf(info.versionCode));
    } catch (NameNotFoundException e) {
      Log.w(LOG_TAG, "Exception when retrieving package:" + packageName);
    }
  }

  private static String getScreenSize(Context context) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return String
        .format("%d-%d", metrics.widthPixels, metrics.heightPixels);
  }

  private static String getDeviceModel(Context context) {
    return Build.MODEL;
  }

  private static String getDeviceId(Context context) {
    TelephonyManager tm = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    String tmDevice = null;
    String tmSerial = null;
    String androidId = null;
    try {
      tmDevice = tm.getDeviceId();
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }
    try {
      tmSerial = tm.getSimSerialNumber();
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }
    androidId = Secure.getString(context.getContentResolver(),
        Secure.ANDROID_ID);
    if (tmDevice == null) {
      tmDevice = "";
    }
    if (tmSerial == null) {
      tmSerial = "";
    }
    if (androidId == null) {
      androidId = "";
    }
    UUID deviceUuid = new UUID(androidId.hashCode(),
        ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
    return "lsa-kp" + deviceUuid.toString();
  }

  private static String getOperators(Context context) {
    TelephonyManager tel = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    return tel == null ? "" : tel.getSimOperator();
  }

  private static String getChannel(Context context) {
    loadAppInfo(context);
    return sValueMap.get(ConstKey.CHANNEL);
  }

  private static void loadAppInfo(Context context) {
    try {
      PackageManager pmg = context.getPackageManager();
      ApplicationInfo info = pmg.getApplicationInfo(
          context.getPackageName(), PackageManager.GET_META_DATA);

      CharSequence label = info.loadLabel(pmg);

      sValueMap.put(ConstKey.APP_NAME, label == null ? info.packageName
          : label.toString());

      Bundle bundle = info.metaData;
      String value = bundle == null ? null : bundle
          .getString("UMENG_CHANNEL");
      sValueMap.put(ConstKey.CHANNEL, value == null ? "" : value);
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }
  }

  private static String getProcessName(Context context) {
    ActivityManager activityManager = (ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningAppProcessInfo> infos = activityManager
        .getRunningAppProcesses();
    int pid = Process.myPid();
    RunningAppProcessInfo myInfo = null;
    for (RunningAppProcessInfo info : infos) {
      if (info.pid == pid) {
        myInfo = info;
        break;
      }
    }

    return myInfo == null ? null : myInfo.processName;
  }

  private static String getPhoneNumber(Context context) {
    String result = null;
    try {
      TelephonyManager tm = (TelephonyManager) context
          .getSystemService(Context.TELEPHONY_SERVICE);
      result = tm.getLine1Number();
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }
    return result == null ? "" : result;
  }

  private static String getPhoneIMEI(Context context) {
    String result = null;
    try {
      TelephonyManager tm = (TelephonyManager) context
          .getSystemService(Context.TELEPHONY_SERVICE);
      result = tm.getDeviceId();
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }

    return result == null ? "" : result;
  }

  private static String getPhoneIMSI(Context context) {
    String result = null;
    try {
      TelephonyManager tm = (TelephonyManager) context
          .getSystemService(Context.TELEPHONY_SERVICE);
      result = tm.getSubscriberId();
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }

    return result == null ? "" : result;
  }
}
