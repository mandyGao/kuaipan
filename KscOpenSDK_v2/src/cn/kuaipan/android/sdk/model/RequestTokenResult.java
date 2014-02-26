
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.sdk.exception.KscException;

import java.util.Map;

public class RequestTokenResult extends AbsKscData {

    private static final String OAUTH_TOKEN_KEY = CommonData.OAUTH_TOKEN_KEY;
    private static final String OAUTH_TOKEN_SECRET = CommonData.OAUTH_TOKEN_SECRET;
    private static final String OAUTH_CALLBACK_CONFIRMED = "oauth_callback_confirmed";
    private static final String EXPIRES_IN = "expires_in";
    private static final String NOTIFY_URL = "notify_url";

    public final String key;
    public final String secret;
    private boolean callbackConfirmed = false;
    private int expiresTime;
    private String notifyUrl;

    private RequestTokenResult(String key, String secret) {
        super();
        this.key = key;
        this.secret = secret;
    }

    public boolean isCallbackConfirmed() {
        return callbackConfirmed;
    }

    public int getExpiresTime() {
        return expiresTime;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public static final Parser<RequestTokenResult> PARSER = new Parser<RequestTokenResult>() {

        @Override
        public RequestTokenResult parserMap(Map<String, Object> map,
                String... requireds) throws KscException {
            if (map == null) {
                return null;
            }

            String key = asStringOrThrow(map, OAUTH_TOKEN_KEY);
            String secret = asStringOrThrow(map, OAUTH_TOKEN_SECRET);
            RequestTokenResult result = new RequestTokenResult(key, secret);

            result.callbackConfirmed = asBoolean(
                    map.get(OAUTH_CALLBACK_CONFIRMED), false);
            result.expiresTime = asNumber(map.get(EXPIRES_IN), -1).intValue();
            result.notifyUrl = asString(map, NOTIFY_URL);

            return result;
        }
    };
}
