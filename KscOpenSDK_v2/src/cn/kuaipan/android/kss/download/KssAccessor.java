
package cn.kuaipan.android.kss.download;

import cn.kuaipan.android.utils.Encode;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.ReentrantLock;

public class KssAccessor {

    private final RandomAccessFile mFileAccessor;
    // private final File mFile;

    private boolean mClosed = false;
    private final FileLock mFilelocker;
    private final ReentrantLock mLocker = new ReentrantLock();

    public KssAccessor(File file) throws IOException {
        // mFile = file;
        mFileAccessor = new RandomAccessFile(file, "rws");

        FileChannel channel = mFileAccessor.getChannel();
        mFilelocker = channel.tryLock();
        if (mFilelocker == null) {
            throw new IOException("Failed Lock the target file: " + file);
        }
    }

    public void lock() {
        mLocker.lock();
    }

    public void unlock() {
        mLocker.unlock();
    }

    public String sha1(long start, long len) throws IOException {
        if (mClosed) {
            throw new IOException();
        }
        lock();
        try {
            return Encode.SHA1Encode(mFileAccessor, start, len);
        } finally {
            unlock();
        }

    }

    public int write(byte[] buffer, int offset, int count, LoadRecorder recorder)
            throws IOException {
        if (mClosed) {
            throw new IOException();
        }

        if (Thread.interrupted()) {
            throw new InterruptedIOException();
        }

        lock();
        try {
            if (recorder != null) {
                mFileAccessor.seek(recorder.getStart());
                count = (int) Math.min(count, recorder.size());
            }
            mFileAccessor.write(buffer, offset, count);
            if (recorder != null) {
                recorder.add(count);
            }
            return count;
        } finally {
            unlock();
        }
    }

    public void inflate(long targetSize) throws IOException {
        if (mClosed) {
            throw new IOException();
        }

        lock();
        try {
            mFileAccessor.seek(targetSize - 1);
            mFileAccessor.write(0);
        } finally {
            unlock();
        }
    }

    public void close() throws IOException {
        mClosed = true;
        if (mFilelocker != null) {
            mFilelocker.release();
        }
        if (mFileAccessor != null) {
            mFileAccessor.close();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (!mClosed) {
                close();
            }
        } finally {
            super.finalize();
        }
    }

}
