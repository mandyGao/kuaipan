
package cn.kuaipan.android.sdk.model;

import cn.kuaipan.android.sdk.exception.KscException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class SessionArray extends AbsKscData {

    private static final String KEY_SESSION = "session";
    public static final String KEY_LATEST_TIME = "latest_time";

    public static final Parser<SessionArray> PARSER = new Parser<SessionArray>() {

        @Override
        public SessionArray parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException, KscException {
            try {
                return new SessionArray(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    public final ArrayList<SessionInfo> mSessionInfos;
    public final String mLatestTime;

    public SessionArray(Map<String, Object> map) {
        mSessionInfos = new ArrayList<SessionInfo>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> array = (List<Map<String, Object>>) map
                .get(KEY_SESSION);
        mLatestTime = asString(map, KEY_LATEST_TIME);
        for (Map<String, Object> entity : array) {
            SessionInfo sessionInfo = new SessionInfo(entity);
            mSessionInfos.add(sessionInfo);
        }
    }

}
