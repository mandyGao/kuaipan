
package cn.kuaipan.android.sdk.exception;

import org.apache.http.conn.HttpHostConnectException;

import java.net.ConnectException;

public class HttpHostConnectExceptionWrapper extends ConnectException {

    private static final long serialVersionUID = 5881503577351939800L;

    public HttpHostConnectExceptionWrapper(HttpHostConnectException e) {
        super(e.getMessage());
        Throwable t = e.getCause();
        if (t != null) {
            initCause(t);
        }
    }
}
