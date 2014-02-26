package org.sky.base.http.multipart;

import java.util.Arrays;

import org.apache.http.NameValuePair;
import org.apache.http.util.LangUtils;

import android.text.TextUtils;

public class ByteArrayValuePair implements NameValuePair, Cloneable {

  private final String name;
  private final String filename;
  private final byte[] data;

  public ByteArrayValuePair(String name, String filename, byte[] data) {
    this.name = name;
    this.filename = filename;
    this.data = data;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getValue() {
    return filename;
  }

  public byte[] getData() {
    return data;
  }

  /**
   * Get a string representation of this pair.
   * 
   * @return A string representation.
   */
  public String toString() {
    StringBuilder buffer = new StringBuilder(this.name);
    if (this.filename != null && data != null) {
      buffer.append("=File[name=");
      buffer.append(this.filename);
      buffer.append(", data=");
      buffer.append(Arrays.toString(data));
      buffer.append("]");
    }
    return buffer.toString();
  }

  public boolean equals(final Object object) {
    if (object == null)
      return false;
    if (this == object)
      return true;
    if (object instanceof ByteArrayValuePair) {
      ByteArrayValuePair that = (ByteArrayValuePair) object;
      return TextUtils.equals(this.name, that.name)
          && TextUtils.equals(this.filename, that.filename)
          && Arrays.equals(this.data, that.data);
    } else {
      return false;
    }
  }

  public int hashCode() {
    int hash = LangUtils.HASH_SEED;
    hash = LangUtils.hashCode(hash, this.name);
    hash = LangUtils.hashCode(hash, this.filename);
    hash = LangUtils.hashCode(hash, Arrays.hashCode(this.data));
    return hash;
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }
}
