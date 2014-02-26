package cn.kuaipan.android.sdk.internal;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.http.KscHttpRequest.HttpMethod;
import cn.kuaipan.android.kss.IKssRequestor;
import cn.kuaipan.android.kss.KssMaster;
import cn.kuaipan.android.kss.upload.KssHelper;
import cn.kuaipan.android.kss.upload.UploadFileInfo;
import cn.kuaipan.android.sdk.PublicApi;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.sdk.model.CommonData;
import cn.kuaipan.android.sdk.model.FileArray;
import cn.kuaipan.android.sdk.model.FileSyncInfo;
import cn.kuaipan.android.sdk.model.IKscData;
import cn.kuaipan.android.sdk.model.KssDownloadRequestResult;
import cn.kuaipan.android.sdk.model.KssUploadRequestResult;
import cn.kuaipan.android.sdk.model.ResultMsg;
import cn.kuaipan.android.sdk.model.SessionArray;
import cn.kuaipan.android.sdk.model.ShareInfo;
import cn.kuaipan.android.sdk.model.ShareToMap;
import cn.kuaipan.android.sdk.model.SignInfo;
import cn.kuaipan.android.sdk.model.TokenArray;
import cn.kuaipan.android.sdk.oauth.AccessToken;
import cn.kuaipan.android.sdk.oauth.OAuthSession.SignType;
import cn.kuaipan.android.sdk.oauth.Session;
import cn.kuaipan.android.utils.OAuthTimeUtils;

public class OAuthApi extends PublicApi implements IKssRequestor {
  private static final String LOG_TAG = "OAuthApi";

  public static final int SHARE_SUCCES = 1;
  public static final int SHARE_IGNORE = 0;
  public static final int SHARE_REFUSED = -1;

  private static final int API_REGISTER = 1;
  private static final int API_LOGIN = 2;
  private static final int API_AUTHORIZE_TEMP_TOKEN = 3;
  private static final int API_RELOGIN = 4;
  private static final int API_CHECK_IN = 5;

  private static final int API_FOLDER_FILTER = 6;
  private static final int API_SYNC_FILE = 7;
  private static final int API_SYNC_SHARE = 8;

  private static final int API_SHARE_FROM_INFO = 9;
  private static final int API_SHARE_TO_INFO = 10;
  private static final int API_SHARE_TO = 11;

  private static final int API_KSS_UPLOAD_REQUEST = 16;
  private static final int API_KSS_UPLOAD_COMMIT = 17;
  private static final int API_KSS_DOWNLOAD_REQUEST = 18;

  // private static final int API_CREATE_SHAREREF = 18;
  // private static final int API_QUERY_SHAREREF = 19;

  private static final int API_PUSH_SERVER = 20;

  private static final int API_MOBILEREG = 21;
  private static final int API_MOBILEREG_REQUEST = 22;
  private static final int API_BINDMOBILE = 23;
  private static final int API_BINDMOBILE_REQUEST = 24;

  // For Web
  private static final int API_GET_WEBTOKEN = 25;

  // For skyworth
  private static final int API_DOWNLOAD_LINK = 30;
  private static final int API_BIND_ACCOUNT = 31;
  private static final int API_RECTENT_FILE = 32;

  private static final int API_ALL_OPENAPI_SESSIONS = 33;
  private static final int API_ALL_API_SESSIONS = 34;
  private static final int API_DELETE_OPENAPI_SESSION = 35;
  private static final int API_DELETE_API_SESSION = 36;

  private static final SparseArray<ApiConfig> CONFIGS;

  public static enum ShareRight {
    read, write, cancel
  }

  public final Context mContext;
  private KssMaster mKss;

  public OAuthApi(Context context, Session session) {
    super(context, session);
    mContext = context;
  }

