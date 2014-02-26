
package cn.kuaipan.android.sdk.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class FileArray extends AbsKscData {
    private final static String KEY_FILES = "files";
    private final static String KEY_PATH = "path";

    public final static Parser<FileArray> PARSER = new Parser<FileArray>() {
        @SuppressWarnings("unchecked")
        @Override
        public FileArray parserMap(Map<String, Object> map, String... requireds)
                throws DataFormatException {
            try {
                List<Map<String, Object>> array = (List<Map<String, Object>>) map
                        .get(KEY_FILES);

                return new FileArray(array);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private final List<String> mList;

    public FileArray(List<Map<String, Object>> array) {
        HashSet<String> result = new HashSet<String>();
        for (Map<String, Object> entity : array) {
            Object obj = entity.get(KEY_PATH);
            String path = obj == null ? null : String.valueOf(obj);
            if (!TextUtils.isEmpty(path)) {
                result.add(path);
            } else {
                result.add("/");
            }
        }

        mList = new ArrayList<String>(result);
    }

    public List<String> getList() {
        return mList;
    }
}
