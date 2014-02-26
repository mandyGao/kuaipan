
package cn.kuaipan.android.sdk.model;

import android.content.ContentValues;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.zip.DataFormatException;

public class CommonData implements IKscData {
    public static final String MSG = "msg";
    public static final String TOKEN = "token";
    public static final String EXPIRES = "expires";
    public static final String OAUTH_TOKEN_KEY = "oauth_token";
    public static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String USER_ID = "user_id";
    public static final String FILE_ID = "file_id";
    public static final String URL = "url";
    public static final String DEVICE = "device";
    public static final String COPY_REF = "copy_ref";
    public static final String SHARE_REF = "share_ref";
    public static final String ACCESS_CODE = "access_code";
    public static final String R = "_r_";
    public static final String COUNT = "count";

    private final ContentValues mDatas;

    public CommonData() {
        mDatas = new ContentValues();
    }

    public void put(String key, String value) {
        mDatas.put(key, value);
    }

    private void put(String key, Integer value) {
        mDatas.put(key, value);
    }

    private void put(String key, Long value) {
        mDatas.put(key, value);
    }

    public String getString(String key) {
        return mDatas.getAsString(key);
    }

    public int getInt(String key) {
        Integer result = mDatas.getAsInteger(key);
        return result == null ? 0 : result.intValue();
    }

    public long getLong(String key) {
        Long result = mDatas.getAsLong(key);
        return result == null ? 0 : result.longValue();
    }

    public static final Parser<CommonData> PARSER = new Parser<CommonData>() {

        @Override
        public CommonData parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            if (map == null && (requireds == null || requireds.length <= 0)) {
                return null;
            }
            if (map == null) {
                throw new DataFormatException("Miss required params: "
                        + Arrays.toString(requireds));
            }

            CommonData result = new CommonData();
            for (String key : requireds) {
                Object obj = map.get(key);

                // TODO: need remove USER_ID if server updated.
                if (obj == null && !TextUtils.equals(USER_ID, key)) {
                    throw new DataFormatException("Miss required data: " + key);
                }

                if (obj instanceof String) {
                    result.put(key, (String) obj);
                } else if (obj instanceof Integer) {
                    result.put(key, ((Number) obj).intValue());
                } else if (obj instanceof Long) {
                    result.put(key, ((Number) obj).longValue());
                }
            }

            return result;
        }
    };
}
