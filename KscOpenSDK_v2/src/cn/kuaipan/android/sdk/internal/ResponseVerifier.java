
package cn.kuaipan.android.sdk.internal;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;

import android.text.TextUtils;
import android.util.Log;
import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.utils.OAuthTimeUtils;

public abstract class ResponseVerifier {

    private static final String LOG_TAG = "ResponseVerifier";

    abstract public void verify(KscHttpResponse response, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException;

    protected static void throwServerError(int statusCode, String msg,
            KscHttpResponse origResponse) throws ServerException,
            ServerMsgException {
        if (TextUtils.isEmpty(msg)) {
            throw new ServerException(statusCode, origResponse.dump());
        } else {
            ServerMsgException e = new ServerMsgException(statusCode, msg,
                    origResponse.dump());
            if (e.getErrorCode() == ErrorCode.MSG401_REQUEST_EXPIRED) {
                syncServTime(origResponse.getResponse());
            }
            throw e;
        }
    }

    private static void syncServTime(HttpResponse response) {
        try {
            Header dateHeader = response
                    .getLastHeader(org.apache.http.protocol.HTTP.DATE_HEADER);
            Date date = new Date(dateHeader.getValue());
            OAuthTimeUtils.setRealTime(date.getTime());
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed sync server time.", e);
        }
    }
}
