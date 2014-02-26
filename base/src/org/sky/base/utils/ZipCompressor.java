package org.sky.base.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.util.Log;

public class ZipCompressor {
  private static final String LOG_TAG = "ZipCompressor";

  private static final int BUFFER = 16 * 1024;

  public static boolean compress(File zipFile, File... files)
      throws IOException {
    if (zipFile == null) {
      return false;
    }

    if (files == null || files.length <= 0) {
      return true;
    }

    if (zipFile.exists()) {
      if (!zipFile.delete()) {
        throw new IOException("Can't delete " + zipFile);
      }
    }

    File parent = zipFile.getParentFile();
    if (!parent.exists()) {
      parent.mkdirs();
    }

    ZipOutputStream out = null;
    try {
      out = new ZipOutputStream(new CheckedOutputStream(
          new FileOutputStream(zipFile), new CRC32()));

      for (File file : files) {
        compress(out, file, "");
      }
      return true;
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private static void compress(ZipOutputStream out, File file, String basePath)
      throws IOException {
    if (!file.exists()) {
      Log.w(LOG_TAG, "Can't found file to compress. " + file);
      return;
    }

    if (file.isDirectory()) {
      compressDirectory(out, file, basePath);
    } else if (file.isFile()) {
      compressFile(out, file, basePath);
    } else {
      Log.w(LOG_TAG, "Compress target is not a file or directory. "
          + file);
    }
  }

  private static void compressDirectory(ZipOutputStream out, File dir,
      String basePath) throws IOException {
    File[] files = dir.listFiles();
    if (files == null) {
      // XXX not support create an empty directory in ZIP file
      return;
    }

    for (int i = 0; i < files.length; i++) {
      compress(out, files[i], basePath + dir.getName() + "/");
    }
  }

  private static void compressFile(ZipOutputStream out, File file,
      String basePath) throws IOException {
    if (!file.exists()) {
      Log.w(LOG_TAG, "Can't found file to compress. " + file);
      return;
    }

    BufferedInputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(file), BUFFER);
      ZipEntry entry = new ZipEntry(basePath + file.getName());
      out.putNextEntry(entry);
      int len;
      byte data[] = new byte[BUFFER];
      while ((len = in.read(data, 0, BUFFER)) >= 0) {
        out.write(data, 0, len);
      }
    } finally {
      try {
        in.close();
      } catch (Throwable t) {// ignore
      }
    }
  }
}
