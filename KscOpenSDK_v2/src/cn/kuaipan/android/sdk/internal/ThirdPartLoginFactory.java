
package cn.kuaipan.android.sdk.internal;

import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.sdk.PublicApi;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.internal.OAuthApi.I3PartCallback;
import cn.kuaipan.android.sdk.model.CommonData;
import cn.kuaipan.android.sdk.oauth.OAuthSession.SignType;

import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

public final class ThirdPartLoginFactory {

    private static final String URI_LOGIN = "http://www.kuaipan.cn/api.php";
    private static final String PARAM_CLIENT_NAME = "cn";
    private static final String PARAM_CLIENT_VERSION = "cv";
    private static final String PARAM_CLIENT_ID = "cid";
    private static final String PARAM_OPRATION = "op";
    private static final String PARAM_ACCESSOR = "ac";
    private static final String PARAM_ACCESS_TOKEN = "at";
    private static final String PARAM_OPENID = "openid";
    private static final String PARAM_UID = "uid";
    private static final String PARAM_OAUTH_TOKEN = "ot";
    private static final String PARAM_OAUTH_TOKEN2 = "oauth_token";
    private static final String PARAM_TOKEN = "token";

    private static final String VALUE_OPRATION_LOGIN = "bind";
    private static final String VALUE_ACCESSOR_QQ = "qq";
    private static final String VALUE_ACCESSOR_WEIBO = "sina";
    private static final String VALUE_CLIENT_NAME = "KuaipanAndroid";
    private static final String VALUE_CLIENT_VERSION = "1";
    private static final String VALUE_CLIENT_ID = "lsa-kp";
    private static final String VALUE_SWAP_TOKEN = "swaptoken";
    private static final String VALUE_OPEN = "open";

    private static final int CLIENT_TYPE = KscHttpTransmitter.TYPE_UNKEEPALIVE;

    private static final int API_QQ_LOGIN = 1;
    private static final int API_WEIBO_LOGIN = 2;
    private static final int API_XIAOMI_LOGIN = 3;

    private static final SparseArray<ApiConfig> CONFIGS;

    public static final I3PartCallback QQ_CALLBACK = new I3PartCallback() {
        @Override
        public void doCallback(PublicApi api, String accessToken, String id,
                String kscUserToken, String requestResult) throws KscException,
                KscRuntimeException, InterruptedException {
            Map<String, String> params = new HashMap<String, String>();
            params.put(PARAM_OPRATION, VALUE_OPRATION_LOGIN);
            params.put(PARAM_ACCESSOR, VALUE_ACCESSOR_QQ);

            params.put(PARAM_ACCESS_TOKEN, accessToken);
            params.put(PARAM_OPENID, id);
            params.put(PARAM_OAUTH_TOKEN, kscUserToken);

            params.put(PARAM_CLIENT_NAME, VALUE_CLIENT_NAME);
            params.put(PARAM_CLIENT_VERSION, VALUE_CLIENT_VERSION);
            params.put(PARAM_CLIENT_ID, VALUE_CLIENT_ID);

            ApiConfig config = CONFIGS.get(API_QQ_LOGIN);
            CommonData data = api.execute(config, null, params, null,
                    CommonData.class);

            String msg = data.getString(CommonData.R);
            PublicApi.verifyMsg(config.apiName, msg, false);
        }
    };

    public static final I3PartCallback WEIBO_CALLBACK = new I3PartCallback() {
        @Override
        public void doCallback(PublicApi api, String accessToken, String id,
                String kscUserToken, String requestResult) throws KscException,
                KscRuntimeException, InterruptedException {
            Map<String, String> params = new HashMap<String, String>();
            params.put(PARAM_OPRATION, VALUE_OPRATION_LOGIN);
            params.put(PARAM_ACCESSOR, VALUE_ACCESSOR_WEIBO);

            params.put(PARAM_ACCESS_TOKEN, accessToken);
            params.put(PARAM_UID, id);
            params.put(PARAM_OAUTH_TOKEN, kscUserToken);

            params.put(PARAM_CLIENT_NAME, VALUE_CLIENT_NAME);
            params.put(PARAM_CLIENT_VERSION, VALUE_CLIENT_VERSION);
            params.put(PARAM_CLIENT_ID, VALUE_CLIENT_ID);

            ApiConfig config = CONFIGS.get(API_WEIBO_LOGIN);
            CommonData data = api.execute(config, null, params, null,
                    CommonData.class);

            String msg = data.getString(CommonData.R);
            PublicApi.verifyMsg(config.apiName, msg, false);
        }
    };

    public static final I3PartCallback XIAOMI_CALLBACK = new I3PartCallback() {
        @Override
        public void doCallback(PublicApi api, String accessToken, String id,
                String kscUserToken, String requestResult) throws KscException,
                KscRuntimeException, InterruptedException {
            Map<String, String> params = new HashMap<String, String>();
            params.put(PARAM_OAUTH_TOKEN2,
                    requestResult.substring(Constants.AUTH_URL.length()));
            params.put(PARAM_TOKEN, accessToken);
            params.put(PARAM_OPRATION, VALUE_SWAP_TOKEN);
            params.put(PARAM_ACCESSOR, VALUE_OPEN);
            ApiConfig config = CONFIGS.get(API_XIAOMI_LOGIN);
            CommonData data = api.execute(config, null, params, null,
                    CommonData.class);

            String msg = data.getString(CommonData.MSG);
            PublicApi.verifyMsg(config.apiName, msg, false);
        }
    };

    static {
        CONFIGS = new SparseArray<ApiConfig>();
        CONFIGS.append(
                API_QQ_LOGIN,
                new ApiConfig("qqLogin", HttpMethod.POST, URI_LOGIN,
                        SignType.NONE, CLIENT_TYPE)
                        .setQuerys(true, PARAM_OPRATION, PARAM_ACCESSOR)
                        .setPosts(true, PARAM_CLIENT_NAME,
                                PARAM_CLIENT_VERSION, PARAM_CLIENT_ID,
                                PARAM_ACCESS_TOKEN, PARAM_OPENID,
                                PARAM_OAUTH_TOKEN).setRequires(CommonData.R));
        CONFIGS.append(
                API_WEIBO_LOGIN,
                new ApiConfig("weiboLogin", HttpMethod.POST, URI_LOGIN,
                        SignType.NONE, CLIENT_TYPE)
                        .setQuerys(true, PARAM_OPRATION, PARAM_ACCESSOR)
                        .setPosts(true, PARAM_CLIENT_NAME,
                                PARAM_CLIENT_VERSION, PARAM_CLIENT_ID,
                                PARAM_ACCESS_TOKEN, PARAM_UID,
                                PARAM_OAUTH_TOKEN).setRequires(CommonData.R));
        CONFIGS.append(
                API_XIAOMI_LOGIN,
                new ApiConfig("xiaomiLogin", HttpMethod.POST, URI_LOGIN,
                        SignType.NONE, CLIENT_TYPE)
                        .setQuerys(true, PARAM_OPRATION, PARAM_ACCESSOR)
                        .setPosts(true, PARAM_OAUTH_TOKEN2, PARAM_TOKEN)
                        .setRequires(CommonData.MSG));
    }

}
