package org.sky.base.utils;

import org.apache.http.util.LangUtils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileLock;

public class FileUtils {
  private static final String TAG = "FileUtils";

  private static final File EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY = new File(
      new File(Environment.getExternalStorageDirectory(), "Android"),
      "data");

  public static File getExternalStorageAppCacheDirectory(String packageName) {
    return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
        packageName), "cache");
  }

  public static File getExternalStorageAppDataDirectory(String packageName) {
    return new File(new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
        packageName), "files");
  }

  private static final Object mSync = new Object();
  private static File sExternalCacheDir;
  private static File sExternalDataDir;

  public static File getCacheDir(Context context, String folderName,
      boolean external) {
    File cacheDir = getCacheDir(context, external);
    if (cacheDir == null) {
      return null;
    }
    File result = new File(cacheDir, folderName);
    if (!result.exists()) {
      result.mkdirs();
      if (!result.exists() || !result.isDirectory()) {
        Log.w(TAG, "Unable to create cache directory:" + result);
        return null;
      }
    }
    return result;
  }

  public static File getCacheDir(Context context, boolean external) {
    return getCacheDir(context, external, true);
  }

  public static File getCacheDir(Context context, boolean external,
      boolean mustExist) {
    File result = null;
    if (external) {
      result = getExternalCacheDir(context, mustExist);
    } else {
      result = context.getCacheDir();
    }
    return result;
  }

  /**
   * Return true if all children have been delete
   * 
   * @return true if all children have been delete
   */
  public static boolean deleteChildren(File folder) {
    if (folder == null || !folder.exists() || !folder.isDirectory()) {
      return true;
    }

    File[] children = folder.listFiles();
    if (children == null) {
      return true;
    }

    boolean success = true;
    for (File child : children) {
      success = deletes(child) && success;
    }

    return success;
  }

  public static boolean deletes(File file) {
    if (file == null || !file.exists()) {
      return true;
    }
    if (file.isDirectory()) {
      deleteChildren(file);
    }

    return file.delete();
  }

  public static boolean move(File src, File dest) {
    if (LangUtils.equals(src, dest)) {
      return true;
    }

    boolean result = false;
    if (dest.exists()) {
      return false;
    }

    File destParent = dest.getParentFile();
    if (!destParent.exists()) {
      destParent.mkdirs();
    }

    if (!destParent.exists() || !destParent.isDirectory()
        || !destParent.canWrite()) {
      return false;
    }

    try {
      long modified = src.lastModified();
      result = src.renameTo(dest);
      if (result) {
        dest.setLastModified(modified);
      }
    } catch (Exception e) {
      // ignore
    }

    if (result) {
      return true;
    }

    if (copyFiles(src, dest)) {
      result = deletes(src);
    }
    return result;
  }

  public static boolean copyFiles(File src, File dest) {
    if (src.isDirectory()) {
      if (!dest.exists()) {
        dest.mkdirs();
      }

      if (!dest.exists() || !dest.canWrite() || !dest.isDirectory()) {
        return false;
      }

      for (File child : src.listFiles()) {
        if (!copyFiles(child, new File(dest, child.getName()))) {
          return false;
        }
      }
      return true;
    } else {
      File destParent = dest.getParentFile();
      if (!destParent.exists()) {
        destParent.mkdirs();
      }

      if (!destParent.exists() || !destParent.canWrite()
          || (dest.exists() && !dest.isFile())) {
        return false;
      }

      return copy(src, dest, -1);
    }
  }

  public static boolean copy(File src, File dest, long size) {
    FileInputStream in = null;
    FileOutputStream out = null;
    FileLock locker = null;
    try {
      size = size < 0 ? src.length() : size;
      if (src.getCanonicalFile().equals(dest.getCanonicalFile())
          && size >= src.length()) {
        return true;
      }

      in = new FileInputStream(src);
      out = new FileOutputStream(dest);
      locker = out.getChannel().lock();
      byte[] buf = new byte[8 * 1024];
      int len;
      long pos = 0;
      while ((len = in.read(buf, 0,
          (int) Math.min(buf.length, size - pos))) >= 0) {
        out.write(buf, 0, len);
        pos += len;
        if (pos >= size) {
          break;
        }
      }
      return true;
    } catch (Exception e) {
      Log.e(TAG, "Failed copy \"" + src + "\" to \"" + dest + "\"", e);
      return false;
    } finally {
      try {
        locker.release();
      } catch (Throwable t) {}

      try {
        in.close();
      } catch (Throwable t) {}
      try {
        out.close();
      } catch (Throwable t) {}

      if (dest.length() == src.length()) {
        dest.setLastModified(src.lastModified());
      }
    }
  }

  public static boolean mkdirs(String path) {
    if (TextUtils.isEmpty(path)) {
      return false;
    }
    File dest = new File(path);
    if (dest.exists() && dest.isDirectory()) {
      return true;
    }
    return dest.mkdirs();
  }

  public static void writeTo(InputStream in, File dest) throws IOException {
    FileOutputStream out = null;
    FileLock locker = null;
    try {
      out = new FileOutputStream(dest);
      locker = out.getChannel().lock();
      byte[] buf = new byte[8 * 1024];
      int len;

      while ((len = in.read(buf)) >= 0) {
        out.write(buf, 0, len);
      }
    } finally {
      try {
        locker.release();
      } catch (Throwable t) {}

      try {
        out.close();
      } catch (Throwable t) {}
    }
  }

  public static long getSize(File file) {
    if (file == null || !file.exists()) {
      return 0;
    }
    long size = file.length();

    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children != null) {
        for (File child : children) {
          size += getSize(child);
        }
      }
    }
    return size;
  }

  public static long getFreeSize(String path) {
    StatFs sf = new StatFs(path);
    long blockSize = sf.getBlockSize();
    long freeBlocks = sf.getAvailableBlocks();
    return freeBlocks * blockSize;
  }

  public static File getExternalCacheDir(Context context, boolean mustExist) {
    synchronized (mSync) {
      if (sExternalCacheDir == null) {
        sExternalCacheDir = getExternalStorageAppCacheDirectory(context
            .getPackageName());
      }
      if (!sExternalCacheDir.exists()) {
        try {
          (new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
              ".nomedia")).createNewFile();
        } catch (IOException e) {}
        sExternalCacheDir.mkdirs();
        if (mustExist
            && (!sExternalCacheDir.exists() || !sExternalCacheDir
                .isDirectory())) {
          Log.w(TAG, "Unable to create external cache directory");
          return null;
        }
      }
      return sExternalCacheDir;
    }
  }

  public static File getExternalDataDir(Context context, boolean mustExist) {
    synchronized (mSync) {
      if (sExternalDataDir == null) {
        sExternalDataDir = getExternalStorageAppDataDirectory(context
            .getPackageName());
      }
      if (!sExternalDataDir.exists()) {
        try {
          (new File(EXTERNAL_STORAGE_ANDROID_DATA_DIRECTORY,
              ".nomedia")).createNewFile();
        } catch (IOException e) {}
        if (!sExternalDataDir.mkdirs() && mustExist) {
          Log.w(TAG, "Unable to create external cache directory");
          return null;
        }
      }
      return sExternalDataDir;
    }
  }

  public static int setPermissions(String file, int mode, int uid, int gid) {
    int result = 0;
    try {
      Class<?> clazz = Class.forName("android.os.FileUtils");
      result = (Integer) JavaCalls.callStaticMethodOrThrow(clazz,
          "setPermissions", file, mode, uid, gid);
    } catch (Exception e) {
      Log.e(TAG, "Failed set Permissions for file: " + file);
    }
    return result;
  }

  public static boolean isChildPath(String path, String testPath) {
    if (TextUtils.isEmpty(path) || TextUtils.isEmpty(testPath)) {
      return false;
    }

    boolean result = false;
    try {
      // XXX use .getCanonicalFile();
      File parentFile = new File(path);
      File testFile = new File(testPath);

      while (testFile != null) {
        if (parentFile.equals(testFile)) {
          result = true;
          break;
        }
        testFile = testFile.getParentFile();
      }
    } catch (Exception e) {
      // ignore
    }
    return result;
  }
}
