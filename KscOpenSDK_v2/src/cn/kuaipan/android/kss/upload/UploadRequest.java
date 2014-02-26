
package cn.kuaipan.android.kss.upload;

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

public class UploadRequest implements KssDef {
    private static final String LOG_TAG = "UploadRequest";

    private static final String GEN_TIME = "gen_time";

    public final String stat;
    private byte[] secure_key;
    public final String file_meta;
    private final Block[] blocks;
    private String[] node_urls;

    public final long generateTime;

    @SuppressWarnings("unchecked")
    public UploadRequest(String kssStr) throws KscException {
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
            file_meta = AbsKscData.asString(kssData, KEY_FILE_META);

            Collection<Map<String, Object>> blockDatas = (Collection<Map<String, Object>>) kssData
                    .get(KEY_BLOCK_METAS);
            if (blockDatas != null) {
                blocks = new Block[blockDatas.size()];
                int i = 0;
                for (Map<String, Object> blockData : blockDatas) {
                    boolean exist = AbsKscData.asNumber(
                            blockData.get(KEY_IS_EXISTED), 0).intValue() != 0;
                    String key = exist ? KEY_COMMIT_META : KEY_BLOCK_META;
                    String meta = AbsKscData.asString(blockData, key);
                    Block block = new Block(meta, exist);
                    blocks[i++] = block;
                }
            } else {
                throw new KscException(ErrorCode.DATA_UNSCHEDULE,
                        "Not fount block_metas in " + kssStr);
            }

            Collection<String> urls = (Collection<String>) kssData
                    .get(KEY_NODE_URLS);
            if (urls != null) {
                node_urls = new String[urls.size()];
                int i = 0;
                for (String url : urls) {
                    node_urls[i++] = url;
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

    public Block[] getBlocks() {
        return blocks;
    }

    public String[] getNodeUrls() {
        return node_urls;
    }

    public boolean hasEmptyBlock() {
        return getNextEmptyBlockIndex() >= 0;
    }

    public int getNextEmptyBlockIndex() {
        if (blocks == null || blocks.length <= 0) {
            return -1;
        }

        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i] != null && !blocks[i].exist) {
                return i;
            }
        }
        return -1;
    }

    public String getCommit() {
        JSONObject root = null;
        try {
            root = new JSONObject();
            root.put(KEY_FILE_META, file_meta);

            JSONArray blocks = new JSONArray();
            if (this.blocks != null) {
                for (Block block : this.blocks) {
                    JSONObject blockObj = new JSONObject();
                    blockObj.put(KEY_COMMIT_META, block.meta);
                    blocks.put(blockObj);
                }
            }
            root.put(KEY_COMMIT_METAS, blocks);
        } catch (JSONException e) {
            Log.w(LOG_TAG,
                    "Failed generate Json String for UploadRequestResult");
            root = null;
        }
        return String.valueOf(root);
    }

    @Override
    public String toString() {
        JSONObject root = null;
        try {
            root = new JSONObject();
            root.put(GEN_TIME, generateTime);
            root.put(KEY_STAT, stat);
            root.put(KEY_SECURE_KEY, Encode.byteArrayToHexString(secure_key));
            root.put(KEY_FILE_META, file_meta);

            JSONArray urls = null;
            if (node_urls != null) {
                urls = new JSONArray(Arrays.asList(node_urls));
            } else {
                urls = new JSONArray();
            }
            root.put(KEY_NODE_URLS, urls);

            JSONArray blocks = new JSONArray();
            if (this.blocks != null) {
                for (Block block : this.blocks) {
                    JSONObject blockObj = new JSONObject();
                    blockObj.put(KEY_IS_EXISTED, block.exist ? 1 : 0);
                    if (block.exist) {
                        blockObj.put(KEY_COMMIT_META, block.meta);
                    } else {
                        blockObj.put(KEY_BLOCK_META, block.meta);
                    }
                    blocks.put(blockObj);
                }
            }
            root.put(KEY_BLOCK_METAS, blocks);
        } catch (JSONException e) {
            Log.w(LOG_TAG,
                    "Failed generate Json String for UploadRequestResult");
            root = null;
        }
        return String.valueOf(root);
    }

    public static class Block {
        public String meta;
        public boolean exist;

        public Block(String meta, boolean exist) {
            super();
            this.meta = meta;
            this.exist = exist;
        }
    }
}
