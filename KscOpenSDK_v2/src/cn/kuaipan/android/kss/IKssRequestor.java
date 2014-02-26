
package cn.kuaipan.android.kss;

import cn.kuaipan.android.kss.download.DownloadRequest;
import cn.kuaipan.android.kss.download.FileInfo;
import cn.kuaipan.android.kss.upload.UploadFileInfo;
import cn.kuaipan.android.kss.upload.UploadRequest;

public interface IKssRequestor {
    public static interface IKssUploadRequestResult {
        String getStub();

        UploadRequest getRequest();

        String getMsg();
    }

    public static interface IKssDownloadRequestResult {
        FileInfo getFileInfo();

        DownloadRequest getRequest();

        String getMsg();
    }

    String getUserToken();

    IKssUploadRequestResult requestUpload(UploadFileInfo fileInfo,
            long lastModified, String remotePath) throws Exception;

    void commitUpload(String stub, String commit) throws Exception;

    IKssDownloadRequestResult requestDownload(String remotePath, int rev)
            throws Exception;
}
