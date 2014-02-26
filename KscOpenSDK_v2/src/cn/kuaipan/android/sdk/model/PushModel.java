
package cn.kuaipan.android.sdk.model;

import java.util.Map;

public class PushModel {
    public String url;
    public String deviceId;
    public String fileCursor;
    public String shareCursor;
    public String userName;
    public KuaipanFile kuaipanFile;

    public PushModel(Map<String, Object> map) {
        this.parseFromMap(map);
    }

    private void parseFromMap(Map<String, Object> map) {
        this.url = (String) map.get("url");
        this.deviceId = (String) map.get("device");
    }

    public static class LongResponseModel {
        public String shareVer;
        public String deviceId;
        public String opVer;
    }
}
