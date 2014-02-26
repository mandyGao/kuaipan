package cn.kuaipan.android.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.sky.base.utils.ContextUtils;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import cn.kuaipan.android.http.client.KscHttpClient;
import cn.kuaipan.android.http.client.URIRedirector;
import cn.kuaipan.android.sdk.PubConstants;
import cn.kuaipan.android.sdk.exception.ErrorHelper;
import cn.kuaipan.android.utils.HttpUtils;

public class KscHttpTransmitter {
  private static final String LOG_TAG = "KscHttpTransmitter";
  private static final int SAFE_REUSE_DURATION = 3 * 60 * 1000;
  private static final int SPEED_RECODE_DURATION = 30 * 60;

  private static final int FLAG_TRYRESEND = 0x01;
  private static final int FLAG_UNKEEPALIVE = 0x02;
  private static final int FLAG_TRANSMISSION = 0x04;
  private static final int FLAG_MASK = FLAG_TRYRESEND | FLAG_UNKEEPALIVE
      | FLAG_TRANSMISSION;

  public static final int TYPE_KEEPALIVE = 0;
  public static final int TYPE_KEEPALIVE_TRYRESEND = FLAG_TRYRESEND;
  public static final int TYPE_UNKEEPALIVE = FLAG_UNKEEPALIVE;
  public static final int TYPE_UNKEEPALIVE_TRYRESEND = FLAG_TRYRESEND
      | FLAG_UNKEEPALIVE;
  public static final int TYPE_KSS_TRANSMISSION = FLAG_TRANSMISSION;

  private static String sUserAgent;
  private static String sShortUserAgent;

  private final Context mContext;
  private final KscSpeedManager mUploadSpeedManager;
  private final KscSpeedManager mDownloadSpeedManager;
  private final NetCacheManager mCacheManager;
  private final SparseArray<Pair<Long, ? extends HttpClient>> mClients;
  private URIRedirector mRedirector;

  public KscHttpTransmitter(Context context) {
    ContextUtils.init(context);
    mContext = context;
    mUploadSpeedManager = new KscSpeedManager(SPEED_RECODE_DURATION);
    mDownloadSpeedManager = new KscSpeedManager(SPEED_RECODE_DURATION);

    mCacheManager = NetCacheManager.getInstance(context, true);
    mClients = new SparseArray<Pair<Long, ? extends HttpClient>>(4);
  }

  public Context getContext() {
    return mContext;
  }

  public void setRedirector(URIRedirector redirector) {
    mRedirector = redirector;
  }

  @SuppressWarnings("unchecked")
  public KscHttpResponse execute(KscHttpRequest request, int type)
      throws InterruptedException {
    IKscTransferListener listener = request.getListener();
    KscHttpResponse resp = new KscHttpResponse(mCacheManager);
    HttpContext context = new BasicHttpContext();
    HttpUriRequest req = null;
    List<HttpMessage> messages = null;
    try {
      req = request.getRequest();
      resp.setOrigRequest(req);

      HttpClient client = getClient(type);
      if ((type & FLAG_TRYRESEND) != 0 && mRedirector != null) {
        context.setAttribute(KscHttpClient.KSC_CONNECT_REDIRECTOR,
            mRedirector);
      }

      String host = getRequestHost(request.getRequest());
      KscSpeedMonitor uploadMonitor = mUploadSpeedManager
          .getMoniter(host);
      KscSpeedMonitor downloadMonitor = mDownloadSpeedManager
          .getMoniter(host);

      setMonitor(req, uploadMonitor, listener);

      long start = KscSpeedManager.current();
      HttpResponse response = client.execute(req, context);
      long end = KscSpeedManager.current();
      Object obj = context.getAttribute(KscHttpClient.KSC_MESSAGE_LIST);
      if (obj instanceof List) {
        try {
          messages = (List<HttpMessage>) obj;
          if (messages != null && !messages.isEmpty()) {
            resp.setMessage(messages);
          }

          long uploadSize = HttpUtils
              .getRequestSize(getRequest(messages));
          long downloadSize = HttpUtils
              .getResponseSize(getResponse(messages))
              + HttpUtils.getResponseSize(response, false);

          uploadMonitor.recode(start, end, uploadSize);
          downloadMonitor.recode(start, end, downloadSize);
        } catch (Exception e) {
          Log.w(LOG_TAG, "Failed get requestList from context.", e);
        }
      }

      setMonitor(response, downloadMonitor, listener);
      resp.handleResponse(request, response,
          (type & FLAG_TRANSMISSION) == 0);

    } catch (Throwable e) {
      ErrorHelper.handleInterruptException(e);
      Log.w(LOG_TAG, "Meet exception when execute a KscHttpRequest.", e);
      resp.setError(e);
    }

    return resp;
  }

