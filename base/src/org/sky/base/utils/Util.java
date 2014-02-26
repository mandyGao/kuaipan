package org.sky.base.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;

@Deprecated
public class Util {
  private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

  private static final String LOG_TAG = "Util";

  public static boolean isSDCardReady() {
    return Environment.MEDIA_MOUNTED.equals(Environment
        .getExternalStorageState());
  }

  public static String makePath(String path1, String path2) {
    if (path1.endsWith(File.separator))
      return path1 + path2;

    return path1 + File.separator + path2;
  }

  public static String getSdDirectory() {
    return Environment.getExternalStorageDirectory().getPath();
  }

  public static long getSDFreeSize() {
    File path = Environment.getExternalStorageDirectory();
    StatFs sf = new StatFs(path.getPath());
    long blockSize = sf.getBlockSize();
    long freeBlocks = sf.getAvailableBlocks();
    return freeBlocks * blockSize;
  }

  public static long getSDAllSize() {
    File path = Environment.getExternalStorageDirectory();
    StatFs sf = new StatFs(path.getPath());
    long blockSize = sf.getBlockSize();
    long allBlocks = sf.getBlockCount();
    return allBlocks * blockSize;
  }

  public static File getCachePath(Context context) {
    String cachepath = "";
    if (Environment.MEDIA_MOUNTED.equals(Environment
        .getExternalStorageState())) {
      cachepath = String.format("%s/Android/data/%s/cache", Environment
          .getExternalStorageDirectory().getAbsolutePath(), context
          .getApplicationInfo().packageName);
    } else {
      cachepath = context.getCacheDir().getAbsolutePath();
    }
    File cacheFile = new File(cachepath);
    if (!cacheFile.exists()) {
      cacheFile.mkdirs();
    }
    return cacheFile;
  }

  public static boolean isNormalFile(String fullName) {
    return !fullName.equals(ANDROID_SECURE);
  }

  public static Drawable getApkIcon(Context context, String path) {
    PackageManager pm = context.getPackageManager();
    PackageInfo info = pm.getPackageArchiveInfo(path,
        PackageManager.GET_ACTIVITIES);
    if (info != null) {
      ApplicationInfo appInfo = info.applicationInfo;
      try {
        return pm.getApplicationIcon(appInfo);
      } catch (OutOfMemoryError e) {
        Log.e(LOG_TAG, e.toString());
      }
    }
    return null;
  }

  public static String getAppMetadata(Context context, String key) {
    String strValue = "";
    try {
      PackageManager mgr = context.getPackageManager();
      Bundle bundle = mgr.getApplicationInfo(context.getPackageName(),
          PackageManager.GET_META_DATA).metaData;
      // Bundle bundle = context.getApplicationInfo().metaData;
      if (bundle != null && bundle.containsKey(key)) {
        strValue = bundle.getString(key);
      }
    } catch (Exception e) {
      Log.w(LOG_TAG, e);
    }

    return strValue;
  }

  public static String getExtFromFilename(String filename) {
    int dotPosition = filename.lastIndexOf('.');
    if (dotPosition != -1) {
      return filename.substring(dotPosition + 1, filename.length());
    }
    return "";
  }

  public static String getNameFromFilename(String filename) {
    int dotPosition = filename.lastIndexOf('.');
    if (dotPosition != -1) {
      return filename.substring(0, dotPosition);
    }
    return "";
  }

  public static String getPathFromFilepath(String filepath) {
    int pos = filepath.lastIndexOf('/');
    if (pos != -1) {
      return filepath.substring(0, pos);
    }
    return "";
  }

  public static String getNameFromFilepath(String filepath) {
    int pos = filepath.lastIndexOf('/');
    if (pos != -1) {
      return filepath.substring(pos + 1);
    }
    return "";
  }

