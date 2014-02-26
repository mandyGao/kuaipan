
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.kss.IKssRequestor.IKssUploadRequestResult;
import cn.kuaipan.android.kss.upload.UploadRequest;
import cn.kuaipan.android.sdk.exception.KscException;

import java.util.Map;
import java.util.zip.DataFormatException;

public class KssUploadRequestResult extends AbsKscData implements
        IKssUploadRequestResult {
    private static final String KEY_KSS = "kss";
    private static final String KEY_S = "s";
    private static final String KEY_MSG = "msg";

    public final static Parser<KssUploadRequestResult> PARSER = new Parser<KssUploadRequestResult>() {
        @Override
        public KssUploadRequestResult parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException, KscException {
            try {
                return new KssUploadRequestResult(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private String msg;
    private final String stub;
    private final UploadRequest kssRequest;

    public KssUploadRequestResult(Map<String, Object> dataMap)
            throws KscException {
        msg = asString(dataMap, KEY_MSG);
        if (!ResultMsg.MSG_OK.equalsIgnoreCase(msg)) {
            stub = null;
            kssRequest = null;
            return;
        }

        stub = asString(dataMap, KEY_S);
        String kssStr = asString(dataMap, KEY_KSS);

        kssRequest = new UploadRequest(kssStr);
        if (!ResultMsg.MSG_OK.equalsIgnoreCase(kssRequest.stat)) {
            msg = kssRequest.stat;
        }
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String getStub() {
        return stub;
    }

    @Override
    public UploadRequest getRequest() {
        return kssRequest;
    }
}
