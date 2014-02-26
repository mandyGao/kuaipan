
package cn.kuaipan.android.sdk.internal;

import org.sky.base.utils.SystemProperties;

public class Constants {
    public static final boolean DEBUG_INTERNAL = SystemProperties.getBoolean(
            "ksc.sdk.internal", false);
    public static final boolean DEBUG = SystemProperties.getBoolean(
            "ksc.sdk.debug", false);

    protected static final String HTTP = "http://";
    protected static final String HTTPS = "https://";
    protected static final String API_HOST;
    protected static final String DATA_HOST;
    protected static final String DFS_HOST;
    protected static final String PATH_PRIV = "/open";
    protected static final String PATH_PUB = "/1";

    public final static String AUTH_URL = "https://www.kuaipan.cn/api.php?ac=open&op=authorise&oauth_token=";

    public static final String URI_REQUEST_TOKEN;
    public static final String URI_ACCESS_TOKEN;
    public static final String URI_REFRESH_TOKEN;

    static final String URI_REGISTER;
    static final String URI_LOGIN;
    static final String URI_RELOGIN;
    static final String URI_AUTHORIZE_TEMP_TOKEN;

    public static final String URI_SERVTIME;
    public static final String URI_USERINFO;

    public static final String URI_METADATA;
    static final String URI_FOLDER_FILTER;
    public static final String URI_COPY;
    public static final String URI_MOVE;
    public static final String URI_DELETE;
    public static final String URI_MKDIRS;

    static final String URI_SHARE_TO_INFO;
    static final String URI_SHARE_TO;
    static final String URI_SHARE_FROM;

    public static final String URI_MK_PUBLINK;
    public static final String URI_MK_COPYREF;
    public static final String URI_FILE_HISTORY;

    public static final String URI_THUMB;
    public static final String URI_DOCUMENT;

    public static final String URI_UPLOAD_LOCATE;
    public static final String URI_UPLOAD_FILE;
    public static final String URI_DOWNLOAD_FILE;

    static final String URI_PUSH_SERVER;
    static final String URI_SYNC_FILE;
    static final String URI_SYNC_SHARE;

    static final String URI_CHECK_IN;

    static final String URI_MOBILEREG;
    static final String URI_MOBILEREG_REQUEST;
    static final String URI_BINDMOBILE;
    static final String URI_BINDMOBILE_REQUEST;

    static final String URI_KSS_UPLOAD_REQUEST;
    static final String URI_KSS_UPLOAD_COMMIT;
    static final String URI_KSS_DOWNLOAD_REQUEST;

    static final String URI_GET_WEBTOKEN;

    static final String URI_ALL_OPENAPI_SESSIONS;
    static final String URI_ALL_API_SESSIONS;
    static final String URI_DELETE_OPENAPI_SESSION;
    static final String URI_DELETE_API_SESSION;

    // static final String URI_CREATE_SHAREREF;
    // static final String URI_QUERY_SHAREREF;

    static final String URI_BIND_ACCOUNT;
    static final String URI_RECTENT_FILE;

    public static final String PARAM_EXPIRED_TOKEN = "expired_token";
    public static final String PARAM_DISABLE_TOKEN = "disableToken";

    static final String PARAM_TEMP_TOKEN = "temp_token";
    static final String PARAM_EXPIRES_IN = "expires_in";
    public static final String PARAM_RETURN_NOTIFY = "return_notify_url";

    public static final String PARAM_LIST = "list";
    public static final String PARAM_FILE_LIMIT = "file_limit";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_FILTER_EXT = "filter_ext";
    static final String PARAM_FILTER_SIZE = "filter_size";
    public static final String PARAM_SORT_BY = "sort_by";

    public static final String PARAM_ROOT = "root";
    public static final String PARAM_PATH = "path";
    public static final String PARAM_FROM = "from_path";
    public static final String PARAM_SNK_IN_GET = "snk_in_get";
    public static final String PARAM_SHOW_FN = "show_fn";
    public static final String PARAM_TO = "to_path";
    public static final String PARAM_FROM_REF = "from_copy_ref";
    public static final String PARAM_TO_RECYCLE = "to_recycle";

