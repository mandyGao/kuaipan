
package cn.kuaipan.android.kss;

/**
 * @author panshanjun KSS SDK 中的一些常量定义
 */

public interface KssDef {
    long META_VALID_TIME = 24L * 60 * 60 * 1000;
    long MIN_META_VALID_TIME = 1L * 60 * 60 * 1000;
    long CHUNK_VALIDATE_DUR = META_VALID_TIME - (1024 * 1000); // 留出1024秒确保一个block可以被传完（1024s=4MB/(4KB/s)）

    int BLOCKSIZE = 1024 * 1024 * 4; // 数据分块是4M
    int MIN_CHUNKSIZE = 1024 * 64; // 断点续传的分片大小，服务端约定不小于64K
    int MAX_CHUNKSIZE = Math.min(BLOCKSIZE, MIN_CHUNKSIZE * 64);

    int RETRY_TIMES = 3; // 网络重传次数，我们先设为3次

    String FUNC_UPLOAD = "/upload_block_chunk";

    String SHA1 = "sha1";
    String MD5 = "md5";
    String SIZE = "size";

    // for kss request json
    String KEY_BLOCKINFO = "block_infos";

    // for kss request result
    String KEY_STAT = "stat";
    String KEY_NODE_URLS = "node_urls";
    String KEY_FILE_META = "file_meta";
    String KEY_BLOCK_METAS = "block_metas";
    String KEY_COMMIT_METAS = "commit_metas";
    String KEY_IS_EXISTED = "is_existed";
    String KEY_COMMIT_META = "commit_meta";
    String KEY_BLOCK_META = "block_meta";
    String KEY_SECURE_KEY = "secure_key";

    String KEY_BLOCKS = "blocks";
    String KEY_SHA1 = "sha1";
    String KEY_SIZE = "size";
    String KEY_URLS = "urls";
    //
    String KEY_MODIFY_TIME = "modify_time";
    // String KEY_PROXIES = "proxies";
    // String KEY_URL = "url";
    // String KEY_URLS = "urls";
    //
    // String KEY_STOID = "stoid";
    //
    // String KEY_NODEURLS = "node_urls";
    //

    // for upload chunk url
    String KEY_CHUNKPOS = "chunk_pos";
    String KEY_BODYSUM = "body_sum";
    // String KEY_DESTURL = "Dest-Url";
    //
    // String KEY_MD5 = "md5";
    //

    // Keys for chunk json
    String KEY_NEXT_POS = "next_pos";
    String KEY_LEFT_BYTES = "left_bytes";
    String KEY_UPLOAD_ID = "upload_id";

    // Value of Request
    String VALUE_OK = "OK";
    String VALUE_AUTO_COMMIT = "autoCommit";
    String VALUE_FILE_EXISTED = "FILE_EXISTED";

    // Value of Chunk upload
    String VALUE_CONTINUE_UPLOAD = "CONTINUE_UPLOAD";
    String VALUE_BLOCK_COMPLETED = "BLOCK_COMPLETED";

    // Error of Chunk upload
    String ERR_INVALID_FILE_META = "ERR_INVALID_FILE_META";
    String ERR_INVALID_BLOCK_META = "ERR_INVALID_BLOCK_META";
    String ERR_INVALID_UPLOAD_ID = "ERR_INVALID_UPLOAD_ID";
    String ERR_INVALID_CHUNK_POS = "ERR_INVALID_CHUNK_POS";
    String ERR_INVALID_CHUNK_SIZE = "ERR_INVALID_CHUNK_SIZE";
    String ERR_CHUNK_OUT_OF_RANGE = "ERR_CHUNK_OUT_OF_RANGE";
    String ERR_CHUNK_CORRUPTED = "ERR_CHUNK_CORRUPTED";
    String ERR_BLOCK_CORRUPTED = "ERR_BLOCK_CORRUPTED";
    String ERR_SERVER_EXCEPTION = "ERR_SERVER_EXCEPTION";
    String ERR_STORAGE_REQUEST_ERROR = "ERR_STORAGE_REQUEST_ERROR";
    String ERR_STORAGE_REQUEST_FAILED = "ERR_STORAGE_REQUEST_FAILED";
    String ERR_TOO_MANY_CURRENT_BLOCKS = "ERR_TOO_MANY_CURRENT_BLOCKS";
    //
    // // Func Name
    // String FUNC_UPLOADBLOCKCHUNK = "upload_block_chunk";
}
