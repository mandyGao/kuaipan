
package cn.kuaipan.android.sdk.model;

import java.util.Map;

public class ResultMsg implements IKscData {
    public static final String MSG_OK = "ok";
    public static final String MSG_IGNORE = "ignore";
    public static final String MSG_SHARED = "shared";

    private static final String KEY_MSG = "msg";

    public final String msg;

    private ResultMsg(String msg) {
        super();
        this.msg = msg;
    }

    public static final Parser<ResultMsg> PARSER = new Parser<ResultMsg>() {

        @Override
        public ResultMsg parserMap(Map<String, Object> map, String... requireds) {
            if (map == null) {
                return null;
            }
            Object obj = map.get(KEY_MSG);

            return new ResultMsg((String) obj);
        }
    };
}
