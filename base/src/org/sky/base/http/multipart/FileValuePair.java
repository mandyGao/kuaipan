package org.sky.base.http.multipart;

import org.apache.http.NameValuePair;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.LangUtils;

import android.text.TextUtils;

import java.io.File;

public class FileValuePair implements NameValuePair, Cloneable {

  private final String name;
  private final File file;

  public FileValuePair(String name, File file) {
    this.name = name;
    this.file = file;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return file.getName();
  }

  public File getFile() {
    return file;
  }

  /**
   * Get a string representation of this pair.
   * 
   * @return A string representation.
   */
  public String toString() {
    // don't call complex default formatting for a simple toString

    int len = this.name.length();
    if (this.file != null)
      len += 12 + this.file.getPath().length();
    CharArrayBuffer buffer = new CharArrayBuffer(len);

    buffer.append(this.name);
    if (this.file != null) {
      buffer.append("=File[path=");
      buffer.append(this.file.getPath());
      buffer.append("]");
    }
    return buffer.toString();
  }

  public boolean equals(final Object object) {
    if (object == null)
      return false;
    if (this == object)
      return true;
    if (object instanceof FileValuePair) {
      FileValuePair that = (FileValuePair) object;
      return TextUtils.equals(this.name, that.name)
          && LangUtils.equals(this.file, that.file);
    } else {
      return false;
    }
  }

  public int hashCode() {
    int hash = LangUtils.HASH_SEED;
    hash = LangUtils.hashCode(hash, this.name);
    hash = LangUtils.hashCode(hash, this.file);
    return hash;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
