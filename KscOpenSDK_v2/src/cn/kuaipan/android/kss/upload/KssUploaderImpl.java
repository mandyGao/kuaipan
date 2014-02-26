
package cn.kuaipan.android.kss.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CRC32;

import org.apache.http.HttpStatus;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import cn.kuaipan.android.http.DecoderInputStream;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.http.KscHttpRequest;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.kss.FileTranceListener;
import cn.kuaipan.android.kss.IKssRequestor;
import cn.kuaipan.android.kss.IKssRequestor.IKssUploadRequestResult;
import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.kss.KssInputStreamEntity;
import cn.kuaipan.android.kss.KssMaster;
import cn.kuaipan.android.kss.KssUploadInfo;
import cn.kuaipan.android.kss.KssUploader;
import cn.kuaipan.android.kss.RC4Encoder;
import cn.kuaipan.android.kss.upload.UploadFileInfo.BlockInfo;
import cn.kuaipan.android.kss.upload.UploadRequest.Block;
import cn.kuaipan.android.sdk.exception.ErrorHelper;
import cn.kuaipan.android.sdk.exception.ServerException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.sdk.internal.ApiDataHelper;
import cn.kuaipan.android.sdk.internal.Constants;
import cn.kuaipan.android.sdk.internal.OAuthApiExecutor;
import cn.kuaipan.android.utils.Encode;
import cn.kuaipan.android.utils.IObtainable;
import cn.kuaipan.android.utils.OAuthTimeUtils;
import cn.kuaipan.android.utils.RandomFileInputStream;

public class KssUploaderImpl implements KssUploader, KssDef {

    private static final String LOG_TAG = "KssUploaderImpl";

    private static final CRC32 sCRC32 = new CRC32();
    private static final byte[] CRC_BUF = new byte[8 * 1024];

    private final KssMaster mMaster;
    private final KscHttpTransmitter mTransmitter;
    private final IKssRequestor mRequestor;

    private int mChunkSize = MIN_CHUNKSIZE;

    public KssUploaderImpl(KscHttpTransmitter transmitter, KssMaster master,
            IKssRequestor requestor) {
        mTransmitter = transmitter;
        mMaster = master;
        mRequestor = requestor;
    }

    /**
     * @param apiRequestor
     * @param localFile
     * @param remotePath
     * @param listener
     * @param taskHash a hash code for query/update info to master
     * @param fileInfo
     * @return null if upload success and not need commit, or a KssUploadInfo
     *         stored request info.
     * @throws Exception
     */
    public KssUploadInfo upload(File localFile, String remotePath,
            IKscTransferListener listener, int taskHash, UploadFileInfo fileInfo)
            throws Exception {
        FileTranceListener sendListener = null;
        if (listener != null) {
            sendListener = new FileTranceListener(listener, true);
            listener.setSendTotal(localFile.length());
        }

        KssUploadInfo info = null;
        do {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            boolean verified = false;
            info = mMaster.getStoredUploadInfo(taskHash);
            if (info == null) {
                IKssUploadRequestResult requestResult = mRequestor
                        .requestUpload(fileInfo, localFile.lastModified(),
                                remotePath);
                String msg = requestResult.getMsg();

                if (KssDef.VALUE_AUTO_COMMIT.equalsIgnoreCase(msg)) {
                    if (listener != null) {
                        listener.setSendPos(localFile.length());
                    }
                    return null;
                }

                if (!KssDef.VALUE_OK.equalsIgnoreCase(msg)) {
                    throw new Exception("Received invalid msg: " + msg);
                }

                info = new KssUploadInfo(fileInfo, requestResult.getStub(),
                        requestResult.getRequest());

                mMaster.updateUploadInfo(taskHash, info, 0);
                verified = true;
            }

            uploadBlock(taskHash, localFile, remotePath, sendListener, info,
                    !verified);
        } while (info.request != null && info.request.hasEmptyBlock());

        return info;
    }

    private void uploadBlock(int hash, File file, String remotePath,
            FileTranceListener listener, KssUploadInfo info, boolean needVerify)
            throws Exception, InvalidKeyException {
        if (info == null) {
            throw new IllegalArgumentException(
                    "The KssUploadInfo can not be empty.");
        }

        UploadRequest request = info.request;
        int blockIndex = request.getNextEmptyBlockIndex();

        if (blockIndex >= 0) {

            if (needVerify) {
                verifyBlock(file, info.fileInfo, blockIndex);
            }

            uploadBlock(hash, file, listener, info, blockIndex);
        }
    }

