package cn.kuaipan.android.http;

import java.util.zip.DataFormatException;

public interface IKscDecoder extends Cloneable {
  /**
   * Init Decoder
   */
  void init();

  boolean supportRepeat();

  void skip(long size);

  void end();

  int needFill();

  /**
   * fill date to decoder
   * 
   * @param input
   * @param inputOffset
   * @param inputLen
   * @return true if data is filled, false if no buffer to fill.
   */
  void fillData(byte[] input, int inputOffset, int inputLen);

  /**
   * Read decoded data from decoder's buffer
   * 
   * @param output
   * @param outputOffset
   * @param inputOffset
   * @return
   */
  int readDecodeData(byte[] output, int outputOffset, int outputLen)
      throws DataFormatException;

  boolean canEnd();

  IKscDecoder clone();

}
