
package cn.kuaipan.android.kss;

import java.io.File;

import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.kss.upload.UploadFileInfo;

public interface KssUploader {
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
    KssUploadInfo upload(File localFile, String remotePath,
            IKscTransferListener listener, int taskHash, UploadFileInfo fileInfo)
            throws Exception;
}
