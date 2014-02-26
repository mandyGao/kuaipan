
package cn.kuaipan.android.sdk.internal;

import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.utils.IObtainable;

import android.text.TextUtils;

import java.util.Map;

public class KssReqVerifier extends DefaultOAuthVerifier {
    private static final String KEY_MSG = "msg";

    private final boolean ignoreAutoCommit;

    public KssReqVerifier(boolean ignoreAutoCommit) {
        this.ignoreAutoCommit = ignoreAutoCommit;
    }

    @Override
    public void verify(KscHttpResponse response, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException {
        super.verify(response, appendMode);

        int statusCode = response.getStatusCode();

        String msg = null;
        Throwable t = null;
        Map<String, Object> dataMap = null;
        try {
            dataMap = ApiDataHelper.contentToMap(response);
            msg = dataMap == null ? null : String.valueOf(dataMap.get(KEY_MSG));
        } catch (InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            t = e;
        } finally {
            if (dataMap != null && dataMap instanceof IObtainable) {
                ((IObtainable) dataMap).recycle();
            }
        }

        if (t != null) {
            throw KscException.newException(t,
                    response == null ? "Response is null" : response.dump());
        } else if (TextUtils.isEmpty(msg)) {
            throw new KscException(ErrorCode.DATA_TYPE_INVALID,
                    response == null ? "Response is null" : response.dump());
        } else if (!KssDef.VALUE_OK.equalsIgnoreCase(msg)
                && !(ignoreAutoCommit && KssDef.VALUE_AUTO_COMMIT
                        .equalsIgnoreCase(msg))) {
            throw new ServerMsgException(statusCode, msg,
                    response == null ? "Response is null" : response.dump());
        }

    }
}
