package cn.kuaipan.android.kss;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.InputStreamEntity;

public class KssInputStreamEntity extends InputStreamEntity {

  private final InputStream content;

  public KssInputStreamEntity(InputStream instream, long length) {
    super(instream, length);
    instream.mark((int) Math.min(Integer.MAX_VALUE, length));
    content = instream;
  }

  @Override
  public boolean isRepeatable() {
    return super.isRepeatable() || content.markSupported();
  }

  @Override
  public InputStream getContent() throws IOException {
    content.reset();

    return super.getContent();
  }

  @Override
  public void writeTo(OutputStream outstream) throws IOException {
    content.reset();
    super.writeTo(outstream);
  }

}