    public static final String PARAM_NAME = "name";
    public static final String PARAM_ACCESS_CODE = "access_code";
    public static final String PARAM_WIDTH = "width";
    public static final String PARAM_HEIGHT = "height";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_VIEW_TYPE = "view";
    public static final String PARAM_ZIP = "zip";
    public static final String PARAM_FILE = "file";
    public static final String PARAM_OVERWRITE = "overwrite";
    public static final String PARAM_REV = "rev";

    static final String PARAM_XAUTH_USERNAME = "x_auth_username";
    static final String PARAM_XAUTH_PASSWORD = "x_auth_password";
    static final String PARAM_XAUTH_MODE = "x_auth_mode";

    static final String PARAM_USER = "user"; // $#%$#$%#$ why not username?
    static final String PARAM_USERNAME = "username";
    static final String PARAM_PASSWORD = "password";
    static final String PARAM_MOBILE = "mobile";
    static final String PARAM_CODE = "code";

    static final String PARAM_CURSOR = "cursor";
    static final String PARAM_INCLUDE_DEL = "include_deleted";
    static final String PARAM_RIGHT = "right";
    static final String PARAM_LIMIT = "limit";
    static final String PARAM_IGNORED_PATH = "ignored_path";

    static final String PARAM_SIZE = "size";
    static final String PARAM_TOKEN = "token";
    static final String PARAM_CTIME = "ctime";
    static final String PARAM_SID = "sid";
    static final String PARAM_DISABLED_TOKEN = "disabledToken";
    static final String PARAM_DEL_TOKEN = "del_token";

    static final String PARAM_SHA1 = "sha1";
    static final String PARAM_BLOCKS = "block_infos";
    static final String PARAM_MODIFY = "modify_time";
    static final String PARAM_STUB = "stub";
    static final String PARAM_METAS = "metas";

    static final String PARAM_DEVICE = "device";
    static final String PARAM_SECURE = "secure";

    static final String VALUE_XAUTH_MODE = "client_auth";
    public static final String VALUE_VIEW_TYPE = "android";
    public static final String VALUE_OVERWRITE = "True";
    public static final String VALUE_SECURE = "False";
    public static final String PATH_UPLOAD = PATH_PUB + "/fileops/upload_file";