  public long login(String userName, String password) throws KscException,
      KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_XAUTH_MODE, Constants.VALUE_XAUTH_MODE);
    params.put(Constants.PARAM_XAUTH_USERNAME, userName);
    params.put(Constants.PARAM_XAUTH_PASSWORD, password);

    CommonData result = execute(CONFIGS.get(API_LOGIN), null, params,
        CommonData.class);

    String key = result.getString(CommonData.OAUTH_TOKEN_KEY);
    String secret = result.getString(CommonData.OAUTH_TOKEN_SECRET);

    setAuthToken(key, secret);
    return result.getLong(CommonData.USER_ID);
  }

  public interface I3PartCallback {
    void doCallback(PublicApi api, String accessToken, String id,
        String kscUserToken, String requestResult) throws KscException,
        KscRuntimeException, InterruptedException;
  }

  public long loginBy3Part(String accessToken, String id,
      I3PartCallback callback) throws KscException, KscRuntimeException,
      InterruptedException {
    String requestResult = requestToken();

    String kscUserToken = getSession().getUserToken().getKey();
    callback.doCallback(this, accessToken, id, kscUserToken, requestResult);

    return accessToken();
  }

  public long register(String userName, String password) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_USERNAME, userName);
    params.put(Constants.PARAM_PASSWORD, password);

    CommonData result = execute(CONFIGS.get(API_REGISTER), null, params,
        CommonData.class);
    return result.getLong(CommonData.USER_ID);
  }

  public void authorizeTempToken(String tempToken, long expiresTime)
      throws KscException, KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_TEMP_TOKEN, tempToken);
    params.put(Constants.PARAM_EXPIRES_IN, String.valueOf(expiresTime));

    ApiConfig config = CONFIGS.get(API_AUTHORIZE_TEMP_TOKEN);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);
    verifyMsg(config.apiName, msg, false);
  }

  public AccessToken relogin(boolean disableOldToken,
      boolean replaceWithNewToken) throws KscException,
      KscRuntimeException, InterruptedException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_DISABLE_TOKEN, disableOldToken ? "1" : "0");

    CommonData result = execute(CONFIGS.get(API_RELOGIN), null, params,
        CommonData.class);

    String key = result.getString(CommonData.OAUTH_TOKEN_KEY);
    String secret = result.getString(CommonData.OAUTH_TOKEN_SECRET);

    AccessToken token = new AccessToken(key, secret);

    if (replaceWithNewToken) {
      token = setAuthToken(key, secret);
      if (mListener != null) {
        mListener.onUserTokenChanged(token);
      }
    }

    return token;
  }

  public List<String> folderFilter(String exts, long minSize)
      throws KscException, KscRuntimeException, InterruptedException {
    minSize = Math.max(0, minSize);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_FILTER_EXT, exts);
    params.put(Constants.PARAM_FILTER_SIZE, String.valueOf(minSize));

    FileArray array = execute(CONFIGS.get(API_FOLDER_FILTER), null, params,
        FileArray.class);

    return array.getList();
  }

  public FileSyncInfo getFileSyncInfo(String cursor) throws KscException,
      KscRuntimeException, InterruptedException {

    Map<String, Object> params = null;
    if (!TextUtils.isEmpty(cursor)) {
      params = new HashMap<String, Object>();
      params.put(Constants.PARAM_CURSOR, cursor);
      params.put(Constants.PARAM_INCLUDE_DEL, Boolean.TRUE.toString());
    }

    return execute(CONFIGS.get(API_SYNC_FILE), null, params,
        FileSyncInfo.class);
  }

  public FileSyncInfo getShareSyncInfo(String cursor) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_CURSOR, cursor);
    params.put(Constants.PARAM_INCLUDE_DEL, Boolean.TRUE.toString());

    return execute(CONFIGS.get(API_SYNC_SHARE), null, params,
        FileSyncInfo.class);
  }

  public ShareInfo getShareFromInfo() throws KscException,
      KscRuntimeException, InterruptedException {
    return execute(CONFIGS.get(API_SHARE_FROM_INFO), null, null,
        ShareInfo.class);
  }

  public ShareToMap getShareToInfo() throws KscException,
      KscRuntimeException, InterruptedException {
    return execute(CONFIGS.get(API_SHARE_TO_INFO), null, null,
        ShareToMap.class);
  }

  public int shareTo(String path, String username, ShareRight right)
      throws KscException, KscRuntimeException, InterruptedException {
    if (right == null) {
      right = ShareRight.write;
    }

    Map<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, path);
    params.put(Constants.PARAM_USERNAME, username);
    params.put(Constants.PARAM_RIGHT, String.valueOf(right));

    ApiConfig config = CONFIGS.get(API_SHARE_TO);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    int res;
    if (ResultMsg.MSG_OK.equalsIgnoreCase(msg)) {
      res = SHARE_SUCCES;
    } else if (ResultMsg.MSG_IGNORE.equalsIgnoreCase(msg)) {
      res = SHARE_IGNORE;
    } else if (ResultMsg.MSG_SHARED.equalsIgnoreCase(msg)) {
      res = SHARE_REFUSED;
    } else {
      KscException e = new ServerMsgException(200, msg,
          "msg is not \"ok\", \"ignore\", or \"shared\""
              + ", but statusCode is 200. msg=" + msg);

      Log.w(LOG_TAG, "Verify msg info failed.", e);
      throw e;
    }

    return res;
  }

  public SignInfo checkIn() throws KscException, KscRuntimeException,
      InterruptedException {
    return execute(CONFIGS.get(API_CHECK_IN), null, null, SignInfo.class);
  }

  public synchronized KssMaster getKssMaster() {
    if (mKss == null) {
      mKss = new KssMaster(mContext, this);
    }
    return mKss;
  }

  public String getUserToken() {
    Session session = getSession();
    session.assertAuth();
    return session.getUserToken().first;
  }

  public void cleanKssDownload(File savePath) {
    getKssMaster().cleanDownload(savePath);
  }

  public File kssDownload(String remotePath, int rev, File savePath,
      boolean append, IKscTransferListener listener) throws KscException,
      KscRuntimeException, InterruptedException {
    Session session = getSession();
    session.assertAuth();

    try {
      return getKssMaster().download(remotePath, rev, savePath, append,
          listener);
    } catch (KscException e) {
      throw e;
    } catch (KscRuntimeException e) {
      throw e;
    } catch (Exception e) {
      KscException e1 = KscException.newException(e, null);
      Log.w(LOG_TAG, e);
      throw e1;
    }
  }

  public void kssUpload(File localFile, String remotePath,
      IKscTransferListener listener) throws KscException,
      KscRuntimeException, InterruptedException {
    Session session = getSession();
    session.assertAuth();

    try {
      getKssMaster().upload(localFile, remotePath, listener);
    } catch (KscException e) {
      throw e;
    } catch (KscRuntimeException e) {
      throw e;
    } catch (Exception e) {
      KscException e1 = KscException.newException(e, null);
      Log.w(LOG_TAG, e);
      throw e1;
    }
  }

  public KssUploadRequestResult requestUpload(File localFile,
      String remotePath) throws KscException, KscRuntimeException {
    try {
      return requestUpload(KssHelper.getUploadFileInfo(localFile),
          localFile.lastModified(), remotePath);
    } catch (KscException e) {
      throw e;
    } catch (KscRuntimeException e) {
      throw e;
    } catch (Exception e) {
      KscException e1 = new KscException(ErrorCode.UNKNOW_ERR_LOCAL_IO, e);
      Log.w(LOG_TAG, "Unknow exception", e);
      throw e1;
    }
  }

  @Override
  public KssUploadRequestResult requestUpload(UploadFileInfo fileInfo,
      long modifyTime, String remotePath) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, remotePath);
    params.put(PARAM_OVERWRITE, VALUE_OVERWRITE);
    params.put(Constants.PARAM_MODIFY, OAuthTimeUtils.toString(modifyTime));

    params.put(Constants.PARAM_SHA1, fileInfo.getSha1());
    params.put(Constants.PARAM_BLOCKS, fileInfo.getKssString());
    return execute(CONFIGS.get(API_KSS_UPLOAD_REQUEST), null, params,
        KssUploadRequestResult.class);
  }

  @Override
  public void commitUpload(String stub, String commit) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_STUB, stub);
    params.put(Constants.PARAM_METAS, commit);

    ApiConfig config = CONFIGS.get(API_KSS_UPLOAD_COMMIT);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  @Override
  public IKssDownloadRequestResult requestDownload(String remotePath, int rev)
      throws KscException, KscRuntimeException, InterruptedException {
    rev = Math.max(0, rev);

    Map<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, remotePath);
    params.put(PARAM_REV, rev);

    return execute(CONFIGS.get(API_KSS_DOWNLOAD_REQUEST), null, params,
        KssDownloadRequestResult.class);
  }

  // public String createShareRef(String path, String accessCode)
  // throws KscException, KscRuntimeException {
  // accessCode = accessCode == null ? "" : accessCode;
  //
  // Map<String, Object> params = new HashMap<String, Object>();
  // params.put(PARAM_ROOT, getRoot());
  // params.put(PARAM_PATH, path);
  // params.put(PARAM_ACCESS_CODE, accessCode);
  //
  // CommonData result = execute(CONFIGS.get(API_CREATE_SHAREREF), null,
  // params, CommonData.class);
  // // XXX
  // return result.getString(CommonData.SHARE_REF);
  // }
  //
  // public CommonData queryShareRef(String path) throws KscException,
  // KscRuntimeException {
  // Map<String, Object> params = new HashMap<String, Object>();
  // params.put(PARAM_ROOT, getRoot());
  // params.put(PARAM_PATH, path);
  //
  // return execute(CONFIGS.get(API_QUERY_SHAREREF), null, params,
  // CommonData.class);
  // }

  // public String getShareRef(String path, String accessCode)
  // throws KscException, KscRuntimeException {
  // accessCode = accessCode == null ? "" : accessCode;
  //
  // CommonData oldRef = queryShareRef(path);
  // if (oldRef != null) {
  // String ref = oldRef.getString(CommonData.SHARE_REF);
  // String code = oldRef.getString(CommonData.ACCESS_CODE);
  // if (!TextUtils.isEmpty(ref) && TextUtils.equals(accessCode, code)) {
  // return ref;
  // }
  // }
  //
  // return createShareRef(path, accessCode);
  // }

  public CommonData getPushServer() throws KscException, KscRuntimeException,
      InterruptedException {
    ApiConfig config = CONFIGS.get(API_PUSH_SERVER);
    CommonData result = execute(config, null, null, CommonData.class);
    String url = result.getString(CommonData.URL);
    String device = result.getString(CommonData.DEVICE);
    if (TextUtils.isEmpty(url) || TextUtils.isEmpty(device)) {
      KscException e = new KscException(ErrorCode.DATA_TYPE_INVALID,
          "Returns url or device is empty when getPushServer");
      Log.w(LOG_TAG, "url:" + url + " device:" + device, e);
      throw e;
    }
    return result;
  }

  public long mobileRegister(String phoneNumber, String password, String code)
      throws KscException, KscRuntimeException, InterruptedException {
    ApiConfig config = CONFIGS.get(API_MOBILEREG);
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_MOBILE, phoneNumber);
    params.put(Constants.PARAM_PASSWORD, password);
    params.put(Constants.PARAM_CODE, code);

    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);
    verifyMsg(config.apiName, msg, false);
    return result.getLong(CommonData.USER_ID);
  }

  public void mobileRegisterRequest(String phoneNumber) throws KscException,
      KscRuntimeException, InterruptedException {
    ApiConfig config = CONFIGS.get(API_MOBILEREG_REQUEST);
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_MOBILE, phoneNumber);

    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);
    verifyMsg(config.apiName, msg, false);
  }

  public void bindMobile(String phoneNumber, String password, String code)
      throws KscException, KscRuntimeException, InterruptedException {
    ApiConfig config = CONFIGS.get(API_BINDMOBILE);
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_MOBILE, phoneNumber);
    params.put(Constants.PARAM_PASSWORD, password);
    params.put(Constants.PARAM_CODE, code);

    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);
    verifyMsg(config.apiName, msg, false);
  }

  public void bindMobileRequest(String phoneNumber) throws KscException,
      KscRuntimeException, InterruptedException {
    ApiConfig config = CONFIGS.get(API_BINDMOBILE_REQUEST);
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_MOBILE, phoneNumber);

    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);
    verifyMsg(config.apiName, msg, false);
  }

  public String getWebToken() throws KscException, KscRuntimeException,
      InterruptedException {
    ApiConfig config = CONFIGS.get(API_GET_WEBTOKEN);
    String token = "android:oauth:" + getUserToken();

    HashMap<String, Object> params = new HashMap<String, Object>();

    params.put(Constants.PARAM_DEVICE, token);
    params.put(Constants.PARAM_SECURE, VALUE_SECURE);

    CommonData result = execute(config, null, params, CommonData.class);
    return result.getString(CommonData.TOKEN);
  }

  public String generateDownloadLink(String remotePath)
      throws KscRuntimeException, KscException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put(PARAM_ROOT, getRoot());
    params.put(PARAM_PATH, adjustPath(remotePath));
    params.put(PARAM_SHOW_FN, "1");
    params.put(PARAM_SNK_IN_GET, "1");
    return getSignedUri(CONFIGS.get(API_DOWNLOAD_LINK), null, params)
        .toString();
  }

  public void bindAccount(String username, String password)
      throws KscException, KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_USER, username);
    params.put(Constants.PARAM_PASSWORD, password);

    ApiConfig config = CONFIGS.get(API_BIND_ACCOUNT);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  public FileSyncInfo getRecentFiles(String cursor, int limit,
      String filterExt, String ignoredPath) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    if (!TextUtils.isEmpty(cursor)) {
      params.put(Constants.PARAM_CURSOR, cursor);
    }
    limit = Math.max(0, Math.min(50, limit));
    if (limit > 0) {
      params.put(Constants.PARAM_LIMIT, String.valueOf(limit));
    }
    if (!TextUtils.isEmpty(filterExt)) {
      params.put(Constants.PARAM_FILTER_EXT, filterExt);
    }
    if (!TextUtils.isEmpty(ignoredPath)) {
      params.put(Constants.PARAM_IGNORED_PATH, ignoredPath);
    }

    return execute(CONFIGS.get(API_RECTENT_FILE), null, params,
        FileSyncInfo.class);
  }

  public TokenArray getAllOpenApiSessions(String cursor, String size)
      throws KscException, KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_CURSOR, cursor);
    params.put(Constants.PARAM_SIZE, size);

    ApiConfig config = CONFIGS.get(API_ALL_OPENAPI_SESSIONS);
    TokenArray result = execute(config, null, params, TokenArray.class);

    return result;
  }

  // queryAllSessionInfo(token = self.user1.token, ctime = '', size = 100)
  public SessionArray getAllApiSessions(String ctime, String size)
      throws KscException, KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_CTIME, ctime);
    params.put(Constants.PARAM_SIZE, size);

    ApiConfig config = CONFIGS.get(API_ALL_API_SESSIONS);
    SessionArray result = execute(config, null, params, SessionArray.class);

    return result;
  }

  public void deleteOpenApiSession(String token) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_DEL_TOKEN, token);

    ApiConfig config = CONFIGS.get(API_DELETE_OPENAPI_SESSION);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  public void deleteApiSession(String sid) throws KscException,
      KscRuntimeException, InterruptedException {
    Map<String, Object> params = new HashMap<String, Object>();
    params.put(Constants.PARAM_SID, sid);

    ApiConfig config = CONFIGS.get(API_DELETE_API_SESSION);
    CommonData result = execute(config, null, params, CommonData.class);
    String msg = result.getString(CommonData.MSG);

    verifyMsg(config.apiName, msg, false);
  }

  private <T extends IKscData> T execute(ApiConfig config, String appendPath,
      Map<String, Object> params, Class<T> resultClass)
      throws KscException, KscRuntimeException, InterruptedException {
    return execute(config, appendPath, params, null, resultClass);
  }

  static {
    CONFIGS = new SparseArray<ApiConfig>();
    CONFIGS.append(
        API_LOGIN,
        new ApiConfig("login", HttpMethod.POST, Constants.URI_LOGIN,
            SignType.CONSUMER, CLIENT_RESEND).setPosts(true,
            Constants.PARAM_XAUTH_MODE,
            Constants.PARAM_XAUTH_USERNAME,
            Constants.PARAM_XAUTH_PASSWORD).setRequires(
            CommonData.OAUTH_TOKEN_KEY,
            CommonData.OAUTH_TOKEN_SECRET, CommonData.USER_ID));
    CONFIGS.append(
        API_REGISTER,
        new ApiConfig("register", HttpMethod.POST,
            Constants.URI_REGISTER, SignType.CONSUMER,
            CLIENT_NO_RESEND).setPosts(true,
            Constants.PARAM_USERNAME, Constants.PARAM_PASSWORD)
            .setRequires(CommonData.USER_ID));
    CONFIGS.append(
        API_AUTHORIZE_TEMP_TOKEN,
        new ApiConfig("authorize_temp_token", HttpMethod.POST,
            Constants.URI_AUTHORIZE_TEMP_TOKEN, SignType.USER,
            CLIENT_NO_RESEND).setPosts(true,
            Constants.PARAM_TEMP_TOKEN, Constants.PARAM_EXPIRES_IN)
            .setRequires(CommonData.MSG));

    CONFIGS.append(
        API_RELOGIN,
        new ApiConfig("relogin", HttpMethod.GET, Constants.URI_RELOGIN,
            SignType.USER, CLIENT_NO_RESEND).setQuerys(true,
            PARAM_DISABLE_TOKEN).setRequires(
            CommonData.OAUTH_TOKEN_KEY,
            CommonData.OAUTH_TOKEN_SECRET));

    CONFIGS.append(API_FOLDER_FILTER, new ApiConfig("folderFilter",
        HttpMethod.GET, Constants.URI_FOLDER_FILTER, SignType.USER,
        CLIENT_RESEND).setQuerys(true, PARAM_FILTER_EXT,
        Constants.PARAM_FILTER_SIZE));

    CONFIGS.append(API_SYNC_FILE, new ApiConfig("syncFile", HttpMethod.GET,
        Constants.URI_SYNC_FILE, SignType.USER, CLIENT_RESEND)
        .setQuerys(false, Constants.PARAM_CURSOR,
            Constants.PARAM_INCLUDE_DEL));
    CONFIGS.append(API_SYNC_SHARE, new ApiConfig("syncShare",
        HttpMethod.GET, Constants.URI_SYNC_SHARE, SignType.USER,
        CLIENT_RESEND).setQuerys(true, Constants.PARAM_CURSOR,
        Constants.PARAM_INCLUDE_DEL));

    CONFIGS.append(API_SHARE_FROM_INFO, new ApiConfig("shareFromInfo",
        HttpMethod.GET, Constants.URI_SHARE_FROM, SignType.USER,
        CLIENT_RESEND));
    CONFIGS.append(API_SHARE_TO_INFO, new ApiConfig("shareToInfo",
        HttpMethod.GET, Constants.URI_SHARE_TO_INFO, SignType.USER,
        CLIENT_RESEND));

    CONFIGS.append(
        API_SHARE_TO,
        new ApiConfig("shareTo", HttpMethod.GET,
            Constants.URI_SHARE_TO, SignType.USER, CLIENT_RESEND)
            .setQuerys(true, PARAM_ROOT, PARAM_PATH,
                Constants.PARAM_USERNAME, Constants.PARAM_RIGHT)
            .setRequires(CommonData.MSG));
    CONFIGS.append(API_CHECK_IN, new ApiConfig("checkIn", HttpMethod.GET,
        Constants.URI_CHECK_IN, SignType.USER, CLIENT_NO_RESEND)
        .setVerifyer(new CheckinVerifier()));

    CONFIGS.append(
        API_KSS_UPLOAD_REQUEST,
        new ApiConfig("kssUploadRequest", HttpMethod.POST,
            Constants.URI_KSS_UPLOAD_REQUEST, SignType.USER,
            CLIENT_RESEND).setPosts(true, PARAM_ROOT, PARAM_PATH,
            Constants.PARAM_MODIFY, Constants.PARAM_SHA1,
            Constants.PARAM_BLOCKS, PARAM_OVERWRITE).setVerifyer(
            new KssReqVerifier(true)));
    CONFIGS.append(
        API_KSS_UPLOAD_COMMIT,
        new ApiConfig("kssUploadCommit", HttpMethod.POST,
            Constants.URI_KSS_UPLOAD_COMMIT, SignType.USER,
            CLIENT_RESEND).setPosts(true, Constants.PARAM_STUB,
            Constants.PARAM_METAS).setRequires(CommonData.MSG));
    CONFIGS.append(
        API_KSS_DOWNLOAD_REQUEST,
        new ApiConfig("kssDownalodRequest", HttpMethod.GET,
            Constants.URI_KSS_DOWNLOAD_REQUEST, SignType.USER,
            CLIENT_RESEND).setQuerys(true, PARAM_ROOT, PARAM_PATH,
            PARAM_REV).setVerifyer(new KssReqVerifier(false)));

    // CONFIGS.append(API_CREATE_SHAREREF, new ApiConfig(HttpMethod.GET,
    // Constants.URI_CREATE_SHAREREF, SignType.USER, CLIENT_RESEND)
    // .setQuerys(true, PARAM_ROOT, PARAM_PATH, PARAM_ACCESS_CODE)
    // .setRequires(CommonData.SHARE_REF));
    // CONFIGS.append(
    // API_QUERY_SHAREREF,
    // new ApiConfig(HttpMethod.GET, Constants.URI_QUERY_SHAREREF,
    // SignType.USER, CLIENT_RESEND).setQuerys(true,
    // PARAM_ROOT, PARAM_PATH).setRequires(
    // CommonData.SHARE_REF, CommonData.ACCESS_CODE));

    CONFIGS.append(API_PUSH_SERVER, new ApiConfig("getPushServer",
        HttpMethod.GET, Constants.URI_PUSH_SERVER, SignType.USER,
        CLIENT_RESEND).setRequires(CommonData.URL, CommonData.DEVICE));
    CONFIGS.append(
        API_MOBILEREG,
        new ApiConfig("mobileRegister", HttpMethod.POST,
            Constants.URI_MOBILEREG, SignType.CONSUMER,
            CLIENT_NO_RESEND).setPosts(true,
            Constants.PARAM_MOBILE, Constants.PARAM_PASSWORD,
            Constants.PARAM_CODE).setRequires(CommonData.MSG,
            CommonData.USER_ID));
    CONFIGS.append(
        API_MOBILEREG_REQUEST,
        new ApiConfig("mobileRegRequest", HttpMethod.GET,
            Constants.URI_MOBILEREG_REQUEST, SignType.CONSUMER,
            CLIENT_NO_RESEND).setQuerys(true,
            Constants.PARAM_MOBILE).setRequires(CommonData.MSG));
    CONFIGS.append(
        API_BINDMOBILE,
        new ApiConfig("bindMobile", HttpMethod.POST,
            Constants.URI_BINDMOBILE, SignType.USER,
            CLIENT_NO_RESEND).setPosts(true,
            Constants.PARAM_MOBILE, Constants.PARAM_PASSWORD,
            Constants.PARAM_CODE).setRequires(CommonData.MSG));
    CONFIGS.append(API_BINDMOBILE_REQUEST, new ApiConfig(
        "bindMobileRequest", HttpMethod.GET,
        Constants.URI_BINDMOBILE_REQUEST, SignType.USER,
        CLIENT_NO_RESEND).setQuerys(true, Constants.PARAM_MOBILE)
        .setRequires(CommonData.MSG));

    CONFIGS.append(
        API_GET_WEBTOKEN,
        new ApiConfig("transformToken", HttpMethod.POST,
            Constants.URI_GET_WEBTOKEN, SignType.USER,
            CLIENT_RESEND).setQuerys(true, Constants.PARAM_DEVICE,
            Constants.PARAM_SECURE).setRequires(CommonData.TOKEN,
            CommonData.EXPIRES));

    CONFIGS.append(API_DOWNLOAD_LINK,
        new ApiConfig("genDownloadLink", HttpMethod.GET,
            URI_DOWNLOAD_FILE, SignType.USER, CLIENT_RESEND)
            .setQuerys(true, PARAM_ROOT, PARAM_SNK_IN_GET,
                PARAM_SHOW_FN, PARAM_PATH));
    CONFIGS.append(
        API_BIND_ACCOUNT,
        new ApiConfig("bindAccount", HttpMethod.POST,
            Constants.URI_BIND_ACCOUNT, SignType.USER,
            CLIENT_NO_RESEND).setPosts(true, Constants.PARAM_USER,
            Constants.PARAM_PASSWORD).setRequires(CommonData.MSG));
    CONFIGS.append(API_RECTENT_FILE, new ApiConfig("recentFile",
        HttpMethod.GET, Constants.URI_RECTENT_FILE, SignType.USER,
        CLIENT_RESEND).setQuerys(false, Constants.PARAM_CURSOR,
        Constants.PARAM_LIMIT, PARAM_FILTER_EXT,
        Constants.PARAM_IGNORED_PATH));

    CONFIGS.append(API_ALL_OPENAPI_SESSIONS, new ApiConfig(
        "queryOpenSessionInfo", HttpMethod.POST,
        Constants.URI_ALL_OPENAPI_SESSIONS, SignType.USER,
        CLIENT_RESEND).setPosts(true, Constants.PARAM_CURSOR,
        Constants.PARAM_SIZE));
    CONFIGS.append(API_DELETE_OPENAPI_SESSION, new ApiConfig(
        "delOpenSessionInfo", HttpMethod.POST,
        Constants.URI_DELETE_OPENAPI_SESSION, SignType.USER,
        CLIENT_RESEND).setPosts(true, Constants.PARAM_DEL_TOKEN)
        .setRequires(CommonData.MSG));
    CONFIGS.append(API_ALL_API_SESSIONS, new ApiConfig(
        "queryApiSessionInfo", HttpMethod.POST,
        Constants.URI_ALL_API_SESSIONS, SignType.USER, CLIENT_RESEND)
        .setPosts(true, Constants.PARAM_CTIME, Constants.PARAM_SIZE));
    CONFIGS.append(API_DELETE_API_SESSION, new ApiConfig(
        "delApiSessionInfo", HttpMethod.POST,
        Constants.URI_DELETE_API_SESSION, SignType.NONE, CLIENT_RESEND)
        .setPosts(true, Constants.PARAM_SID)
        .setRequires(CommonData.MSG));
  }
}
