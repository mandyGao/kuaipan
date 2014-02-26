package org.sky.base.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MountUtil {

  private static final File FILE_MNT = new File("/mnt");

  public static File[] getStorages() {
    List<File> storages = new ArrayList<File>();
    File[] files = FILE_MNT.listFiles();

    if (files != null) {
      for (File file : files) {
        if (canWrite(file)) {
          storages.add(file);
        }
      }
    }

    return storages.toArray(new File[storages.size()]);
  }

  public static boolean canWrite(File file) {
    if (file == null || !file.exists()) {
      return false;
    }

    String testName = "." + System.currentTimeMillis();
    File child = new File(file, testName);

    boolean result = child.mkdir();
    if (result) {
      result = child.delete();
    }

    return result;
  }
}
