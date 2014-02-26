package cn.kuaipan.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;

import android.util.Log;
import cn.kuaipan.android.utils.HttpUtils;

public class KscHttpResponse {

  private static final String LOG_TAG = "KscHttpResponse";

  private final NetCacheManager mCache;

  private HttpUriRequest mOrigRequest;
  private List<HttpMessage> mMessages;
  private HttpResponse mResponse;
  private Throwable mError;

  public KscHttpResponse(NetCacheManager cache) {
    mCache = cache;
  }

  void setOrigRequest(HttpUriRequest request) {
    mOrigRequest = request;
  }

  void setMessage(List<HttpMessage> messages) {
    mMessages = messages;
  }

  void handleResponse(KscHttpRequest request, HttpResponse response,
      boolean toRepeatable) {
    mOrigRequest = request.getRequest();
    mResponse = response;

    final IKscDecoder decoder = request.getDecoder();
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      if (toRepeatable) {
        response.setEntity(KscHttpEntity.getRepeatableEntity(entity,
            decoder, mCache));
      } else if (decoder != null) {
        response.setEntity(new KscHttpEntity(entity, decoder));
      }
    }
  }

  void setError(Throwable e) {
    mError = e;
  }

  public Throwable getError() {
    return mError;
  }

  public HttpResponse getResponse() {
    return mResponse;
  }

  public InputStream getContent() throws IllegalStateException, IOException {
    HttpEntity entity = mResponse == null ? null : mResponse.getEntity();
    if (entity == null) {
      return null;
    }

    InputStream result = entity.getContent();
    Header header = entity.getContentEncoding();
    String encoding = header == null ? null : header.getValue();
    if (encoding != null && encoding.contains("gzip")) {
      result = new GZIPInputStream(result);
    }

    return result;
  }

  public int getStatusCode() {
    int statusCode = -1;
    if (mResponse != null) {
      StatusLine line = mResponse.getStatusLine();
      if (line != null) {
        statusCode = line.getStatusCode();
      }
    }
    return statusCode;
  }

  public void release() throws IOException {
    if (mResponse != null && mResponse.getEntity() != null) {
      try {
        mResponse.getEntity().consumeContent();
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        Log.w(LOG_TAG,
            "Meet exception when release a KscHttpResponse.", e);
      } finally {
        mResponse = null;
      }
    }
  }

  public String dump() {
    StringBuilder builder = new StringBuilder();
    int i = 0;
    int j = 0;

    if (mMessages != null) {
      for (HttpMessage message : mMessages) {
        if (message instanceof HttpRequest) {
          builder.append("[Request " + i++ + "]\n");
          builder.append(HttpUtils.toString((HttpRequest) message));
        } else if (message instanceof HttpResponse) {
          builder.append("[Response " + j++ + "]\n");
          builder.append(HttpUtils.toString((HttpResponse) message));
        }
      }
    }

    if (builder.length() <= 0) {
      builder.append("[Origin Request]\n");
      builder.append(HttpUtils.toString(mOrigRequest));
    }

    builder.append("\n[Response " + j + "]\n");
    builder.append(HttpUtils.toString(mResponse));

    if (mError != null) {
      builder.append("\n[Error]\n");
      builder.append(Log.getStackTraceString(mError));
    }

    String resurt = builder.toString();
    resurt = resurt.replaceAll("password=.*&", "password=[secData]&");
    return resurt;
  }
}
