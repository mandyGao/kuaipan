
package cn.kuaipan.android.sdk.model;

import org.apache.http.util.LangUtils;

import android.text.TextUtils;

import java.util.Map;
import java.util.zip.DataFormatException;

public class KuaipanUser extends AbsKscData {
    private final static String KEY_USER_ID = "user_id";
    private final static String KEY_USER_NAME = "user_name";
    private final static String KEY_MAX_FILE = "max_file_size";
    private final static String KEY_QUOTA_TOTAL = "quota_total";
    private final static String KEY_QUOTA_USED = "quota_used";
    private final static String KEY_QUOTA_RECYCLED = "quota_recycled";
    private final static String KEY_MOBILE = "mobile";
    private final static String KEY_EMAIL = "e_mail";

    public final static Parser<KuaipanUser> PARSER = new Parser<KuaipanUser>() {
        @Override
        public KuaipanUser parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                return new KuaipanUser(map, null, null, null);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    public final int user_id;
    public final String user_name;
    public final long max_file_size;
    public final long quota_total;
    public final long quota_used;
    public final long quota_recycled;
    public final String mobile;
    public final String e_mail;

    KuaipanUser(Map<String, Object> data, Long defMaxFile, Long defTotal,
            Long defUsed) {
        user_id = asNumber(data.get(KEY_USER_ID), null).intValue();
        user_name = (String) data.get(KEY_USER_NAME);
        max_file_size = asNumber(data.get(KEY_MAX_FILE), defMaxFile)
                .longValue();
        quota_total = asNumber(data.get(KEY_QUOTA_TOTAL), defTotal).longValue();
        quota_used = asNumber(data.get(KEY_QUOTA_USED), defUsed).longValue();
        quota_recycled = asNumber(data.get(KEY_QUOTA_RECYCLED), 0).longValue();
        mobile = asString(data, KEY_MOBILE);
        e_mail = asString(data, KEY_EMAIL);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{user_id:");
        buf.append(user_id);
        buf.append(", user_name:\"");
        buf.append(user_name);
        buf.append("\", max_file_size:");
        buf.append(max_file_size);
        buf.append(", quota_total:");
        buf.append(quota_total);
        buf.append(", quota_used:");
        buf.append(quota_used);
        buf.append(", quota_recycled:");
        buf.append(quota_recycled);
        buf.append(", e_mail:");
        buf.append(e_mail);
        buf.append(", mobile:");
        buf.append(mobile);
        buf.append("}");
        return buf.toString();
    }

    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, user_id);
        hash = LangUtils.hashCode(hash, user_name);
        hash = LangUtils.hashCode(hash, max_file_size);
        hash = LangUtils.hashCode(hash, quota_total);
        hash = LangUtils.hashCode(hash, quota_used);
        hash = LangUtils.hashCode(hash, quota_recycled);
        hash = LangUtils.hashCode(hash, mobile);
        hash = LangUtils.hashCode(hash, e_mail);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (o instanceof KuaipanUser) {
            KuaipanUser that = (KuaipanUser) o;
            return user_id == that.user_id
                    && TextUtils.equals(user_name, that.user_name)
                    && max_file_size == that.max_file_size
                    && quota_total == that.quota_total
                    && quota_used == that.quota_used
                    && quota_recycled == that.quota_recycled;
        } else {
            return false;
        }
    }
}
