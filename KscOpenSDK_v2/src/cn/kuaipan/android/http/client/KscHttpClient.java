
package cn.kuaipan.android.http.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;

import android.os.SystemClock;
import android.util.Log;
import cn.kuaipan.android.sdk.internal.Constants;

public class KscHttpClient extends DefaultHttpClient {
    private static final String LOG_TAG = "KscHttpClient";

    public static final String KSC_CONNECT_START = "ksc.connect_start";
    public static final String KSC_CONNECT_REDIRECTOR = "ksc.connect_redirector";
    public static final String KSC_CONNECT_TYPE = "ksc.connect_type";
    public static final String KSC_MESSAGE_LIST = "ksc.message_list";

    private static final int TIMEOUT = 30 * 1000;
    private static final int SO_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private static final int ERROR_TIMEOUT = 10 * 1000;

    private static final int CONNECTION_TOTAL_COUNT = 30;

    private static final int SO_BUFFER_SIZE = 8 * 1024;

    /**
     * Create a new HttpClient with reasonable defaults (which you can update).
     * 
     * @param userAgent to report in your HTTP requests
     * @param context to use for caching SSL sessions (may be null for no
     *            caching)
     * @return KscHttpClient for you to use for all your requests.
     */
    public static KscHttpClient newInstance(String userAgent,
            boolean keepAlive, boolean requestSentRetryEnabled) {
        HttpParams params = getHttpParams(userAgent);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        // schemeRegistry.register(new Scheme("https", SSLSocketFactory
        // .getSocketFactory(), 443));
        schemeRegistry.register(ignoreAllCertificate());

        ClientConnectionManager manager = new ThreadSafeClientConnManager(
                params, schemeRegistry);

        // We use a factory method to modify superclass initialization
        // parameters without the funny call-a-static-method dance.
        return new KscHttpClient(manager, params, keepAlive,
                requestSentRetryEnabled);
    }