    static {
        API_HOST = DEBUG_INTERNAL ? "192.168.10.200" : "openapi.kuaipan.cn";
        DATA_HOST = "conv.kuaipan.cn";
        DFS_HOST = "api-content.dfs.kuaipan.cn";

        URI_REQUEST_TOKEN = HTTPS + API_HOST + PATH_PRIV + "/requestToken";
        URI_ACCESS_TOKEN = HTTPS + API_HOST + PATH_PRIV + "/accessToken";
        URI_REFRESH_TOKEN = HTTP + API_HOST + PATH_PRIV + "/refreshToken";

        URI_REGISTER = HTTPS + API_HOST + PATH_PRIV
                + "/registerWithoutVerification";
        URI_LOGIN = HTTPS + API_HOST + PATH_PRIV + "/xAccessToken";
        URI_RELOGIN = HTTPS + API_HOST + PATH_PRIV + "/reLogin";
        URI_AUTHORIZE_TEMP_TOKEN = HTTPS + API_HOST + PATH_PRIV
                + "/authorizeTempToken";

        URI_SERVTIME = HTTP + API_HOST + PATH_PRIV + "/time";
        URI_USERINFO = HTTP + API_HOST + PATH_PUB + "/account_info";

        URI_METADATA = HTTP + API_HOST + PATH_PUB + "/metadata";
        URI_FOLDER_FILTER = HTTP + API_HOST + PATH_PRIV + "/folderFilter";
        URI_COPY = HTTP + API_HOST + PATH_PUB + "/fileops/copy";
        URI_MOVE = HTTP + API_HOST + PATH_PUB + "/fileops/move";
        URI_DELETE = HTTP + API_HOST + PATH_PUB + "/fileops/delete";
        URI_MKDIRS = HTTP + API_HOST + PATH_PUB + "/fileops/create_folder";

        URI_SHARE_TO = HTTP + API_HOST + PATH_PRIV + "/shareTo";
        URI_SHARE_TO_INFO = HTTP + API_HOST + PATH_PRIV + "/getShareToInfo";
        URI_SHARE_FROM = HTTP + API_HOST + PATH_PRIV + "/shareFrom";

        URI_MK_PUBLINK = HTTP + API_HOST + PATH_PUB + "/shares";
        URI_MK_COPYREF = HTTP + API_HOST + PATH_PUB + "/copy_ref";
        URI_FILE_HISTORY = HTTP + API_HOST + PATH_PUB + "/history";

        URI_THUMB = HTTP + DATA_HOST + PATH_PUB + "/fileops/thumbnail";
        URI_DOCUMENT = HTTP + DATA_HOST + PATH_PUB
                + "/fileops/documentView?__conv_k_inget=1";

        URI_UPLOAD_LOCATE = HTTP + DFS_HOST + PATH_PUB
                + "/fileops/upload_locate";
        URI_UPLOAD_FILE = "";// HTTP + DFS_HOST + PATH_PUB +
                             // "/fileops/upload_file";
        URI_DOWNLOAD_FILE = HTTP + DFS_HOST + PATH_PUB
                + "/fileops/download_file";

        URI_PUSH_SERVER = HTTP + API_HOST + PATH_PRIV + "/getPushServer";
        URI_SYNC_FILE = HTTP + API_HOST + PATH_PRIV + "/syncFiles";
        URI_SYNC_SHARE = HTTP + API_HOST + PATH_PRIV + "/shareDiff";

        URI_KSS_UPLOAD_REQUEST = HTTP + API_HOST + PATH_PRIV
                + "/requestUploadKss";
        URI_KSS_UPLOAD_COMMIT = HTTP + API_HOST + PATH_PRIV + "/commitKss";
        URI_KSS_DOWNLOAD_REQUEST = HTTP + API_HOST + PATH_PRIV
                + "/requestDownloadKss";

        // URI_CREATE_SHAREREF = HTTP+ API_HOST + PATH_PRIV + "/createShareRef";
        // URI_QUERY_SHAREREF = HTTP+ API_HOST + PATH_PRIV + "/queryShareRef";
        URI_BIND_ACCOUNT = HTTP + API_HOST + PATH_PRIV + "/bindAccount";
        URI_RECTENT_FILE = HTTP + API_HOST + PATH_PRIV + "/getRecentFiles";

        URI_CHECK_IN = HTTP + "point.wps.cn/kpoints/submit/sign";
        URI_MOBILEREG = HTTPS + API_HOST + PATH_PRIV + "/mRegister";
        URI_MOBILEREG_REQUEST = HTTP + API_HOST + PATH_PRIV
                + "/mRequestRegister";
        URI_BINDMOBILE = HTTPS + API_HOST + PATH_PRIV + "/bindMobile";
        URI_BINDMOBILE_REQUEST = HTTP + API_HOST + PATH_PRIV
                + "/requestBindMobile";

        URI_GET_WEBTOKEN = HTTPS + API_HOST + PATH_PRIV + "/transformToken";

        URI_ALL_OPENAPI_SESSIONS = HTTP + API_HOST + PATH_PRIV
                + "/queryOpenSessionInfo";
        URI_DELETE_OPENAPI_SESSION = HTTP + API_HOST + PATH_PRIV
                + "/delOpenSessionInfo";

        URI_ALL_API_SESSIONS = HTTP + API_HOST + PATH_PRIV
                + "/queryApiSessionInfo";
        URI_DELETE_API_SESSION = HTTP + API_HOST + PATH_PRIV
                + "/delApiSessionInfo";
    }

}
