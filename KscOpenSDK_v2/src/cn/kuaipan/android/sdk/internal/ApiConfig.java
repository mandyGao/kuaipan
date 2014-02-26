
package cn.kuaipan.android.sdk.internal;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.sky.base.http.multipart.FileValuePair;

import android.net.Uri;
import android.text.TextUtils;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.IllegalParamsException;
import cn.kuaipan.android.sdk.oauth.OAuthSession.SignType;

public class ApiConfig {
    public final String apiName;

    public final SignType signType;
    public final HttpMethod method;
    private final Uri uri;
    private Uri customUri;
    private boolean gzip;
    public final int clientType;

    public boolean fullMatchQuery;
    public boolean fullMatchPost;
    private final HashSet<String> queryParams;
    private final HashSet<String> postParams;

    private String[] requires;
    private ResponseVerifier mVerifier;

    public ApiConfig(String name, HttpMethod method, String uri,
            SignType signType, int clientType) {
        super();
        this.apiName = name;
        this.method = method;
        this.uri = Uri.parse(uri);
        this.signType = signType;
        this.clientType = clientType;
        queryParams = new HashSet<String>();
        postParams = new HashSet<String>();
    }

    public ApiConfig setGZip(boolean gzip) {
        this.gzip = gzip;
        return this;
    }

    public boolean getGZip() {
        return gzip;
    }

    public ApiConfig setQuerys(boolean fullMatch, String... querys) {
        fullMatchQuery = fullMatch;
        queryParams.clear();
        queryParams.addAll(Arrays.asList(querys));
        return this;
    }

    public ApiConfig setPosts(boolean fullMatch, String... posts) {
        fullMatchPost = fullMatch;
        postParams.clear();
        postParams.addAll(Arrays.asList(posts));
        return this;
    }

    public ApiConfig setRequires(String... params) {
        requires = params;
        return this;
    }

    String[] getRequires() {
        return requires;
    }

    Uri getUri() {
        if (customUri != null) {
            return customUri;
        }
        return uri;
    }

    public void setCustomUri(String uri) {
        if (TextUtils.isEmpty(uri)) {
            customUri = null;
        } else {
            customUri = Uri.parse(uri);
        }
    }

    ResponseVerifier getVerifier() {
        return mVerifier;
    }

    public ApiConfig setVerifyer(ResponseVerifier verifyer) {
        mVerifier = verifyer;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("Config(");
        builder.append(String.format("%4s, ", method));
        builder.append("Sign:");
        builder.append(String.format("%7s, ", signType));
        builder.append(uri);
        if (queryParams != null) {
            builder.append(", query[");
            for (String query : queryParams) {
                builder.append(query);
                builder.append(", ");
            }
            builder.append("]");
            builder.append(fullMatchQuery);
        }
        if (postParams != null) {
            builder.append(", post[");
            for (String post : postParams) {
                builder.append(post);
                builder.append(", ");
            }
            builder.append("]");
            builder.append(fullMatchPost);
        }

        builder.append(")");
        return builder.toString();
    }

    public List<NameValuePair> filterQuerys(Map<String, ? extends Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<NameValuePair> result = new LinkedList<NameValuePair>();
        if (fullMatchQuery) {
            for (String key : queryParams) {
                Object value = params.get(key);
                if (value == null) {
                    throw new IllegalParamsException(ErrorCode.NULL_PARAM, key
                            + " can't be null.");
                }
                result.add(new BasicNameValuePair(key, String.valueOf(value)));
            }
        } else {
            for (String key : params.keySet()) {
                if (queryParams.contains(key)) {
                    Object value = params.get(key);
                    result.add(new BasicNameValuePair(key, String
                            .valueOf(value)));
                }
            }
        }
        return result;
    }

    public List<NameValuePair> filterPosts(Map<String, ? extends Object> params) {
        if (params == null || params.isEmpty()) {
            return null;
        }

        List<NameValuePair> result = new LinkedList<NameValuePair>();
        if (fullMatchPost) {
            for (String key : postParams) {
                Object value = params.get(key);
                NameValuePair pair = getNameValuePair(key, value);
                if (pair == null) {
                    throw new IllegalParamsException(ErrorCode.NULL_PARAM, key
                            + " can't be null.");
                } else {
                    result.add(pair);
                }
            }
        } else {
            for (String key : params.keySet()) {
                if (postParams.contains(key)) {
                    Object value = params.get(key);
                    NameValuePair pair = getNameValuePair(key, value);
                    if (pair != null) {
                        result.add(pair);
                    }
                }
            }
        }
        return result;
    }

    private static NameValuePair getNameValuePair(String key, Object value) {
        if (value == null || TextUtils.isEmpty(key)) {
            return null;
        }

        if (value instanceof String) {
            return new BasicNameValuePair(key, (String) value);
        } else if (value instanceof File) {
            return new FileValuePair(key, (File) value);
        } else {
            return null;
        }
    }

}
