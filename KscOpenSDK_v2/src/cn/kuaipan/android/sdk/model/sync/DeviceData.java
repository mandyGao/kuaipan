
package cn.kuaipan.android.sdk.model.sync;

import cn.kuaipan.android.sdk.model.AbsKscData;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class DeviceData implements Parcelable {

    private String mSid = null;
    private String mDetails = null;
    private String mIMEI = null;
    private String mIMSI = null;
    private String mDevice = null;
    private String mTelphone = null;
    private String mSystem = null;

    private DeviceData() {
    }

    public String getSid() {
        return mSid;
    }

    public String getDetails() {
        return mDetails;
    }

    public String getIMEI() {
        return mIMEI;
    }

    public String getIMSI() {
        return mIMSI;
    }

    public String getTelphone() {
        return mTelphone;
    }

    public String getmSystem() {
        return mSystem;
    }

    public String getDevice() {
        return mDevice;
    }

    public static final Parcelable.Creator<DeviceData> CREATOR = new Parcelable.Creator<DeviceData>() {
        @Override
        public DeviceData createFromParcel(Parcel in) {
            return new DeviceData(in);
        }

        @Override
        public DeviceData[] newArray(int size) {
            return new DeviceData[size];
        }
    };

    private DeviceData(Parcel src) {
        mSid = src.readString();
        mDetails = src.readString();
        mIMEI = src.readString();
        mIMSI = src.readString();
        mDevice = src.readString();
        mTelphone = src.readString();
        mSystem = src.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSid);
        dest.writeString(mDetails);
        dest.writeString(mIMEI);
        dest.writeString(mIMSI);
        dest.writeString(mDevice);
        dest.writeString(mTelphone);
        dest.writeString(mSystem);
    }

    private static final String DETAILS = "details";
    private static final String SID = "sid";
    private static final String DEVICE = "device";
    private static final String IMEI = "imei";
    private static final String IMSI = "imsi";
    private static final String PHONE_NUM = "phone_num";
    private static final String PLATFORM = "platform";

    public static DeviceData parser(Map<String, Object> item) {
        DeviceData device = new DeviceData();

        device.mDetails = AbsKscData.asString(item, DETAILS);
        device.mSid = AbsKscData.asString(item, SID);
        device.mDevice = AbsKscData.asString(item, DEVICE);
        device.mIMEI = AbsKscData.asString(item, IMEI);
        device.mIMSI = AbsKscData.asString(item, IMSI);
        device.mSystem = AbsKscData.asString(item, PLATFORM);
        device.mTelphone = AbsKscData.asString(item, PHONE_NUM);
        return device;
    }
}
