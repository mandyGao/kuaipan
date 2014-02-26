
package cn.kuaipan.android.sdk.exception;

import cn.kuaipan.android.R;

import android.content.res.Resources;
import android.util.SparseIntArray;

public final class ErrorReason implements ErrorCode {

    private static final SparseIntArray sErrResMap;
    static {
        sErrResMap = new SparseIntArray();
        sErrResMap.put(UNKNOW_ERR_RUNTIME, R.string.KSCSDK_UNKNOW_ERR_RUNTIME);
        sErrResMap.put(UNKNOW_ERR_DATA, R.string.KSCSDK_UNKNOW_ERR_DATA);
        sErrResMap.put(UNKNOW_ERR_NETWORK, R.string.KSCSDK_UNKNOW_ERR_NETWORK);
        sErrResMap.put(UNKNOW_ERR_SERVER, R.string.KSCSDK_UNKNOW_ERR_SERVER);
        sErrResMap
                .put(UNKNOW_ERR_SERV_MSG, R.string.KSCSDK_UNKNOW_ERR_SERV_MSG);
        sErrResMap
                .put(UNKNOW_ERR_LOCAL_IO, R.string.KSCSDK_UNKNOW_ERR_LOCAL_IO);
        sErrResMap.put(UNKNOW_ERR, R.string.KSCSDK_UNKNOW_ERR);

        sErrResMap.put(MISS_USER_TOKEN, R.string.KSCSDK_MISS_USER_TOKEN);
        sErrResMap.put(NULL_PARAM, R.string.KSCSDK_NULL_PARAM);
        sErrResMap.put(INVALID_PARAM, R.string.KSCSDK_INVALID_PARAM);
        sErrResMap.put(LIMIT_NO_SPACE, R.string.KSCSDK_LIMIT_NO_SPACE);
        sErrResMap
                .put(FRAMEWORK_UNSUPPORT, R.string.KSCSDK_FRAMEWORK_UNSUPPORT);

        sErrResMap.put(DATA_MISS_PARSER, R.string.KSCSDK_DATA_MISS_PARSER);
        sErrResMap.put(BAD_DATA_PARSER, R.string.KSCSDK_BAD_DATA_PARSER);

        sErrResMap.put(DATA_IS_NOT_JSON, R.string.KSCSDK_DATA_IS_NOT_JSON);
        sErrResMap.put(DATA_UNSCHEDULE, R.string.KSCSDK_DATA_UNSCHEDULE);
        sErrResMap.put(DATA_TYPE_INVALID, R.string.KSCSDK_DATA_TYPE_INVALID);
        sErrResMap.put(DATA_IS_EMPTY, R.string.KSCSDK_DATA_IS_EMPTY);

        sErrResMap.put(SERV_ERR_202, R.string.KSCSDK_SERV_ERR_202);
        sErrResMap.put(SERV_ERR_400, R.string.KSCSDK_SERV_ERR_400);
        sErrResMap.put(SERV_ERR_401, R.string.KSCSDK_SERV_ERR_401);
        sErrResMap.put(SERV_ERR_403, R.string.KSCSDK_SERV_ERR_403);
        sErrResMap.put(SERV_ERR_404, R.string.KSCSDK_SERV_ERR_404);
        sErrResMap.put(SERV_ERR_406, R.string.KSCSDK_SERV_ERR_406);
        sErrResMap.put(SERV_ERR_413, R.string.KSCSDK_SERV_ERR_413);
        sErrResMap.put(SERV_ERR_500, R.string.KSCSDK_SERV_ERR_500);
        sErrResMap.put(SERV_ERR_504, R.string.KSCSDK_SERV_ERR_504);
        sErrResMap.put(SERV_ERR_507, R.string.KSCSDK_SERV_ERR_507);
        sErrResMap.put(SERV_ERR_5xx, R.string.KSCSDK_SERV_ERR_5xx);

        sErrResMap.put(NET_SOCKET_EINVAL, R.string.KSCSDK_NET_SOCKET_EINVAL);
        sErrResMap.put(NET_SOCKET_ENETUNREACH,
                R.string.KSCSDK_NET_SOCKET_ENETUNREACH);
        sErrResMap.put(NET_SOCKET_ETIMEDOUT,
                R.string.KSCSDK_NET_SOCKET_ETIMEDOUT);
        sErrResMap.put(NET_ECONNREFUSED, R.string.KSCSDK_NET_ECONNREFUSED);
        sErrResMap.put(NET_SOCKET_EHOSTUNREACH,
                R.string.KSCSDK_NET_SOCKET_EHOSTUNREACH);
        sErrResMap.put(NET_SOCKET_TIMEOUT, R.string.KSCSDK_NET_SOCKET_TIMEOUT);
        sErrResMap.put(NET_ERROR_HTTP_PROTOCOL,
                R.string.KSCSDK_NET_ERROR_HTTP_PROTOCOL);
        sErrResMap.put(NET_ERROR_UNKNOW_HOST,
                R.string.KSCSDK_NET_ERROR_UNKNOW_HOST);

        sErrResMap.put(MSG200_FILE_EXIST, R.string.KSCSDK_MSG200_FILE_EXIST);
        sErrResMap.put(MSG200_BAD_PARAMS, R.string.KSCSDK_MSG200_BAD_PARAMS);
        sErrResMap.put(MSG200_SERVER_EXCEPTION,
                R.string.KSCSDK_MSG200_SERVER_EXCEPTION);
        sErrResMap.put(MSG200_INVALID_CUSTOMERID,
                R.string.KSCSDK_MSG200_INVALID_CUSTOMERID);
        sErrResMap.put(MSG200_INVALID_STOID,
                R.string.KSCSDK_MSG200_INVALID_STOID);
        sErrResMap.put(MSG200_STORAGE_REQUEST_ERROR,
                R.string.KSCSDK_MSG200_STORAGE_REQUEST_ERROR);
        sErrResMap.put(MSG200_STORAGE_REQUEST_FAILED,
                R.string.KSCSDK_MSG200_STORAGE_REQUEST_FAILED);
        sErrResMap.put(MSG200_COMMIT_FAIL, R.string.KSCSDK_MSG200_COMMIT_FAIL);

        sErrResMap.put(MSG202_BAD_ACCOUNT_FORMAT,
                R.string.KSCSDK_MSG202_BAD_ACCOUNT_FORMAT);
        sErrResMap.put(MSG202_ACCOUNT_CONFLICT,
                R.string.KSCSDK_MSG202_ACCOUNT_CONFLICT);
        sErrResMap.put(MSG202_LOGIN_FAIL, R.string.KSCSDK_MSG202_LOGIN_FAIL);
        sErrResMap.put(MSG202_BAD_OPENID, R.string.KSCSDK_MSG202_BAD_OPENID);
        sErrResMap.put(MSG202_WRONG_CODE, R.string.KSCSDK_MSG202_WRONG_CODE);
        sErrResMap.put(MSG202_CANNOT_MKROOT,
                R.string.KSCSDK_MSG202_CANNOT_MKROOT);

        sErrResMap.put(MSG202_FILE_EXIST, R.string.KSCSDK_MSG202_FILE_EXIST);
        sErrResMap.put(MSG202_FILE_NOT_EXIST,
                R.string.KSCSDK_MSG202_FILE_NOT_EXIST);
        sErrResMap.put(MSG202_FILE_TOO_MANY,
                R.string.KSCSDK_MSG202_FILE_TOO_MANY);
        sErrResMap.put(MSG202_FILE_TOO_LARGE,
                R.string.KSCSDK_MSG202_FILE_TOO_LARGE);
        sErrResMap.put(MSG202_OVER_SPACE, R.string.KSCSDK_MSG202_OVER_SPACE);

        sErrResMap.put(MSG202_COMMIT_FAIL, R.string.KSCSDK_MSG202_COMMIT_FAIL);
        sErrResMap.put(MSG202_FORBIDDEN, R.string.KSCSDK_MSG202_FORBIDDEN);
        sErrResMap.put(MSG202_SERVER_DOWN, R.string.KSCSDK_MSG202_SERVER_DOWN);

        sErrResMap.put(MSG202_BAD_ACCESS_CODE,
                R.string.KSCSDK_MSG202_BAD_ACCESS_CODE);
        sErrResMap.put(MSG202_LONG_ACCESS_CODE,
                R.string.KSCSDK_MSG202_LONG_ACCESS_CODE);

        sErrResMap.put(MSG202_CYCLE_SHARE, R.string.KSCSDK_MSG202_CYCLE_SHARE);
        sErrResMap.put(MSG202_ACCOUNT_BINDED,
                R.string.KSCSDK_MSG202_ACCOUNT_BINDED);

        sErrResMap.put(MSG400_BAD_PARAMS, R.string.KSCSDK_MSG400_BAD_PARAMS);
        sErrResMap.put(MSG400_BAD_REQEST, R.string.KSCSDK_MSG400_BAD_REQEST);
        sErrResMap.put(MSG400_BAD_API, R.string.KSCSDK_MSG400_BAD_API);
        sErrResMap.put(MSG400_SERVER_ERR, R.string.KSCSDK_MSG400_SERVER_ERR);
        sErrResMap.put(MSG400_ACCOUNT_SERVER_ERR,
                R.string.KSCSDK_MSG400_ACCOUNT_SERVER_ERR);
        sErrResMap.put(MSG400_UNKNOW_ERR, R.string.KSCSDK_MSG400_UNKNOW_ERR);
        sErrResMap
                .put(MSG400_REQUEST_FAIL, R.string.KSCSDK_MSG400_REQUEST_FAIL);
        sErrResMap.put(MSG400_MOBILE_BINDED,
                R.string.KSCSDK_MSG400_MOBILE_BINDED);
        sErrResMap
                .put(MSG400_SEND_MSG_ERR, R.string.KSCSDK_MSG400_SEND_MSG_ERR);
        sErrResMap
                .put(MSG400_MANY_REQUEST, R.string.KSCSDK_MSG400_MANY_REQUEST);
        sErrResMap
                .put(MSG400_FREQ_REQUEST, R.string.KSCSDK_MSG400_FREQ_REQUEST);
        sErrResMap
                .put(MSG400_INVALID_CODE, R.string.KSCSDK_MSG400_INVALID_CODE);
        sErrResMap.put(MSG400_INVALID_MOBILE,
                R.string.KSCSDK_MSG400_INVALID_MOBILE);
        sErrResMap.put(MSG400_EMPTY_PASSWORD,
                R.string.KSCSDK_MSG400_EMPTY_PASSWORD);
        sErrResMap.put(MSG400_LONG_PASSWORD,
                R.string.KSCSDK_MSG400_LONG_PASSWORD);
        sErrResMap.put(MSG400_NOT_FOUND_USER,
                R.string.KSCSDK_MSG400_NOT_FOUND_USER);
        sErrResMap.put(MSG400_MOBILE_BINDED,
                R.string.KSCSDK_MSG400_MOBILE_BINDED);
        sErrResMap.put(MSG400_CANNOT_SET_PWD,
                R.string.KSCSDK_MSG400_CANNOT_SET_PWD);
        sErrResMap.put(MSG400_NOT_REQUEST, R.string.KSCSDK_MSG400_NOT_REQUEST);
        sErrResMap.put(MSG400_FILE_NOT_EXIST,
                R.string.KSCSDK_MSG400_FILE_NOT_EXIST);

        sErrResMap.put(MSG401_BAD_SIGN, R.string.KSCSDK_MSG401_BAD_SIGN);
        sErrResMap
                .put(MSG401_REUSED_NONCE, R.string.KSCSDK_MSG401_REUSED_NONCE);
        sErrResMap
                .put(MSG401_BAD_CONSUMER, R.string.KSCSDK_MSG401_BAD_CONSUMER);
        sErrResMap.put(MSG401_REQUEST_EXPIRED,
                R.string.KSCSDK_MSG401_REQUEST_EXPIRED);
        sErrResMap.put(MSG401_AUTHMODE_UNSUPPORT,
                R.string.KSCSDK_MSG401_AUTHMODE_UNSUPPORT);
        sErrResMap
                .put(MSG401_AUTH_EXPIRED, R.string.KSCSDK_MSG401_AUTH_EXPIRED);
        sErrResMap.put(MSG401_APICALL_LIMIT,
                R.string.KSCSDK_MSG401_APICALL_LIMIT);
        sErrResMap.put(MSG401_NOAPI_PERMISSION,
                R.string.KSCSDK_MSG401_NOAPI_PERMISSION);
        sErrResMap.put(MSG401_BAD_VERIFER, R.string.KSCSDK_MSG401_BAD_VERIFER);
        sErrResMap.put(MSG401_AUTH_FAILED, R.string.KSCSDK_MSG401_AUTH_FAILED);

        sErrResMap.put(MSG403_FILE_EXIST, R.string.KSCSDK_MSG403_FILE_EXIST);
        sErrResMap.put(MSG403_FORBIDDEN, R.string.KSCSDK_MSG403_FORBIDDEN);

        sErrResMap.put(MSG404_FILE_NOT_EXIST,
                R.string.KSCSDK_MSG404_FILE_NOT_EXIST);
        sErrResMap
                .put(MSG404_NO_SUCH_USER, R.string.KSCSDK_MSG404_NO_SUCH_USER);
        sErrResMap.put(MSG406_FILE_TOO_MANY,
                R.string.KSCSDK_MSG406_FILE_TOO_MANY);
        sErrResMap.put(MSG413_FILE_TOO_LARGE,
                R.string.KSCSDK_MSG413_FILE_TOO_LARGE);
        sErrResMap.put(MSG500_SERVER_ERR, R.string.KSCSDK_MSG500_SERVER_ERR);
        sErrResMap.put(MSG500_SERVER_API_ERR,
                R.string.KSCSDK_MSG500_SERVER_API_ERR);
        sErrResMap.put(MSG507_OVER_SPACE, R.string.KSCSDK_MSG507_OVER_SPACE);

        sErrResMap.put(IO_ERRNO_ENOENT, R.string.KSCSDK_IOERR_MISS_FILE);
        sErrResMap.put(IO_CUSTOM_FILE_CHANGED,
                R.string.KSCSDK_IOERR_FILE_CHANGED);
        sErrResMap.put(IO_ERRNO_EACCES, R.string.KSCSDK_IOERR_NO_PROMISSION);
    }

