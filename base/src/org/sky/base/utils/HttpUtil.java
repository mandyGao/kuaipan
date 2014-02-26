package org.sky.base.utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.sky.base.http.multipart.FilePart;
import org.sky.base.http.multipart.MultipartEntity;
import org.sky.base.http.multipart.Part;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * å¢?å¼ºå??Httpè¾???©ç±»
 */
public class HttpUtil {

  private static final String TAG = "HttpUtil";
  private static final int TOME_OUT = 30 * 1000;
  private static final int RETRY_COUNT = 3;

  private static volatile HttpClient sHttpClient;

  private static HttpClient getHttpClient() {
    HttpClient result = sHttpClient;

    if (null == result) {
      synchronized (HttpUtil.class) {
        result = sHttpClient;
        if (result == null) {
          HttpParams params = new BasicHttpParams();
          HttpProtocolParams.setUseExpectContinue(params, false);
          ConnManagerParams.setMaxTotalConnections(params, 100);
          ConnManagerParams.setMaxConnectionsPerRoute(params,
              new ConnPerRoute() {
                @Override
                public int getMaxForRoute(HttpRoute httproute) {
                  return 32;
                }
              });

          HttpClientParams.setRedirecting(params, true);

          params.setParameter(
              CoreConnectionPNames.CONNECTION_TIMEOUT, TOME_OUT);
          params.setParameter(CoreConnectionPNames.SO_TIMEOUT,
              TOME_OUT);
          params.setParameter(
              CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024);

          params.setParameter(ClientPNames.HANDLE_REDIRECTS,
              Boolean.FALSE);
          params.setParameter(
              CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8"); // é»?è®¤ä¸ºISO-8859-1
          params.setParameter(
              CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8"); // é»?è®¤ä¸ºUS-ASCII
          params.removeParameter(ConnRouteParams.DEFAULT_PROXY);

          SchemeRegistry schReg = new SchemeRegistry();
          schReg.register(new Scheme("http", PlainSocketFactory
              .getSocketFactory(), 80));
          schReg.register(new Scheme("https", SSLSocketFactory
              .getSocketFactory(), 443));
          ThreadSafeClientConnManager conMgr = new ThreadSafeClientConnManager(
              params, schReg);
          sHttpClient = result = new DefaultHttpClient(conMgr, params);
        }
      }

    }
    return result;
  }

  private static HttpPost getPost(String uri, List<BasicNameValuePair> params) {
    HttpPost post = new HttpPost(uri);
    try {
      if (params != null) {
        post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
      }
    } catch (UnsupportedEncodingException e) {
      Log.w(TAG, "Framework not support UTF-8", e);
    }
    return post;
  }

  public static String postString(String uri, List<BasicNameValuePair> params)
      throws Exception {
    HttpResponse response;
    int currCount = 0; // å½????è¯·æ??æ¬¡æ??
    Exception th = null;
    while (currCount < RETRY_COUNT) {
      currCount++;
      HttpClient httpClient = getHttpClient();
      HttpPost post = getPost(uri, params);
      try {
        response = httpClient.execute(post);
        int state = response.getStatusLine().getStatusCode();
        if (state == HttpStatus.SC_OK) {
          return EntityUtils.toString(response.getEntity());
        } else {
          post.abort();
          Map<String, String> infoMap = new HashMap<String, String>();
          infoMap.put("uri", uri);
          infoMap.put("status", Integer.toString(state));
          th = new IllegalStateException(infoMap.toString());
          // retry
        }
      } catch (Exception e) {
        th = e;
      }
    }
    Log.e(TAG, th.getMessage(), th);
    throw th;
  }

  public static void upload(String uri, String name, File file)
      throws Exception {
    if (!file.exists() || TextUtils.isEmpty(name)) {
      return;
    }
    FilePart part = new FilePart(name, file);
    MultipartEntity reqEntity = new MultipartEntity(new Part[] {
        part
    });

    HttpPost post = getPost(uri, null);
    post.setEntity(reqEntity);
    HttpResponse response = getHttpClient().execute(post);
    int state = response.getStatusLine().getStatusCode();
    if (state != HttpStatus.SC_OK) {
      throw new IllegalStateException("status=" + state);
    }
  }

  public static HttpResponse postStream(String uri, InputStream stream,
      int length) throws Exception {
    HttpPost post = getPost(uri, null);
    byte[] data = new byte[length];
    stream.read(data);
    post.setEntity(new ByteArrayEntity(data));
    HttpResponse response = getHttpClient().execute(post);
    int state = response.getStatusLine().getStatusCode();
    if (state != HttpStatus.SC_OK) {
      throw new IllegalStateException("status=" + state);
    }

    return response;
  }

  public static HttpResponse postData(String uri, byte[] data)
      throws Exception {
    HttpPost post = getPost(uri, null);
    ByteArrayEntity entity = new ByteArrayEntity(data);
    post.setEntity(entity);
    HttpResponse response = getHttpClient().execute(post);
    int state = response.getStatusLine().getStatusCode();
    if (state != HttpStatus.SC_OK) {
      throw new IllegalStateException("status=" + state);
    }

    return response;
  }

  public static HttpResponse postEmptyRequest(String uri) throws Exception {
    HttpPost post = getPost(uri, null);
    HttpResponse response = getHttpClient().execute(post);
    int state = response.getStatusLine().getStatusCode();
    if (state != HttpStatus.SC_OK) {
      throw new IllegalStateException("status=" + state);
    }

    return response;
  }

  public static void postFile(String uri, File file, String userAgent,
      boolean isGziped) throws Exception {
    if (!file.exists()) {
      return;
    }

    String contentType = isGziped ? null : "text/ht*/js/css/php";

    HttpPost post = getPost(uri, null);
    FileEntity entity = new FileEntity(file, contentType);
    if (isGziped) {
      entity.setContentEncoding("gzip");
    }
    post.setEntity(entity);
    if (!TextUtils.isEmpty(userAgent)) {
      post.setHeader(HTTP.USER_AGENT, userAgent);
    }

    HttpResponse response = getHttpClient().execute(post);
    int state = response.getStatusLine().getStatusCode();
    if (state != HttpStatus.SC_OK) {
      throw new IllegalStateException("status=" + state);
    }
  }

  public static void postString(String uri, String data, String userAgent)
      throws Exception {
    if (TextUtils.isEmpty(data)) {
      return;
    }

    HttpPost post = getPost(uri, null);
    HttpEntity entity = new StringEntity(data, HTTP.UTF_8);

    post.setEntity(entity);
    if (!TextUtils.isEmpty(userAgent)) {
      post.setHeader(HTTP.USER_AGENT, userAgent);
    }

    HttpResponse response = getHttpClient().execute(post);
    int state = response.getStatusLine().getStatusCode();
    if (state != HttpStatus.SC_OK) {
      throw new IllegalStateException("status=" + state);
    }
  }

  // public static void upload(String uri, String domin, String path,
  // ITransListener listener) {
  // File file = new File(path);
  // if (!file.exists()) {
  // listener.onFailed(new FileNotFoundException(path));
  // return;
  // }
  //
  // HttpResponse response;
  // int currCount = 0;
  // Exception th = null;
  // while (currCount < RETRY_COUNT) {
  // currCount++;
  // HttpClient httpClient = getHttpClient();
  // HttpPost post = getPost(uri, null);
  // try {
  // MultipartEntity reqEntity = new MultipartEntity();
  // reqEntity.addPart(domin, new CountingFileBody(file, listener));
  // post.setEntity(reqEntity);
  // response = httpClient.execute(post);
  // int state = response.getStatusLine().getStatusCode();
  // if (state == HttpStatus.SC_OK) {
  // return;
  // } else {
  // post.abort();
  // Map<String, String> infoMap = new HashMap<String, String>();
  // infoMap.put("uri", uri);
  // infoMap.put("status", Integer.toString(state));
  // th = new IllegalStateException(infoMap.toString());
  // // retry
  // }
  // } catch (Exception e) {
  // th = e;
  // }
  // }
  // Log.e(TAG, th.getMessage(), th);
  // if (listener != null) {
  // listener.onFailed(th);
  // }
  // }
  //
  // public static void download(String uri, String path, ITransListener
  // listener) {
  // InputStream in = null;
  // OutputStream out = null;
  // HttpResponse response;
  // int currCount = 0;
  // Exception th = null;
  // while (currCount < RETRY_COUNT) {
  // currCount++;
  // HttpClient httpClient = getHttpClient();
  // HttpGet get = new HttpGet(uri);
  // try {
  // response = httpClient.execute(get);
  // int state = response.getStatusLine().getStatusCode();
  // if (state == HttpStatus.SC_OK) {
  // in = response.getEntity().getContent();
  // File file = new File(path);
  // if (!file.exists()) {
  // file.createNewFile();
  // }
  // out = new FileOutputStream(file);
  // int l = 0;
  // long size = 0;
  // long total = response.getEntity().getContentLength();
  // byte[] buf = new byte[1024 * 4];
  // boolean doNext = true;
  // if (listener != null) {
  // listener.onStartTrans();
  // }
  // while (doNext && (l = in.read(buf)) != -1) {
  // out.write(buf, 0, l);
  // out.flush();
  // size += l;
  // if (listener != null) {
  // doNext = listener.onTrans(size, total);
  // }
  // if (listener != null) {
  // if (doNext) {
  // listener.onSuccess();
  // } else {
  // listener.onCancel();
  // }
  // }
  // }
  // return;
  // } else {
  // get.abort();
  // Map<String, String> infoMap = new HashMap<String, String>();
  // infoMap.put("uri", uri);
  // infoMap.put("status", Integer.toString(state));
  // th = new IllegalStateException(infoMap.toString());
  // // retry
  // }
  // } catch (Exception e) {
  // th = e;
  // } finally {
  // if (out != null) {
  // try {
  // out.close();
  // } catch (IOException e) {
  // }
  // }
  // }
  // }
  // Log.e(TAG, th.getMessage(), th);
  // if (listener != null) {
  // listener.onFailed(th);
  // }
  //
  // }
  //
  // public static class DefaultTransListener implements ITransListener {
  //
  // @Override
  // public void onStartTrans() {
  // }
  //
  // @Override
  // public boolean onTrans(long size, long total) {
  // return true;
  // }
  //
  // @Override
  // public void onSuccess() {
  // }
  //
  // @Override
  // public void onCancel() {
  // }
  //
  // @Override
  // public void onFailed(Throwable e) {
  // }
  //
  // }

}
