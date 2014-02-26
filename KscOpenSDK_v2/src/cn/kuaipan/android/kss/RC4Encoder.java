
package cn.kuaipan.android.kss;

import java.security.InvalidKeyException;

import cn.kuaipan.android.http.IKscDecoder;
import cn.kuaipan.android.utils.RC4;

public class RC4Encoder implements IKscDecoder {

    private static final int BUF_SIZE = 8 * 1024;

    private final byte[] key;
    private final RC4 rc4;

    private byte[] buf;
    private int bufUsed = 0;

    public RC4Encoder(byte[] key) throws InvalidKeyException {
        this.key = key;
        rc4 = new RC4();
        rc4.makeKey(key);
        buf = new byte[BUF_SIZE];
    }

    @Override
    public void init() {
        try {
            rc4.makeKey(key);
            bufUsed = 0;
        } catch (InvalidKeyException e) {
            // ignore
        }
    }

    @Override
    public boolean supportRepeat() {
        return true;
    }

    @Override
    public void skip(long size) {
        try {
            rc4.makeKey(key);
            bufUsed = 0;
            rc4.skip(size);
        } catch (InvalidKeyException e) {
            // ignore
        }
    }

    @Override
    public void end() {
        bufUsed = 0;
    }

    @Override
    public int needFill() {
        return Math.max(BUF_SIZE - bufUsed, 0);
    }

    @Override
    public synchronized void fillData(byte[] input, int inputOffset,
            int inputLen) {
        final int needSize = bufUsed + inputLen;

        if (needSize > buf.length) {
            byte[] old = buf;
            int newSize = old.length << 1;
            while (needSize > newSize) {
                newSize = newSize << 1;
            }

            buf = new byte[newSize];
            System.arraycopy(old, 0, buf, 0, bufUsed);
        }

        rc4.genRC4(input, inputOffset, inputLen, input, inputOffset);
        System.arraycopy(input, inputOffset, buf, bufUsed, inputLen);
        bufUsed = needSize;
    }

    @Override
    public synchronized int readDecodeData(byte[] output, int outputOffset,
            int outputLen) {
        if (bufUsed <= 0) {
            return 0;
        }

        int len = Math.min(bufUsed, outputLen);
        len = Math.min(len, output.length - outputOffset);

        System.arraycopy(buf, 0, output, outputOffset, len);
        int rel = bufUsed - len;
        if (rel > 0) {
            System.arraycopy(buf, len, buf, 0, rel);
        }

        bufUsed = rel;
        return len;
    }

    @Override
    public boolean canEnd() {
        return true;
    }

    @Override
    public RC4Encoder clone() {
        try {
            return new RC4Encoder(key);
        } catch (InvalidKeyException e) {
            return null;
        }
    }
}
