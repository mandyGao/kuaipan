
package cn.kuaipan.android.kss.upload;

import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.sdk.model.AbsKscData;

import java.util.HashSet;
import java.util.Map;

class UploadChunkInfo implements KssDef {

    public final String stat;
    public long next_pos;
    public long left_bytes;
    public final String upload_id;
    public final String commit_meta;

    public UploadChunkInfo(long nextPos, long leftPos) {
        stat = VALUE_CONTINUE_UPLOAD;
        next_pos = nextPos;
        left_bytes = leftPos;
        upload_id = null;
        commit_meta = null;
    }

    public UploadChunkInfo(Map<String, Object> dataMap) {
        stat = AbsKscData.asString(dataMap, KEY_STAT);
        next_pos = AbsKscData.asNumber(dataMap.get(KEY_NEXT_POS), -1)
                .longValue();
        left_bytes = AbsKscData.asNumber(dataMap.get(KEY_LEFT_BYTES), -1)
                .longValue();
        upload_id = AbsKscData.asString(dataMap, KEY_UPLOAD_ID);
        commit_meta = AbsKscData.asString(dataMap, KEY_COMMIT_META);
    }

    public boolean isComplete() {
        return VALUE_BLOCK_COMPLETED.equalsIgnoreCase(stat);
    }

    public boolean isContinue() {
        return VALUE_CONTINUE_UPLOAD.equalsIgnoreCase(stat);
    }

    private static final HashSet<String> sReRequestSet;

    static {
        sReRequestSet = new HashSet<String>();

        sReRequestSet.add(ERR_INVALID_FILE_META);
        sReRequestSet.add(ERR_INVALID_BLOCK_META);
        sReRequestSet.add(ERR_INVALID_UPLOAD_ID);
        sReRequestSet.add(ERR_INVALID_CHUNK_POS);
        sReRequestSet.add(ERR_INVALID_CHUNK_SIZE);
        sReRequestSet.add(ERR_CHUNK_OUT_OF_RANGE);
        sReRequestSet.add(ERR_CHUNK_CORRUPTED);
        sReRequestSet.add(ERR_BLOCK_CORRUPTED);
        sReRequestSet.add(ERR_SERVER_EXCEPTION);
        sReRequestSet.add(ERR_STORAGE_REQUEST_ERROR);
        sReRequestSet.add(ERR_STORAGE_REQUEST_FAILED);
    }

    public boolean canRetry() {
        return ERR_CHUNK_CORRUPTED.equalsIgnoreCase(stat);
    }

    public boolean needRequestAgain() {
        String stat = this.stat == null ? null : this.stat.toUpperCase();
        return sReRequestSet.contains(stat);
    }
}
