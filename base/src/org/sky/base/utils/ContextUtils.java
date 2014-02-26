package org.sky.base.utils;

import org.apache.http.util.LangUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.util.List;

public class ContextUtils {
  private static final String LOG_TAG = "ContextUtils";

  private volatile static Context sMyContext;

  public static void init(Context myContext) {
    if (myContext != null) {
      sMyContext = myContext;
    }
  }

  public static Context getContext() {
    Context result = sMyContext;
    if (result == null) {
      synchronized (ContextUtils.class) {
        result = sMyContext;
        if (result == null) {
          try {
            Object obj = JavaCalls.callStaticMethod(
                "android.app.ActivityThread",
                "currentActivityThread");
            result = JavaCalls.callMethod(obj, "getApplication");
          } catch (Exception e) {
            // ignore
          }
          if (result == null) {
            throw new RuntimeException(
                "My Application havn't be call onCreate by Framework.");
          }
          sMyContext = result;
        }
      }

    }
    return result;
  }

  public static String getPackageName() {
    Context context = getContext();
    return context.getPackageName();
  }

  public static String getAppName() {
    return getAppName(getContext());
  }

  public static String getAppName(Context context) {
    ApplicationInfo info = context.getApplicationInfo();
    CharSequence label = info.loadLabel(context.getPackageManager());

    return label == null ? info.packageName : label.toString();
  }

  public static String getAppVersion() {
    return getAppVersion(getContext());
  }

  public static String getAppVersion(Context context) {
    String packageName = context.getPackageName();
    PackageManager pm = context.getPackageManager();

    String result = null;
    try {
      PackageInfo info = pm.getPackageInfo(packageName, 0);
      result = info.versionName;
    } catch (NameNotFoundException e) {
      Log.w(LOG_TAG, "Exception when retrieving package:" + packageName);
    }

    return result;
  }

  public static int getAppVersionCode(Context context) {
    String packageName = context.getPackageName();
    PackageManager pm = context.getPackageManager();

    int result = -1;
    try {
      PackageInfo info = pm.getPackageInfo(packageName, 0);
      result = info.versionCode;
    } catch (NameNotFoundException e) {
      Log.w(LOG_TAG, "Exception when retrieving package:" + packageName);
    }

    return result;
  }

  public static boolean isAtFront(Context context) {
    ActivityManager manager = (ActivityManager) context
        .getSystemService(Context.ACTIVITY_SERVICE);
    List<RunningTaskInfo> task = manager.getRunningTasks(1);
    if (task == null || task.size() == 0) {
      return false;
    }

    String topPackage = task.get(0).topActivity.getPackageName();
    return LangUtils.equals(topPackage, context.getPackageName());
  }
}
