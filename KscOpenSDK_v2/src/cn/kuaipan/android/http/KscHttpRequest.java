package cn.kuaipan.android.http;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.sky.base.http.multipart.ByteArrayValuePair;
import org.sky.base.http.multipart.FilePart;
import org.sky.base.http.multipart.FileValuePair;
import org.sky.base.http.multipart.MultipartEntity;
import org.sky.base.http.multipart.Part;
import org.sky.base.http.multipart.StringPart;

import android.net.Uri;
import android.util.Log;

public class KscHttpRequest {
  private static final String LOG_TAG = "KscHttpRequest";

  public static enum HttpMethod {
    GET, POST
  }

  private final HttpMethod mMethod;
  private Uri mUri;
  private final ArrayList<NameValuePair> mPostForm = new ArrayList<NameValuePair>();
  private AbstractHttpEntity mPostEntity;
  private boolean mTryGzip = false;

  private final IKscDecoder mDecoder;
  private final IKscTransferListener mListener;

  private HttpUriRequest mRequest;

  public KscHttpRequest() {
    this((Uri) null);
  }

  public KscHttpRequest(String uriString) {
    this(null, Uri.parse(uriString), null, null);
  }

  public KscHttpRequest(HttpMethod method, String uriString) {
    this(method, Uri.parse(uriString), null, null, null);
  }

  public KscHttpRequest(Uri uri) {
    this(null, uri, null, null);
  }

  public KscHttpRequest(HttpMethod method, Uri uri) {
    this(method, uri, null, null, null);
  }

  public KscHttpRequest(HttpMethod method, String uriString,
      IKscTransferListener listener) {
    this(method, Uri.parse(uriString), null, null, listener);
  }

  public KscHttpRequest(HttpMethod method, Uri uri,
      IKscTransferListener listener) {
    this(method, uri, null, null, listener);
  }

  public KscHttpRequest(HttpMethod method, String uriString,
      IKscDecoder decoder, IKscTransferListener listener) {
    this(method, Uri.parse(uriString), null, decoder, listener);
  }

  public KscHttpRequest(HttpMethod method, Uri uri, IKscDecoder decoder,
      IKscTransferListener listener) {
    this(method, uri, null, decoder, listener);
  }

  public KscHttpRequest(HttpMethod method, Uri uri,
      AbstractHttpEntity postEntity, IKscDecoder decoder,
      IKscTransferListener listener) {
    mMethod = method;
    mUri = uri;
    mPostEntity = postEntity;
    mDecoder = decoder;
    mListener = listener;
  }

  public void setGZip(boolean gzip) {
    mTryGzip = gzip;
  }

  private void checkRequest() {
    if (mRequest != null) {
      throw new RuntimeException(
          "HttpRequest has been created. All input can't be changed.");
    }
  }

  public void setUri(String uri) {
    checkRequest();

    mUri = uri == null ? null : Uri.parse(uri);
  }

  public void setUri(Uri uri) {
    checkRequest();
    mUri = uri;
  }

  public void setUri(URI uri) {
    checkRequest();
    mUri = uri == null ? null : Uri.parse(uri.toString());
  }

  public void setUri(URL url) {
    checkRequest();
    mUri = url == null ? null : Uri.parse(url.toString());
  }

  public Uri getUri() {
    return mUri;
  }

  public void appendParameters(NameValuePair... pairs) {
    appendParameters(Arrays.asList(pairs));
  }

  public void appendParameters(Collection<? extends NameValuePair> pairs) {
    checkRequest();

    ArrayList<NameValuePair> queryPairs = new ArrayList<NameValuePair>();
    ArrayList<NameValuePair> postPairs = new ArrayList<NameValuePair>();
    if (pairs != null) {
      for (NameValuePair pair : pairs) {
        if ((pair instanceof FileValuePair)
            || (pair instanceof ByteArrayValuePair)) {
          postPairs.add(pair);
        } else {
          queryPairs.add(pair);
        }
      }
    }

    if (!queryPairs.isEmpty()) {
      appendQueryParameters(queryPairs);
    }
    if (!postPairs.isEmpty()) {
      appendPostParameters(postPairs);
    }
  }

  public void appendQueryParameter(String key, String value) {
    checkRequest();
    if (mUri == null) {
      throw new RuntimeException("A uri should be set firstly");
    }

    mUri = mUri.buildUpon().appendQueryParameter(key, value).build();
  }

