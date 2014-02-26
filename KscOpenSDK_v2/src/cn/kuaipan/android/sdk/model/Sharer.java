
package cn.kuaipan.android.sdk.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Sharer {
    public String userName;
    public Integer userId;
    public List<KuaipanFile> kuaipanFiles = null;

    public Sharer(Map<String, Object> map) {
        this.parseFromMap(map);
    }

    private void parseFromMap(Map<String, Object> map) {
        if (null == map) {
            return;
        }
        this.userName = (String) map.get("user_name");
        this.userId = (Integer) map.get("user_id");
        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> shares = (Collection<Map<String, Object>>) map
                .get("files");
        if (shares != null) {
            Iterator<Map<String, Object>> it = shares.iterator();
            this.kuaipanFiles = new LinkedList<KuaipanFile>();
            while (it.hasNext()) {
                KuaipanFile temp_file = new KuaipanFile(it.next(), null, null);
                this.kuaipanFiles.add(temp_file);
            }
        }
    }
}
