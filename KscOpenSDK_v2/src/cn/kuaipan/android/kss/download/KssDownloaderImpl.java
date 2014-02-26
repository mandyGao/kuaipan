
package cn.kuaipan.android.kss.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.sky.base.utils.FileUtils;

import android.text.TextUtils;
import android.util.Log;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.http.KscHttpRequest;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.http.KscHttpResponse;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.kss.IKssRequestor;
import cn.kuaipan.android.kss.IKssRequestor.IKssDownloadRequestResult;
import cn.kuaipan.android.kss.KssDef;
import cn.kuaipan.android.kss.KssDownloader;
import cn.kuaipan.android.kss.KssMaster;
import cn.kuaipan.android.kss.RC4Encoder;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.ErrorHelper;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.internal.OAuthApiExecutor;

public class KssDownloaderImpl implements KssDownloader, KssDef {
    private static final String LOG_TAG = "KssDownloaderImpl";

    // private final KssMaster mMaster;
    private final KscHttpTransmitter mTransmitter;
    private final IKssRequestor mRequestor;

    public KssDownloaderImpl(KscHttpTransmitter transmitter, KssMaster master,
            IKssRequestor requestor) {
        mTransmitter = transmitter;
        // mMaster = master;
        mRequestor = requestor;
    }

    @Override
    public void clean(File savePath) {
        File infoFile = KInfo.getInfoFile(savePath);
        infoFile.delete();
        savePath.delete();
    }

    @Override
    public File download(String remotePath, int rev, File savePath,
            boolean append, IKscTransferListener listener) throws Exception {

        IKssDownloadRequestResult requestResult = mRequestor.requestDownload(
                remotePath, rev);
        FileInfo fileInfo = requestResult.getFileInfo();
        DownloadRequest request = requestResult.getRequest();

        KssAccessor accessor = null;
        boolean completed = false;
        KInfo kinfo = null;
        LoadMap map = null;
        try {
            final long size = request.getTotalSize();

            if (savePath.exists()) {
                if (!append || savePath.isDirectory()
                        || savePath.length() > size) {
                    if (!FileUtils.deletes(savePath)) {
                        throw new SecurityException(
                                "Failed delete target file. Can't download to dest path: "
                                        + savePath);
                    }
                }
            } else {
                savePath.getParentFile().mkdirs();
            }

            map = new LoadMap(request, listener);// no free space
            boolean mapLoaded = false;

            File infoFile = KInfo.getInfoFile(savePath);
            kinfo = new KInfo(infoFile);
            if (infoFile.exists()) {
                kinfo.load();
                if (TextUtils.equals(kinfo.getSha1(), fileInfo.sha1)) {
                    mapLoaded = kinfo.loadToMap(map);
                }
            }

            if (!mapLoaded && savePath.exists()) {
                map.initSize(savePath.length());
            }

            accessor = new KssAccessor(savePath);
            map.verify(accessor, false);

            long targetSize = request.getTotalSize();
            if (savePath.length() != targetSize) {
                accessor.inflate(targetSize);
            }

            AtomicInteger retry = new AtomicInteger(RETRY_TIMES);
            while (!(completed = map.isCompleted())) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                try {
                    download(request, accessor, map, retry);
                    retry.set(RETRY_TIMES);
                } catch (Exception e) {
                    if (ErrorHelper.isNetworkException(e)
                            && retry.decrementAndGet() >= 0) {
                        // Network error, wait 5s then retry.
                        Thread.sleep(5 * 1000);
                    } else {
                        throw e;
                    }
                }
            }

            savePath.setLastModified(fileInfo.modifyTime.getTime());

            return savePath;
        } finally {
            if (accessor != null) {
                try {
                    accessor.close();
                } catch (Throwable t) {
                    // ignore;
                }
            }
            if (kinfo != null) {
                if (completed) {
                    kinfo.delete();
                } else if (map != null) {
                    kinfo.setSha1(fileInfo.sha1);
                    kinfo.setLoadMap(map);
                    kinfo.save();
                }
            }
        }
    }

    private void download(DownloadRequest request, KssAccessor accessor,
            LoadMap map, AtomicInteger retry) throws Exception {

        LoadRecorder recorder = map.obtainRecorder();
        while (recorder != null) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            long start = recorder.getSpace().getStart();

            String[] urls = request.getBlockUrls(start);
            long range = start - map.getBlockStart(start);

            if (urls == null || urls.length <= 0) {
                throw new IllegalArgumentException(
                        "No available urls to download.");
            }
            RC4Encoder rc4Decoder = new RC4Encoder(request.getSecureKey());

            for (int i = 0; (i < urls.length); i++) {
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }

                KscHttpRequest req = null;
                KscHttpResponse resp = null;
                boolean interrupted = false;
                try {
                    rc4Decoder.init();
                    // String url = urls[i] + "&snk_in_get=1";
                    String url = urls[i];
                    req = new KscHttpRequest(HttpMethod.GET, url, rc4Decoder,
                            null);
                    if (range > 0) {
                        req.getRequest().addHeader("Range",
                                "bytes=" + range + "-");
                    }
                    resp = mTransmitter.execute(req,
                            KscHttpTransmitter.TYPE_KSS_TRANSMISSION);
                    OAuthApiExecutor.throwError(resp);

                    save(resp, accessor, recorder, retry);

                    map.verify(accessor, true);
                    break;
                } catch (Exception e) {
                    interrupted = true;
                    ErrorHelper.handleInterruptException(e);
                    interrupted = false;
                    if (i >= (urls.length - 1)) {
                        throw e;
                    }
                } finally {
                    if (interrupted && req != null) {
                        req.getRequest().abort();
                    } else {
                        releaseResponse(resp);
                    }
                    if (recorder != null) {
                        recorder.recycle();
                    }
                }
            }

            recorder = map.obtainRecorder();
        }
    }

    private void save(final KscHttpResponse response, KssAccessor accessor,
            LoadRecorder recorder, AtomicInteger retry)
            throws IllegalStateException, IOException {
        InputStream in = null;
        boolean received = false;
        try {
            in = response.getContent();
            if (in == null) {
                KscRuntimeException e = new KscRuntimeException(
                        ErrorCode.DATA_IS_EMPTY, response.dump());
                Log.w(LOG_TAG, "No response, but not meet exception", e);
                throw e;
            }

            int len = 0;
            byte[] buf = new byte[8 * 1024];
            while ((len = in.read(buf)) >= 0) {
                received = true;
                if (len > 0 && accessor.write(buf, 0, len, recorder) < len) {
                    break;
                }
            }
        } finally {
            // not need to release stream
            if (received) {
                retry.set(RETRY_TIMES);
            }
        }
    }

    private void releaseResponse(KscHttpResponse response)
            throws InterruptedIOException {
        if (response != null) {
            try {
                response.release(); // not need to close stream
            } catch (InterruptedIOException e) {
                throw e;
            } catch (Throwable t) {
                // ignore
            }
        }
    }
}
