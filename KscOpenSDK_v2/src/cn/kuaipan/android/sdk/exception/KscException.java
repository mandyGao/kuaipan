
package cn.kuaipan.android.sdk.exception;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;

import android.content.res.Resources;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;

public class KscException extends Exception implements IKscError, ErrorCode {

    private static final long serialVersionUID = 7461260166746901326L;

    private final int errCode;
    protected final String detailMessage;

    public KscException(int errCode) {
        this(errCode, null, null);
    }

    public KscException(int errCode, String detail) {
        this(errCode, detail, null);
    }

    public KscException(int errCode, Throwable t) {
        this(errCode, t == null ? null : t.toString(), t);
    }

    public KscException(int errCode, String detail, Throwable t) {
        super("ErrCode: " + errCode
                + (detail == null ? "" : "\n" + getDetail(errCode, detail)),
                getSerial(t));
        this.errCode = errCode;
        this.detailMessage = detail;
    }

    private static String getDetail(int errCode, String detail) {
        if (detail != null && errCode >= ERR_MIN_LOCAL_IO
                && errCode <= ERR_MAX_LOCAL_IO && detail.length() > 100) {
            return detail.substring(0, 100);
        }
        return detail;
    }

    public int getErrorCode() {
        return errCode;
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
        return ErrorReason.getReason(res, getErrorCode());
    }

    public static KscException newException(Throwable t, String detailState)
            throws InterruptedException {
        if (t instanceof KscException) {
            return (KscException) t;
        }

        ErrorHelper.handleInterruptException(t);
        return newInstance(t, detailState);
    }

    static Throwable getSerial(Throwable t) {
        if (t == null) {
            return t;
        }

        Throwable result = t;
        if (t instanceof HttpHostConnectException) {
            result = new HttpHostConnectExceptionWrapper(
                    (HttpHostConnectException) t);
        }
        return result;
    }

    public static KscException newInstance(Throwable t, String detailState) {
        if (t instanceof KscException) {
            return (KscException) t;
        }

        // SocketException - connect failed
        // SocketTimeoutException - transf
        // UnknownHostException -
        // HttpHostConnectException -
        // ConnectTimeoutException - can't connect
        // ClientProtocolException

        if (t instanceof ConnectException) {
            return new NetworkException(NET_ECONNREFUSED, detailState, t);
        } else if (t instanceof SocketException) {
            return new NetworkException(UNKNOW_ERR_NETWORK, detailState, t);
        } else if (t instanceof SocketTimeoutException) {
            return new NetworkException(NET_SOCKET_TIMEOUT, detailState, t);
        } else if (t instanceof ConnectTimeoutException) {
            return new NetworkException(NET_SOCKET_ETIMEDOUT, detailState, t);
        } else if (t instanceof ClientProtocolException) {
            return new NetworkException(NET_ERROR_HTTP_PROTOCOL, detailState, t);
        } else if (t instanceof UnknownHostException) {
            return new NetworkException(NET_ERROR_UNKNOW_HOST, detailState, t);
        } else if (t instanceof InvalidKeyException) {
            return new KscException(INVALID_DATA, detailState, t);
        } else if (t instanceof IOException) {
            return new KscException(getIOCode((IOException) t), detailState, t);
        } else if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else {
            return new KscException(UNKNOW_ERR, detailState, t);
        }
    }

    public static int getIOCode(IOException e) {
        String msg = e.getMessage();
        int result = UNKNOW_ERR_LOCAL_IO;
        if (!TextUtils.isEmpty(msg)) {
            if (msg.endsWith("(No such file or directory)")) {
                result = IO_ERRNO_ENOENT;
            } else if (msg.endsWith("No space left on device")
                    || msg.endsWith("(No space left on device)")) {
                result = IO_ERRNO_ENOSPC;
            } else if (msg.endsWith("(Read-only file system)")) {
                result = IO_ERRNO_EROFS;
            } else if (msg.endsWith("(Bad file number)")) {
                result = IO_ERRNO_EBADF;
            } else if (msg.endsWith("I/O error") || msg.endsWith("(I/O error)")) {
                result = IO_ERRNO_EIO;
            } else if (msg.endsWith("(Try again)")) {
                result = IO_ERRNO_EAGAIN;
            } else if (msg.endsWith("(Permission denied)")) {
                result = IO_ERRNO_EACCES;
            }
        }

        if (result == UNKNOW_ERR_LOCAL_IO && e instanceof FileNotFoundException) {
            result = IO_CUSTOM_UNKNOW_FILE_NOT_FOUND;
        }
        return result;
    }
}
