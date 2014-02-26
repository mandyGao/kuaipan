package cn.kuaipan.android.http;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.zip.DataFormatException;

import cn.kuaipan.android.utils.RandomFileInputStream;

public class DecoderInputStream extends FilterInputStream {

  private static final int BUF_SIZE = 512;

  /**
   * The inflater used for this stream.
   */
  private final IKscDecoder decoder;

  /**
   * The input buffer used for decompression.
   */
  private final byte[] buf;

  private boolean closed;

  /**
   * True if this stream's last byte has been returned to the user. This could
   * be because the underlying stream has been exhausted, or if errors were
   * encountered while inflating that stream.
   */
  private boolean eof;

  private long markPos = -1;
  private final long startPos;

  /**
   * This constructor lets you pass a specifically initialized Inflater, for
   * example one that expects no ZLIB header.
   * 
   * @param is the {@code InputStream} to read data from.
   * @param inf the specific {@code Inflater} for uncompressing data.
   */
  public DecoderInputStream(InputStream is, IKscDecoder decoder) {
    this(is, decoder, BUF_SIZE);
  }

  /**
   * This constructor lets you specify both the {@code Inflater} as well as
   * the internal buffer size to be used.
   * 
   * @param is the {@code InputStream} to read data from.
   * @param inf the specific {@code Inflater} for uncompressing data.
   * @param bsize the size to be used for the internal buffer.
   */
  public DecoderInputStream(InputStream is, IKscDecoder decoder, int bsize) {
    super(is);
    if (is == null || decoder == null) {
      throw new NullPointerException();
    }
    if (bsize <= 0) {
      throw new IllegalArgumentException();
    }
    decoder.init();
    this.decoder = decoder;
    buf = new byte[bsize];
    if (in instanceof RandomFileInputStream) {
      startPos = ((RandomFileInputStream) in).getCurrentPos();
    } else {
      startPos = 0;
    }

  }

  /**
   * Reads a single byte of decompressed data.
   * 
   * @return the byte read.
   * @throws IOException if an error occurs reading the byte.
   */
  @Override
  public int read() throws IOException {
    byte[] b = new byte[1];
    if (read(b, 0, 1) == -1) {
      return -1;
    }
    return b[0] & 0xff;
  }

  /**
   * Reads up to {@code nbytes} of decompressed data and stores it in {@code buffer} starting at
   * {@code off}.
   * 
   * @param buffer the buffer to write data to.
   * @param off offset in buffer to start writing.
   * @param nbytes number of bytes to read.
   * @return Number of uncompressed bytes read
   * @throws IOException if an IOException occurs.
   */
  @Override
  public int read(byte[] buffer, int off, int nbytes) throws IOException {
    /* archive.1E=Stream is closed */
    if (closed) {
      throw new IOException("Stream is closed"); //$NON-NLS-1$
    }

    if (null == buffer) {
      throw new NullPointerException();
    }

    if (off < 0 || nbytes < 0 || off + nbytes > buffer.length) {
      throw new IndexOutOfBoundsException();
    }

    if (nbytes == 0) {
      return 0;
    }

    // if (eof) {
    // return -1;
    // }

    // avoid int overflow, check null buffer
    if (off > buffer.length || nbytes < 0 || off < 0
        || buffer.length - off < nbytes) {
      throw new ArrayIndexOutOfBoundsException();
    }

    try {
      fill();

      int result = decoder.readDecodeData(buffer, off, nbytes);
      if (result > 0) {
        // byte[] b = new byte[result];
        // System.arraycopy(buffer, off, b, 0, result);
        //
        // Log.d("TestRunner",
        // ((RandomFileInputStream) in).getCurrentPos() + ":"
        // + Arrays.toString(b));
        return result;
      } else if (eof && decoder.canEnd()) {
        return -1;
      } else if (eof) {
        throw new EOFException();
      } else {
        throw (IOException) new IOException()
            .initCause(new DataFormatException(
                "Failed read data from decoder."));
      }
    } catch (DataFormatException e) {
      if (eof) {
        throw new EOFException();
      }
      throw (IOException) (new IOException().initCause(e));
    }
  }

