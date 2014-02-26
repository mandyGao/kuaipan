package cn.kuaipan.android.utils;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;

import android.os.SystemClock;

public class OAuthTimeUtils {
  private static long sTimeDiff = 0;

  public static long currentTime() {
    if (sTimeDiff == 0) {
      return System.currentTimeMillis();
    }

    return SystemClock.elapsedRealtime() + sTimeDiff;
  }

  public static void setRealTime(long current) {
    long systemTime = SystemClock.elapsedRealtime();
    sTimeDiff = current - systemTime;
  }

  private static final SimpleDateFormat sFormat = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");
  private static final ParsePosition sFormatPosition = new ParsePosition(0);
  private static final StringBuffer sFormatBuffer = new StringBuffer();
  private static final FieldPosition sFormatFieldPos = new FieldPosition(0);
  static {
    sFormat.setTimeZone(TimeZone.getTimeZone("GMT+0800"));
  }

  public synchronized static Date parser(String dataStr, Date defaultValue) {
    if (dataStr == null) {
      return defaultValue;
    }

    Date result = defaultValue;
    try {
      sFormatPosition.setErrorIndex(-1);
      sFormatPosition.setIndex(0);
      result = sFormat.parse(dataStr, sFormatPosition);
      if (sFormatPosition.getErrorIndex() != -1) {
        throw new UnknownFormatConversionException("Date: " + dataStr);
      }

      if (sFormatPosition.getIndex() == 0) {
        result = defaultValue;
      }
    } catch (Exception e) {
      throw new UnknownFormatConversionException("Date:" + dataStr);
    }
    return result;
  }

  public synchronized static String toString(long time) {
    // FIXME: the time should be adjust as GMT+0800
    sFormatBuffer.delete(0, sFormatBuffer.length());
    return sFormat.format(new Date(time), sFormatBuffer, sFormatFieldPos)
        .toString();
  }
}
