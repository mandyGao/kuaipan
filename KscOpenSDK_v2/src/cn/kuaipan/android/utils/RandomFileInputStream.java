package cn.kuaipan.android.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;

public class RandomFileInputStream extends InputStream {

  private RandomAccessFile rFile;
  private final File file;
  private long pos = 0;
  private long markedPos = 0;

  private boolean closed = false;

  public RandomFileInputStream(File file) throws FileNotFoundException {
    this.file = file;
    rFile = new RandomAccessFile(file, "r");
  }

  @Override
  public synchronized int read() throws IOException {
    if (Thread.interrupted()) {
      throw new InterruptedIOException();
    }
    int result = rFile.read();
    if (result >= 0) {
      pos++;
    }
    return result;
  }

  @Override
  public synchronized int available() throws IOException {
    return (int) (rFile.length() - pos);
  }

  @Override
  public synchronized void close() throws IOException {
    rFile.close();
    closed = true;
  }

  public synchronized void moveToPos(long pos) throws IOException {
    if (pos < rFile.length()) {
      rFile.seek(pos);
      this.pos = pos;
    } else {
      throw new IOException("Seek possion is not availabel");
    }
  }

  public synchronized long getCurrentPos() {
    return pos;
  }

  @Override
  public void mark(int readlimit) {
    if (!closed) {
      markedPos = pos;
    }
  }

  @Override
  public boolean markSupported() {
    return true;
  }

  @Override
  public synchronized int read(byte[] b) throws IOException {
    if (Thread.interrupted()) {
      throw new InterruptedIOException();
    }

    int result = rFile.read(b);
    if (result > 0) {
      pos += result;
    }
    return result;
  }

  @Override
  public synchronized int read(byte[] b, int offset, int length)
      throws IOException {

    if (Thread.interrupted()) {
      throw new InterruptedIOException();
    }

    int result = rFile.read(b, offset, length);
    if (result > 0) {
      pos += result;
    }
    return result;
  }

  @Override
  public synchronized void reset() throws IOException {
    if (closed) {
      closed = false;
      rFile = new RandomAccessFile(file, "r");
    }

    rFile.seek(markedPos);
    pos = markedPos;
  }

  @Override
  public long skip(long count) throws IOException {
    if (count == 0) {
      return 0;
    }
    if (count < 0) {
      // KA013=Number of bytes to skip cannot be negative
      throw new IOException("Number of bytes to skip cannot be negative"); //$NON-NLS-1$
    }

    // Read and discard count bytes in 8k chunks
    long skipped = 0, numSkip;
    int chunk = count < 8192 ? (int) count : 8192;
    synchronized (this) {
      for (long i = count / chunk; i >= 0; i--) {
        if (Thread.interrupted()) {
          throw new InterruptedIOException();
        }

        numSkip = rFile.skipBytes(chunk);
        skipped += numSkip;
        pos += numSkip;
        if (numSkip < chunk) {
          return skipped;
        }
      }
      return skipped;
    }
  }
}
