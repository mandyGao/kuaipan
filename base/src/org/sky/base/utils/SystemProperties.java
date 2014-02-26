package org.sky.base.utils;

import android.util.Log;

import java.lang.reflect.Method;

public class SystemProperties {
  private static final String LOG_TAG = "SystemProperties";

  private static final String CLASS_NAME = "android.os.SystemProperties";
  private static final Method GET;
  private static final Method GET_DEF;
  private static final Method GETINT;
  private static final Method GETLONG;
  private static final Method GETBOOLEAN;
  private static final Method SET;

  static {
    Class<?> clazz = null;
    try {
      clazz = Class.forName(CLASS_NAME);
    } catch (Throwable t) {}
    GET = getMethod(clazz, "get", String.class);
    GET_DEF = getMethod(clazz, "get", String.class, String.class);
    GETINT = getMethod(clazz, "getInt", String.class, Integer.TYPE);
    GETLONG = getMethod(clazz, "getLong", String.class, Long.TYPE);
    GETBOOLEAN = getMethod(clazz, "getBoolean", String.class, Boolean.TYPE);
    SET = getMethod(clazz, "set", String.class, String.class);
  }

  private static Method getMethod(Class<?> clazz, String name,
      Class<?>... parameterTypes) {
    if (clazz == null) {
      return null;
    }

    Method result = null;
    try {
      result = clazz.getMethod(name, parameterTypes);
    } catch (Throwable t) {
      Log.w(LOG_TAG, "Not found method:" + name + " in " + CLASS_NAME);
    }
    return result;
  }

  public static String get(String key) {
    if (GET == null) {
      return null;
    }

    String res = null;
    try {
      Object obj = GET.invoke(null, key);
      res = obj == null ? null : String.valueOf(obj);
    } catch (Exception e) {}

    return res;
  }

  public static String get(String key, String def) {
    if (GET_DEF == null) {
      return null;
    }

    String res = null;
    try {
      Object obj = GET_DEF.invoke(null, key, def);
      res = obj == null ? null : String.valueOf(obj);
    } catch (Exception e) {}

    return res;
  }

  public static int getInt(String key, int def) {
    if (GETINT == null) {
      return def;
    }

    int res = def;
    try {
      Object obj = GETINT.invoke(null, key, def);
      if (obj == null) {
        res = def;
      } else if (obj instanceof Number) {
        res = ((Number) obj).intValue();
      } else {
        res = Integer.parseInt(String.valueOf(obj));
      }
    } catch (Exception e) {}

    return res;
  }

  public static long getLong(String key, long def) {
    if (GETLONG == null) {
      return def;
    }

    long res = def;
    try {
      Object obj = GETLONG.invoke(null, key, def);
      if (obj == null) {
        res = def;
      } else if (obj instanceof Number) {
        res = ((Number) obj).longValue();
      } else {
        res = Long.parseLong(String.valueOf(obj));
      }
    } catch (Exception e) {}

    return res;
  }

  public static boolean getBoolean(String key, boolean def) {
    if (GETBOOLEAN == null) {
      return def;
    }

    boolean res = def;
    try {
      Object obj = GETBOOLEAN.invoke(null, key, def);
      if (obj == null) {
        res = def;
      } else if (obj instanceof Boolean) {
        res = ((Boolean) obj);
      } else {
        res = Boolean.parseBoolean(String.valueOf(obj));
      }
    } catch (Exception e) {}

    return res;
  }

  public static void set(String key, String val) {
    if (SET == null) {
      return;
    }

    try {
      SET.invoke(null, key, val);
    } catch (Exception e) {}

  }
}
