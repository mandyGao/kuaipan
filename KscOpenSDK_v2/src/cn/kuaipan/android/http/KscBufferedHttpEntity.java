package cn.kuaipan.android.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

public class KscBufferedHttpEntity extends HttpEntityWrapper {

  private final RandomInputBuffer buffer;
  private final IOException err;

  public KscBufferedHttpEntity(final HttpEntity entity, NetCacheManager cache) {
    super(entity);
    IOException err = null;

    if (!entity.isRepeatable() || entity.getContentLength() < 0) {
      RandomInputBuffer buffer = null;
      try {
        buffer = new RandomInputBuffer(entity.getContent(), cache);
      } catch (IOException e) {
        err = e;
      }
      this.buffer = buffer;
    } else {
      this.buffer = null;
    }
    this.err = err;
  }

  public long getContentLength() {
    return wrappedEntity.getContentLength();
  }

  public InputStream getContent() throws IOException {
    if (this.buffer != null) {
      return new BufferInputStream(buffer);
    } else if (err == null) {
      return wrappedEntity.getContent();
    } else {
      throw err;
    }
  }

  /**
   * Tells that this entity does not have to be chunked.
   * 
   * @return <code>false</code>
   */
  public boolean isChunked() {
    return (buffer == null) && wrappedEntity.isChunked();
  }

  /**
   * Tells that this entity is repeatable.
   * 
   * @return <code>true</code>
   */
  public boolean isRepeatable() {
    return true;
  }

  public void writeTo(final OutputStream outstream) throws IOException {
    if (outstream == null) {
      throw new IllegalArgumentException("Output stream may not be null");
    }
    if (this.buffer != null) {
      InputStream instream = null;
      try {
        instream = getContent();
        int l;
        byte[] tmp = new byte[4096];
        while ((l = instream.read(tmp)) != -1) {
          outstream.write(tmp, 0, l);
        }
      } finally {
        if (instream != null) {
          instream.close();
        }
      }
    } else {
      wrappedEntity.writeTo(outstream);
    }
  }

  // non-javadoc, see interface HttpEntity
  public boolean isStreaming() {
    return (buffer == null) && wrappedEntity.isStreaming();
  }

  @Override
  public void consumeContent() throws IOException {
    if (buffer != null) {
      buffer.close();
    }
    super.consumeContent();
  }

}
