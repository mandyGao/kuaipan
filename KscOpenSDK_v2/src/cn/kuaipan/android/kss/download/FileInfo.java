
package cn.kuaipan.android.kss.download;

import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.sdk.model.AbsKscData;

import java.util.Date;
import java.util.Map;

public class FileInfo implements KssDef {
    public final String sha1;
    public final Date modifyTime;

    // public final int op_ver;
    // public final int file_ver;

    public FileInfo(Map<String, Object> dataMap) {
        sha1 = AbsKscData.asString(dataMap, KEY_SHA1);
        modifyTime = AbsKscData.asDate(dataMap.get(KEY_MODIFY_TIME), null);
        // op_ver = AbsKscData.asNumber(dataMap.get("op_ver"), -1).intValue();
        // file_ver = AbsKscData.asNumber(dataMap.get("file_ver"),
        // -1).intValue();
    }
}
