package cn.kuaipan.android.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

public class HttpUtils {
  private HttpUtils() {}

  public static StringBuffer toString(HttpRequest request) {
    if (request == null) {
      return null;
    }

    StringBuffer builder = new StringBuffer();
    builder.append(request.getRequestLine());
    builder.append("\n");

    // builder.append("Uri:\n");
    // if (request instanceof HttpUriRequest) {
    // builder.append(((HttpUriRequest) request).getURI());
    // builder.append("\n");
    // }

    // HttpRequest tempRequest = request;
    // while (tempRequest instanceof RequestWrapper) {
    // tempRequest = ((RequestWrapper) tempRequest).getOriginal();
    // if (tempRequest instanceof HttpUriRequest) {
    // builder.append(((HttpUriRequest) tempRequest).getURI());
    // builder.append("\n");
    // }
    // }

    // builder.append("Header:\n");
    for (Header header : request.getAllHeaders()) {
      builder.append(header.toString().trim());
      builder.append("\n");
    }

    if (request instanceof HttpEntityEnclosingRequest) {
      HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) request;
      HttpEntity entity = entityRequest.getEntity();
      if (entity != null) {
        builder.append("Content:\n");
        try {
          if (entity.isRepeatable()) {
            builder.append(entityToString(entity, 1024));
          } else {
            builder.append(" [DATA CAN NOT REPEAT]");
          }
        } catch (Exception e) {
          builder.append(" [FAILED OUTPUT DATA]");
        }

        builder.append("\n");
      }
    }

    return builder;
  }

  private static String entityToString(HttpEntity entity, int maxLength)
      throws IOException {
    final long length = entity.getContentLength();
    long len = length < 0 ? Integer.MAX_VALUE : length;

    ByteArrayOutputStream outstream = new ByteArrayOutputStream(
        (int) Math.min(len, maxLength));
    boolean fullOutput = length >= 0 && length <= maxLength;

    int readed = 0;
    if (fullOutput) {
      entity.writeTo(outstream);
    } else {
      InputStream instream = entity.getContent();
      try {
        byte[] tmp = new byte[1024];
        int pos = 0;
        int l, ol;
        while ((l = instream.read(tmp)) != -1 && pos < maxLength) {
          ol = Math.min(l, maxLength - pos);
          outstream.write(tmp, 0, ol);
          pos += l;
        }
        readed = pos;
      } finally {
        instream.close();
      }
    }

    String content = outstream.toString();
    return content
        + ((fullOutput || readed < maxLength) ? ""
            : "\n [TOO MUCH DATA TO INCLUDE, SIZE=" + length + "]");
  }

  public static StringBuffer toString(HttpResponse response) {
    if (response == null) {
      return null;
    }

    StringBuffer builder = new StringBuffer();
    builder.append(response.getStatusLine());
    builder.append("\n");

    // builder.append("Header:\n");
    for (Header header : response.getAllHeaders()) {
      builder.append(header.toString().trim());
      builder.append("\n");
    }

    HttpEntity entity = response.getEntity();
    if (entity != null) {
      builder.append("Content:\n");
      try {
        if (entity.isRepeatable()) {
          builder.append(entityToString(entity, 1024));
        } else {
          builder.append(" [DATA CAN NOT REPEAT]");
        }
      } catch (Exception e) {
        builder.append(" [FAILED OUTPUT DATA]");
      }

      builder.append("\n");
    }
    return builder;
  }

  public static long getHeaderSize(Header[] headers) {
    if (headers == null || headers.length <= 0) {
      return 0;
    }
    long size = 0;
    for (Header header : headers) {
      size += header.toString().getBytes().length + 1;
    }

    return size;
  }

  public static long getRequestSize(HttpRequest... requests) {
    if (requests == null) {
      return 0;
    }

    long size = 0;
    for (HttpRequest request : requests) {
      size += getRequestSize(request, true);
    }

    return size;
  }

  public static long getRequestSize(HttpRequest request,
      boolean includeContent) {
    if (request == null) {
      return 0;
    }

    long lineSize = request.getRequestLine().toString().getBytes().length + 1;
    long headerSize = getHeaderSize(request.getAllHeaders());
    long contentSize = 0;

    // if (includeContent && (request instanceof
    // HttpEntityEnclosingRequest)) {
    // HttpEntityEnclosingRequest entityRequest =
    // (HttpEntityEnclosingRequest) request;
    // HttpEntity entity = entityRequest.getEntity();
    // if (entity != null) {
    // // the size may not very exact.
    // contentSize = Math.max(0, entity.getContentLength());
    // }
    // }

    return lineSize + headerSize + contentSize;
  }

  public static long getResponseSize(HttpResponse... responses) {
    if (responses == null) {
      return 0;
    }

    long size = 0;
    for (HttpResponse response : responses) {
      size += getResponseSize(response, true);
    }

    return size;
  }

  public static long getResponseSize(HttpResponse response,
      boolean includeContent) {
    if (response == null) {
      return 0;
    }

    long lineSize = response.getStatusLine().toString().getBytes().length + 1;
    long headerSize = getHeaderSize(response.getAllHeaders());
    long contentSize = 0;

    // HttpEntity entity = response.getEntity();
    // if (includeContent && entity != null) {
    // // the size may not very exact.
    // contentSize = Math.max(0, entity.getContentLength());
    // }
    return lineSize + headerSize + contentSize;
  }
}
