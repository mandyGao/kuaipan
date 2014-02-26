/*
 * Copyright (C) 2010-2011 Android Cache Library Project,
 * All rights reserved by PCR(Wanzheng Ma)
 * Version 0.9
 * Date 2011-May-16
 * Support: pcrxjxj@gmail.com
 */

package org.sky.base.utils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

public class SQLUtility {
  private static final String LOG_TAG = "SQLUtility";

  private SQLUtility() {}

  private static final String CMD_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %s (%s);";
  private static final String CMD_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS %s ON %s (%s);";
  private static final String CMD_DROP_TABLE = "DROP TABLE IF EXISTS %s";
  private static final String CMD_DROP_INDEX = "DROP INDEX IF EXISTS %s";
  private static final String CMD_REINDEX = "REINDEX %s";

  private static final String CMD_SELECT = "%s=?";

  public static final String SORT_ASC = " ASC";
  public static final String SORT_DESC = " DESC";
  public static final String SORT_ASC_LOCALIZED = " COLLATE LOCALIZED ASC";
  public static final String SORT_DESC_LOCALIZED = " COLLATE LOCALIZED DESC";

  public static final String WHERE_GREATER = "%s>?";
  public static final String WHERE_LESS = "%s<?";
  public static final String WHERE_EQUSE = "%s=?";
  public static final String WHERE_UNEQUSE = "%s<>?";
  public static final String WHERE_GREATER_OR_EQUSE = "%s>=?";
  public static final String WHERE_LESS_OR_EQUSE = "%s<=?";
  public static final String WHERE_LIKE = "%s LIKE '%s'";
  public static final String WHERE_STR_IN = "( %s >= '%s' ) AND ( %s < '%s' )";
  public static final String WHERE_BETWEEN = "%s BETWEEN ? AND ?";
  public static final String WHERE_IN = "%s IN ( %s )";

  private static final String CMD_OR = "(%s) OR (%s)";
  private static final String CMD_AND = "(%s) AND (%s)";

  private static final String AND = " AND ";
  private static final String OR = " OR ";

  public static void createTable(SQLiteDatabase db, String tableName,
      String columes) {
    db.execSQL(String.format(CMD_CREATE_TABLE, tableName, columes));
  }

  public static void dropTable(SQLiteDatabase db, String tableName) {
    db.execSQL(String.format(CMD_DROP_TABLE, tableName));
  }

  public static void createIndex(SQLiteDatabase db, String tableName,
      String indexName, String columes) {
    db.execSQL(String.format(CMD_CREATE_INDEX, indexName, tableName,
        columes));
  }

  public static void reIndex(SQLiteDatabase db, String tableName) {
    db.execSQL(String.format(CMD_REINDEX, tableName));
  }

  public static void dropIndex(SQLiteDatabase db, String indexName) {
    db.execSQL(String.format(CMD_DROP_INDEX, indexName));
  }

  public static String getSelection(String colume) {
    return String.format(CMD_SELECT, colume);
  }

  public static String getSelectionWithTemplete(String whereTemplete,
      String... colume) {
    return String.format(whereTemplete, (Object[]) colume);
  }

  public static String getSelectionAnd(String... columes) {
    return getSelection(AND, columes);
  }

  public static String getSelectionOr(String... columes) {
    return getSelection(OR, columes);
  }

  private static String getSelection(String fun, String... columes) {
    if (columes == null || columes.length <= 0) {
      return null;
    }
    final StringBuilder builder = new StringBuilder();
    builder.append(String.format(CMD_SELECT, columes[0]));
    final int count = columes.length;
    for (int i = 1; i < count; i++) {
      builder.append(fun);
      builder.append(String.format(CMD_SELECT, columes[i]));
    }
    return builder.toString();
  }

  public static String or(String selection0, String selection1) {
    return fun(CMD_OR, selection0, selection1);
  }

  public static String and(String selection0, String selection1) {
    return fun(CMD_AND, selection0, selection1);
  }

  private static String fun(String fun_cmd, String selection0,
      String selection1) {
    if (selection0 != null && selection1 != null) {
      return String.format(fun_cmd, selection0, selection1);
    } else {
      return selection0 != null ? selection0 : selection1;
    }
  }

  public static String[] mergeSelectionArg(String[]... args) {
    int size = 0;
    String[] result = null;
    for (String[] arg : args) {
      if (arg != null) {
        size += arg.length;
      }
    }

    result = new String[size];
    int index = 0;
    for (String[] arg : args) {
      if (arg != null) {
        System.arraycopy(arg, 0, result, index, arg.length);
        index += arg.length;
      }
    }

    return result;
  }

  public static boolean copyStringValue(ContentValues src,
      ContentValues dest, String key) {
    String value = src.getAsString(key);
    if (value != null) {
      dest.put(key, value);
    }
    return value != null;
  }

  public static boolean copyIntValue(ContentValues src, ContentValues dest,
      String key) {
    Integer value = src.getAsInteger(key);
    if (value != null) {
      dest.put(key, value);
    }
    return value != null;
  }

  public static boolean copyLongValue(ContentValues src, ContentValues dest,
      String key) {
    Long value = src.getAsLong(key);
    if (value != null) {
      dest.put(key, value);
    }
    return value != null;
  }

  public static boolean copyBooleanValue(ContentValues src,
      ContentValues dest, String key) {
    Boolean value = src.getAsBoolean(key);
    if (value != null) {
      dest.put(key, value);
    }
    return value != null;
  }

  public static final int CONFLICT_NONE = 0;
  public static final int CONFLICT_ROLLBACK = 1;
  public static final int CONFLICT_ABORT = 2;
  public static final int CONFLICT_FAIL = 3;
  public static final int CONFLICT_IGNORE = 4;
  public static final int CONFLICT_REPLACE = 5;
  private static final String[] CONFLICT_VALUES = new String[] {
      "", "ROLLBACK", "ABORT", "FAIL", "IGNORE", "REPLACE"
  };

  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  public static long insertWithOnConflict(SQLiteDatabase db, String table,
      String nullColumnHack, ContentValues values, int conflictAlgorithm) {
    long result = -1;
    try {
      if (Build.VERSION.SDK_INT >= 8) {
        result = (Long) JavaCalls.callMethodOrThrow(db,
            "insertWithOnConflict", table, nullColumnHack, values,
            conflictAlgorithm);
      } else {
        if (conflictAlgorithm > CONFLICT_REPLACE) {
          conflictAlgorithm = CONFLICT_NONE;
        }
        Object algorithm = null;
        if (conflictAlgorithm > CONFLICT_NONE) {
          Class enumClazz = Class
              .forName("android.database.sqlite.SQLiteDatabase$ConflictAlgorithm");
          algorithm = Enum.valueOf(enumClazz,
              CONFLICT_VALUES[conflictAlgorithm]);
        }
        result = (Long) JavaCalls.callMethodOrThrow(db,
            "insertWithOnConflict", table, nullColumnHack, values,
            algorithm);
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, "Current SDK Version is " + Build.VERSION.SDK);
      Log.e(LOG_TAG, "Failed to call insertWithOnConflict", e);
    }

    return result;
  }
}
