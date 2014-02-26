
package cn.kuaipan.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;
import java.util.zip.DataFormatException;

public class SignInfo extends AbsKscData implements Parcelable {

    public static final int QUOTA_REACH = 0; // 积分上限
    public static final int SUCCESS = 1;
    public static final int SIGN_AGAIN = -102; // 已经签到

    public static final int ERROR_PARAM = -1; // 参数错误
    public static final int ERROR_INVALID_USER = -2; // 失效的用户

    // 以下是服务器内部错误
    public static final int ERROR_KAPI = -3;
    public static final int ERROR_DENY = -4;
    public static final int ERROR_NO_ENOUGH_SCORES = -5;
    public static final int ERROR_UPDATE_SCORE_FAIL = -6;
    public static final int ERROR_SRVERR = -500;

    public final static Parser<SignInfo> PARSER = new Parser<SignInfo>() {
        @Override
        public SignInfo parserMap(Map<String, Object> map, String... requireds)
                throws DataFormatException {
            try {
                return new SignInfo(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    public final int state;
    public final int increase;
    public final int rewardsize;

    private SignInfo(Map<String, Object> map) {
        state = asNumber(map.get("state"), ERROR_SRVERR).intValue();
        if (state == SUCCESS) {
            increase = asNumber(map.get("increase"), 0).intValue();
            rewardsize = asNumber(map.get("rewardsize"), 0).intValue();
        } else {
            increase = 0;
            rewardsize = 0;
        }
    }

    public static final Parcelable.Creator<SignInfo> CREATOR = new Parcelable.Creator<SignInfo>() {
        public SignInfo createFromParcel(Parcel in) {
            return new SignInfo(in);
        }

        public SignInfo[] newArray(int size) {
            return new SignInfo[size];
        }
    };

    private SignInfo(Parcel in) {
        state = in.readInt();
        increase = in.readInt();
        rewardsize = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(state);
        dest.writeInt(increase);
        dest.writeInt(rewardsize);
    }
}
