package org.sky.base.utils;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtil {

  public static byte[] md5(byte[] byteData, int count) {
    byte[] md5 = null;

    try {
      MessageDigest algorithm = MessageDigest.getInstance("MD5");
      algorithm.update(byteData, 0, count);
      md5 = algorithm.digest();
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return md5;
  }

  public static String md5(String string) {
    byte[] data = string.getBytes();

    byte[] md5 = md5(data, data.length);
    if (null != md5) {
      return byte2hex(md5);
    }
    return null;
  }

  protected static char[] Digit = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
      'D', 'E', 'F'
  };

  // å°?å­??????°ç??è½????ä¸ºå?????è¿???¶å??ç¬?ä¸?
  public static String byte2hex(byte[] bytearray) {

    int nSize = bytearray.length;
    char[] charHex = new char[nSize * 2];

    for (int i = 0; i < nSize; i++) {
      byte hm = bytearray[i];
      charHex[i * 2] = Digit[(hm >>> 4) & 0X0F];
      charHex[i * 2 + 1] = Digit[hm & 0X0F];
    }

    return new String(charHex);
  }

  /**
   * ???16è¿???¶å??ç¬?ä¸²è½¬??¢æ??å­??????°ç??
   * 
   * @param hex
   * @return
   */
  public static byte[] hex2byte(String hex) {
    int len = (hex.length() / 2);
    byte[] result = new byte[len];
    char[] achar = hex.toCharArray();
    for (int i = 0; i < len; i++) {
      int pos = i * 2;
      result[i] = (byte) ((toByte(achar[pos]) << 4) | toByte(achar[pos + 1]));
    }
    return result;
  }

  public static byte toByte(char achar) {
    return (achar >= 'A') ? (byte) (achar + 10 - 'A')
        : (byte) (achar - '0');
  }

  public static boolean isEmail(String str) {
    String expr = "^[\\w\\-\\.]+@[\\w\\-\\.]+(\\.\\w+)+$";
    return TextUtils.isEmpty(str) ? false : str.matches(expr);
  }

  public static boolean isPhoneNum(String num) {
    // String format =
    // "/^(1[3,5,8,7]{1}[\\d]{9})|(((400)-(\\d{3})-(\\d{4}))|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{3,7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$)$\/";
    String format = "^1[3,5,8,7]{1}[\\d]{9}";
    boolean isPhoneNum = false;
    if (!TextUtils.isEmpty(num)) {
      num = num.trim();
      if (num.matches(format)) {
        isPhoneNum = true;
      }
    }
    return isPhoneNum;
  }
}
