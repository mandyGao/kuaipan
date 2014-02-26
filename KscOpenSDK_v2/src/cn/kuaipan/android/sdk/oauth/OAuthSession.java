
package cn.kuaipan.android.sdk.oauth;

import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.NameValuePair;
import org.sky.base.utils.Base64;
import org.sky.base.utils.ConstInfo;
import org.sky.base.utils.ConstInfo.ConstKey;

import android.content.Context;
import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.IllegalParamsException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.utils.Encode;
import cn.kuaipan.android.utils.OAuthTimeUtils;
import cn.kuaipan.android.utils.RandomUtils;
import cn.kuaipan.android.utils.UriUtils;

public class OAuthSession extends Session {

    private static final String PARAM_CONSUMER_KEY = "oauth_consumer_key";
    private static final String PARAM_USER_TOKEN = "oauth_token";
    private static final String PARAM_SIGN_METHOD = "oauth_signature_method";
    private static final String PARAM_SIGN = "oauth_signature";
    private static final String PARAM_TIMESTAMP = "oauth_timestamp";
    private static final String PARAM_NONCE = "oauth_nonce";
    private static final String PARAM_VERSION = "oauth_version";

    private static final String VALUE_SIGN_METHOD = "HMAC-SHA1";
    private static final String VALUE_VERSION = "1.0";

    private final static String SIGN_METHOD = "HmacSHA1";
    private final static char JOIN_AND = '&';

    private static final HashSet<String> OAUTH_PARAMS;
    static {
        OAUTH_PARAMS = new HashSet<String>();
        OAUTH_PARAMS.add(PARAM_CONSUMER_KEY);
        OAUTH_PARAMS.add(PARAM_USER_TOKEN);
        OAUTH_PARAMS.add(PARAM_SIGN_METHOD);
        OAUTH_PARAMS.add(PARAM_SIGN);
        OAUTH_PARAMS.add(PARAM_TIMESTAMP);
        OAUTH_PARAMS.add(PARAM_NONCE);
        OAUTH_PARAMS.add(PARAM_VERSION);
    }

    private final Context mContext;

    public static enum SignType {
        NONE, CONSUMER, USER, AUTO
    }

    public OAuthSession(Context context, Session session) {
        super(session);
        mContext = context;
    }

    public OAuthSession(Context context, Consumer consumerToken, Root root) {
        super(consumerToken, root);
        mContext = context;
    }

    public OAuthSession(Context context, Consumer consumerToken,
            Token userToken, Root root) {
        super(consumerToken, userToken, root);
        mContext = context;
    }

    public OAuthSession(Context context, Consumer consumerToken) {
        super(consumerToken);
        mContext = context;
    }

    public OAuthSession(Context context, String consumerKey,
            String consumerSecret, Root root) {
        super(consumerKey, consumerSecret, root);
        mContext = context;
    }

    public OAuthSession(Context context, String consumerKey,
            String consumerSecret) {
        super(consumerKey, consumerSecret);
        mContext = context;
    }

    public Uri sign(SignType type, HttpMethod method, final Uri origUri,
            List<NameValuePair> postParams) {
        if (origUri == null || method == null) {
            throw new RuntimeException("uriStr and method can not be null.");
        }
        String scheme = origUri.getScheme();
        if (!"http".equalsIgnoreCase(scheme)
                && !"https".equalsIgnoreCase(scheme)) {
            if (type == SignType.CONSUMER || type == SignType.USER) {
                throw new RuntimeException(
                        "Only support sign http & https uri. uri=" + origUri);
            } else {
                return origUri;
            }
        }

        Uri uri = UriUtils.appendQuerys(origUri, postParams);

        if (type != SignType.CONSUMER && type != SignType.USER) {
            type = testSignType(uri, type);
        }

        if (type == SignType.NONE) {
            return origUri;
        }

        List<NameValuePair> querys = UriUtils.getQuerys(uri);
        removeSignParams(querys);

        return getSignedUri(type, method, origUri, querys);
    }

    public SignType testSignType(Uri uri, SignType type) {
        if (uri == null) {
            return SignType.CONSUMER;
        }

        String oldSign = uri.getQueryParameter(PARAM_SIGN);
        String oldConsumerToken = uri.getQueryParameter(PARAM_CONSUMER_KEY);
        String oldUserToken = uri.getQueryParameter(PARAM_USER_TOKEN);

        if (!TextUtils.isEmpty(oldSign)) {
            return TextUtils.isEmpty(oldUserToken) ? SignType.CONSUMER
                    : SignType.USER;
        } else if (type == null && TextUtils.isEmpty(oldConsumerToken)
                && TextUtils.isEmpty(oldUserToken)) {
            return SignType.NONE;
        }

        return mUser == null ? SignType.CONSUMER : SignType.USER;
    }

