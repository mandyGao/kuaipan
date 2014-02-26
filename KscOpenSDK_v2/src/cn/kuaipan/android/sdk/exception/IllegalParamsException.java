
package cn.kuaipan.android.sdk.exception;

public class IllegalParamsException extends KscRuntimeException {

    private static final long serialVersionUID = 1934114237558449485L;

    public IllegalParamsException(int code) {
        super(code);
    }

    public IllegalParamsException(int code, String detail) {
        super(code, detail);
    }
}
