package cn.kuaipan.android.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.sky.base.http.multipart.ByteArrayValuePair;
import org.sky.base.http.multipart.FileValuePair;

import android.net.Uri;
import android.net.Uri.Builder;
import android.text.TextUtils;

public class UriUtils {
  public static List<NameValuePair> getQuerys(Uri uri) {
    if (uri == null) {
      return null;
    }
    // Just a Protection to make sure the uri can be used to create an URI.
    uri = uri.buildUpon().path(uri.getEncodedPath()).build();
    URI uriEntry = URI.create(uri.toString());
    return URLEncodedUtils.parse(uriEntry, HTTP.UTF_8);
  }

  public static Uri updateQuery(Uri uri, String name, String value) {
    if (uri == null || TextUtils.isEmpty(name)) {
      return uri;
    }

    ArrayList<NameValuePair> queries = new ArrayList<NameValuePair>(
        getQuerys(uri));
    int queryIndex = -1;
    for (int i = 0; i < queries.size(); i++) {
      NameValuePair pair = queries.get(i);
      if (TextUtils.equals(pair.getName(), name)) {
        queryIndex = i;
        break;
      }
    }

    if (queryIndex >= 0) {
      queries.remove(queryIndex);
    }
    if (!TextUtils.isEmpty(value)) {
      if (queryIndex >= 0) {
        queries.add(queryIndex, new BasicNameValuePair(name, value));
      } else {
        queries.add(new BasicNameValuePair(name, value));
      }
    }

    return appendQuerys(uri.buildUpon().query(null).build(), queries);
  }

  public static Uri appendQuerys(Uri uri, Collection<NameValuePair> pairs) {
    if (uri == null || pairs == null || pairs.isEmpty()) {
      return uri;
    }

    Builder builder = uri.buildUpon();
    for (NameValuePair pair : pairs) {
      if (!(pair instanceof FileValuePair)
          && !(pair instanceof ByteArrayValuePair)) {
        builder.appendQueryParameter(pair.getName(), pair.getValue());
      }
    }
    return builder.build();
  }

  public static Map<String, String> getDecodedQuerys(Uri uri) {
    Map<String, String> encoded = getEncodedQuerys(uri);
    if (encoded == null || encoded.isEmpty()) {
      return encoded;
    }

    HashMap<String, String> result = new HashMap<String, String>();
    for (Entry<String, String> entry : encoded.entrySet()) {
      result.put(Uri.decode(entry.getKey()), Uri.decode(entry.getValue()));
    }

    return result;
  }

  public static Map<String, String> getEncodedQuerys(Uri uri) {
    if (uri == null) {
      return null;
    }
    String query = uri.getEncodedQuery();
    if (TextUtils.isEmpty(query)) {
      return null;
    }
    String[] pairs = query.split("&");
    HashMap<String, String> result = new HashMap<String, String>();
    for (String pair : pairs) {
      int index = pair.indexOf("=");
      String key;
      String value;
      if (index >= 0) {
        key = pair.substring(0, index);
        value = pair.substring(index + 1);
      } else {
        key = pair;
        value = null;
      }
      result.put(key, value);
    }
    return result;
  }
}
