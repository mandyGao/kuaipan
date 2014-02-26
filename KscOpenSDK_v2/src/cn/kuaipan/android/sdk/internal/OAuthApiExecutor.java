
package cn.kuaipan.android.sdk.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;

import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import android.util.Log;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.http.KscHttpRequest;
import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.ErrorHelper;
import cn.kuaipan.android.sdk.exception.IKscError;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.model.IKscData;
import cn.kuaipan.android.sdk.oauth.AccessToken;
import cn.kuaipan.android.sdk.oauth.OAuthRedirector;
import cn.kuaipan.android.sdk.oauth.OAuthSession;
import cn.kuaipan.android.sdk.oauth.OAuthSession.SignType;
import cn.kuaipan.android.sdk.oauth.Session;
import cn.kuaipan.android.utils.IObtainable;

public final class OAuthApiExecutor extends Constants {
    private static final String LOG_TAG = "OAuthApiExecutor";

    private static final ResponseVerifier sDefaultVerifier = new DefaultOAuthVerifier();

    private final OAuthSession mSession;
    private final KscHttpTransmitter mTransmitter;

    // private final NetCacheManager mCache;

    public OAuthApiExecutor(KscHttpTransmitter transmitter, Session session) {
        if (session == null || transmitter == null) {
            throw new NullPointerException(
                    "Session and Transmitter can't be null");
        }
        mTransmitter = transmitter;
        // mCache = NetCacheManager.getInstance(transmitter.getContext(),
        // false);
        mSession = new OAuthSession(transmitter.getContext(), session);
        transmitter.setRedirector(new OAuthRedirector(mSession));
    }

    public OAuthSession getSession() {
        return mSession;
    }

    public String getRoot() {
        return String.valueOf(mSession.mRoot);
    }

    public AccessToken setAuthToken(String key, String secret) {
        return mSession.setAuthToken(key, secret);
    }

    public void setTempToken(String key, String secret) {
        mSession.setTempToken(key, secret);
    }

    public <T extends IKscData> T exec(ApiConfig config, String appendPath,
            Map<String, ? extends Object> params,
            IKscTransferListener listener, Class<T> resultClass)
            throws KscException, KscRuntimeException, InterruptedException {
        Throwable t = null;
        try {
            return _exec(config, appendPath, params, listener, resultClass);
        } catch (KscException e) {
            int errCode = e.getErrorCode();
            if (errCode == ErrorCode.MSG401_REQUEST_EXPIRED
                    || errCode == ErrorCode.MSG401_REUSED_NONCE) {
                try {
                    return _exec(config, appendPath, params, listener,
                            resultClass);
                } catch (KscException e1) {
                    t = e1;
                    throw e1;
                } catch (RuntimeException e1) {
                    t = e1;
                    throw e1;
                }
            }
            t = e;
            throw e;
        } catch (RuntimeException e) {
            t = e;
            throw e;
        } finally {
            if (t != null) {
                boolean needReport = true;
                if ((t instanceof IKscError)) {
                    needReport = canReport(config.apiName, (IKscError) t);
                }

                if (needReport) {
                    Log.w(LOG_TAG, "API exception:", t);
                }
            }
        }
    }

    private boolean canReport(String apiName, IKscError t) {
        int code = t.getErrorCode();
        if (code == ErrorCode.MSG404_FILE_NOT_EXIST
                && TextUtils.equals(apiName, "metadata")) {
            return false;
        }
        if (code == ErrorCode.MSG403_FILE_EXIST
                && TextUtils.equals(apiName, "move")) {
            return false;
        }
        return true;
    }

