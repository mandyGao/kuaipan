
package cn.kuaipan.android.sdk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class KuaipanFile extends AbsKscData implements Parcelable {
    // private static final String LOG_TAG = "KuaipanFile";

    public final static Parser<KuaipanFile> PARSER = new Parser<KuaipanFile>() {
        @Override
        public KuaipanFile parserMap(Map<String, Object> map,
                String... requireds) throws DataFormatException {
            try {
                return new KuaipanFile(map, null, null);
            } catch (NullPointerException e) {
                throw new DataFormatException("Some required param is null");
            }
        }
    };

    private static enum FileType {
        FOLDER(0), FILE(1);

        private String value;

        private FileType(int index) {
            switch (index) {
                case 0:
                    value = "folder";
                    break;
                case 1:
                    value = "file";
                    break;
            }
        }

        public static FileType parser(Object obj) {
            return obj == null ? FILE : FileType.valueOf(obj.toString()
                    .toUpperCase());
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static enum Right {
        write, unshare;

        public static Right parser(Object obj) {
            return obj == null ? null : Right.valueOf(obj.toString()
                    .toLowerCase());
        }
    }

    public final String file_id;
    public String path;
    public final String name;
    public final FileType type;
    public final long size;
    // public final String root;
    public final String sha1;
    public final String rev;
    public final Date create_time;
    public final Date modify_time;

    public final boolean is_deleted;

    private List<KuaipanFile> children;
    public final int file_count;
    public final String share_id;
    // public final String hash;

    public final Right right; // 共享文件的操作权限write, unshare 权限

    public KuaipanFile(String path, Collection<KuaipanFile> children) {
        super();
        this.file_id = null;
        this.path = path;
        this.name = new File(path).getName();
        this.rev = "1";
        this.type = children == null ? FileType.FILE : FileType.FOLDER;
        this.size = 0;
        this.sha1 = null;
        this.create_time = null;
        this.modify_time = null;
        this.is_deleted = false;
        this.file_count = children == null ? 0 : children.size();
        this.share_id = null;
        this.right = null;
        addChildren(children);
    }

    KuaipanFile(Map<String, Object> dataMap, String parent, Boolean isDeleted) {
        if (null == dataMap) {
            throw new IllegalArgumentException(
                    "DataMap can't be null when parse");
        }
        file_id = asString(dataMap, "file_id");

        if (parent == null) {
            String path = asString(dataMap, "path");
            path = path == null ? "" : path;
            File file = new File("/" + path);
            this.name = file.getName();
            this.path = file.getAbsolutePath();
        } else {
            String name = asString(dataMap, "name");
            this.name = name == null ? "" : name;
            this.path = new File("/" + parent, this.name).getPath();
        }
        type = TextUtils.equals("/", path) ? FileType.FOLDER : FileType
                .parser(dataMap.get("type"));
        size = asNumber(dataMap.get("size"), 0).longValue();

        // root = asString(map, "root");
        sha1 = asString(dataMap, "sha1");
        rev = asString(dataMap, "rev");

        create_time = asDate(dataMap.get("create_time"), null);
        modify_time = asDate(dataMap.get("modify_time"), null);

        is_deleted = isDeleted == null ? asBoolean(dataMap.get("is_deleted"),
                false) : isDeleted;

        file_count = asNumber(dataMap.get("files_total"), -1).intValue();
        share_id = asString(dataMap, "share_id");
        right = Right.parser(dataMap.get("rigth"));
        // hash = asString(map, "hash");

        LinkedList<KuaipanFile> children = new LinkedList<KuaipanFile>();

        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> childrenData = (Collection<Map<String, Object>>) dataMap
                .get("files");
        if (childrenData != null && !childrenData.isEmpty()) {
            for (Map<String, Object> childData : childrenData) {
                KuaipanFile child = new KuaipanFile(childData, path, null);
                children.add(child);
            }
        }

        @SuppressWarnings("unchecked")
        Collection<Map<String, Object>> deletedShares = (Collection<Map<String, Object>>) dataMap
                .get("del_shared_files");
        if (deletedShares != null && !deletedShares.isEmpty()) {
            for (Map<String, Object> childData : deletedShares) {
                KuaipanFile child = new KuaipanFile(childData, path, true);
                children.add(child);
            }
        }

        this.children = children.isEmpty() ? null : children;
    }

    public boolean isFile() {
        return type == FileType.FILE;
    }

    public boolean isDirectory() {
        return type == FileType.FOLDER;
    }

    public void addChildren(Collection<KuaipanFile> children) {
        if (children == null) {
            return;
        }

        if (this.children == null) {
            this.children = new LinkedList<KuaipanFile>();
        }
        this.children.addAll(children);
    }

    public List<KuaipanFile> getChildren() {
        return children;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("\nFile:{");

        if (path != null) {
            buf.append("path:\"");
            buf.append(path);
            buf.append("\"");
        }
        if (file_id != null) {
            buf.append(", file_id:");
            buf.append(file_id);
        }
        if (type != null) {
            buf.append(", type:\"");
            buf.append(type);
            buf.append("\"");
        }

        if (name != null) {
            buf.append(", name:\"");
            buf.append(name);
            buf.append("\"");
        }

        if (sha1 != null) {
            buf.append(", sha1:\"");
            buf.append(sha1);
            buf.append("\"");
        }

        // if (hash != null) {
        // buf.append(", hash:\"");
        // buf.append(hash);
        // buf.append("\"");
        // }

        if (is_deleted) {
            buf.append(", is_deleted:");
            buf.append(is_deleted);
        }

        if (size >= 0) {
            buf.append(", size:");
            buf.append(size);
        }

        // if (root != null) {
        // buf.append(", root:\"");
        // buf.append(root);
        // buf.append("\"");
        // }

        if (rev != null) {
            buf.append(", rev:");
            buf.append(rev);
        }

        if (create_time != null) {
            buf.append(", create_time:\"");
            buf.append(create_time);
            buf.append("\"");
        }

        if (modify_time != null) {
            buf.append(", modify_time:\"");
            buf.append(modify_time);
            buf.append("\"");
        }

        if (children != null) {
            buf.append(", children:{");
            buf.append(Arrays.toString(children.toArray()));
            buf.append("}");
        }

        buf.append("}");
        return buf.toString();
    }

    public static final Parcelable.Creator<KuaipanFile> CREATOR = new Parcelable.Creator<KuaipanFile>() {
        @Override
        public KuaipanFile createFromParcel(Parcel source) {
            KuaipanFile file = new KuaipanFile(source);
            return file;
        }

        @Override
        public KuaipanFile[] newArray(int size) {
            return new KuaipanFile[size];
        }

    };

    public KuaipanFile(Parcel source) {
        file_id = source.readString();
        path = source.readString();
        name = source.readString();
        String typeStr = source.readString();
        type = typeStr == null ? null : FileType.parser(typeStr);
        size = source.readLong();
        sha1 = source.readString();
        rev = source.readString();
        long cTime = source.readLong();
        create_time = new Date(cTime);
        long mTime = source.readLong();
        modify_time = new Date(mTime);
        is_deleted = source.readInt() != 0;
        file_count = source.readInt();
        share_id = source.readString();
        String rightStr = source.readString();
        right = rightStr == null ? null : Right.parser(rightStr);
        children = source.createTypedArrayList(CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(file_id);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeString(type == null ? null : type.toString());
        dest.writeLong(size);
        dest.writeString(sha1);
        dest.writeString(rev);
        dest.writeLong(create_time == null ? -1 : create_time.getTime());
        dest.writeLong(modify_time == null ? -1 : modify_time.getTime());
        dest.writeInt(is_deleted ? 1 : 0);
        dest.writeInt(file_count);
        dest.writeString(share_id);
        dest.writeString(right == null ? null : right.toString());
        dest.writeTypedList(children);
    }
}
