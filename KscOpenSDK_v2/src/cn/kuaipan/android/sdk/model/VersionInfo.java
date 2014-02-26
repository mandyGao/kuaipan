
package cn.kuaipan.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;
import java.util.zip.DataFormatException;

public class VersionInfo extends AbsKscData implements Parcelable {

    public static final String KEY_VERSIONCODE = "upversion";
    public static final String KEY_DESC = "upgradeinfo";
    public static final String KEY_URL = "url";
    public static final String KEY_RESULT = "result";

    public static final Parcelable.Creator<VersionInfo> CREATOR = new Parcelable.Creator<VersionInfo>() {
        public VersionInfo createFromParcel(Parcel in) {
            return new VersionInfo(in);
        }

        public VersionInfo[] newArray(int size) {
            return new VersionInfo[size];
        }
    };

    public final static Parser<VersionInfo> PARSER = new Parser<VersionInfo>() {
        @Override
        public VersionInfo parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                return new VersionInfo(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    public String mVersionCode;
    public String mDescription;
    public String mDownloadURL;
    public String mResult;

    public VersionInfo(Map<String, Object> dataMap) {
        if (null == dataMap) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse");
        }
        mResult = asString(dataMap, KEY_RESULT);
        mVersionCode = asString(dataMap, KEY_VERSIONCODE);
        mDescription = asString(dataMap, KEY_DESC);
        mDownloadURL = asString(dataMap, KEY_URL);
    }

    public VersionInfo() {
    }

    private VersionInfo(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    private void readFromParcel(Parcel in) {
        mVersionCode = in.readString();
        mDescription = in.readString();
        mDownloadURL = in.readString();
        mResult = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mVersionCode);
        dest.writeString(mDescription);
        dest.writeString(mDownloadURL);
        dest.writeString(mResult);
    }
}
