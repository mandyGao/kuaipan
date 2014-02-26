
package cn.kuaipan.android.sdk.exception;

import android.content.res.Resources;

public class KscRuntimeException extends RuntimeException implements IKscError {

    private static final long serialVersionUID = 4693852528580738850L;

    private final int errCode;
    private final String detailMessage;

    public KscRuntimeException(int code) {
        this(code, null, null);
    }

    public KscRuntimeException(int code, String details) {
        this(code, details, null);
    }

    public KscRuntimeException(int code, Throwable t) {
        this(code, t == null ? null : t.toString(), t);
    }

    public KscRuntimeException(int code, String details, Throwable e) {
        super("ErrCode: " + code + (details == null ? "" : "\n" + details),
                KscException.getSerial(e));
        this.errCode = code;
        this.detailMessage = details;
    }

    public String getSimpleMessage() {
        String name = getClass().getName();
        String result = name + "(ErrCode: " + errCode + ")";
        if (detailMessage != null && detailMessage.length() < 100) {
            result = result + ": " + detailMessage;
        }
        return result;
    }

    @Override
    public String getReason(Resources res) {
        int result = getErrorCode();
        if (result > ERR_MAX_RUNTIME || result < ERR_MIN_RUNTIME) {
            result = UNKNOW_ERR_RUNTIME;
        }

        return ErrorReason.getReason(res, result);
    }

    @Override
    public int getErrorCode() {
        return errCode;
    }
}