  // return new file path if successful, or return null
  public static String copyFile(String src, String dest) {
    File file = new File(src);
    if (!file.exists() || file.isDirectory()) {
      Log.v(LOG_TAG, "copyFile: file not exist or is directory, " + src);
      return null;
    }

    FileInputStream fi = null;
    FileOutputStream fo = null;
    try {
      File destPlace = new File(dest);
      if (!destPlace.exists()) {
        if (!destPlace.mkdirs())
          return null;
      }

      String destPath = Util.makePath(dest, file.getName());
      File destFile = new File(destPath);
      int i = 1;
      while (destFile.exists()) {
        String destName = Util.getNameFromFilename(file.getName())
            + " " + i++ + "."
            + Util.getExtFromFilename(file.getName());
        destPath = Util.makePath(dest, destName);
        destFile = new File(destPath);
      }

      if (!destFile.createNewFile())
        return null;

      fi = new FileInputStream(file);
      fo = new FileOutputStream(destFile);
      final int count = 1024 * 100;
      byte[] buffer = new byte[count];

      int read = 0;
      while ((read = fi.read(buffer, 0, count)) != -1) {
        fo.write(buffer, 0, read);
      }

      return destPath;
    } catch (FileNotFoundException e) {
      Log.e(LOG_TAG, "copyFile: file not found, " + src);
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(LOG_TAG, "copyFile: " + e.toString());
    } finally {
      try {
        if (fi != null)
          fi.close();
        if (fo != null)
          fo.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return null;
  }

  public static boolean setText(View view, int id, String text) {
    TextView textView = (TextView) view.findViewById(id);
    if (textView == null) {
      return false;
    }

    textView.setText(text);
    return true;
  }

  public static boolean setText(View view, int id, int text) {
    TextView textView = (TextView) view.findViewById(id);
    if (textView == null) {
      return false;
    }

    textView.setText(text);
    return true;
  }

  private static String DEVICE_ID = null;

  public static String getDeviceId(Context context) {
    if (!TextUtils.isEmpty(DEVICE_ID)) {
      return DEVICE_ID;
    }

    TelephonyManager tm = (TelephonyManager) context
        .getSystemService(Context.TELEPHONY_SERVICE);
    String tmDevice, tmSerial, androidId;
    try {
      tmDevice = tm.getDeviceId();
    } catch (Exception exc) {
      tmDevice = null;
      exc.printStackTrace();
    }
    if (tmDevice == null) {
      tmDevice = "";
    }
    tmSerial = tm.getSimSerialNumber();
    if (tmSerial == null) {
      tmSerial = "";
    }
    androidId = android.provider.Settings.Secure.getString(
        context.getContentResolver(),
        android.provider.Settings.Secure.ANDROID_ID);
    if (androidId == null) {
      androidId = "";
    }
    UUID deviceUuid = new UUID(androidId.hashCode(),
        ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
    DEVICE_ID = deviceUuid.toString();
    return DEVICE_ID;
  }

  // comma separated number
  public static String convertNumber(long number) {
    return String.format("%,d", number);
  }

  // storage, G M K B
  public static String convertStorage(long size) {
    long kb = 1024;
    long mb = kb * 1024;
    long gb = mb * 1024;

    if (size >= gb) {
      return String.format("%.1f GB", (float) size / gb);
    } else if (size >= mb) {
      float f = (float) size / mb;
      return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
    } else if (size >= kb) {
      float f = (float) size / kb;
      return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
    } else
      return String.format("%d B", size);
  }

  public static boolean is3G(Context context) {
    boolean isConnect = NetworkHelpers.isNetworkAvailable(context);
    boolean isWifi = NetworkHelpers.isNetworkAvailable(context, false,
        false);
    return isConnect && !isWifi;
  }

  public static boolean runInSelfProcess() {
    return Binder.getCallingPid() == android.os.Process.myPid();
  }

  public static boolean openFile(Context c, File f) {
    if (f == null || !f.exists()) {
      return false;
    }
    try {
      c.startActivity(getViewIntent(f));
      return true;
    } catch (ActivityNotFoundException e) {
      return false;
    }
  }

  public static Intent getViewIntent(File f) {
    String type = getMIMEType(f);
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setDataAndType(Uri.fromFile(f), type);
    return intent;
  }

  public static String getMIMEType(File file) {
    String type = "*/*";
    String ext = getExtFromFilename(file.getName());
    if (!TextUtils.isEmpty(ext)) {
      String mimeType = MimeTypeMap.getSingleton()
          .getMimeTypeFromExtension(ext);
      if (!TextUtils.isEmpty(mimeType)) {
        type = mimeType;
      }
    }
    return type;
  }

  public static class SDCardInfo {
    public long total;
    public long free;
  }

  public static String formatDateString(Context context, long time) {
    DateFormat dateFormat = android.text.format.DateFormat
        .getDateFormat(context);
    DateFormat timeFormat = android.text.format.DateFormat
        .getTimeFormat(context);
    Date date = new Date(time);
    return dateFormat.format(date) + " " + timeFormat.format(date);
  }

  public static String formatTimeString(long ms) {
    int ss = 1000;
    int mi = ss * 60;
    int hh = mi * 60;
    int dd = hh * 24;

    long day = ms / dd;
    long hour = (ms - day * dd) / hh;
    long minute = (ms - day * dd - hour * hh) / mi;
    long second = (ms - day * dd - hour * hh - minute * mi) / ss;

    String strDay = day < 10 ? "0" + day : "" + day;
    String strHour = hour < 10 ? "0" + hour : "" + hour;
    String strMinute = minute < 10 ? "0" + minute : "" + minute;
    String strSecond = second < 10 ? "0" + second : "" + second;
    StringBuffer sb = new StringBuffer();

    if (day > 0) {
      sb.append(strDay).append("å¤?");
    }

    if (hour > 0) {
      sb.append(strHour).append("å°????");
    }

    if (minute > 0) {
      sb.append(strMinute).append("???");
    }

    if (second > 0) {
      sb.append(strSecond);
    } else {
      sb.append("0");
    }

    sb.append("ç§?");

    return sb.toString();
  }

  public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
    /**
		 * 
		 */
    private static final long serialVersionUID = 312842725517781500L;

    {
      add("text/plain");
      add("text/plain");
      add("application/pdf");
      add("application/msword");
      add("application/vnd.ms-excel");
      add("application/vnd.ms-excel");
    }
  };

  public static String sZipFileMimeType = "application/zip";

  public static int CATEGORY_TAB_INDEX = 0;
  public static int SDCARD_TAB_INDEX = 1;
  public static int REMOTE_TAB_INDEX = 2;
}
