package cn.kuaipan.android.kss;

import android.text.TextUtils;
import cn.kuaipan.android.kss.upload.UploadFileInfo;
import cn.kuaipan.android.kss.upload.UploadRequest;

public class KssUploadInfo {
  public final UploadFileInfo fileInfo;
  public final String stub;
  public final UploadRequest request;

  public KssUploadInfo(UploadFileInfo fileInfo, String stub,
      UploadRequest request) {
    super();
    if (fileInfo == null || request == null || TextUtils.isEmpty(stub)) {
      throw new IllegalArgumentException("One of member is invalid.");
    }

    this.fileInfo = fileInfo;
    this.stub = stub;
    this.request = request;
  }
}
