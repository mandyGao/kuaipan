
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.kss.IKssRequestor.IKssDownloadRequestResult;
import cn.kuaipan.android.kss.download.DownloadRequest;
import cn.kuaipan.android.kss.download.FileInfo;
import cn.kuaipan.android.sdk.exception.KscException;

import java.util.Map;
import java.util.zip.DataFormatException;

public class KssDownloadRequestResult extends AbsKscData implements
        IKssDownloadRequestResult {

    private static final String KEY_KSS = "kss";
    private static final String KEY_FILEINFO = "fileInfo";
    private static final String KEY_MSG = "msg";

    public final static Parser<KssDownloadRequestResult> PARSER = new Parser<KssDownloadRequestResult>() {
        @Override
        public KssDownloadRequestResult parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException, KscException {
            try {
                return new KssDownloadRequestResult(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private String msg;
    private final FileInfo info;
    private final DownloadRequest kssRequest;

    @SuppressWarnings("unchecked")
    public KssDownloadRequestResult(Map<String, Object> dataMap)
            throws KscException {
        msg = asString(dataMap, KEY_MSG);
        if (!ResultMsg.MSG_OK.equalsIgnoreCase(msg)) {
            info = null;
            kssRequest = null;
            return;
        }

        info = new FileInfo((Map<String, Object>) dataMap.get(KEY_FILEINFO));

        String kssStr = asString(dataMap, KEY_KSS);
        kssRequest = new DownloadRequest(kssStr);
        if (!ResultMsg.MSG_OK.equalsIgnoreCase(kssRequest.stat)) {
            msg = kssRequest.stat;
        }
    }

    @Override
    public FileInfo getFileInfo() {
        return info;
    }

    @Override
    public DownloadRequest getRequest() {
        return kssRequest;
    }

    @Override
    public String getMsg() {
        return msg;
    }

}
