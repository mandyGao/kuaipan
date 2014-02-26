
package cn.kuaipan.android.kss.download;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.model.AbsKscData;
import cn.kuaipan.android.utils.Encode;
import cn.kuaipan.android.utils.IObtainable;
import cn.kuaipan.android.utils.JsonUtils;
import cn.kuaipan.android.utils.OAuthTimeUtils;

public class DownloadRequest implements KssDef {
    private static final String LOG_TAG = "DownloadRequest";

    private static final String GEN_TIME = "gen_time";

    public final String stat;
    private byte[] secure_key;
    private Block[] blocks;

    public final long generateTime;

    @SuppressWarnings("unchecked")
    public DownloadRequest(String kssStr) throws KscException {
        Map<String, Object> kssData = null;
        try {
            kssData = (Map<String, Object>) JsonUtils.parser(new StringReader(
                    kssStr));
            generateTime = AbsKscData.asNumber(kssData.get(GEN_TIME),
                    OAuthTimeUtils.currentTime()).longValue();

            stat = AbsKscData.asString(kssData, KEY_STAT);

            // need update
            secure_key = Encode.hexStringToByteArray(AbsKscData.asString(
                    kssData, KEY_SECURE_KEY));

            Collection<Map<String, Object>> blockDatas = (Collection<Map<String, Object>>) kssData
                    .get(KEY_BLOCKS);
            if (blockDatas != null) {
                blocks = new Block[blockDatas.size()];
                int i = 0;
                for (Map<String, Object> blockData : blockDatas) {
                    String sha1 = AbsKscData.asString(blockData, KEY_SHA1);
                    long size = AbsKscData.asNumber(blockData.get(KEY_SIZE), 0)
                            .longValue();

                    String[] urls = null;
                    Collection<String> urlDatas = (Collection<String>) blockData
                            .get(KEY_URLS);
                    if (urlDatas != null) {
                        urls = new String[urlDatas.size()];
                        int j = 0;
                        for (String url : urlDatas) {
                            urls[j++] = url;
                        }
                    }

                    Block block = new Block(sha1, urls, size);
                    blocks[i++] = block;
                }
            }

        } catch (IOException e) {
            throw new KscException(ErrorCode.DATA_IS_EMPTY, "kss is null", e);
        } catch (JSONException e) {
            throw new KscException(ErrorCode.DATA_IS_NOT_JSON,
                    "kss is not json", e);
        } finally {
            if (kssData != null && kssData instanceof IObtainable) {
                ((IObtainable) kssData).recycle();
            }
        }
    }

    public byte[] getSecureKey() {
        return secure_key;
    }

    public int getBlockCount() {
        return blocks == null ? -1 : blocks.length;
    }

    public Block getBlock(int index) {
        return blocks[index];
    }

    public String[] getBlockUrls(long start) {
        if (start < 0 || blocks == null) {
            return null;
        }
        String[] result = null;
        long pos = 0;

        for (int i = 0; i < blocks.length; i++) {
            long end = pos + blocks[i].size;

            if (start >= pos && start < end) {
                result = blocks[i].urls;
                break;
            }
            pos = end;
        }
        return result;
    }

    @Override
    public String toString() {
        JSONObject root = null;
        try {
            root = new JSONObject();
            root.put(GEN_TIME, generateTime);
            root.put(KEY_STAT, stat);
            root.put(KEY_SECURE_KEY, Encode.byteArrayToHexString(secure_key));

            JSONArray blocks = new JSONArray();
            if (this.blocks != null) {
                for (Block block : this.blocks) {
                    JSONObject blockObj = new JSONObject();
                    blockObj.put(KEY_SHA1, block.sha1);
                    blockObj.put(KEY_SIZE, block.size);
                    JSONArray urls = null;
                    if (block.urls != null) {
                        urls = new JSONArray(Arrays.asList(block.urls));
                    } else {
                        urls = new JSONArray();
                    }
                    blockObj.put(KEY_URLS, urls);

                    blocks.put(blockObj);
                }
            }

            root.put(KEY_BLOCKS, blocks);
        } catch (JSONException e) {
            Log.w(LOG_TAG,
                    "Failed generate Json String for UploadRequestResult");
            root = null;
        }
        return String.valueOf(root);
    }

    public long getTotalSize() {
        if (blocks == null) {
            return 0;
        }

        long result = 0;
        for (Block block : blocks) {
            result += block.size;
        }

        return result;
    }

    public static class Block {
        public final String sha1;
        private String[] urls;
        public final long size;

        public Block(String sha1, String[] urls, long size) {
            super();
            this.sha1 = sha1;
            this.urls = urls;
            this.size = size;
        }

        public String[] getUrls() {
            return urls;
        }
    }
}
