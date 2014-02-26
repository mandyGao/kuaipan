
package cn.kuaipan.android.kss.upload;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;
import cn.kuaipan.android.kss.KssDef;

public class UploadFileInfo implements KssDef {
    private static final String LOG_TAG = "UploadFileInfo";

    private String mSha1;
    private final ArrayList<BlockInfo> mBlockInfos = new ArrayList<BlockInfo>();

    UploadFileInfo() {
    }

    public UploadFileInfo(String kssString) {
        try {
            JSONObject root = new JSONObject(kssString);
            mSha1 = root.optString(SHA1);
            JSONArray blockArray = root.optJSONArray(KEY_BLOCKINFO);
            if (blockArray == null) {
                return;
            }
            final int count = blockArray.length();
            for (int i = 0; i < count; i++) {
                JSONObject block = blockArray.optJSONObject(i);
                String sha1 = block == null ? null : block.optString(SHA1);
                String md5 = block == null ? null : block.optString(MD5);
                int size = block == null ? -1 : block.optInt(SIZE, -1);
                if (!TextUtils.isEmpty(sha1) && !TextUtils.isEmpty(md5)
                        && size >= 0) {
                    appendBlock(sha1, md5, size);
                }
            }
        } catch (JSONException e) {
            Log.w(LOG_TAG,
                    "Failed parser UploadFileInfo from a String. The String:"
                            + kssString, e);
        }
    }

    private JSONObject getKss() {
        try {
            JSONArray blockArray = new JSONArray();
            for (BlockInfo blockInfo : mBlockInfos) {
                JSONObject block = new JSONObject();
                block.put(SHA1, blockInfo.sha1);
                block.put(MD5, blockInfo.md5);
                block.put(SIZE, blockInfo.size);
                blockArray.put(block);
            }

            JSONObject root = new JSONObject();
            root.put(KEY_BLOCKINFO, blockArray);

            return root;
        } catch (Throwable t) {
            Log.w(LOG_TAG, "Failed generate Json String for UploadRequestInfo");
            return null;
        }
    }

    void setSha1(String sha1) {
        mSha1 = sha1;
    }

    public String getSha1() {
        return mSha1;
    }

    void appendBlock(String sha1, String md5, long size) {
        mBlockInfos.add(new BlockInfo(sha1, md5, (int) size));
    }

    public BlockInfo getBlockInfo(int index) {
        if (index >= mBlockInfos.size()) {
            return null;
        }

        return mBlockInfos.get(index);
    }

    public String getKssString() {
        JSONObject obj = getKss();
        return obj == null ? null : obj.toString();
    }

    @Override
    public String toString() {
        JSONObject obj = getKss();
        try {
            obj.put(SHA1, mSha1);
        } catch (Throwable t) {
            // ignore
        }
        return String.valueOf(obj);
    }

    public static class BlockInfo {
        public final String sha1;
        public final String md5;
        public final int size;

        private BlockInfo(String sha1, String md5, int size) {
            super();
            this.sha1 = sha1;
            this.md5 = md5;
            this.size = size;
        }
    }
}
