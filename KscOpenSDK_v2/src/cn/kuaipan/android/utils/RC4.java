package cn.kuaipan.android.utils;

import java.security.InvalidKeyException;

public class RC4 {
  /** Contents of the current set S-box. */
  private final int[] sBox = new int[256];

  /**
   * The two indices for the S-box computation referred to as i and j in
   * Schneier.
   */
  private int x;
  private int y;

  /**
   * The block size of this cipher. Being a stream cipher this value is 1!
   */

  public RC4() {
    super();
  }

  /**
   * Expands a user-key to a working key schedule.
   * <p>
   * The key bytes are first extracted from the user-key and then used to build the contents of this
   * key schedule.
   * <p>
   * The method's only exceptions are when the user-key's contents are null, or a byte array of zero
   * length.
   * 
   * @param key the user-key object to use.
   * @exception InvalidKeyException if one of the following occurs:
   *              <ul>
   *              <li> key.getEncoded() == null; <li> The encoded byte array form of the key is
   *              zero-length;
   *              </ul>
   */
  public void makeKey(byte[] key) throws InvalidKeyException {
    byte[] userkey = key;
    if (userkey == null)
      throw new InvalidKeyException("Null user key");

    int len = userkey.length;
    if (len == 0)
      throw new InvalidKeyException("Invalid user key length");

    x = 0;
    y = 0;
    for (int i = 0; i < 256; i++)
      sBox[i] = i;

    int i1 = 0;
    int i2 = 0;
    int t = 0;

    for (int i = 0; i < 256; i++) {
      i2 = ((userkey[i1] & 0xFF) + sBox[i] + i2) & 0xFF;

      t = sBox[i];
      sBox[i] = sBox[i2];
      sBox[i2] = t;

      i1 = (i1 + 1) % len;
    }
  }

  /**
   * RC4 encryption/decryption.
   * 
   * @param in the input data.
   * @param inOffset the offset into in specifying where the data starts.
   * @param inLen the length of the subarray.
   * @param out the output array.
   * @param outOffset the offset indicating where to start writing into the
   *          out array.
   */
  public void genRC4(byte[] in, int inOffset, int inLen, byte[] out,
      int outOffset) {
    int xorIndex = 0;
    int t = 0;
    for (int i = 0; i < inLen; i++) {
      x = (x + 1) & 0xFF;
      y = (sBox[x] + y) & 0xFF;

      t = sBox[x];
      sBox[x] = sBox[y];
      sBox[y] = t;

      xorIndex = (sBox[x] + sBox[y]) & 0xFF;
      out[outOffset++] = (byte) (in[inOffset++] ^ sBox[xorIndex]);
    }
  }

  public void skip(long size) {
    int t = 0;
    for (long i = 0; i < size; i++) {
      x = (x + 1) & 0xFF;
      y = (sBox[x] + y) & 0xFF;

      t = sBox[x];
      sBox[x] = sBox[y];
      sBox[y] = t;
    }
  }

}
