
package cn.kuaipan.android.sdk.oauth;

import android.os.Parcel;
import android.os.Parcelable;

public class AccessToken extends Token implements Parcelable {

    public AccessToken(String key, String secret) {
        super(key, secret);
    }

    public static final Parcelable.Creator<AccessToken> CREATOR = new Parcelable.Creator<AccessToken>() {
        public AccessToken createFromParcel(Parcel in) {
            return new AccessToken(in);
        }

        public AccessToken[] newArray(int size) {
            return new AccessToken[size];
        }
    };

    private AccessToken(Parcel in) {
        super(in.readString(), in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getKey());
        dest.writeString(getSecret());
    }
}