    public void exec(ApiConfig config, String appendPath,
            Map<String, Object> params, String savePath,
            IKscTransferListener listener, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException {
        Throwable t = null;

        try {
            _exec(config, appendPath, params, savePath, listener, appendMode);
        } catch (KscException e) {
            int errCode = e.getErrorCode();
            if (errCode == ErrorCode.MSG401_REQUEST_EXPIRED
                    || errCode == ErrorCode.MSG401_REUSED_NONCE) {
                try {
                    _exec(config, appendPath, params, savePath, listener,
                            appendMode);
                    return;
                } catch (KscException e1) {
                    t = e1;
                    throw e1;
                } catch (RuntimeException e1) {
                    t = e1;
                    throw e1;
                }
            }
            t = e;
            throw e;
        } catch (RuntimeException e) {
            t = e;
            throw e;
        } finally {
            if (t != null) {
                Log.w(LOG_TAG, "API exception:", t);
            }
        }
    }

    public Uri getSignedUri(ApiConfig config, String appendPath,
            Map<String, Object> params) {
        if (config == null) {
            throw new RuntimeException("API Config can not be null");
        }

        if (config.signType == SignType.USER) {
            mSession.assertAuth();
        }

        Uri uri = config.getUri();
        if (!TextUtils.isEmpty(appendPath)) {
            String[] segments = appendPath.split("/");
            Builder build = uri.buildUpon();
            for (String segment : segments) {
                if (!TextUtils.isEmpty(segment)) {
                    build.appendPath(segment);
                }
            }

            uri = build.build();
        }

        KscHttpRequest request = new KscHttpRequest(config.method, uri,
                (IKscTransferListener) null);

        request.appendQueryParameters(config.filterQuerys(params));
        List<NameValuePair> posts = config.filterPosts(params);
        request.appendPostParameters(posts);

        return mSession.sign(config.signType, config.method, request.getUri(),
                posts);
    }

    private KscHttpResponse execute(ApiConfig config, String appendPath,
            Map<String, ? extends Object> params, long range,
            IKscTransferListener listener) throws KscException,
            InterruptedException {
        if (config == null) {
            throw new RuntimeException("API Config can not be null");
        }

        if (config.signType == SignType.USER) {
            mSession.assertAuth();
        }

        Uri uri = config.getUri();
        if (!TextUtils.isEmpty(appendPath)) {
            String[] segments = appendPath.split("/");
            Builder build = uri.buildUpon();
            for (String segment : segments) {
                if (!TextUtils.isEmpty(segment)) {
                    build.appendPath(segment);
                }
            }

            uri = build.build();
        }

        KscHttpRequest request = new KscHttpRequest(config.method, uri,
                listener);

        request.appendQueryParameters(config.filterQuerys(params));
        List<NameValuePair> posts = config.filterPosts(params);
        request.appendPostParameters(posts);

        uri = mSession.sign(config.signType, config.method, request.getUri(),
                posts);
        request.setUri(uri);
        if (config.getGZip()) {
            request.setGZip(true);
        }

        if (range > 0) {
            request.getRequest().addHeader("Range", "bytes=" + range + "-");
        }

        return mTransmitter.execute(request, config.clientType);
    }

    public static void throwError(KscHttpResponse response)
            throws KscException, InterruptedException {
        Throwable t = response == null ? null : response.getError();
        if (t != null) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof InterruptedException) {
                throw (InterruptedException) t;
            } else {
                throw KscException.newException(t,
                        response == null ? "No response." : response.dump());
            }
        }
    }

    private <T extends IKscData> T _exec(ApiConfig config, String appendPath,
            Map<String, ? extends Object> params,
            IKscTransferListener listener, Class<T> resultClass)
            throws KscException, KscRuntimeException, InterruptedException {

        KscHttpResponse response = null;
        Map<String, Object> dataMap = null;
        try {
            response = execute(config, appendPath, params, -1, listener);
            throwError(response);

            ResponseVerifier verifier = config.getVerifier();
            verifier = verifier == null ? sDefaultVerifier : verifier;
            verifier.verify(response, false);

            dataMap = ApiDataHelper.contentToMap(response);
            return resultClass == null ? null : ApiDataHelper.parser(response,
                    dataMap, resultClass, config.getRequires());
        } finally {
            if (dataMap != null && dataMap instanceof IObtainable) {
                ((IObtainable) dataMap).recycle();
            }

            try {
                response.release();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private void _exec(ApiConfig config, String appendPath,
            Map<String, Object> params, String savePath,
            IKscTransferListener listener, boolean appendMode)
            throws KscException, KscRuntimeException, InterruptedException {
        if (TextUtils.isEmpty(savePath)) {
            throw new IllegalArgumentException("savePath can not be empty.");
        }

        KscHttpResponse response = null;
        try {
            long range = -1;
            if (appendMode) {
                File target = new File(savePath);
                if (target.exists() && target.isFile()) {
                    range = target.length();
                }
            }

            response = execute(config, appendPath, params, range, listener);
            throwError(response);

            ResponseVerifier verifier = config.getVerifier();
            verifier = verifier == null ? sDefaultVerifier : verifier;
            verifier.verify(response, appendMode);

            save(response, savePath, appendMode);
        } finally {
            try {
                response.release();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private void save(KscHttpResponse response, String savePath,
            boolean appendMode) throws KscException, InterruptedException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = response.getContent();
            if (in == null) {
                throw new KscException(ErrorCode.DATA_IS_EMPTY, response.dump());
            }

            File saveFile = new File(savePath);
            if (saveFile.exists() && !appendMode) {
                saveFile.delete();
            }
            out = new FileOutputStream(saveFile, appendMode);
            int len = 0;
            byte[] buf = new byte[8 * 1024];
            while ((len = in.read(buf)) >= 0) {
                if (Thread.interrupted()) {
                    throw new InterruptedIOException();
                }

                out.write(buf, 0, len);
            }
            out.close();
        } catch (ClosedByInterruptException e) {
            ErrorHelper.handleInterruptException(e);
        } catch (IOException e) {
            throw KscException.newInstance(e, response.dump());
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
            try {
                out.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public KscHttpTransmitter getTransmitter() {
        return mTransmitter;
    }
}
