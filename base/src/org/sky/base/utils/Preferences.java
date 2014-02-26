package org.sky.base.utils;

import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;

@SuppressLint({"WorldWriteableFiles", "NewApi"})
public class Preferences implements SharedPreferences {

  public static final String PREFERENCE_ACCOUNT_PREFIX = "config_id_";
  public static final String PREFERENCE_COMMON = "config_common";

  public static final int MODE_MULTI_PROCESS = 0x0004;

  public static Preferences getById(Context context, String id) {
    SharedPreferences body = context.getSharedPreferences(
        PREFERENCE_ACCOUNT_PREFIX + id, Context.MODE_WORLD_READABLE
            | MODE_MULTI_PROCESS);
    return new Preferences(body);
  }

  public static Preferences getCommon(Context context) {
    SharedPreferences body = context.getSharedPreferences(
        PREFERENCE_COMMON, Context.MODE_WORLD_READABLE
            | MODE_MULTI_PROCESS);
    return new Preferences(body);
  }

  private static String bundleToString(Bundle bundle) {
    if (bundle == null) {
      return null;
    }
    Parcel parcel = Parcel.obtain();
    try {
      bundle.writeToParcel(parcel, 0);
      return Base64.encodeToString(parcel.marshall(), Base64.DEFAULT);
    } finally {
      parcel.recycle();
    }
  }

  private static Bundle stringToBundle(String source) {
    byte[] input = Base64.decode(source, Base64.DEFAULT);
    Parcel parcel = Parcel.obtain();
    try {
      parcel.unmarshall(input, 0, input.length);
      parcel.setDataPosition(0);
      return Bundle.CREATOR.createFromParcel(parcel);
    } finally {
      parcel.recycle();
    }
  }

  private SharedPreferences mPreference;

  public Preferences(SharedPreferences preference) {
    this.mPreference = preference;
  }

  @Override
  public boolean contains(String key) {
    return mPreference.contains(key);
  }

  @Override
  public CustomEditor edit() {
    return new CustomEditor(mPreference.edit());
  }

  @Override
  public Map<String, ?> getAll() {
    return mPreference.getAll();
  }

  @Override
  public boolean getBoolean(String key, boolean defValue) {
    return mPreference.getBoolean(key, defValue);
  }

  public Bundle getBundle(String key, Bundle defValue) {
    String source = mPreference.getString(key, null);
    if (source == null)
      return defValue;
    return stringToBundle(source);
  }

  @Override
  public float getFloat(String key, float defValue) {
    return mPreference.getFloat(key, defValue);
  }

  @Override
  public int getInt(String key, int defValue) {
    return mPreference.getInt(key, defValue);
  }

  @Override
  public long getLong(String key, long defValue) {
    return mPreference.getLong(key, defValue);
  }

  @Override
  public String getString(String key, String defValue) {
    return mPreference.getString(key, defValue);
  }

  @Override
  public void registerOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    mPreference.registerOnSharedPreferenceChangeListener(listener);
  }

  @Override
  public void unregisterOnSharedPreferenceChangeListener(
      OnSharedPreferenceChangeListener listener) {
    mPreference.unregisterOnSharedPreferenceChangeListener(listener);
  }

  public static class CustomEditor implements Editor {
    private Editor mEditor;

    public CustomEditor(Editor editor) {
      this.mEditor = editor;
    }

    @Override
    public Editor clear() {
      return new CustomEditor(mEditor.clear());
    }

    @Override
    public boolean commit() {
      return mEditor.commit();
    }

    @Override
    public Editor putBoolean(String key, boolean value) {
      return new CustomEditor(mEditor.putBoolean(key, value));
    }

    public Editor putBundle(String key, Bundle value) {
      return new CustomEditor(mEditor.putString(key,
          bundleToString(value)));
    }

    @Override
    public Editor putFloat(String key, float value) {
      return new CustomEditor(mEditor.putFloat(key, value));
    }

    @Override
    public Editor putInt(String key, int value) {
      return new CustomEditor(mEditor.putInt(key, value));
    }

    @Override
    public Editor putLong(String key, long value) {
      return new CustomEditor(mEditor.putLong(key, value));
    }

    @Override
    public Editor putString(String key, String value) {
      return new CustomEditor(mEditor.putString(key, value));
    }

    @Override
    public Editor remove(String key) {
      return new CustomEditor(mEditor.remove(key));
    }

    @Override
    public void apply() {
      mEditor.apply();
    }

    @Override
    public Editor putStringSet(String arg0, Set<String> arg1) {
      // TODO Auto-generated method stub
      return null;
    }

  }

  @Override
  public Set<String> getStringSet(String arg0, Set<String> arg1) {
    return mPreference.getStringSet(arg0, arg1);
  }
}
