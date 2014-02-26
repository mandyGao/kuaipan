
package cn.kuaipan.android.kss;

import java.io.File;

import cn.kuaipan.android.http.IKscTransferListener;

public interface KssDownloader {

    File download(String remotePath, int rev, File savePath, boolean append,
            IKscTransferListener listener) throws Exception;

    void clean(File savePath);

}
