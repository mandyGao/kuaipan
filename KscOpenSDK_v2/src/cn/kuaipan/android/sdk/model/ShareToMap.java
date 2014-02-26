
package cn.kuaipan.android.sdk.model;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import android.text.TextUtils;
import android.util.Log;

public class ShareToMap extends AbsKscData {
    private static final String LOG_TAG = "ShareToMap";

    private static final String KEY_FILES = "files";
    private static final String KEY_PATH = "path";
    private static final String KEY_SHARERS = "sharers";

    public final static Parser<ShareToMap> PARSER = new Parser<ShareToMap>() {
        @SuppressWarnings("unchecked")
        @Override
        public ShareToMap parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                Collection<Map<String, Object>> array = (Collection<Map<String, Object>>) map
                        .get(KEY_FILES);

                return new ShareToMap(array);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private final HashMap<String, HashSet<KuaipanUser>> mInfoMap;

    private ShareToMap(Collection<Map<String, Object>> dataList) {
        mInfoMap = new HashMap<String, HashSet<KuaipanUser>>();

        if (dataList != null && !dataList.isEmpty()) {
            for (Map<String, Object> dataMap : dataList) {
                parseFromMap(dataMap);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseFromMap(Map<String, Object> map) {
        String path = asString(map, KEY_PATH);
        Collection<Map<String, Object>> shareList = (Collection<Map<String, Object>>) map
                .get(KEY_SHARERS);
        if (TextUtils.isEmpty(path) || shareList == null || shareList.isEmpty()) {
            Log.w(LOG_TAG,
                    "A share to info will be discard. The path is invalid or sharer is empty");
            return;
        }

        path = new File("/" + path).getAbsolutePath();
        HashSet<KuaipanUser> users = mInfoMap.get(path);
        if (users == null) {
            users = new HashSet<KuaipanUser>();
            mInfoMap.put(path, users);
        }

        for (Map<String, Object> user : shareList) {
            users.add(new KuaipanUser(user, -1L, -1L, -1L));
        }
    }

    public Set<String> getSharedPath() {
        return mInfoMap.keySet();
    }

    public Set<KuaipanUser> getSharedUser(String path) {
        return mInfoMap.get(path);
    }
}