  private static void setMonitor(HttpMessage httpMsg,
      KscSpeedMonitor monitor, IKscTransferListener listener) {
    if (httpMsg instanceof HttpEntityEnclosingRequestBase) {
      HttpEntityEnclosingRequestBase entityReq = (HttpEntityEnclosingRequestBase) httpMsg;
      HttpEntity entity = entityReq.getEntity();
      if (entity != null) {
        entityReq.setEntity(new ProcessMonitorEntity(entity, monitor,
            listener, true));
      }
    } else if (httpMsg instanceof HttpResponse) {
      HttpResponse resp = (HttpResponse) httpMsg;
      HttpEntity entity = resp.getEntity();
      if (entity != null) {
        resp.setEntity(new ProcessMonitorEntity(entity, monitor,
            listener, false));
      }
    }
  }

  private static HttpResponse[] getResponse(List<HttpMessage> messages) {
    if (messages == null) {
      return null;
    }

    ArrayList<HttpResponse> result = new ArrayList<HttpResponse>();
    for (HttpMessage message : messages) {
      if (message instanceof HttpResponse) {
        result.add((HttpResponse) message);
      }
    }
    return result.toArray(new HttpResponse[result.size()]);
  }

  private HttpRequest[] getRequest(List<HttpMessage> messages) {
    if (messages == null) {
      return null;
    }

    ArrayList<HttpRequest> result = new ArrayList<HttpRequest>();
    for (HttpMessage message : messages) {
      if (message instanceof HttpRequest) {
        result.add((HttpRequest) message);
      }
    }
    return result.toArray(new HttpRequest[result.size()]);
  }

  private HttpClient getClient(int type) {
    type = type & FLAG_MASK;
    final long current = SystemClock.elapsedRealtime();
    Pair<Long, ? extends HttpClient> pair = mClients.get(type);
    HttpClient result;
    if (pair == null || (current - pair.first) > SAFE_REUSE_DURATION) {
      boolean inKss = (type & FLAG_TRANSMISSION) != 0;
      if (inKss) {
        String userAgent = getShortUserAgent();
        result = KscHttpClient.newKssInstance(userAgent);
      } else {
        String userAgent = getUserAgent();
        result = KscHttpClient.newInstance(userAgent,
            (type & FLAG_UNKEEPALIVE) == 0,
            (type & FLAG_TRYRESEND) != 0);
      }

      pair = Pair.create(current, result);
      mClients.put(type, pair);
    } else {
      result = pair.second;
    }
    return result;
  }

  public KscSpeedManager getUploadSpeeder() {
    return mUploadSpeedManager;
  }

  public KscSpeedManager getDownloadSpeeder() {
    return mDownloadSpeedManager;
  }

  private static String getUserAgent() {
    if (sUserAgent == null) {
      final String base = "KscOAuth/1.0 (Linux; U; Android %s; %s/%s) %s/%s";
      sUserAgent = String
          .format(base, getFrameworkVersion(), PubConstants.SDKName,
              PubConstants.SDKVersion,
              ContextUtils.getPackageName(),
              ContextUtils.getAppVersion());
    }
    return sUserAgent;
  }

  private static String getShortUserAgent() {
    if (sShortUserAgent == null) {
      final String base = "KssRC4/1.0 %s/%s";
      sShortUserAgent = String
          .format(base, ContextUtils.getPackageName(),
              ContextUtils.getAppVersion());
    }
    return sShortUserAgent;
  }

  private static String getFrameworkVersion() {
    StringBuilder buffer = new StringBuilder();
    // Add Framework version
    final String version = Build.VERSION.RELEASE;
    if (version.length() > 0) {
      buffer.append(version);
    } else {
      // default to "1.0"
      buffer.append("1.0");
    }
    buffer.append("; ");
    Locale locale = Locale.SIMPLIFIED_CHINESE;
    final String language = locale.getLanguage();
    if (language != null) {
      buffer.append(language.toLowerCase());
      final String country = locale.getCountry();
      if (country != null) {
        buffer.append("-");
        buffer.append(country.toLowerCase());
      }
    } else {
      // default to "en"
      buffer.append("en");
    }
    // add the model for the release build
    if ("REL".equals(Build.VERSION.CODENAME)) {
      final String model = Build.MODEL;
      if (model.length() > 0) {
        buffer.append("; ");
        buffer.append(model);
      }
    }
    final String id = Build.ID;
    if (id.length() > 0) {
      buffer.append(" Build/");
      buffer.append(id);
    }

    return buffer.toString();
  }

  private static String getRequestHost(HttpUriRequest request) {
    if (request == null) {
      return null;
    }

    URI uri = request.getURI();
    return uri == null ? null : uri.getHost();
  }
}
