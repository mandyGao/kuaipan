package org.sky.base.utils;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class JavaCalls {
  private final static String LOG_TAG = "JavaCalls";

  private final static HashMap<Class<?>, Class<?>> PRIMITIVE_MAP =
      new HashMap<Class<?>, Class<?>>();
  static {
    PRIMITIVE_MAP.put(Boolean.class, boolean.class);
    PRIMITIVE_MAP.put(Byte.class, byte.class);
    PRIMITIVE_MAP.put(Character.class, char.class);
    PRIMITIVE_MAP.put(Short.class, short.class);
    PRIMITIVE_MAP.put(Integer.class, int.class);
    PRIMITIVE_MAP.put(Float.class, float.class);
    PRIMITIVE_MAP.put(Long.class, long.class);
    PRIMITIVE_MAP.put(Double.class, double.class);
    PRIMITIVE_MAP.put(boolean.class, boolean.class);
    PRIMITIVE_MAP.put(byte.class, byte.class);
    PRIMITIVE_MAP.put(char.class, char.class);
    PRIMITIVE_MAP.put(short.class, short.class);
    PRIMITIVE_MAP.put(int.class, int.class);
    PRIMITIVE_MAP.put(float.class, float.class);
    PRIMITIVE_MAP.put(long.class, long.class);
    PRIMITIVE_MAP.put(double.class, double.class);
  }

  public static class JavaParam<T> {
    public final Class<? extends T> clazz;
    public final T obj;

    public JavaParam(Class<? extends T> clazz, T obj) {
      super();
      this.clazz = clazz;
      this.obj = obj;
    }
  }

  public static <T> T callMethod(Object targetInstance, String methodName,
      Object... args) {
    try {
      return callMethodOrThrow(targetInstance, methodName, args);
    } catch (Exception e) {
      Log.w(LOG_TAG, "Meet exception when call Method '" + methodName
          + "' in " + targetInstance, e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T callMethodOrThrow(Object targetInstance,
      String methodName, Object... args) throws SecurityException,
      NoSuchMethodException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    final Class<?> clazz = targetInstance.getClass();

    Method method = getDeclaredMethod(clazz, methodName,
        getParameterTypes(args));

    T result = (T) method.invoke(targetInstance, getParameters(args));
    return result;
  }

  public static <T> T callStaticMethod(String className, String methodName,
      Object... args) {
    try {
      Class<?> clazz = Class.forName(className);
      return callStaticMethodOrThrow(clazz, methodName, args);
    } catch (Exception e) {
      Log.w(LOG_TAG, "Meet exception when call Method '" + methodName
          + "' in " + className, e);
      return null;
    }
  }

  private static Method getDeclaredMethod(final Class<?> clazz, String name,
      Class<?>... parameterTypes) throws NoSuchMethodException,
      SecurityException {

    Method[] methods = clazz.getDeclaredMethods();
    Method method = findMethodByName(methods, name, parameterTypes);

    return method;
  }

  private static Method findMethodByName(Method[] list, String name,
      Class<?>[] parameterTypes) throws NoSuchMethodException {
    if (name == null) {
      throw new NullPointerException("Method name must not be null.");
    }

    for (Method method : list) {
      if (method.getName().equals(name)
          && compareClassLists(method.getParameterTypes(),
              parameterTypes)) {
        return method;
      }
    }

    throw new NoSuchMethodException(name);
  }

  private static boolean compareClassLists(Class<?>[] a, Class<?>[] b) {
    if (a == null) {
      return (b == null) || (b.length == 0);
    }

    int length = a.length;

    if (b == null) {
      return (length == 0);
    }

    if (length != b.length) {
      return false;
    }

    for (int i = length - 1; i >= 0; i--) {
      if (a[i].isAssignableFrom(b[i])
          || (PRIMITIVE_MAP.containsKey(a[i]) && PRIMITIVE_MAP.get(
              a[i]).equals(PRIMITIVE_MAP.get(b[i])))) {
        return true;
      }
    }

    return false;
  }

  @SuppressWarnings("unchecked")
  public static <T> T callStaticMethodOrThrow(final String className,
      String methodName, Object... args) throws SecurityException,
      NoSuchMethodException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException,
      ClassNotFoundException {
    Class<?> clazz = Class.forName(className);
    Method method = getDeclaredMethod(clazz, methodName,
        getParameterTypes(args));

    T result = (T) method.invoke(null, getParameters(args));
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T> T callStaticMethodOrThrow(final Class<?> clazz,
      String methodName, Object... args) throws SecurityException,
      NoSuchMethodException, IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
    Method method = getDeclaredMethod(clazz, methodName,
        getParameterTypes(args));

    T result = (T) method.invoke(null, getParameters(args));
    return result;
  }

  public static <T> T getInstance(Class<?> clazz, Object... args) {
    try {
      return getInstanceOrThrow(clazz, args);
    } catch (Exception e) {
      Log.w(LOG_TAG,
          "Meet exception when make instance as a "
              + clazz.getSimpleName(), e);
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> T getInstanceOrThrow(Class<?> clazz, Object... args)
      throws SecurityException, NoSuchMethodException,
      IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    Constructor<?> constructor = clazz
        .getConstructor(getParameterTypes(args));
    return (T) constructor.newInstance(getParameters(args));
  }

  public static Object getInstance(String className, Object... args) {
    try {
      return getInstanceOrThrow(className, args);
    } catch (Exception e) {
      Log.w(LOG_TAG, "Meet exception when make instance as a "
          + className, e);
      return null;
    }
  }

  public static Object getInstanceOrThrow(String className, Object... args)
      throws SecurityException, NoSuchMethodException,
      IllegalArgumentException, InstantiationException,
      IllegalAccessException, InvocationTargetException,
      ClassNotFoundException {
    return getInstanceOrThrow(Class.forName(className), getParameters(args));
  }

  private static Class<?>[] getParameterTypes(Object... args) {
    Class<?>[] parameterTypes = null;

    if (args != null && args.length > 0) {
      parameterTypes = new Class<?>[args.length];
      for (int i = 0; i < args.length; i++) {
        Object param = args[i];
        if (param != null && param instanceof JavaParam<?>) {
          parameterTypes[i] = ((JavaParam<?>) param).clazz;
        } else {
          parameterTypes[i] = param == null ? null : param.getClass();
        }
      }
    }
    return parameterTypes;
  }

  private static Object[] getParameters(Object... args) {
    Object[] parameters = null;

    if (args != null && args.length > 0) {
      parameters = new Object[args.length];
      for (int i = 0; i < args.length; i++) {
        Object param = args[i];
        if (param != null && param instanceof JavaParam<?>) {
          parameters[i] = ((JavaParam<?>) param).obj;
        } else {
          parameters[i] = param;
        }
      }
    }
    return parameters;
  }
}
