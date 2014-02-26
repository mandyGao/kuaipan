
package cn.kuaipan.android.sdk.exception;

public class ServerException extends KscException {

    private static final long serialVersionUID = 6373467541984892922L;

    private final int statusCode;

    public ServerException(int statusCode, String detail) {
        super(ERR_MIN_SERVER + validCode(statusCode), detail);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    private static int validCode(int statusCode) {
        if (statusCode >= 100 && statusCode <= 599) {
            return statusCode;
        } else {
            return 0;
        }
    }

    public String getSimpleMessage() {
        String name = getClass().getName();
        String result = name + "(ErrCode: " + getErrorCode()
                + "): StatusCode: " + statusCode;
        if (detailMessage != null && detailMessage.length() < 100) {
            result += ", " + detailMessage;
        }
        return result;
    }
}