  public void appendQueryParameters(NameValuePair... pairs) {
    appendQueryParameters(Arrays.asList(pairs));
  }

  public void appendQueryParameters(Collection<? extends NameValuePair> pairs) {
    checkRequest();
    if (mUri == null) {
      throw new RuntimeException("A uri should be set firstly");
    }

    if (pairs == null) {
      return;
    }

    Uri.Builder builder = mUri.buildUpon();
    for (NameValuePair pair : pairs) {
      builder.appendQueryParameter(pair.getName(), pair.getValue());
    }
    mUri = builder.build();
  }

  public void appendPostParameters(NameValuePair... pairs) {
    appendPostParameters(Arrays.asList(pairs));
  }

  public void appendPostParameters(Collection<? extends NameValuePair> pairs) {
    checkRequest();

    if (pairs == null) {
      return;
    }

    if (!isFormEntity(mPostEntity)) {
      throw new RuntimeException("Http not support send form data and"
          + " binary data in one request.");
    }

    mPostForm.addAll(pairs);
  }

  public String getQuery() {
    if (mUri == null) {
      return null;
    }
    return mUri.getQuery();
  }

  public String getPostString() {
    AbstractHttpEntity entity = mPostEntity;
    if (entity == null) {
      return URLEncodedUtils.format(filterStringPair(mPostForm),
          HTTP.UTF_8);
    }

    if (entity instanceof MultipartEntity) {
      ArrayList<NameValuePair> stringPairs = new ArrayList<NameValuePair>();

      Part[] parts = ((MultipartEntity) entity).getParts();
      if (parts != null) {
        stringPairs.addAll(filterStringPart(parts));
      }

      stringPairs.addAll(filterStringPair(mPostForm));
      return URLEncodedUtils.format(stringPairs, HTTP.UTF_8);
    }

    if (URLEncodedUtils.isEncoded(entity)) {
      ArrayList<NameValuePair> pairs = getMergedPostValue(entity,
          mPostForm);
      return URLEncodedUtils.format(filterStringPair(pairs), HTTP.UTF_8);
    }

    String result = null;
    if (mPostForm.isEmpty()) {
      try {
        result = EntityUtils.toString(entity);
      } catch (Exception e) {
        Log.i(LOG_TAG, "Entity can't be cover to a String", e);
      }
    }
    return result;
  }

  private static List<NameValuePair> filterStringPair(
      Collection<NameValuePair> pairs) {
    ArrayList<NameValuePair> stringPairs = new ArrayList<NameValuePair>();
    if (pairs == null) {
      return stringPairs;
    }
    for (NameValuePair pair : pairs) {
      if (!(pair instanceof FileValuePair)
          && !(pair instanceof ByteArrayValuePair)) {
        stringPairs.add(pair);
      } else {
        Log.i(LOG_TAG,
            "Lost a non-string valuePair when getPostString."
                + " pair=" + pair);
      }
    }
    return stringPairs;
  }

  private static List<NameValuePair> filterStringPart(Part[] parts) {
    ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
    if (parts == null) {
      return result;
    }
    for (Part part : parts) {
      if (part instanceof StringPart) {
        result.add(new BasicNameValuePair(part.getName(),
            ((StringPart) part).getValue()));
      } else {
        Log.i(LOG_TAG, "Lost a non-string in parts. part=" + part);
      }
    }
    return result;
  }

  private static ArrayList<NameValuePair> getMergedPostValue(
      AbstractHttpEntity entity, List<NameValuePair> postForm) {
    ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
    if (entity != null) {
      try {
        List<NameValuePair> oldPairs = URLEncodedUtils.parse(entity);
        pairs.addAll(oldPairs);
      } catch (IOException e) {
        Log.e(LOG_TAG, "Failed parse an user entity.", e);
        throw new RuntimeException(
            "Failed parse an user entity. The user entity "
                + "should be parseable by URLEncodedUtils.parse(HttpEntity)",
            e);
      }
    }

    pairs.addAll(postForm);
    return pairs;
  }

  public void setPostData(byte[] data) {
    setPostEntity(new ByteArrayEntity(data));
  }

  public void setPostEntity(AbstractHttpEntity entity) {
    checkRequest();
    mPostEntity = entity;
    if (!isFormEntity(entity)) {
      mPostForm.clear();
    }
  }

