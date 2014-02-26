
package cn.kuaipan.android.sdk.internal;

import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.model.ResultMsg;
import cn.kuaipan.android.utils.IObtainable;

import android.text.TextUtils;

import java.util.Map;

public class OAuthMsgVerifier extends DefaultOAuthVerifier {
    private final String[] mIgnores;

    public OAuthMsgVerifier(String... ignores) {
        mIgnores = ignores;
    }

    @Override
    public void verify(KscHttpResponse response, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException {
        super.verify(response, appendMode);

        Map<String, Object> dataMap = null;
        try {
            dataMap = ApiDataHelper.contentToMap(response);
            ResultMsg data = ApiDataHelper.parser(response, dataMap,
                    ResultMsg.class);
            String msg = data == null ? null : data.msg;

            verifyMsg(response, msg);
        } catch (InterruptedException e) {
            throw e;
        } finally {
            if (dataMap != null && dataMap instanceof IObtainable) {
                ((IObtainable) dataMap).recycle();
            }
        }
    }

    public void verifyMsg(KscHttpResponse response, String msg)
            throws KscException {

        int statusCode = response.getStatusCode();
        boolean result = false;
        if (ResultMsg.MSG_OK.equalsIgnoreCase(msg)) {
            result = true;
        }
        if (mIgnores != null && !result) {
            for (String ignore : mIgnores) {
                if (TextUtils.isEmpty(ignore)) {
                    continue;
                }
                if (ignore.equalsIgnoreCase(msg)) {
                    result = true;
                    break;
                }
            }
        }

        if (!result) {
            throwServerError(statusCode, msg, response);
        }
    }
}
