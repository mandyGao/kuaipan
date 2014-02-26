
package cn.kuaipan.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class FileSyncInfo extends AbsKscData implements Parcelable {

    private final static String KEY_CURSOR = "cursor";
    private final static String KEY_FILES = "files";

    public final static Parser<FileSyncInfo> PARSER = new Parser<FileSyncInfo>() {

        @Override
        public FileSyncInfo parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                return new FileSyncInfo(map);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private List<KuaipanFile> syncedInfo;
    public final String cursor;

    public FileSyncInfo(Map<String, Object> dataMap) {
        if (null == dataMap) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse");
        }
        cursor = asString(dataMap, KEY_CURSOR);

        LinkedList<KuaipanFile> children = new LinkedList<KuaipanFile>();

        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> childrenData = (Collection<Map<String, Object>>) dataMap
                .get(KEY_FILES);
        if (childrenData != null && !childrenData.isEmpty()) {
            for (Map<String, Object> childData : childrenData) {
                KuaipanFile child = new KuaipanFile(childData, null, null);
                children.add(child);
            }
        }

        syncedInfo = children.isEmpty() ? null : children;
    }

    public List<KuaipanFile> getSyncInfo() {
        return syncedInfo;
    }

    public static final Parcelable.Creator<FileSyncInfo> CREATOR = new Parcelable.Creator<FileSyncInfo>() {
        @Override
        public FileSyncInfo createFromParcel(Parcel source) {
            FileSyncInfo info = new FileSyncInfo(source);
            return info;
        }

        @Override
        public FileSyncInfo[] newArray(int size) {
            return new FileSyncInfo[size];
        }

    };

    private FileSyncInfo(Parcel source) {
        cursor = source.readString();
        syncedInfo = source.createTypedArrayList(KuaipanFile.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cursor);
        dest.writeTypedList(syncedInfo);
    }
}