    private void verifyBlock(File file, UploadFileInfo request, int blockIndex)
            throws IOException {
        BlockInfo info = request.getBlockInfo(blockIndex);
        int size = (int) Math.min(file.length() - blockIndex * BLOCKSIZE,
                BLOCKSIZE);
        if (size != info.size) {
            throw new IOException("Block size has changed.");
        }

        long start = blockIndex * BLOCKSIZE;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            long skip = in.skip(start);
            if (skip != start) {
                throw new IOException("File size has changed.");
            }

            String sha1 = Encode.SHA1Encode(in, size);
            if (!TextUtils.equals(sha1, info.sha1)) {
                throw new IOException("Block has changed.");
            }
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private void uploadBlock(int taskHash, File file,
            FileTranceListener listenerGroup, KssUploadInfo info, int blockIndex)
            throws Exception, InvalidKeyException {
        long pos = mMaster.getUploadPos(taskHash);
        pos = pos - (pos % MIN_CHUNKSIZE);
        if (pos >= ((blockIndex + 1) * BLOCKSIZE)) {
            pos = blockIndex * BLOCKSIZE;
        }

        long blockEnd = Math.min(file.length(), (blockIndex + 1) * BLOCKSIZE);

        if (Constants.DEBUG) {
            Log.d(LOG_TAG,
                    "RC4 key:" + Arrays.toString(info.request.getSecureKey()));
        }
        RC4Encoder rc4Decoder = new RC4Encoder(info.request.getSecureKey());
        RandomFileInputStream in = new RandomFileInputStream(file);
        try {
            in.moveToPos(pos);

            if (listenerGroup != null) {
                listenerGroup.setSendPos(pos);
            }

            UploadChunkInfo chunkInfo = new UploadChunkInfo(pos % BLOCKSIZE,
                    blockEnd - pos);
            while (chunkInfo.next_pos < blockEnd && chunkInfo.left_bytes > 0) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                IKscTransferListener listener = listenerGroup == null ? null
                        : listenerGroup.getChunkListaner(pos
                                + chunkInfo.next_pos);

                chunkInfo = uploadChunk(in, rc4Decoder, listener, info,
                        blockIndex, chunkInfo);

                if (chunkInfo != null) {
                    if (chunkInfo.isContinue()) {
                        mMaster.updateUploadInfo(taskHash, info, blockIndex
                                * BLOCKSIZE + chunkInfo.next_pos);
                        continue;
                    }
                    if (chunkInfo.isComplete()) {
                        Block block = info.request.getBlocks()[blockIndex];
                        block.meta = chunkInfo.commit_meta;
                        block.exist = true;
                        mMaster.updateUploadInfo(
                                taskHash,
                                info,
                                Math.min((blockIndex + 1) * BLOCKSIZE,
                                        file.length()));
                        break;
                    }
                    if (chunkInfo.needRequestAgain()) {
                        mMaster.deleteUploadInfo(taskHash);
                        if ((OAuthTimeUtils.currentTime() - info.request.generateTime) >= MIN_META_VALID_TIME) {
                            break;
                        }
                    }

                    ServerMsgException e = new ServerMsgException(200,
                            chunkInfo.stat);
                    Log.w(LOG_TAG, "Exception in uploadBlock", e);
                    throw e;
                } else {
                    throw new NullPointerException("Return chunkInfo is null");
                }
            }
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private UploadChunkInfo uploadChunk(RandomFileInputStream in,
            RC4Encoder rc4Decoder, IKscTransferListener listener,
            KssUploadInfo info, int blockIndex, UploadChunkInfo chunkInfo)
            throws Exception {
        UploadChunkInfo result = null;
        String[] urls = info.request.getNodeUrls();
        // urls = new String[] {
        // "http://192.168.135.93/kss_node"
        // };
        if (urls == null || urls.length <= 0) {
            throw new IllegalArgumentException("No available urls.");
        }

        for (int i = 0; i < urls.length; i++) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            in.moveToPos(BLOCKSIZE * blockIndex + chunkInfo.next_pos);
            in.mark(BLOCKSIZE);
            try {
                Uri uri = Uri.parse(urls[i] + FUNC_UPLOAD);
                Uri.Builder builder = uri.buildUpon();
                builder.appendQueryParameter(KEY_CHUNKPOS,
                        String.valueOf(chunkInfo.next_pos));
                if (!TextUtils.isEmpty(chunkInfo.upload_id)) {
                    builder.appendQueryParameter(KEY_UPLOAD_ID,
                            chunkInfo.upload_id);
                } else {
                    builder.appendQueryParameter(KEY_FILE_META,
                            info.request.file_meta);
                    builder.appendQueryParameter(KEY_BLOCK_META,
                            info.request.getBlocks()[blockIndex].meta);
                }

                result = _uploadChunk(builder.build(), chunkInfo.next_pos, in,
                        rc4Decoder, listener);
                break;
            } catch (Exception e) {
                ErrorHelper.handleInterruptException(e);
                if (i >= (urls.length - 1)) {
                    throw e;
                }
            }
        }

        return result;
    }

    private UploadChunkInfo _uploadChunk(Uri uri, long pos,
            RandomFileInputStream in, RC4Encoder rc4Decoder,
            IKscTransferListener listener) throws Exception {
        UploadChunkInfo result = null;
        AtomicInteger retry = new AtomicInteger(RETRY_TIMES);
        while (retry.get() >= 0) {
            in.reset();
            long blockSize = Math.min(BLOCKSIZE, in.available() + pos);
            long len = Math.min(mChunkSize, blockSize - pos);
            DecoderInputStream input = new DecoderInputStream(in, rc4Decoder,
                    8 * 1024);

            // check crc sum
            long crc = getCRC(input, len);
            Uri bodyUri = uri.buildUpon()
                    .appendQueryParameter(KEY_BODYSUM, String.valueOf(crc))
                    .build();
            in.reset();
            input = new DecoderInputStream(in, rc4Decoder, 8 * 1024);

            try {
                if (listener != null) {
                    listener.setSendPos(0);
                }
                result = doUpload(bodyUri, input, len, listener);
                if (result.isContinue() || result.isComplete()) {
                    updatePos(result, pos, len, blockSize);
                    mChunkSize = Math.min(MAX_CHUNKSIZE, mChunkSize << 1);
                    break;
                }

                if (result.canRetry() && retry.decrementAndGet() >= 0) {
                    continue;
                }
                return result;
            } catch (Exception e) {
                if (ErrorHelper.isNetworkException(e)
                        && retry.decrementAndGet() >= 0) {
                    mChunkSize = Math.max(MIN_CHUNKSIZE, mChunkSize >> 1);
                    result = null;
                    // Network error, wait 5s then retry.
                    Thread.sleep(5 * 1000);
                } else {
                    throw e;
                }
            }
        }

        return result;
    }

    private UploadChunkInfo doUpload(Uri uri, InputStream input, long len,
            IKscTransferListener listener) throws Exception {
        Map<String, Object> dataMap = null;
        try {
            KscHttpRequest request = new KscHttpRequest(HttpMethod.POST, uri,
                    null, listener);
            request.setPostEntity(new KssInputStreamEntity(input, len));

            KscHttpResponse resp = mTransmitter.execute(request,
                    KscHttpTransmitter.TYPE_KSS_TRANSMISSION);
            OAuthApiExecutor.throwError(resp);

            int statusCode = resp.getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                ServerException e = new ServerException(statusCode, resp.dump());
                Log.w(LOG_TAG, "Exception in doUpload", e);
                throw e;
            }

            dataMap = ApiDataHelper.contentToMap(resp);
            return new UploadChunkInfo(dataMap);
        } finally {
            if (dataMap != null && dataMap instanceof IObtainable) {
                ((IObtainable) dataMap).recycle();
            }
        }
    }

