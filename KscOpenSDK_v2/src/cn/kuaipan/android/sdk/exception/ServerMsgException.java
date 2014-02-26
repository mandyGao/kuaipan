
package cn.kuaipan.android.sdk.exception;

import android.content.res.Resources;
import android.text.TextUtils;

public class ServerMsgException extends KscException {

    private static final long serialVersionUID = -681123175263669159L;

    private final int statusCode;
    private final String origMessage;

    public ServerMsgException(int statusCode, String message) {
        this(statusCode, message, null);
    }

    public ServerMsgException(int statusCode, String message, String details) {
        super(ServerMsgMap.getErrorCode(statusCode, message), details);
        this.statusCode = statusCode;
        this.origMessage = message;
    }

    public ServerMsgException(int statusCode, int errcode, String details) {
        super(errcode, details);
        this.statusCode = statusCode;
        this.origMessage = "Message not come from api server.";
    }

    @Override
    public String getReason(Resources res) {
        StringBuilder bulider = new StringBuilder();
        bulider.append(super.getReason(res));
        if (getErrorCode() == UNKNOW_ERR_SERV_MSG) {
            bulider.append(" (code=");
            bulider.append(statusCode);
            if (!TextUtils.isEmpty(origMessage)) {
                bulider.append(", ");
                bulider.append(origMessage);
            }
            bulider.append(")");
        }

        return bulider.toString();
    }

    public String getSimpleMessage() {
        String name = getClass().getName();
        String result = name + "(ErrCode: " + getErrorCode()
                + "): StatusCode: " + statusCode;
        if (origMessage != null) {
            result += ", msg: " + origMessage;
        }
        if (detailMessage != null && detailMessage.length() < 100) {
            result += ", " + detailMessage;
        }
        return result;
    }

    @Override
    public String getMessage() {
        if (TextUtils.isEmpty(origMessage)) {
            return super.getMessage();
        } else {
            return origMessage + "\n" + super.getMessage();
        }
    }

    public String getOrigMessage() {
        return origMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
