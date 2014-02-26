
package cn.kuaipan.android.sdk.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.sky.base.utils.XmlUtils;
import org.sky.base.utils.XmlUtils.XLiveXMLObject;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import cn.kuaipan.android.http.KscHttpRequest;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerException;
import cn.kuaipan.android.sdk.model.PhoneInfo;
import cn.kuaipan.android.sdk.model.SyncInfo;
import cn.kuaipan.android.sdk.model.UserInfo;
import cn.kuaipan.android.sdk.model.VersionInfo;
import cn.kuaipan.android.utils.IObtainable;

public class HttpApi {
    private static volatile HttpApi sInstance;

    public static HttpApi getInstance(Context context) {
        HttpApi instance = sInstance;
        if (instance == null) {
            synchronized (HttpApi.class) {
                instance = sInstance;
                if (instance == null && context != null) {
                    instance = new HttpApi(new KscHttpTransmitter(context));
                    sInstance = instance;
                }
            }
        }
        return instance;
    }

    private final KscHttpTransmitter mTransmitter;
    private final ResponseVerifier mDefaultVerifier = new DefaultOAuthVerifier();

    private HttpApi(KscHttpTransmitter transmitter) {
        if (transmitter == null) {
            throw new NullPointerException("Transmitter can't be null");
        }
        mTransmitter = transmitter;
    }

    public UserInfo relogin(String token, OAuthApi api) throws KscException,
            KscRuntimeException, InterruptedException {
        if (TextUtils.isEmpty(token) || api == null) {
            throw new KscRuntimeException(ErrorCode.INVALID_PARAM);
        }

        String deviceId = "android:oauth:" + api.getUserToken();

        Uri uri = Uri.parse("https://userapi.kuaipan.cn/xsvr/reLogin");

        XLiveXMLObject root = new XLiveXMLObject();
        root.addParam("token", token);
        root.addParam("deviceId", deviceId);
        String param = XmlUtils.buildXml(root);
        if (TextUtils.isEmpty(param)) {
            throw new KscRuntimeException(ErrorCode.UNKNOW_ERR_RUNTIME);
        }

        KscHttpRequest request = new KscHttpRequest(HttpMethod.POST, uri);
        request.setPostData(param.getBytes());

        KscHttpResponse response = null;
        InputStream in = null;
        boolean interrupted = false;
        try {
            HttpRequest req = request.getRequest();
            req.addHeader("v", "2");

            response = mTransmitter.execute(request,
                    KscHttpTransmitter.TYPE_KEEPALIVE);
            Throwable t = response.getError();
            if (t != null) {
                throw t;
            }

            int statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new ServerException(statusCode, response.dump());
            }

            in = response.getContent();

            return UserInfo.parser(in);
        } catch (KscException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            try {
                throw KscException.newException(t,
                        response == null ? "No Response" : response.dump());
            } catch (InterruptedException e) {
                interrupted = true;
                throw e;
            }
        } finally {
            if (interrupted && request != null) {
                request.getRequest().abort();
            } else {
                try {
                    response.release();
                } catch (Throwable t) {
                    // ignore
                }
            }
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public SyncInfo sendPushRequest(String urlStr, int timeOut)
            throws KscException, KscRuntimeException, InterruptedException {

        KscHttpRequest request = new KscHttpRequest(HttpMethod.GET, urlStr);

        KscHttpResponse response = null;
        InputStream in = null;
        boolean interrupted = false;
        try {
            HttpRequest req = request.getRequest();
            HttpConnectionParams.setSoTimeout(req.getParams(), timeOut);

            response = mTransmitter.execute(request,
                    KscHttpTransmitter.TYPE_KEEPALIVE);
            Throwable t = response.getError();
            if (t != null) {
                throw t;
            }

            int statusCode = response.getStatusCode();
            if (statusCode == HttpStatus.SC_GATEWAY_TIMEOUT) {
                return null;
            }

            if (statusCode != HttpStatus.SC_OK) {
                throw new ServerException(statusCode, response.dump());
            }

            in = response.getContent();
            return SyncInfo.parser(in);
            // } catch (SocketTimeoutException e) {
            // // ignore the error
            // return null;
        } catch (KscException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            try {
                throw KscException.newException(t,
                        response == null ? "No Response" : response.dump());
            } catch (InterruptedException e) {
                interrupted = true;
                throw e;
            }
        } finally {
            if (interrupted && request != null) {
                request.getRequest().abort();
            } else {
                try {
                    response.release();
                } catch (Throwable t) {
                    // ignore
                }
            }
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    public VersionInfo getUpgradeInfo(PhoneInfo phoneInfo) throws KscException,
            KscRuntimeException, InterruptedException {
        String urlStr = "http://www.kuaipan.cn/kpkmg/androidupdate";
        KscHttpRequest request = new KscHttpRequest(HttpMethod.POST, urlStr);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

        if (phoneInfo.mChannel != null) {
            params.add(new BasicNameValuePair(PhoneInfo.CHANNEL,
                    phoneInfo.mChannel));
        }
        if (phoneInfo.mImei != null) {
            params.add(new BasicNameValuePair(PhoneInfo.IMEI, phoneInfo.mImei));
        }
        if (phoneInfo.mResolution != null) {
            params.add(new BasicNameValuePair(PhoneInfo.RESOLUTION,
                    phoneInfo.mResolution));
        }
        if (phoneInfo.mKPver != null) {
            params.add(new BasicNameValuePair(PhoneInfo.KPVER, phoneInfo.mKPver));
        }
        if (phoneInfo.mOSver != null) {
            params.add(new BasicNameValuePair(PhoneInfo.OSVER, phoneInfo.mOSver));
        }
        if (phoneInfo.mUserID != null) {
            params.add(new BasicNameValuePair(PhoneInfo.USERID,
                    phoneInfo.mUserID));
        }
        if (phoneInfo.mPackage != null) {
            params.add(new BasicNameValuePair(PhoneInfo.PACKAGE,
                    phoneInfo.mPackage));
        }

        request.appendPostParameters(params);

        KscHttpResponse response = null;
        Map<String, Object> dataMap = null;
        try {
            response = mTransmitter.execute(request,
                    KscHttpTransmitter.TYPE_UNKEEPALIVE_TRYRESEND);
            OAuthApiExecutor.throwError(response);

            mDefaultVerifier.verify(response, false);

            dataMap = ApiDataHelper.contentToMap(response);
            return ApiDataHelper.parser(response, dataMap, VersionInfo.class);
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

    public KscHttpResponse reuqest(KscHttpRequest request)
            throws InterruptedException {
        KscHttpResponse response = mTransmitter.execute(request,
                KscHttpTransmitter.TYPE_UNKEEPALIVE_TRYRESEND);
        return response;
    }
    
}
