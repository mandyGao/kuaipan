
package cn.kuaipan.android.sdk.oauth;

import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.IllegalParamsException;

public class Session {
    public static enum Root {
        KUAIPAN, APP_FOLDER;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public static Root parser(String root) {
            return root == null ? null : Root.valueOf(root.toUpperCase());
        }
    }

    public final Root mRoot;
    public final Consumer mConsumer;
    protected Token mUser;

    public Session(Session session) {
        this(session.mConsumer, session.mUser, session.mRoot);
    }

    public Session(String consumerKey, String consumerSecret) {
        this(new Consumer(consumerKey, consumerSecret), null, null);
    }

    public Session(String consumerKey, String consumerSecret, Root root) {
        this(new Consumer(consumerKey, consumerSecret), null, root);
    }

    public Session(Consumer consumerToken) {
        this(consumerToken, null, null);
    }

    public Session(Consumer consumerToken, Root root) {
        this(consumerToken, null, root);
    }

    public Session(Consumer consumerToken, Token userToken, Root root) {
        if (consumerToken == null) {
            throw new NullPointerException("consumerToken can't be null.");
        }

        mConsumer = consumerToken;
        mUser = userToken;
        mRoot = root == null ? Root.APP_FOLDER : root;
    }

    public AccessToken setAuthToken(String key, String secret) {
        AccessToken token = new AccessToken(key, secret);
        mUser = token;
        return token;
    }

    public void setTempToken(String key, String secret) {
        mUser = new RequestToken(key, secret);
    }

    public Token getUserToken() {
        return mUser;
    }

    public boolean isAuth() {
        return (mUser != null) && (mUser instanceof AccessToken);
    }

    public void unAuth() {
        mUser = null;
    }

    public void assertAuth() {
        if (!isAuth()) {
            throw new IllegalParamsException(ErrorCode.MISS_USER_TOKEN);
        }
    }
}