    public synchronized static int getCRC(InputStream in, final long len)
            throws IOException {
        sCRC32.reset();
        int size = 0;
        long leftSize = len;
        while (leftSize > 0
                && (size = in.read(CRC_BUF, 0,
                        (int) Math.min(CRC_BUF.length, leftSize))) >= 0) {
            leftSize -= size;
            sCRC32.update(CRC_BUF, 0, size);
        }

        return (int) sCRC32.getValue();
    }

    private static void updatePos(UploadChunkInfo result, long pos, long len,
            long blockSize) {
        if (result == null) {
            return;
        }
        if (result.isComplete()) {
            result.next_pos = blockSize;
            result.left_bytes = 0;
        } else if (result.isContinue()) {
            long nextPos = pos + len;
            long nextLen = blockSize - nextPos;
            if (result.next_pos != nextPos || result.left_bytes != nextLen) {
                Log.w(LOG_TAG, "Chunk pos is (" + result.next_pos + ", "
                        + result.left_bytes + "), but in process is ("
                        + nextPos + ", " + nextLen + ")");
                result.next_pos = nextPos;
                result.left_bytes = nextLen;
            }
        } else {
            result.next_pos = pos;
            result.left_bytes = blockSize - pos;
        }
    }

    public void resetChunkSize() {
        mChunkSize = MIN_CHUNKSIZE;
    }
}
