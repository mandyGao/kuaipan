
package cn.kuaipan.android.sdk.exception;

import android.content.res.Resources;
import android.text.TextUtils;

public class NetworkException extends KscException {

    private static final long serialVersionUID = 3410936099313815279L;

    private final String origMessage;

    public NetworkException(int errorCode, String detailState, Throwable t) {
        super(errorCode, detailState, t);
        origMessage = t == null ? null : t.getMessage();
    }

    @Override
    public String getReason(Resources res) {
        if (TextUtils.isEmpty(origMessage)) {
            return super.getReason(res);
        }
        StringBuilder bulider = new StringBuilder();
        bulider.append(super.getReason(res));
        bulider.append("\n (res: ");
        bulider.append(origMessage);
        bulider.append(")");

        return bulider.toString();
    }

    @Override
    public String getMessage() {
        if (TextUtils.isEmpty(origMessage)) {
            return super.getMessage();
        } else {
            return origMessage + "\n" + super.getMessage();
        }
    }

    public String getSimpleMessage() {
        String name = getClass().getName();
        String result = name + "(ErrCode: " + getErrorCode() + ")";
        Throwable t = getCause();
        if (t != null) {
            result += " - [" + t.getClass().getName();
            if (origMessage != null) {
                result += ": " + origMessage;
            }
            result += "]";
        }

        if (detailMessage != null && detailMessage.length() < 100) {
            result += ": " + detailMessage;
        }
        return result;
    }
}
