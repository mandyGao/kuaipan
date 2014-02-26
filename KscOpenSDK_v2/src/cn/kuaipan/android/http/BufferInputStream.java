package cn.kuaipan.android.http;

import java.io.IOException;
import java.io.InputStream;

public class BufferInputStream extends InputStream {
  private static final byte[] tempBuf = new byte[8 * 1024];

  /**
   * The buffer containing the current bytes read from the target InputStream.
   */
  private RandomInputBuffer buf;

  /**
   * The currently marked position. -1 indicates no mark has been set or the
   * mark has been invalidated.
   */
  private int markpos = -1;

  /**
   * The current position within the byte array {@code buf}.
   */
  private int pos;

  /**
   * Constructs a new {@code BufferedInputStream}, providing {@code in} with {@code size} bytes of
   * buffer.
   * <p>
   * <strong>Warning:</strong> passing a null source creates a closed {@code BufferedInputStream}.
   * All read operations on such a stream will fail with an IOException.
   * 
   * @param in the {@code InputStream} the buffer reads from.
   * @param size the size of buffer in bytes.
   * @throws IllegalArgumentException if {@code size <= 0}.
   */
  public BufferInputStream(RandomInputBuffer buffer) {
    super();
    buf = buffer;
  }

  /**
   * Returns an estimated number of bytes that can be read or skipped without
   * blocking for more input. This method returns the number of bytes
   * available in the buffer plus those available in the source stream, but
   * see {@link InputStream#available} for important caveats.
   * 
   * @return the estimated number of bytes available
   * @throws IOException if this stream is closed or an error occurs
   */
  @Override
  public int available() throws IOException {
    RandomInputBuffer localIn = buf; // 'in' could be invalidated by close()
    if (localIn == null) {
      throw streamClosed();
    }
    return buf.available(pos);
  }

  private IOException streamClosed() throws IOException {
    throw new IOException("BufferedInputStream is closed");
  }

  /**
   * Closes this stream. The source stream is closed and any resources
   * associated with it are released.
   * 
   * @throws IOException if an error occurs while closing this stream.
   */
  @Override
  public void close() throws IOException {
    buf = null;
  }

  /**
   * Sets a mark position in this stream. The parameter {@code readlimit} indicates how many bytes
   * can be read before a mark is invalidated.
   * Calling {@code reset()} will reposition the stream back to the marked
   * position if {@code readlimit} has not been surpassed. The underlying
   * buffer may be increased in size to allow {@code readlimit} number of
   * bytes to be supported.
   * 
   * @param readlimit the number of bytes that can be read before the mark is
   *          invalidated.
   * @see #reset()
   */
  @Override
  public synchronized void mark(int readlimit) {
    markpos = pos;
  }

  /**
   * Indicates whether {@code BufferedInputStream} supports the {@code mark()} and {@code reset()}
   * methods.
   * 
   * @return {@code true} for BufferedInputStreams.
   * @see #mark(int)
   * @see #reset()
   */
  @Override
  public boolean markSupported() {
    return true;
  }

  /**
   * Reads a single byte from this stream and returns it as an integer in the
   * range from 0 to 255. Returns -1 if the end of the source string has been
   * reached. If the internal buffer does not contain any available bytes then
   * it is filled from the source stream and the first byte is returned.
   * 
   * @return the byte read or -1 if the end of the source stream has been
   *         reached.
   * @throws IOException if this stream is closed or another IOException
   *           occurs.
   */
  @Override
  public synchronized int read() throws IOException {
    // Use local refs since buf and in may be invalidated by an
    // unsynchronized close()
    RandomInputBuffer localIn = buf;
    if (localIn == null) {
      throw streamClosed();
    }

    int result = localIn.read(pos);
    if (result >= 0) {
      pos++;
    }
    return result;
  }

  /**
   * Reads at most {@code length} bytes from this stream and stores them in
   * byte array {@code buffer} starting at offset {@code offset}. Returns the
   * number of bytes actually read or -1 if no bytes were read and the end of
   * the stream was encountered. If all the buffered bytes have been used, a
   * mark has not been set and the requested number of bytes is larger than
   * the receiver's buffer size, this implementation bypasses the buffer and
   * simply places the results directly into {@code buffer}.
   * 
   * @param buffer the byte array in which to store the bytes read.
   * @param offset the initial position in {@code buffer} to store the bytes
   *          read from this stream.
   * @param length the maximum number of bytes to store in {@code buffer}.
   * @return the number of bytes actually read or -1 if end of stream.
   * @throws IndexOutOfBoundsException if {@code offset < 0} or {@code length < 0}, or if
   *           {@code offset + length} is greater
   *           than the size of {@code buffer}.
   * @throws IOException if the stream is already closed or another
   *           IOException occurs.
   */
  @Override
  public synchronized int read(byte[] buffer, int offset, int length)
      throws IOException {
    RandomInputBuffer localIn = buf;
    if (localIn == null) {
      throw streamClosed();
    }

    int result = localIn.read(pos, buffer, offset, length);
    if (result > 0) {
      pos += result;
    }
    return result;
  }

  /**
   * Resets this stream to the last marked location.
   * 
   * @throws IOException if this stream is closed, no mark has been set or the
   *           mark is no longer valid because more than {@code readlimit} bytes have been read
   *           since setting the mark.
   * @see #mark(int)
   */
  @Override
  public synchronized void reset() throws IOException {
    if (buf == null) {
      throw new IOException("Stream is closed");
    }
    if (-1 == markpos) {
      throw new IOException("Mark has been invalidated.");
    }
    pos = markpos;
  }

  /**
   * Skips {@code amount} number of bytes in this stream. Subsequent {@code read()}'s will not
   * return these bytes unless {@code reset()} is
   * used.
   * 
   * @param amount the number of bytes to skip. {@code skip} does nothing and
   *          returns 0 if {@code amount} is less than zero.
   * @return the number of bytes actually skipped.
   * @throws IOException if this stream is closed or another IOException
   *           occurs.
   */
  @Override
  public synchronized long skip(long amount) throws IOException {
    if (amount < 1) {
      return 0;
    }
    RandomInputBuffer localIn = buf;
    if (localIn == null) {
      throw streamClosed();
    }

    long dest = pos + amount;
    int result = 0;
    int len;
    while (dest > pos
        && (len = localIn.read(pos, tempBuf, 0,
            (int) Math.min(tempBuf.length, dest - pos))) >= 0) {
      pos += len;
      result += len;
    }

    return result;
  }
}