  public HttpUriRequest getRequest() {
    if (mRequest == null) {
      mRequest = createHttpRequest();
    }
    return mRequest;
  }

  private HttpUriRequest createHttpRequest() {
    if (!isValidUri(mUri)) {
      throw new IllegalArgumentException("Request uri is not valid. uri="
          + mUri);
    }

    final String uri = mUri.toString();

    HttpMethod method = mMethod;
    if (method == null) {
      method = (mPostEntity == null && mPostForm.isEmpty()) ? HttpMethod.GET
          : HttpMethod.POST;
    }

    HttpUriRequest result = null;
    switch (method) {
      case GET:
        result = new HttpGet(uri);
        if (mPostEntity != null || !mPostForm.isEmpty()) {
          Log.w(LOG_TAG,
              "Post data is not empty, but method is GET. All post data is lost.");
        }
        break;
      case POST:
        HttpPost post = new HttpPost(uri);
        if (!mPostForm.isEmpty()) {
          mPostEntity = makeFormEntity();
        }
        post.setEntity(mPostEntity);
        result = post;
        break;
    }

    if (mTryGzip) {
      result.setHeader("Accept-Encoding", "gzip");
    }
    mRequest = result;
    return result;
  }

  private AbstractHttpEntity makeFormEntity() {
    final AbstractHttpEntity srcEntity = mPostEntity;
    final ArrayList<NameValuePair> postForm = mPostForm;
    if (mPostForm.isEmpty()) {
      return mPostEntity;
    }

    boolean multipart = (srcEntity != null)
        && (srcEntity instanceof MultipartEntity);
    if (!multipart) {
      for (NameValuePair pair : postForm) {
        if ((pair instanceof FileValuePair)
            || (pair instanceof ByteArrayValuePair)) {
          multipart = true;
          break;
        }
      }
    }

    AbstractHttpEntity result;
    if (multipart) {
      MultipartEntity entity;
      if ((srcEntity != null) && (srcEntity instanceof MultipartEntity)) {
        entity = (MultipartEntity) srcEntity;
        Part[] parts = toPartArray(postForm);
        entity.appendPart(parts);
      } else {
        Part[] parts = toPartArray(getMergedPostValue(srcEntity,
            postForm));
        entity = new MultipartEntity(parts);
      }
      result = entity;
    } else {
      try {
        result = new UrlEncodedFormEntity(mPostForm, HTTP.UTF_8);
      } catch (UnsupportedEncodingException e) {
        Log.e(LOG_TAG, "JVM not support UTF_8?", e);
        throw new RuntimeException("JVM not support UTF_8?", e);
      }
    }
    return result;
  }

  private boolean isValidUri(Uri uri) {
    if (uri == null) {
      return false;
    }

    String scheme = uri.getScheme();
    return "http".equalsIgnoreCase(scheme)
        || "https".equalsIgnoreCase(scheme);
  }

  public IKscDecoder getDecoder() {
    return mDecoder;
  }

  private static boolean isFormEntity(AbstractHttpEntity entity) {
    return (entity == null) || (entity instanceof MultipartEntity)
        || URLEncodedUtils.isEncoded(entity);
  }

  private static Part[] toPartArray(List<NameValuePair> pairs) {
    if (pairs == null || pairs.isEmpty()) {
      return null;
    }

    final int size = pairs.size();
    Part[] parts = new Part[size];
    for (int i = 0; i < size; i++) {
      NameValuePair pair = pairs.get(i);
      if (pair instanceof FileValuePair) {
        try {
          parts[i] = new FilePart(pair.getName(),
              ((FileValuePair) pair).getFile());
        } catch (FileNotFoundException e) {
          throw new RuntimeException(
              "The file to be sent should be exist. file="
                  + ((FileValuePair) pair).getFile(), e);
        }
      } else if (pair instanceof ByteArrayValuePair) {
        parts[i] = new FilePart(pair.getName(), pair.getValue(),
            ((ByteArrayValuePair) pair).getData());
      } else {
        parts[i] = new StringPart(pair.getName(), pair.getValue(),
            HTTP.UTF_8);
      }
    }
    return parts;
  }

  public IKscTransferListener getListener() {
    return mListener;
  }

  public long getContentLength() {
    return mPostEntity == null ? -1 : mPostEntity.getContentLength();
  }
}
