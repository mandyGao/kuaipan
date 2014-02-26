package org.sky.base.http.multipart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NullCipher;

public class CipherFilePartSource extends FilePartSource {
  private Cipher cipher;
  private long length = -1;

  public CipherFilePartSource(File file, Cipher cipher)
      throws FileNotFoundException {
    super(file);
    this.cipher = cipher;
  }

  public CipherFilePartSource(String fileName, File file, Cipher cipher)
      throws FileNotFoundException {
    super(fileName, file);
    this.cipher = cipher;
  }

  @Override
  public InputStream createInputStream() throws IOException {
    InputStream in = super.createInputStream();
    if (cipher == null) {
      return new CipherInputStream(in, new NullCipher());
    } else {
      return new CipherInputStream(in, cipher);
    }
  }

  @Override
  public long getLength() {
    if (length == -1) {
      byte[] temp = new byte[1024];
      int size = 0;
      InputStream in = null;
      try {
        in = createInputStream();
        int i;
        while ((i = in.read(temp)) >= 0) {
          size += i;
        }
        length = size;
      } catch (IOException e) {
        length = 0;
      } finally {
        try {
          in.close();
        } catch (Exception e) {
          // ignore
        }
      }
    }
    return length;
  }
}