  /**
   * Fills the input buffer with data to be decompressed.
   * 
   * @throws IOException if an {@code IOException} occurs.
   */
  private void fill() throws IOException, DataFormatException {
    if (closed) {
      throw new IOException("Stream is closed"); //$NON-NLS-1$
    }

    int len;
    int lastlen = Integer.MAX_VALUE;
    while (!eof && (len = decoder.needFill()) > 0) {
      if (len > lastlen) {
        throw new DataFormatException("Needed data is increased");
      }
      lastlen = len;
      if (Thread.interrupted()) {
        throw new InterruptedIOException();
      }

      len = Math.min(len, buf.length);
      len = in.read(buf, 0, len);
      if (len >= 0) {
        decoder.fillData(buf, 0, len);
      } else {
        eof = true;
      }
    }
  }

  /**
   * Skips up to n bytes of uncompressed data.
   * 
   * @param nbytes the number of bytes to skip.
   * @return the number of uncompressed bytes skipped.
   * @throws IOException if an error occurs skipping.
   */
  @Override
  public long skip(long nbytes) throws IOException {
    if (nbytes >= 0) {
      long count = 0, rem = 0;
      while (count < nbytes) {
        if (Thread.interrupted()) {
          throw new InterruptedIOException();
        }

        int x = read(buf, 0,
            (rem = nbytes - count) > buf.length ? buf.length
                : (int) rem);
        if (x == -1) {
          // BEGIN android-removed
          // eof = true;
          // END android-removed
          return count;
        }
        count += x;
      }
      return count;
    }
    throw new IllegalArgumentException();
  }

  /**
   * Returns 0 when when this stream has exhausted its input; and 1 otherwise.
   * A result of 1 does not guarantee that further bytes can be returned, with
   * or without blocking.
   * <p>
   * Although consistent with the RI, this behavior is inconsistent with
   * {@link InputStream#available()}, and violates the <a
   * href="http://en.wikipedia.org/wiki/Liskov_substitution_principle">Liskov Substitution
   * Principle</a>. This method should not be used.
   * 
   * @return 0 if no further bytes are available. Otherwise returns 1, which
   *         suggests (but does not guarantee) that additional bytes are
   *         available.
   * @throws IOException If an error occurs.
   */
  @Override
  public int available() throws IOException {
    if (closed) {
      // archive.1E=Stream is closed
      throw new IOException("Stream is closed"); //$NON-NLS-1$
    }
    if (eof) {
      return 0;
    }
    return 1;
  }

  /**
   * Closes the input stream.
   * 
   * @throws IOException If an error occurs closing the input stream.
   */
  @Override
  public void close() throws IOException {
    if (!closed) {
      try {
        decoder.end();
      } finally {
        closed = true;
        eof = true;
        super.close();
      }
    }
  }

  /**
   * Marks the current position in the stream. This implementation overrides
   * the super type implementation to do nothing at all.
   * 
   * @param readlimit of no use.
   */
  @Override
  public void mark(int readlimit) {
    if (in instanceof RandomFileInputStream) {
      markPos = ((RandomFileInputStream) in).getCurrentPos();
    }
  }

  /**
   * Reset the position of the stream to the last marked position. This
   * implementation overrides the supertype implementation and always throws
   * an {@link IOException IOException} when called.
   * 
   * @throws IOException if the method is called
   */
  @Override
  public void reset() throws IOException {
    if (!markSupported()) {
      throw new IOException();
    }
    if (markPos >= 0) {
      if (((RandomFileInputStream) in).getCurrentPos() > markPos) {
        eof = false;
      }

      ((RandomFileInputStream) in).moveToPos(markPos);
      decoder.init();
      decoder.skip(markPos - startPos);
    }
  }

  /**
   * Returns whether the receiver implements {@code mark} semantics. This type
   * does not support {@code mark()}, so always responds {@code false}.
   * 
   * @return false, always
   */
  @Override
  public boolean markSupported() {
    return (in instanceof RandomFileInputStream) && decoder.supportRepeat();
  }

}
