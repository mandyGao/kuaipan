
package cn.kuaipan.android.sdk.exception;

import org.sky.base.utils.TwoKeyHashMap;

import android.text.TextUtils;

final class ServerMsgMap implements ErrorCode {

    private static TwoKeyHashMap<Integer, String, Integer> CODE_MAP;
    static {
        CODE_MAP = new TwoKeyHashMap<Integer, String, Integer>();
        add2Map(202, MSG202_BAD_ACCOUNT_FORMAT, "badEmailFormat");
        add2Map(202, MSG202_ACCOUNT_CONFLICT, "sameEmailRegisteredBefore");

        add2Map(202, MSG202_LOGIN_FAIL, "login fail");
        add2Map(202, MSG202_BAD_OPENID, "bad openid");
        add2Map(202, MSG202_WRONG_CODE, "wrong verification code");
        add2Map(202, MSG202_CANNOT_MKROOT, "cannot create app folder");
        add2Map(202, MSG202_BAD_ACCESS_CODE, "pickupCodeNotSupport");
        add2Map(202, MSG202_LONG_ACCESS_CODE, "pickupCodeTooLong");

        add2Map(202, MSG202_FILE_EXIST, "file exist");
        add2Map(202, MSG202_FILE_NOT_EXIST, "file not exist");
        add2Map(202, MSG202_FILE_TOO_MANY, "tooManyFiles");
        add2Map(202, MSG202_FILE_TOO_LARGE, "file too large");
        add2Map(202, MSG202_OVER_SPACE, "over space");
        add2Map(202, MSG202_PATH_TOO_LONG, "fnameTooLong");

        add2Map(202, MSG202_COMMIT_FAIL, "commit fail");
        add2Map(202, MSG202_FORBIDDEN, "forbidden");
        add2Map(202, MSG202_SERVER_DOWN, "account server error");

        add2Map(202, MSG202_CYCLE_SHARE, "shared");
        add2Map(202, MSG202_ACCOUNT_BINDED, "cannotBind");

        add2Map(400, MSG400_BAD_PARAMS, "bad parameters");
        add2Map(400, MSG400_BAD_REQEST, "bad request");
        add2Map(400, MSG400_BAD_API, "no such api implemented");
        add2Map(400, MSG400_BAD_PARAMS, "clientBadParams");
        add2Map(400, MSG400_SERVER_ERR, "serverError");
        add2Map(400, MSG400_ACCOUNT_SERVER_ERR, "accountServerError");
        add2Map(400, MSG400_UNKNOW_ERR, "unknownError");
        add2Map(400, MSG400_REQUEST_FAIL, "requestFail");
        add2Map(400, MSG400_MOBILE_BINDED, "mobileExists");
        add2Map(400, MSG400_SEND_MSG_ERR, "sendMsgError");
        add2Map(400, MSG400_MANY_REQUEST, "tooManyRequests");
        add2Map(400, MSG400_FREQ_REQUEST, "tooOften");
        add2Map(400, MSG400_INVALID_CODE, "invalidCode");
        add2Map(400, MSG400_INVALID_MOBILE, "invalidMobile");
        add2Map(400, MSG400_EMPTY_PASSWORD, "emptyPassword");
        add2Map(400, MSG400_LONG_PASSWORD, "passwordTooLong");
        add2Map(400, MSG400_NOT_FOUND_USER, "noSuchUser");
        add2Map(400, MSG400_EMPTY_PASSWORD, "needPassword");
        add2Map(400, MSG400_CANNOT_SET_PWD, "canNotSetPassword");
        add2Map(400, MSG400_NOT_REQUEST, "verifyNotRequest");
        add2Map(400, MSG400_EXPIRED_CODE, "expiredCode");
        add2Map(400, MSG400_FILE_NOT_EXIST, "file not exist");

        add2Map(401, MSG401_BAD_SIGN, "bad signature");
        add2Map(401, MSG401_REUSED_NONCE, "reused nonce");
        add2Map(401, MSG401_BAD_CONSUMER, "bad consumer key");
        add2Map(401, MSG401_REQUEST_EXPIRED, "request expired");
        add2Map(401, MSG401_AUTHMODE_UNSUPPORT, "not supported auth mode");
        add2Map(401, MSG401_AUTH_EXPIRED, "authorization expired");
        add2Map(401, MSG401_APICALL_LIMIT, "api daily limit");
        add2Map(401, MSG401_NOAPI_PERMISSION, "no right to call this api");
        add2Map(401, MSG401_BAD_VERIFER, "bad verifier");
        add2Map(401, MSG401_AUTH_FAILED, "authorization failed");
        add2Map(401, MSG401_INVALID_TOKEN, "invalid token");

        add2Map(403, MSG403_FILE_EXIST, "file exist");
        add2Map(403, MSG403_FORBIDDEN, "forbidden");

        add2Map(404, MSG404_FILE_NOT_EXIST, "file not exist");
        add2Map(404, MSG404_NO_SUCH_USER, "no such user");
        add2Map(406, MSG406_FILE_TOO_MANY, "too many files");
        add2Map(413, MSG413_FILE_TOO_LARGE, "file too large");
        add2Map(500, MSG500_SERVER_ERR, "server error");
        add2Map(507, MSG507_OVER_SPACE, "over space");

        // for kss
        add2Map(200, MSG200_FILE_EXIST, "file exist");
        add2Map(200, MSG200_COMMIT_FAIL, "commit fail");
        add2Map(200, MSG200_BAD_PARAMS, "ERR_BAD_PARAMS");
        add2Map(200, MSG200_SERVER_EXCEPTION, "ERR_SERVER_EXCEPTION");
        add2Map(200, MSG200_INVALID_CUSTOMERID, "ERR_INVALID_CUSTOMERID");
        add2Map(200, MSG200_INVALID_STOID, "ERR_INVALID_STOID");
        add2Map(200, MSG200_STORAGE_REQUEST_ERROR, "ERR_STORAGE_REQUEST_ERROR");
        add2Map(200, MSG200_STORAGE_REQUEST_FAILED,
                "ERR_STORAGE_REQUEST_FAILED");
        add2Map(200, MSG200_ERR_CHUNK_OUT_OF_RANGE, "ERR_CHUNK_OUT_OF_RANGE");
        add2Map(200, MSG200_ERR_INVALID_UPLOAD_ID, "ERR_INVALID_UPLOAD_ID");
        add2Map(200, MSG200_ERR_INVALID_CHUNK_POS, "ERR_INVALID_CHUNK_POS");
        add2Map(200, MSG200_ERR_INVALID_CHUNK_SIZE, "ERR_INVALID_CHUNK_SIZE");
        add2Map(200, MSG200_ERR_CHUNK_CORRUPTED, "ERR_CHUNK_CORRUPTED");
        add2Map(200, MSG200_ERR_BLOCK_CORRUPTED, "ERR_BLOCK_CORRUPTED");
        add2Map(200, MSG200_ERR_TOO_MANY_CURRENT_BLOCKS,
                "ERR_TOO_MANY_CURRENT_BLOCKS");
        add2Map(200, MSG200_ERR_STORAGE_COMMIT_ERROR,
                "ERR_STORAGE_COMMIT_ERROR");
        // XXX need verify
        add2Map(200, MSG200_FORBIDDEN, "forbidden");
        add2Map(200, MSG200_OVER_SPACE, "over space");
        add2Map(200, MSG200_TARGET_NOTEXIST, "targetNotExist");
        add2Map(200, MSG200_STUB_FAIL, "get stub fail");
        add2Map(200, MSG200_UNSUPPORTED_CHAR, "unsupportedCharRange");
        add2Map(200, MSG200_DATA_OPER_FAIL, "dataOperationFailed");
        add2Map(200, MSG200_FILE_TOO_LARGE, "file too large");
    }

    private static void add2Map(int statusCode, int errorCode, String msg) {
        CODE_MAP.put(statusCode, msg == null ? null : msg.toLowerCase(),
                errorCode);
    }

    public static int getErrorCode(int statusCode, String message) {
        if (TextUtils.isEmpty(message)) {
            message = null;
        } else {
            message = message.trim().toLowerCase();
        }
        Integer result = CODE_MAP.get(statusCode, message);

        return result == null ? UNKNOW_ERR_SERV_MSG : result;
    }
}
