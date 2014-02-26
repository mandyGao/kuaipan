package cn.kuaipan.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

public class KscHttpEntity extends HttpEntityWrapper {
  // private final static long MAX_BYTE_CONTENT_LENGTH = 128 * 1024;

  private final IKscDecoder mDecoder;

  public KscHttpEntity(HttpEntity wrapped, IKscDecoder decoder) {
    super(wrapped);
    mDecoder = decoder;
  }

  @Override
  public InputStream getContent() throws IOException {
    InputStream result = null;
    if (mDecoder == null) {
      result = super.getContent();
    } else {
      result = new DecoderInputStream(super.getContent(), mDecoder);
    }
    return result;
  }

  @Override
  // FIXME may read more than length.
  public void writeTo(OutputStream outstream) throws IOException {
    if (outstream == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }
    InputStream instream = getContent();
    int l;
    byte[] tmp = new byte[4096];
    while ((l = instream.read(tmp)) != -1) {
      outstream.write(tmp, 0, l);
    }
  }

  public static KscHttpEntity getRepeatableEntity(HttpEntity entity,
      IKscDecoder decoder, NetCacheManager cache) {
    if (entity == null) {
      return null;
    }
    HttpEntity repeatable = getRepeatable(entity, cache);
    return new KscHttpEntity(repeatable, decoder);
  }

  private static HttpEntity getRepeatable(HttpEntity entity,
      NetCacheManager cache) {
    if (entity == null || entity.isRepeatable()) {
      return entity;
    }

    return new KscBufferedHttpEntity(entity, cache);
  }

  // private static HttpEntity getRepeatable1(HttpEntity entity,
  // NetCacheManager cache) throws IOException {
  // if (entity == null || entity.isRepeatable()) {
  // return entity;
  // }
  // HttpEntity result;
  // long length = entity.getContentLength();
  // if (length <= MAX_BYTE_CONTENT_LENGTH || length < 0) {
  // result = toByteArrayEntity(entity);
  // } else {
  // result = toFileEntity(entity, cache);
  // }
  //
  // return result;
  // }

  // private static HttpEntity toFileEntity(HttpEntity entity,
  // NetCacheManager cache) throws IOException {
  // InputStream in = null;
  // OutputStream out = null;
  // try {
  // final File file = cache.assignCache();
  // in = entity.getContent();
  // out = new FileOutputStream(file);
  //
  // final byte[] buf = new byte[8 * 1024];
  // int len;
  // while ((len = in.read(buf)) >= 0) {
  // out.write(buf, 0, len);
  // }
  // out.flush();
  //
  // KscFileEntity result = new KscFileEntity(file, null, cache);
  // result.setContentType(entity.getContentType());
  // result.setContentEncoding(entity.getContentEncoding());
  // return result;
  // } finally {
  // try {
  // in.close();
  // entity.consumeContent();
  // } catch (Exception e) {
  // // ignore
  // }
  // try {
  // out.close();
  // } catch (Exception e) {
  // // ignore
  // }
  // }
  // }

  // private static HttpEntity toByteArrayEntity(HttpEntity entity)
  // throws IOException {
  //
  // InputStream in = null;
  // ByteArrayOutputStream out = null;
  // try {
  // long length = entity.getContentLength();
  // if (length > 0 && length < Integer.MAX_VALUE) {
  // out = new ByteArrayOutputStream((int) length);
  // } else {
  // out = new ByteArrayOutputStream();
  // }
  // in = entity.getContent();
  //
  // final byte[] buf = new byte[1024];
  // int len;
  // while ((len = in.read(buf)) >= 0) {
  // out.write(buf, 0, len);
  // }
  //
  // ByteArrayEntity result = new ByteArrayEntity(out.toByteArray());
  // result.setContentType(entity.getContentType());
  // result.setContentEncoding(entity.getContentEncoding());
  // return result;
  // } finally {
  // try {
  // in.close();
  // entity.consumeContent();
  // } catch (Exception e) {
  // // ignore
  // }
  // try {
  // out.close();
  // } catch (Exception e) {
  // // ignore
  // }
  // }
  // }

  // private static class KscFileEntity extends FileEntity {
  // private final NetCacheManager mCache;
  //
  // public KscFileEntity(File file, String contentType,
  // NetCacheManager cache) {
  // super(file, contentType);
  // mCache = cache;
  // }
  //
  // @Override
  // public void consumeContent() throws IOException {
  // super.consumeContent();
  // if (mCache != null) {
  // mCache.releaseCache(file);
  // }
  // }
  // }
}
