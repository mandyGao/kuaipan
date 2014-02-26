
package cn.kuaipan.android.sdk.internal;

import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.sdk.model.SignInfo;
import cn.kuaipan.android.utils.IObtainable;

import android.util.SparseIntArray;

import java.util.Map;

public class CheckinVerifier extends DefaultOAuthVerifier {

    private static final SparseIntArray sErrorMap;

    @Override
    public void verify(KscHttpResponse response, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException {
        super.verify(response, appendMode);

        int statusCode = response.getStatusCode();

        Integer state = null;
        Map<String, Object> dataMap = null;
        try {
            dataMap = ApiDataHelper.contentToMap(response);
            SignInfo data = ApiDataHelper.parser(response, dataMap,
                    SignInfo.class);
            state = data == null ? null : data.state;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            // ignore
        } finally {
            if (dataMap != null && dataMap instanceof IObtainable) {
                ((IObtainable) dataMap).recycle();
            }
        }
        if (state != SignInfo.SUCCESS && state != SignInfo.SIGN_AGAIN
                && state != SignInfo.QUOTA_REACH) {
            throw new ServerMsgException(statusCode, stateToErr(state),
                    response == null ? "Response is null" : response.dump());
        }
    }

    private int stateToErr(Integer state) {
        if (state == null) {
            return ErrorCode.UNKNOW_ERR_SERV_MSG;
        }
        return sErrorMap.get(state, ErrorCode.UNKNOW_ERR_SERV_MSG);
    }

    static {
        sErrorMap = new SparseIntArray();
        sErrorMap.append(SignInfo.ERROR_PARAM, ErrorCode.MSG400_BAD_PARAMS);
        sErrorMap.append(SignInfo.ERROR_INVALID_USER,
                ErrorCode.MSG401_AUTH_FAILED);
        sErrorMap.append(SignInfo.ERROR_KAPI, ErrorCode.MSG500_SERVER_API_ERR);
        sErrorMap.append(SignInfo.ERROR_DENY, ErrorCode.MSG202_SERVER_DOWN);
        sErrorMap.append(SignInfo.ERROR_NO_ENOUGH_SCORES,
                ErrorCode.MSG500_SERVER_API_ERR);
        sErrorMap.append(SignInfo.ERROR_UPDATE_SCORE_FAIL,
                ErrorCode.MSG500_SERVER_ERR);
        sErrorMap.append(SignInfo.ERROR_SRVERR, ErrorCode.MSG500_SERVER_ERR);

    }

}
