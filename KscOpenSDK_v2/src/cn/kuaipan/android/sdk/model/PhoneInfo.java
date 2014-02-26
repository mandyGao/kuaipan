
package cn.kuaipan.android.sdk.model;

import java.io.Serializable;

public class PhoneInfo implements Serializable {

    private static final long serialVersionUID = 5878101796116639525L;

    public String mOSver;
    public String mKPver;
    public String mImei;
    public String mResolution;
    public String mUserID;
    public String mChannel;
    public String mPackage;

    public static final String OSVER = "osver";
    public static final String KPVER = "kpver";
    public static final String RESOLUTION = "resolution";
    public static final String CHANNEL = "channel";
    public static final String USERID = "userid";
    public static final String IMEI = "imei";
    public static final String PACKAGE = "package";
}