    private String genNonce() {
        String random = RandomUtils.getString(16, RandomUtils.NORMAL_CHARS);
        String src = ConstInfo.getValue(mContext, ConstKey.DEVICE_ID)
                + System.currentTimeMillis() + random;

        String result = Encode.MD5Encode(src.getBytes());
        return TextUtils.isEmpty(result) ? random : result;
    }

    private Uri getSignedUri(SignType type, HttpMethod method, Uri origUri,
            List<NameValuePair> querys) {
        final long timeStamp = OAuthTimeUtils.currentTime() / 1000;
        final String nonce = genNonce();

        TreeMap<String, String> params = new TreeMap<String, String>();
        if (querys != null) {
            for (NameValuePair query : querys) {
                params.put(query.getName(), query.getValue());
            }
        }
        params.put(PARAM_CONSUMER_KEY, mConsumer.getKey());
        params.put(PARAM_SIGN_METHOD, VALUE_SIGN_METHOD);
        params.put(PARAM_TIMESTAMP, String.valueOf(timeStamp));
        params.put(PARAM_NONCE, nonce);
        params.put(PARAM_VERSION, VALUE_VERSION);

        String signKey = mConsumer.getSecret() + "&";

        if (type == SignType.USER) {
            if (mUser == null) {
                throw new IllegalParamsException(ErrorCode.MISS_USER_TOKEN);
            }
            params.put(PARAM_USER_TOKEN, mUser.getKey());
            signKey += mUser.getSecret();
        }

        Iterator<String> iter = params.keySet().iterator();
        Builder builder = origUri.buildUpon().query(null).fragment(null);
        String basicUri = oauthEncode(builder.toString());

        StringBuilder queryBuilder = new StringBuilder();
        while (iter.hasNext()) {
            String key = iter.next();
            String value = params.get(key);
            if (value == null) {
                value = "";
            }

            if (queryBuilder.length() > 0) {
                queryBuilder.append("&");
            }
            queryBuilder.append(oauthEncode(key));
            queryBuilder.append("=");
            queryBuilder.append(oauthEncode(value));
            // builder.appendQueryParameter(key, value);
        }
        // String queryString = builder.build().getEncodedQuery();
        String queryString = queryBuilder.toString();
        final String sign;
        try {
            sign = computeSign(signKey, method, basicUri, queryString);
        } catch (Exception e) {
            throw new KscRuntimeException(ErrorCode.FRAMEWORK_UNSUPPORT, e);
        }

        builder = origUri.buildUpon();
        builder.query(null);
        querys = UriUtils.getQuerys(origUri);
        removeSignParams(querys);
        if (querys != null) {
            for (NameValuePair pair : querys) {
                builder.appendQueryParameter(pair.getName(), pair.getValue());
            }
        }
        builder.appendQueryParameter(PARAM_CONSUMER_KEY, mConsumer.getKey());
        builder.appendQueryParameter(PARAM_SIGN_METHOD, VALUE_SIGN_METHOD);
        builder.appendQueryParameter(PARAM_TIMESTAMP, String.valueOf(timeStamp));
        builder.appendQueryParameter(PARAM_NONCE, nonce);
        builder.appendQueryParameter(PARAM_VERSION, VALUE_VERSION);
        if (type == SignType.USER) {
            builder.appendQueryParameter(PARAM_USER_TOKEN, mUser.getKey());
        }
        builder.appendQueryParameter(PARAM_SIGN, sign);

        return builder.build();
    }

    private String computeSign(String signKey, HttpMethod method,
            String basicUri, String queryString)
            throws NoSuchAlgorithmException, InvalidKeyException {
        StringBuilder builder = new StringBuilder();
        builder.append(method.toString().toUpperCase());
        builder.append(JOIN_AND);
        builder.append(basicUri);
        builder.append(JOIN_AND);
        if (queryString != null) {
            builder.append(oauthEncode(queryString));
        }
        String baseString = builder.toString();

        Mac mac = Mac.getInstance(SIGN_METHOD);
        SecretKeySpec skey = new SecretKeySpec(signKey.getBytes(), SIGN_METHOD);

        mac.init(skey);
        byte[] data = mac.doFinal(baseString.getBytes());
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    private void removeSignParams(List<NameValuePair> querys) {
        if (querys == null) {
            return;
        }
        for (int i = 0; i < querys.size(); i++) {
            NameValuePair pair = querys.get(i);
            if (OAUTH_PARAMS.contains(pair.getName().toLowerCase())) {
                querys.remove(i--);
            }
        }
    }

    private static String oauthEncode(String str) {
        return URLEncoder.encode(str).replaceAll("\\*", "%2A")
                .replaceAll("%7E", "~").replaceAll("\\+", "%20");
    }

}
