
package cn.kuaipan.android.sdk.oauth;

import android.util.Pair;

public abstract class Token extends Pair<String, String> {

    public Token(String key, String secret) {
        super(key, secret);
    }

    public final String getKey() {
        return first;
    }

    public final String getSecret() {
        return second;
    }

    public String toString() {
        int length = 20 + (first == null ? 0 : first.length())
                + (second == null ? 0 : second.length());

        StringBuffer buf = new StringBuffer(length);
        buf.append("Token{key=");
        if (first != null) {
            buf.append(first);
        }
        buf.append(", secret=");
        if (second != null) {
            buf.append(second);
        }
        buf.append("}");
        return buf.toString();
    }
}
