
package cn.kuaipan.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;
import java.util.zip.DataFormatException;

public class SessionInfo extends AbsKscData implements Parcelable {

    public static final String KEY_CTIME = "ctime";
    public static final String KEY_IP = "ip";
    public static final String KEY_DEVICE_ID = "deviceid";
    public static final String KEY_KEY = "key";
    public static final String KEY_UER_NAME = "user_name";
    public static final String KEY_SID = "sid";

    public final String mCTime;
    public final String mIP;
    public final String mDeviceID;
    public final String mKey;
    public final String mSID;
    public final String mUserName;

    public static final Parcelable.Creator<SessionInfo> CREATOR = new Parcelable.Creator<SessionInfo>() {
        public SessionInfo createFromParcel(Parcel in) {
            return new SessionInfo(in);
        }

        public SessionInfo[] newArray(int size) {
            return new SessionInfo[size];
        }
    };

    public final static Parser<SessionInfo> PARSER = new Parser<SessionInfo>() {
        @Override
        public SessionInfo parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                return new SessionInfo(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    public SessionInfo(Map<String, Object> dataMap) {
        if (null == dataMap) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse");
        }
        mCTime = asString(dataMap, KEY_CTIME);
        mIP = asString(dataMap, KEY_IP);
        mDeviceID = asString(dataMap, KEY_DEVICE_ID);
        mKey = asString(dataMap, KEY_KEY);
        mUserName = asString(dataMap, KEY_UER_NAME);
        mSID = asString(dataMap, KEY_SID);
    }

    private SessionInfo(Parcel in) {
        mCTime = in.readString();
        mIP = in.readString();
        mDeviceID = in.readString();
        mKey = in.readString();
        mUserName = in.readString();
        mSID = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCTime);
        dest.writeString(mIP);
        dest.writeString(mDeviceID);
        dest.writeString(mKey);
        dest.writeString(mUserName);
        dest.writeString(mSID);
    }
}
