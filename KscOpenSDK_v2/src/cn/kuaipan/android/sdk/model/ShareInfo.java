
package cn.kuaipan.android.sdk.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;

import android.util.Log;

public class ShareInfo extends AbsKscData {
    private static final String LOG_TAG = "ShareInfo";

    private final static String KEY_CURSOR = "cursor";
    private final static String KEY_FILES = "files";
    private static final String KEY_SHARERS = "sharers";

    public final static Parser<ShareInfo> PARSER = new Parser<ShareInfo>() {

        @Override
        public ShareInfo parserMap(Map<String, Object> map, String... requireds)
                throws DataFormatException {
            try {
                return new ShareInfo(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    public String cursor;
    private HashMap<KuaipanUser, Set<KuaipanFile>> shareMap;

    @SuppressWarnings("unchecked")
    public ShareInfo(Map<String, Object> dataMap) {
        if (null == dataMap) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse");
        }
        cursor = asString(dataMap, KEY_CURSOR);
        shareMap = new HashMap<KuaipanUser, Set<KuaipanFile>>();

        Collection<Map<String, Object>> sharers = (Collection<Map<String, Object>>) dataMap
                .get(KEY_SHARERS);
        if (sharers != null && !sharers.isEmpty()) {
            for (Map<String, Object> sharer : sharers) {
                parseSharer(sharer);
            }
        }
    }

    public Map<KuaipanUser, Set<KuaipanFile>> getShareMap() {
        return shareMap;
    }

    @SuppressWarnings("unchecked")
    private void parseSharer(Map<String, Object> sharerData) {
        KuaipanUser user = new KuaipanUser(sharerData, -1L, -1L, -1L);
        Collection<Map<String, Object>> folderList = (Collection<Map<String, Object>>) sharerData
                .get(KEY_FILES);
        if (folderList == null || folderList.isEmpty()) {
            Log.w(LOG_TAG,
                    "A share from info will be discard. The folderList is empty");
            return;
        }

        Set<KuaipanFile> folders = shareMap.get(user);
        if (folders == null) {
            folders = new HashSet<KuaipanFile>();
            shareMap.put(user, folders);
        }

        for (Map<String, Object> folderData : folderList) {
            folders.add(new KuaipanFile(folderData, null, null));
        }
    }
}
