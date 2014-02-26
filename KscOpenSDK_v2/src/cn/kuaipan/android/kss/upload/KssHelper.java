
package cn.kuaipan.android.kss.upload;

import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.utils.Encode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class KssHelper implements KssDef {
    // private static final String LOG_TAG = "KssHelper";

    private KssHelper() {
    }

    @SuppressWarnings("resource")
    public static UploadFileInfo getUploadFileInfo(File file)
            throws KscException, KscRuntimeException, InterruptedException {
        UploadFileInfo info;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            info = new UploadFileInfo();

            MessageDigest fileSha1 = MessageDigest.getInstance("SHA1");
            MessageDigest blockSha1 = MessageDigest.getInstance("SHA1");
            MessageDigest blockMd5 = MessageDigest.getInstance("MD5");

            byte buf[] = new byte[8 * 1024];
            int len;
            long pos = 0;
            int blockIndex = 1;
            while ((len = in.read(buf)) >= 0) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                pos += len;
                fileSha1.update(buf, 0, len);

                if (pos < blockIndex * BLOCKSIZE) {
                    blockSha1.update(buf, 0, len);
                    blockMd5.update(buf, 0, len);
                } else {
                    int blockOffset = len
                            - (int) (pos - blockIndex * BLOCKSIZE);
                    blockIndex++;

                    blockSha1.update(buf, 0, blockOffset);
                    blockMd5.update(buf, 0, blockOffset);

                    info.appendBlock(
                            Encode.byteArrayToHexString(blockSha1.digest()),
                            Encode.byteArrayToHexString(blockMd5.digest()),
                            BLOCKSIZE);

                    if (len > blockOffset) {
                        blockSha1.update(buf, blockOffset, len - blockOffset);
                        blockMd5.update(buf, blockOffset, len - blockOffset);
                    }
                }
            }

            if ((blockIndex * BLOCKSIZE > pos && blockIndex * BLOCKSIZE < (pos + BLOCKSIZE))
                    || pos == 0) {
                info.appendBlock(
                        Encode.byteArrayToHexString(blockSha1.digest()),
                        Encode.byteArrayToHexString(blockMd5.digest()), pos
                                - (BLOCKSIZE * (blockIndex - 1)));
            }

            info.setSha1(Encode.byteArrayToHexString(fileSha1.digest()));
            return info;
        } catch (NoSuchAlgorithmException e) {
            throw new KscRuntimeException(ErrorCode.FRAMEWORK_UNSUPPORT, e);
        } catch (IOException e) {
            throw KscException.newInstance(e, "Failed on build UploadInfo");
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
                // ignore
            }
        }
    }
}
