package cn.kuaipan.android.utils;

import java.security.SecureRandom;
import java.util.Random;

import org.sky.base.utils.Base64;

public class RandomUtils {
  public static final char[] NORMAL_CHARS =
      "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
          .toCharArray();

  private static SecureRandom sRandom;

  private static Random getRandomSeed() {
    if (sRandom == null) {
      sRandom = new SecureRandom();
      sRandom.setSeed(SecureRandom.getSeed(128));
    }
    return sRandom;
  }

  public static int getInt() {
    Random r = getRandomSeed();
    return r.nextInt();
  }

  public static long getLong() {
    Random r = getRandomSeed();
    return r.nextLong();
  }

  public static String getString(int length) {
    Random r = getRandomSeed();

    int num = (length / 4 + ((length % 4) > 0 ? 1 : 0)) * 3;
    byte[] buffer = new byte[num];
    r.nextBytes(buffer);
    String result = Base64.encodeToString(buffer, Base64.NO_WRAP);

    if (result.length() > length) {
      result = result.substring(0, length);
    }

    return result;
  }

  public static String getString(int length, char[] charRange) {
    if (charRange == null) {
      return getString(length);
    }

    Random r = getRandomSeed();
    char[] result = new char[length];
    final int range = charRange.length;
    for (int i = 0; i < length; i++) {
      result[i] = charRange[r.nextInt(range)];
    }

    return String.copyValueOf(result);
  }
}
