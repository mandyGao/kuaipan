
package cn.kuaipan.android.kss.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.sky.base.utils.Base64;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

public class KInfo {
    private static final String LOG_TAG = "KInfo";

    private static final String KEY_DATA = "data";
    private static final String KEY_SHA1 = "sha1";
    private static final String KEY_LOAD_MAP = "load_map";

    private static final String PREFIX = ".";
    private static final String SUFFIX = ".kinfo";

    private final File mFile;
    private final Bundle mData;
    private final Properties mProp;

    public KInfo(File file) {
        mFile = file;
        mData = new Bundle();
        mProp = new Properties();
    }

    public String getSha1() {
        return mData.getString(KEY_SHA1);
    }

    public void setSha1(String sha1) {
        mData.putString(KEY_SHA1, sha1);
    }

    public boolean loadToMap(LoadMap map) {
        Bundle b = mData.getBundle(KEY_LOAD_MAP);
        return map.load(b);
    }

    public void setLoadMap(LoadMap map) {
        Bundle b = new Bundle();
        map.save(b);
        mData.putBundle(KEY_LOAD_MAP, b);
    }

    public void delete() {
        mFile.delete();
    }

    public void save() {
        mProp.put(KEY_DATA, bundleToString(mData));

        OutputStream out = null;
        try {
            out = new FileOutputStream(mFile);
            mProp.save(out, null);
            out.flush();
        } catch (IOException e) {
            Log.w(LOG_TAG, "Failed save kinfo to " + mFile, e);
        } finally {
            try {
                out.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public void load() {
        if (!mFile.exists()) {
            return;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(mFile);
            mProp.load(in);
            String data = mProp.getProperty(KEY_DATA);
            Bundle b = stringToBundle(data);
            mData.clear();
            mData.putAll(b);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Failed load kinfo from " + mFile, e);
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public static File getInfoFile(File savePath) {
        String dir = savePath.getParent();
        String name = savePath.getName() + SUFFIX;

        if (!name.startsWith(PREFIX)) {
            name = PREFIX + name;
        }

        return new File(dir, name);
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
}
