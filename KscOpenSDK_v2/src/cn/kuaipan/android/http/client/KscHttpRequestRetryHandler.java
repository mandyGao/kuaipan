
package cn.kuaipan.android.http.client;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

public class KscHttpRequestRetryHandler implements HttpRequestRetryHandler {

    /** the number of times a method will be retried */
    private final int retryCount;

    /**
     * Whether or not methods that have successfully sent their request will be
     * retried
     */
    private final boolean requestSentRetryEnabled;

    private final int errorTimeOut;

    /**
     * Default constructor
     */
    public KscHttpRequestRetryHandler(int retryCount,
            boolean requestSentRetryEnabled, int errorTimeOut) {
        super();
        this.retryCount = retryCount;
        this.requestSentRetryEnabled = requestSentRetryEnabled;
        this.errorTimeOut = errorTimeOut;
    }

    /**
     * Default constructor
     */
    public KscHttpRequestRetryHandler() {
        this(3, false, 0);
    }

    /**
     * Used <code>retryCount</code> and <code>requestSentRetryEnabled</code> to
     * determine if the given method should be retried.
     */
    public boolean retryRequest(final IOException exception,
            int executionCount, final HttpContext context) {
        if (exception == null) {
            throw new IllegalArgumentException(
                    "Exception parameter may not be null");
        }

        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }

        if (executionCount > this.retryCount) {
            // Do not retry if over max retry count
            return false;
        }
        if (exception instanceof NoHttpResponseException) {
            // Retry if the server dropped connection on us
            return true;
        }
        if (exception instanceof InterruptedIOException) {
            // Timeout
            return false;
        }
        if (exception instanceof UnknownHostException) {
            // Unknown host
            return false;
        }
        if (exception instanceof SSLHandshakeException) {
            // SSL handshake exception
            return false;
        }

        Boolean b = (Boolean) context
                .getAttribute(ExecutionContext.HTTP_REQ_SENT);
        Long l = (Long) context.getAttribute(KscHttpClient.KSC_CONNECT_START);
        RequestWrapper wrapper = (RequestWrapper) context
                .getAttribute(ExecutionContext.HTTP_REQUEST);
        URIRedirector redirector = (URIRedirector) context
                .getAttribute(KscHttpClient.KSC_CONNECT_REDIRECTOR);
        boolean sent = (b != null && b.booleanValue());
        long start = l != null ? l : 0;
        long current = SystemClock.elapsedRealtime();
        if (!sent
                || (this.requestSentRetryEnabled && wrapper.isRepeatable())
                || ((exception instanceof SocketException) && (current - start) <= errorTimeOut)) {
            // Retry if the request has not been sent fully or
            // if it's OK to retry methods that have been sent

            if (redirector != null) {
                return redirector.redirect(context);
            }
            return true;
        }
        // otherwise do not retry
        return false;
    }

    /**
     * @return <code>true</code> if this handler will retry methods that have
     *         successfully sent their request, <code>false</code> otherwise
     */
    public boolean isRequestSentRetryEnabled() {
        return requestSentRetryEnabled;
    }

    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return retryCount;
    }

}
