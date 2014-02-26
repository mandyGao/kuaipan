package cn.kuaipan.android.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;

public class RandomInputBuffer {

  private static final int UNIT = 8 * 1024;

  private InputStream in;
  private int pos;
  private boolean eof;

  private byte[] buf;
  private final NetCacheManager cache;
  private File bufFile;
  private boolean fileAssigned;
  private RandomAccessFile file;

  public RandomInputBuffer(InputStream in, NetCacheManager cache) {
    if (cache == null || in == null) {
      throw new IllegalArgumentException(
          "InputStream & NetCacheManager can not be null.");
    }

    this.in = in;
    this.cache = cache;
    buf = new byte[UNIT];
    pos = 0;
    eof = false;
  }

  public synchronized int available(int pos) throws IOException {
    if (in == null) {
      throw new IOException("RandomInputBuffer has been closed.");
    }

    int result;
    if (!eof) {
      result = in.available() + this.pos - pos;
    } else {
      result = this.pos - pos;
    }
    return result;
  }

  public synchronized void close() throws IOException {
    if (in != null) {
      in.close();
      in = null;
    }

    if (file != null) {
      file.close();
      file = null;
    }

    if (bufFile != null) {
      cache.releaseCache(bufFile);
      bufFile = null;
    }

    buf = null;
    pos = 0;
    eof = false;
  }

  public int read(int from) throws IOException {
    byte[] result = new byte[1];
    if (read(from, result) > 0) {
      return result[0];
    }
    return -1;
  }

  public int read(int from, byte[] buffer) throws IOException {
    return read(from, buffer, 0, buffer.length);
  }

  public synchronized int read(int from, byte[] buffer, int offset, int length)
      throws IOException {
    if (in == null) {
      throw new IOException("RandomInputBuffer has been closed.");
    }

    if (buffer == null) {
      throw new NullPointerException("buffer == null");
    }
    if ((offset | length) < 0 || offset > buffer.length - length) {
      throw new IndexOutOfBoundsException();
    }
    if (length == 0) {
      return 0;
    }

    int dest = from + length;
    if (dest > pos) {
      fillBuf(dest);
    }

    return readFromBuf(from, buffer, offset, length);
  }

  private int readFromBuf(int from, byte[] buffer, int offset, int length)
      throws IOException {
    if (length == 0) {
      return 0;
    }

    if (eof && from >= pos) {
      return -1;
    }

    if (from > pos) {
      throw new IndexOutOfBoundsException("from > pos");
    }

    if (from == pos) {
      return 0;
    }

    int result = Math.min(pos - from, length);
    if (file == null) {
      System.arraycopy(buf, from, buffer, offset, result);
    } else {
      file.seek(from);
      result = file.read(buffer, offset, result);
    }
    return result;
  }

  private void fillBuf(int dest) throws IOException {
    if (eof || dest <= pos) {
      return;
    }

    dest = (dest % UNIT == 0) ? dest : (dest / UNIT * UNIT + UNIT);
    if (!fileAssigned && dest > buf.length) {
      fileAssigned = true;

      try {
        bufFile = cache.assignCache();
        if (bufFile != null) {
          file = new RandomAccessFile(bufFile, "rw");
          file.write(buf, 0, pos);
        }
      } catch (InterruptedIOException e) {
        throw e;
      } catch (Exception e) {
        try {
          if (file != null) {
            file.close();
          }
        } catch (InterruptedIOException e1) {
          throw e1;
        } catch (Exception e1) {
          // ignore;
        }
        file = null;
      }
    }

    if (file == null) {
      if (dest > buf.length) {
        int newLength = buf.length * 2;
        while (newLength < dest) {
          newLength = newLength * 2;
        }

        byte[] newbuf = new byte[newLength];
        System.arraycopy(buf, 0, newbuf, 0, buf.length);
        buf = newbuf;
      }

      int bytesread = in.read(buf, pos,
          Math.min(buf.length - pos, dest - pos));
      if (bytesread >= 0) {
        pos += bytesread;
      } else {
        eof = true;
      }
    } else {
      int bytesread = in.read(buf, 0, Math.min(buf.length, dest - pos));
      if (bytesread >= 0) {
        file.seek(pos);
        file.write(buf, 0, bytesread);
        pos += bytesread;
      } else {
        eof = true;
      }
    }
  }
}
