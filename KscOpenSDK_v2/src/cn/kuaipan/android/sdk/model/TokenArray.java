
package cn.kuaipan.android.sdk.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class TokenArray extends AbsKscData {

    private static final String KEY_TOKENS = "tokens";
    private static final String KEY_OAUTH_TOKEN = "oauth_token";

    public final static Parser<TokenArray> PARSER = new Parser<TokenArray>() {
        @SuppressWarnings("unchecked")
        @Override
        public TokenArray parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                List<Map<String, Object>> array = (List<Map<String, Object>>) map
                        .get(KEY_TOKENS);

                return new TokenArray(array);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private final ArrayList<String> mList;

    public TokenArray(List<Map<String, Object>> array) {
        mList = new ArrayList<String>();
        for (Map<String, Object> entity : array) {
            mList.add(asString(entity, KEY_OAUTH_TOKEN));
        }

    }

    public ArrayList<String> getList() {
        return mList;
    }

}
