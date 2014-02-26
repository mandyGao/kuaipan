package cn.kuaipan.android.sdk;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.sdk.internal.ApiConfig;
import cn.kuaipan.android.sdk.internal.Constants;
import cn.kuaipan.android.sdk.internal.OAuthApiExecutor;
import cn.kuaipan.android.sdk.model.CommonData;
import cn.kuaipan.android.sdk.model.IKscData;
import cn.kuaipan.android.sdk.model.KuaipanFile;
import cn.kuaipan.android.sdk.model.KuaipanUser;
import cn.kuaipan.android.sdk.model.RequestTokenResult;
import cn.kuaipan.android.sdk.model.ResultMsg;
import cn.kuaipan.android.sdk.oauth.AccessToken;
import cn.kuaipan.android.sdk.oauth.OAuthSession;
import cn.kuaipan.android.sdk.oauth.OAuthSession.SignType;
import cn.kuaipan.android.sdk.oauth.Session;
import cn.kuaipan.android.sdk.oauth.Token;

public class PublicApi extends Constants {
  private static final String LOG_TAG = "PublicApi";

  public static interface AuthListener {
    void onUserTokenChanged(AccessToken token);

    void onUserTokenInvalid(Token token);
  }

  public static enum SortType {
    name, time, size
  }

  public static enum DocType {
    PDF, DOC, WPS, CSV, PRN, XLS, ET, PPT, DPS, TXT, RTF;

    public String toString() {
      return super.toString().toLowerCase();
    }

    public static DocType parser(String type) {
      return type == null ? null : valueOf(type.toUpperCase());
    }
  }

  protected static final int CLIENT_RESEND = KscHttpTransmitter.TYPE_UNKEEPALIVE_TRYRESEND;
  protected static final int CLIENT_NO_RESEND = KscHttpTransmitter.TYPE_UNKEEPALIVE;
  protected static final int CLIENT_PUSH = KscHttpTransmitter.TYPE_KEEPALIVE;

  private static final int PAGE_SIZE = 200;

  private static final SparseArray<ApiConfig> CONFIGS;
  private static final int API_REQUEST_TOKEN = 1;
  private static final int API_ACCESS_TOKEN = 2;
  private static final int API_REFRESH_TOKEN = 3;

  private static final int API_USERINFO = 4;

  private static final int API_METADATA = 5;
  private static final int API_COPY = 6;
  private static final int API_MOVE = 7;
  private static final int API_DELETE = 8;
  private static final int API_MKDIRS = 9;

  private static final int API_MK_PUBLINK = 10;
  private static final int API_MK_REF = 11;
  private static final int API_FILE_HISTORY = 12;

  private static final int API_THUMB = 13;
  private static final int API_DOCUMENT = 14;

  private static final int API_UPLOAD_LOCALE = 15;
  private static final int API_UPLOAD_FILE = 16;
  private static final int API_DOWNLOAD_FILE = 17;

  private final OAuthApiExecutor mExecutor;

  protected AuthListener mListener;

  public PublicApi(Context context, Session session) {
    mExecutor = new OAuthApiExecutor(new KscHttpTransmitter(context),
        session);
  }

  public String requestToken() throws KscException, KscRuntimeException,
      InterruptedException {
    RequestTokenResult result = requestToken(false);
    return AUTH_URL + result.key;
  }

  public RequestTokenResult requestToken(boolean returnNotifyUrl)
      throws KscException, KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = null;
    if (returnNotifyUrl) {
      params = new HashMap<String, Object>();
      params.put(PARAM_RETURN_NOTIFY, "1");
    }

    RequestTokenResult result = execute(CONFIGS.get(API_REQUEST_TOKEN),
        null, params, RequestTokenResult.class);

    String key = result.key;
    String secret = result.secret;

    mExecutor.setTempToken(key, secret);