    private ErrorReason() {
    }

    public static String getReason(Resources res, int errCode) {
        int code = sErrResMap.get(errCode, -1);
        if (code == -1) {
            code = sErrResMap.get(getUnknowId(errCode),
                    R.string.KSCSDK_UNKNOW_ERR);
        }
        return res.getString(code);
    }

    private static int getUnknowId(int errCode) {
        // ============================
        // 500xxx - Runtime
        // 501xxx - parse exception
        // 503xxx - server error
        // 504xxx - net
        // 2xxxyy - server message
        // 403xxx - Local IO
        // ============================
        int result = UNKNOW_ERR;
        if (errCode >= ERR_MIN_SERV_MSG && errCode <= ERR_MAX_SERV_MSG) {
            result = UNKNOW_ERR_SERV_MSG;
        } else if (errCode >= ERR_MIN_LOCAL_IO && errCode <= ERR_MAX_LOCAL_IO) {
            result = UNKNOW_ERR_LOCAL_IO;
        } else if (errCode >= ERR_MIN_RUNTIME && errCode <= ERR_MAX_RUNTIME) {
            result = UNKNOW_ERR_RUNTIME;
        } else if (errCode >= ERR_MIN_DATA && errCode <= ERR_MAX_DATA) {
            result = UNKNOW_ERR_DATA;
        } else if (errCode >= ERR_MIN_SERVER && errCode <= ERR_MAX_SERVER) {
            if (errCode > SERV_ERR_500 && errCode <= SERV_ERR_5xx) {
                result = SERV_ERR_5xx;
            } else {
                result = UNKNOW_ERR_SERVER;
            }
        } else if (errCode >= ERR_MIN_NETWORK && errCode <= ERR_MAX_NETWORK) {
            result = UNKNOW_ERR_NETWORK;
        }
        return result;
    }
}