    private static Scheme ignoreAllCertificate() {
        Scheme scheme = null;
        TrustManager[] tm = {
            new IgnoreCertificationTrustManger()
        };

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tm, null);
            SSLSocketFactory socketFactory = new SSLSocketFactory(
                    sslContext.getSocketFactory());
            // socketFactory.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            if (Constants.DEBUG_INTERNAL) {
                socketFactory
                        .setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            }

            scheme = new Scheme("https", socketFactory, 443);
        } catch (Exception e) {
            Log.println(Log.ASSERT, LOG_TAG, "System can't support TLS.");
        }

        return scheme;
    }

    public static KscHttpClient newKssInstance(String userAgent) {
        HttpParams params = getHttpParams(userAgent);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory
                .getSocketFactory(), 443));

        ClientConnectionManager manager = new ThreadSafeClientConnManager(
                params, schemeRegistry);

        // We use a factory method to modify superclass initialization
        // parameters without the funny call-a-static-method dance.
        return new KscHttpClient(manager, params);
    }

    private static HttpParams getHttpParams(String userAgent) {
        HttpParams params = new BasicHttpParams();

        // Turn off stale checking. Our connections break all the time anyway,
        // and it's not worth it to pay the penalty of checking every time.
        HttpConnectionParams.setStaleCheckingEnabled(params, false);

        // Default connection and socket timeout of 30 seconds. Tweak to taste.
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
        HttpConnectionParams.setSocketBufferSize(params, SO_BUFFER_SIZE);

        ConnManagerParams.setTimeout(params, TIMEOUT);
        ConnManagerParams
                .setMaxTotalConnections(params, CONNECTION_TOTAL_COUNT);
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRoute() {
            @Override
            public int getMaxForRoute(HttpRoute httproute) {
                return 32;
            }
        });

        // Handle redirects auto -- return them to the caller. If our code
        // wants to re-POST after a redirect, this feature need to close.
        HttpClientParams.setRedirecting(params, true);

        // Close Expect:100-Continue -- some proxy or server in HTTP/1.0 or
        // lower is not support the feature.
        HttpProtocolParams.setUseExpectContinue(params, false);

        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpProtocolParams.setHttpElementCharset(params, HTTP.UTF_8);

        // Set the specified user agent and register standard protocols.
        HttpProtocolParams.setUserAgent(params, userAgent);
        return params;
    }

    private final boolean mKeepAlive;
    private final boolean mRequestSentRetryEnabled;
    private final boolean mForKssTransmission;

    private RuntimeException mLeakedException = new IllegalStateException(
            "AndroidHttpClient created and never closed");

    private KscHttpClient(ClientConnectionManager ccm, HttpParams params,
            boolean keepAlive, final boolean requestSentRetryEnabled) {
        super(ccm, params);

        mForKssTransmission = false;
        mKeepAlive = keepAlive;
        mRequestSentRetryEnabled = requestSentRetryEnabled;
    }

    private KscHttpClient(ClientConnectionManager ccm, HttpParams params) {
        super(ccm, params);

        mForKssTransmission = true;
        mKeepAlive = true;
        mRequestSentRetryEnabled = true;
    }

    @Override
    protected BasicHttpProcessor createHttpProcessor() {
        BasicHttpProcessor result;
        if (mForKssTransmission) {
            result = new BasicHttpProcessor();
            result.addInterceptor(new RequestDefaultHeaders());
            // Required protocol interceptors
            result.addInterceptor(new RequestContent());
            result.addInterceptor(new RequestTargetHost());
            // Recommended protocol interceptors
            result.addInterceptor(new RequestConnControl());
            result.addInterceptor(new RequestUserAgent());
            // result.addInterceptor(new RequestExpectContinue());
            // HTTP state management interceptors
            // result.addInterceptor(new RequestAddCookies());
            // result.addInterceptor(new ResponseProcessCookies());
            // HTTP authentication interceptors
            // result.addInterceptor(new RequestTargetAuthentication());
            // result.addInterceptor(new RequestProxyAuthentication());
        } else {
            result = super.createHttpProcessor();
        }
        result.addRequestInterceptor(new TimeMarker());
        result.addRequestInterceptor(new CurlLogger());
        return result;
    }

    @Override
    protected HttpRequestRetryHandler createHttpRequestRetryHandler() {
        return new KscHttpRequestRetryHandler(3, mRequestSentRetryEnabled,
                ERROR_TIMEOUT);
    }

    @Override
    protected HttpRoutePlanner createHttpRoutePlanner() {
        return new KscHttpRoutePlanner(getConnectionManager()
                .getSchemeRegistry());
    }

    @Override
    protected RedirectHandler createRedirectHandler() {
        return new KscRedirectHandler();
    }

    @Override
    protected ConnectionReuseStrategy createConnectionReuseStrategy() {
        ConnectionReuseStrategy strategy;
        if (mKeepAlive) {
            strategy = new DefaultConnectionReuseStrategy();
        } else {
            strategy = new NoConnectionReuseStrategy();
        }
        return strategy;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        // if (mLeakedException != null) {
        // Log.e(LOG_TAG, "Leak found", mLeakedException);
        // mLeakedException = null;
        // }
    }

    /**
     * Release resources associated with this client. You must call this, or
     * significant resources (sockets and memory) may be leaked.
     */
    public void close() {
        if (mLeakedException != null) {
            getConnectionManager().shutdown();
            mLeakedException = null;
        }
    }

    /* cURL logging support. */

    /**
     * Logging tag and level.
     */
    private static class LoggingConfiguration {

        private final String tag;
        private final int level;

        private LoggingConfiguration(String tag, int level) {
            this.tag = tag;
            this.level = level;
        }

        /**
         * Returns true if logging is turned on for this configuration.
         */
        private boolean isLoggable() {
            return Log.isLoggable(tag, level);
        }

        /**
         * Prints a message using this configuration.
         */
        private void println(String message) {
            Log.println(level, tag, message);
        }
    }

    /** cURL logging configuration. */
    private volatile LoggingConfiguration curlConfiguration;

    /**
     * Enables cURL request logging for this client.
     * 
     * @param name to log messages with
     * @param level at which to log messages (see {@link android.util.Log})
     */
    public void enableCurlLogging(String name, int level) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (level < Log.VERBOSE || level > Log.ASSERT) {
            throw new IllegalArgumentException("Level is out of range ["
                    + Log.VERBOSE + ".." + Log.ASSERT + "]");
        }

        curlConfiguration = new LoggingConfiguration(name, level);
    }

    /**
     * Disables cURL logging for this client.
     */
    public void disableCurlLogging() {
        curlConfiguration = null;
    }

    /**
     * Logs cURL commands equivalent to requests.
     */
    private class CurlLogger implements HttpRequestInterceptor {
        public void process(HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            LoggingConfiguration configuration = curlConfiguration;
            if (Constants.DEBUG && request instanceof HttpUriRequest) {
                Log.i("CurlLogger", toCurl((HttpUriRequest) request, false));
            }

            if (configuration != null && configuration.isLoggable()
                    && request instanceof HttpUriRequest) {
                // Never print auth token -- we used to check ro.secure=0 to
                // enable that, but can't do that in unbundled code.
                configuration.println(toCurl((HttpUriRequest) request, false));
            }
        }
    }

    private class TimeMarker implements HttpRequestInterceptor {
        @Override
        public void process(HttpRequest request, HttpContext context)
                throws HttpException, IOException {
            context.setAttribute(KSC_CONNECT_START,
                    SystemClock.elapsedRealtime());
            // int type = NetworkHelpers.getCurrentNetType();
            // context.setAttribute(KSS_CONNECT_TYPE, type);

            @SuppressWarnings("unchecked")
            List<HttpMessage> messages = (List<HttpMessage>) context
                    .getAttribute(KSC_MESSAGE_LIST);
            if (messages == null) {
                messages = new LinkedList<HttpMessage>();
                context.setAttribute(KSC_MESSAGE_LIST, messages);
            }
            messages.add(request);
        }
    }

    /**
     * Generates a cURL command equivalent to the given request.
     */
    private static String toCurl(HttpUriRequest request, boolean logAuthToken)
            throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append("curl ");

        for (Header header : request.getAllHeaders()) {
            if (!logAuthToken
                    && (header.getName().equals("Authorization") || header
                            .getName().equals("Cookie"))) {
                continue;
            }
            builder.append("--header \"");
            builder.append(header.toString().trim());
            builder.append("\" ");
        }

        URI uri = request.getURI();

        // If this is a wrapped request, use the URI from the original
        // request instead. getURI() on the wrapper seems to return a
        // relative URI. We want an absolute URI.
        if (request instanceof RequestWrapper) {
            HttpRequest original = ((RequestWrapper) request).getOriginal();
            if (original instanceof HttpUriRequest) {
                uri = ((HttpUriRequest) original).getURI();
            }
        }

        builder.append("\"");
        builder.append(uri);
        builder.append("\"");

        if (request instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
            HttpEntity entity = entityRequest.getEntity();
            if (entity != null && entity.isRepeatable()) {
                if (entity.getContentLength() < 1024) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    entity.writeTo(stream);
                    String entityString = stream.toString();

                    // TODO: Check the content type, too.
                    builder.append(" --data-ascii \"").append(entityString)
                            .append("\"");
                } else {
                    builder.append(" [TOO MUCH DATA TO INCLUDE]");
                }
            }
        }

        return builder.toString();
    }

}