    return result;
  }

  public long accessToken() throws KscException, KscRuntimeException,
      InterruptedException {
    CommonData result = execute(CONFIGS.get(API_ACCESS_TOKEN), null, null,
        CommonData.class);

    String key = result.getString(CommonData.OAUTH_TOKEN_KEY);
    String secret = result.getString(CommonData.OAUTH_TOKEN_SECRET);

    setAuthToken(key, secret);
    return result.getLong(CommonData.USER_ID);
  }

  public void refreshToken() throws KscException, KscRuntimeException,
      InterruptedException {
    Session session = mExecutor.getSession();
    session.assertAuth();
    String userKey = session.getUserToken().getKey();

    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_EXPIRED_TOKEN, userKey);

    CommonData result = execute(CONFIGS.get(API_REFRESH_TOKEN), null,
        params, CommonData.class);

    String key = result.getString(CommonData.OAUTH_TOKEN_KEY);
    String secret = result.getString(CommonData.OAUTH_TOKEN_SECRET);

    AccessToken token = setAuthToken(key, secret);
    if (mListener != null) {
      mListener.onUserTokenChanged(token);
    }
  }

  public KuaipanUser getUserInfo() throws KscException, KscRuntimeException,
      InterruptedException {
    return execute(CONFIGS.get(API_USERINFO), null, null, KuaipanUser.class);
  }

  public KuaipanFile metadata(String path) throws KscException,
      KscRuntimeException, InterruptedException {
    KuaipanFile result = null;

    int pageIndex = 1;
    while (true) {
      KuaipanFile tmpFile = metadata(path, pageIndex, PAGE_SIZE);
      if (pageIndex == 1) {
        result = tmpFile;
      } else {
        result.addChildren(tmpFile.getChildren());
      }
      if (tmpFile.file_count <= (pageIndex * PAGE_SIZE)) {
        break;
      }
      pageIndex++;
    }
    return result;
  }

  public KuaipanFile metadata(String path, int page, int pageSize)
      throws KscException, KscRuntimeException, InterruptedException {
    return metadata(path, true, -1, page, pageSize, null, null);
  }

  public KuaipanFile metadata(String path, boolean listChild, int childLimit,
      int page, int pageSize, String filterExts, SortType sortType)
      throws KscException, KscRuntimeException, InterruptedException {
    String appendPath = adjustPath(getRoot() + "/" + path);

    HashMap<String, Object> params = new HashMap<String, Object>();

    params.put(PARAM_LIST, String.valueOf(listChild));
    if (listChild) {
      if (childLimit > 0) {
        params.put(PARAM_FILE_LIMIT, String.valueOf(childLimit));
      }
      if (page > 0) {
        params.put(PARAM_PAGE, String.valueOf(page));
        if (pageSize > 0) {
          params.put(PARAM_PAGE_SIZE, String.valueOf(pageSize));
        }
      }

      if (!TextUtils.isEmpty(filterExts)) {
        params.put(PARAM_FILTER_EXT, filterExts);
      }
      if (sortType != null) {
        params.put(PARAM_SORT_BY, String.valueOf(sortType));
      }
    }

    return execute(CONFIGS.get(API_METADATA), appendPath, params,
        KuaipanFile.class);
  }

  public void copy(String src, String dest) throws KscException,
      KscRuntimeException, InterruptedException {
    copy(src, dest, null);
  }

  public void copyRef(String ref, String dest) throws KscException,
      KscRuntimeException, InterruptedException {
    copy(null, dest, ref);
  }

  private void copy(String from, String to, String ref) throws KscException,
      KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    if (!TextUtils.isEmpty(ref)) {
      params.put(PARAM_FROM_REF, String.valueOf(ref));
    } else {
      params.put(PARAM_FROM, adjustPath(from));
    }
    params.put(PARAM_TO, adjustPath(to));

    execute(CONFIGS.get(API_COPY), null, params, CommonData.class);
  }

  public void move(String src, String dest) throws KscException,
      KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_FROM, adjustPath(src));
    params.put(PARAM_TO, adjustPath(dest));

    ApiConfig config = CONFIGS.get(API_MOVE);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  public void delete(String path, boolean toRecycle) throws KscException,
      KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, adjustPath(path));
    params.put(PARAM_TO_RECYCLE, String.valueOf(toRecycle));

    ApiConfig config = CONFIGS.get(API_DELETE);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  public void mkdirs(String path) throws KscException, KscRuntimeException,
      InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, adjustPath(path));

    ApiConfig config = CONFIGS.get(API_MKDIRS);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  public String makePublishLink(String path, String name, String accessCode)
      throws KscException, KscRuntimeException, InterruptedException {
    String appendPath = adjustPath(getRoot() + "/" + path);

    HashMap<String, Object> params = new HashMap<String, Object>();
    if (!TextUtils.isEmpty(name)) {
      params.put(PARAM_NAME, name);
    }
    if (!TextUtils.isEmpty(accessCode)) {
      params.put(PARAM_ACCESS_CODE, accessCode);
    }

    ApiConfig config = CONFIGS.get(API_MK_PUBLINK);
    CommonData result = execute(config, appendPath, params,
        CommonData.class);
    String url = result.getString(CommonData.URL);
    if (TextUtils.isEmpty(url)) {
      KscException e = new KscException(ErrorCode.DATA_TYPE_INVALID,
          "url is not String");
      Log.w(LOG_TAG, "Url from server is not a String.", e);
      throw e;
    }
    return url;
  }

  public String makeReference(String path) throws KscException,
      KscRuntimeException, InterruptedException {
    String appendPath = adjustPath(getRoot() + "/" + path);

    ApiConfig config = CONFIGS.get(API_MK_REF);
    CommonData result = execute(config, appendPath, null, CommonData.class);
    String ref = result.getString(CommonData.COPY_REF);

    if (TextUtils.isEmpty(ref)) {
      KscException e = new KscException(ErrorCode.DATA_TYPE_INVALID,
          "copy_ref is not String");
      Log.w(LOG_TAG, "copy_ref from server is not a String.", e);
      throw e;
    }
    return ref;
  }

  @SuppressWarnings("unused")
  private void fileHistory(String path) throws KscException,
      KscRuntimeException, InterruptedException {
    String appendPath = adjustPath(getRoot() + "/" + path);

    // FIXME unsure result, most of "file not exist"
    CommonData result = execute(CONFIGS.get(API_FILE_HISTORY), appendPath,
        null, CommonData.class);
  }

  public void thumb(String path, String savePath, int width, int height,
      IKscTransferListener listener) throws KscException,
      KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, adjustPath(path));
    if (width > 0 && height > 0) {
      params.put(PARAM_WIDTH, String.valueOf(width));
      params.put(PARAM_HEIGHT, String.valueOf(height));
    }

    execute(CONFIGS.get(API_THUMB), null, params, savePath, listener, false);
  }

  public void documentView(String path, DocType type, boolean toZip,
      String savePath, IKscTransferListener listener)
      throws KscException, KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, adjustPath(path));
    params.put(PARAM_TYPE, String.valueOf(type));
    params.put(PARAM_VIEW_TYPE, VALUE_VIEW_TYPE);
    params.put(PARAM_ZIP, toZip ? "1" : "0");

    execute(CONFIGS.get(API_DOCUMENT), null, params, savePath, listener,
        false);
  }

  private String getUploadHost() throws KscRuntimeException, KscException,
      InterruptedException {
    ApiConfig config = CONFIGS.get(API_UPLOAD_LOCALE);
    CommonData result = execute(config, null, null, CommonData.class);
    String url = result.getString(CommonData.URL);

    if (TextUtils.isEmpty(url)) {
      KscException e = new KscException(ErrorCode.DATA_TYPE_INVALID,
          "url is not String");
      Log.w(LOG_TAG, "Url from server is not a String.", e);
      throw e;
    }
    return url;
  }

  public KuaipanFile upload(File localFile, String remotePath,
      IKscTransferListener listener) throws KscRuntimeException,
      KscException, InterruptedException {
    KuaipanFile result = null;
    String uri = getUploadHost() + PATH_UPLOAD;
    ApiConfig config = CONFIGS.get(API_UPLOAD_FILE);
    synchronized (config) {
      config.setCustomUri(uri);

      HashMap<String, Object> params = new HashMap<String, Object>();
      params.put(PARAM_ROOT, getRoot());
      params.put(PARAM_PATH, adjustPath(remotePath));
      params.put(PARAM_OVERWRITE, VALUE_OVERWRITE);
      params.put(PARAM_FILE, localFile);

      result = execute(config, null, params, listener, KuaipanFile.class);
      config.setCustomUri(null);
    }
    return result;
  }

  public void download(String remotePath, String savePath, String rev,
      boolean append, IKscTransferListener listener)
      throws KscRuntimeException, KscException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, adjustPath(remotePath));
    params.put(PARAM_REV, TextUtils.isEmpty(rev) ? "0" : rev);

    execute(CONFIGS.get(API_DOWNLOAD_FILE), null, params, savePath,
        listener, append);
  }

  protected String getRoot() {
    return mExecutor.getRoot();
  }

  public OAuthSession getSession() {
    return mExecutor.getSession();
  }

  public void setAuthListener(AuthListener listener) {
    mListener = listener;
  }

  protected AccessToken setAuthToken(String key, String secret) {
    return mExecutor.setAuthToken(key, secret);
  }

  private <T extends IKscData> T execute(ApiConfig config, String appendPath,
      Map<String, Object> params, Class<T> resultClass)
      throws KscException, KscRuntimeException, InterruptedException {
    return execute(config, appendPath, params, null, resultClass);
  }

  public <T extends IKscData> T execute(ApiConfig config, String appendPath,
      Map<String, ? extends Object> params,
      IKscTransferListener listener, Class<T> resultClass)
      throws KscException, KscRuntimeException, InterruptedException {
    try {
      return mExecutor.exec(config, appendPath, params, listener,
          resultClass);
    } catch (KscException e) {
      if (mListener == null) {
        throw e;
      }

      int errCode = e.getErrorCode();
      if (errCode == ErrorCode.MSG401_AUTH_FAILED) {
        Session session = getSession();
        mListener.onUserTokenInvalid(session == null ? null : session
            .getUserToken());
      } else if (errCode == ErrorCode.MSG401_AUTH_EXPIRED
          && !TextUtils.equals(config.apiName, "refreshToken")) {
        try {
          refreshToken();
        } catch (InterruptedException t) {
          throw t;
        } catch (Throwable t) {
          throw e;
        }

        return mExecutor.exec(config, appendPath, params, listener,
            resultClass);
      }
      throw e;
    }
  }

  private void execute(ApiConfig config, String appendPath,
      HashMap<String, Object> params, String savePath,
      IKscTransferListener listener, boolean appendMode)
      throws KscRuntimeException, KscException, InterruptedException {
    try {
      mExecutor.exec(config, appendPath, params, savePath, listener,
          appendMode);
    } catch (KscException e) {
      if (mListener == null) {
        throw e;
      }

      int errCode = e.getErrorCode();
      if (errCode == ErrorCode.MSG401_AUTH_FAILED) {
        Session session = getSession();
        mListener.onUserTokenInvalid(session == null ? null : session
            .getUserToken());
      } else if (errCode == ErrorCode.MSG401_AUTH_EXPIRED
          && !TextUtils.equals(config.apiName, "refreshToken")) {
        try {
          refreshToken();
        } catch (InterruptedException t) {
          throw t;
        } catch (Throwable t) {
          throw e;
        }
        mExecutor.exec(config, appendPath, params, savePath, listener,
            appendMode);
      } else {
        throw e;
      }
    }
  }

  protected Uri getSignedUri(ApiConfig config, String appendPath,
      Map<String, Object> params) {
    return mExecutor.getSignedUri(config, appendPath, params);
  }

  protected KscHttpTransmitter getTransmitter() {
    return mExecutor.getTransmitter();
  }

  public static void verifyMsg(String apiName, String msg, boolean canIgnore)
      throws KscException {
    if (!ResultMsg.MSG_OK.equalsIgnoreCase(msg)
        && (!canIgnore || !ResultMsg.MSG_IGNORE.equalsIgnoreCase(msg))) {
      KscException e = new ServerMsgException(200, msg,
          "msg is not \"ok\"" + (canIgnore ? " or \"ignore\"" : "")
              + " but statusCode is 200. msg=" + msg);

      Log.w(LOG_TAG, "Verify msg info failed.", e);
      throw e;
    }
  }

  protected static String adjustPath(String path) {
    if (path == null) {
      return "/";
    }
    return new File("/" + path).getAbsolutePath();
  }

  static {
    CONFIGS = new SparseArray<ApiConfig>();
    CONFIGS.append(API_REQUEST_TOKEN, new ApiConfig("requestToken",
        HttpMethod.GET, URI_REQUEST_TOKEN, SignType.CONSUMER,
        CLIENT_RESEND).setQuerys(false, PARAM_RETURN_NOTIFY));

    CONFIGS.append(API_ACCESS_TOKEN, new ApiConfig("accessToken",
        HttpMethod.GET, URI_ACCESS_TOKEN, SignType.AUTO, CLIENT_RESEND)
        .setRequires(CommonData.OAUTH_TOKEN_KEY,
            CommonData.OAUTH_TOKEN_SECRET, CommonData.USER_ID));

    CONFIGS.append(
        API_REFRESH_TOKEN,
        new ApiConfig("refreshToken", HttpMethod.GET,
            URI_REFRESH_TOKEN, SignType.CONSUMER, CLIENT_RESEND)
            .setQuerys(true, PARAM_EXPIRED_TOKEN).setRequires(
                CommonData.OAUTH_TOKEN_KEY,
                CommonData.OAUTH_TOKEN_SECRET));

    CONFIGS.append(API_USERINFO, new ApiConfig("userInfo", HttpMethod.GET,
        URI_USERINFO, SignType.USER, CLIENT_RESEND));

    CONFIGS.append(API_METADATA, new ApiConfig("metadata", HttpMethod.GET,
        URI_METADATA, SignType.USER, CLIENT_RESEND).setQuerys(false,
        PARAM_LIST, PARAM_FILE_LIMIT, PARAM_PAGE, PARAM_PAGE_SIZE,
        PARAM_FILTER_EXT, PARAM_SORT_BY));

    CONFIGS.append(
        API_COPY,
        new ApiConfig("copy", HttpMethod.GET, URI_COPY, SignType.USER,
            CLIENT_NO_RESEND).setQuerys(false, PARAM_ROOT,
            PARAM_FROM, PARAM_TO, PARAM_FROM_REF).setRequires(
            CommonData.FILE_ID));
    CONFIGS.append(
        API_MOVE,
        new ApiConfig("move", HttpMethod.GET, URI_MOVE, SignType.USER,
            CLIENT_NO_RESEND).setQuerys(true, PARAM_ROOT,
            PARAM_FROM, PARAM_TO).setRequires(CommonData.MSG));
    CONFIGS.append(
        API_DELETE,
        new ApiConfig("delete", HttpMethod.GET, URI_DELETE,
            SignType.USER, CLIENT_NO_RESEND).setQuerys(true,
            PARAM_ROOT, PARAM_PATH, PARAM_TO_RECYCLE).setRequires(
            CommonData.MSG));
    CONFIGS.append(
        API_MKDIRS,
        new ApiConfig("mkdirs", HttpMethod.GET, URI_MKDIRS,
            SignType.USER, CLIENT_RESEND).setQuerys(true,
            PARAM_ROOT, PARAM_PATH).setRequires(CommonData.MSG));

    CONFIGS.append(
        API_MK_PUBLINK,
        new ApiConfig("pubLink", HttpMethod.GET, URI_MK_PUBLINK,
            SignType.USER, CLIENT_RESEND).setQuerys(false,
            PARAM_NAME, PARAM_ACCESS_CODE).setRequires(
            CommonData.URL));
    CONFIGS.append(API_MK_REF, new ApiConfig("mkRef", HttpMethod.GET,
        URI_MK_COPYREF, SignType.USER, CLIENT_RESEND)
        .setRequires(CommonData.COPY_REF));
    CONFIGS.append(API_FILE_HISTORY, new ApiConfig("history",
        HttpMethod.GET, URI_FILE_HISTORY, SignType.USER, CLIENT_RESEND)
        .setRequires(CommonData.COPY_REF));

    CONFIGS.append(API_THUMB, new ApiConfig("thumb", HttpMethod.GET,
        URI_THUMB, SignType.USER, CLIENT_RESEND).setQuerys(false,
        PARAM_ROOT, PARAM_PATH, PARAM_WIDTH, PARAM_HEIGHT));
    CONFIGS.append(API_DOCUMENT, new ApiConfig("document", HttpMethod.GET,
        URI_DOCUMENT, SignType.USER, CLIENT_RESEND).setQuerys(true,
        PARAM_ROOT, PARAM_PATH, PARAM_TYPE, PARAM_VIEW_TYPE, PARAM_ZIP));

    CONFIGS.append(API_UPLOAD_LOCALE,
        new ApiConfig("uploadLocale", HttpMethod.GET,
            URI_UPLOAD_LOCATE, SignType.USER, CLIENT_RESEND)
            .setRequires(CommonData.URL));
    CONFIGS.append(API_UPLOAD_FILE, new ApiConfig("upload",
        HttpMethod.POST, URI_UPLOAD_FILE, SignType.USER, CLIENT_RESEND)
        .setQuerys(true, PARAM_ROOT, PARAM_PATH, PARAM_OVERWRITE)
        .setPosts(true, PARAM_FILE));
    CONFIGS.append(API_DOWNLOAD_FILE,
        new ApiConfig("download", HttpMethod.GET, URI_DOWNLOAD_FILE,
            SignType.USER, CLIENT_RESEND).setQuerys(true,
            PARAM_ROOT, PARAM_PATH, PARAM_REV));
  }
}
