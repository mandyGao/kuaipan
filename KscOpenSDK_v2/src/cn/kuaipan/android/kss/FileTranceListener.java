
package cn.kuaipan.android.kss;

import cn.kuaipan.android.http.IKscTransferListener;

public class FileTranceListener {

    private final IKscTransferListener mTotalListener;
    private final boolean mSendMode;
    private long mOffset;

    public FileTranceListener(IKscTransferListener listener, boolean sendMode) {
        mTotalListener = listener;
        mSendMode = sendMode;
    }

    public IKscTransferListener getChunkListaner(long start) {
        if (mSendMode) {
            return new ChunkListaner(this, start, mOffset);
        } else {
            return new ChunkListaner(this, mOffset, start);
        }
    }

    public void setSendTotal(long total) {
        if (!mSendMode) {
            mTotalListener.setSendTotal(mOffset + total);
        }
    }

    public void setReceiveTotal(long total) {
        if (mSendMode) {
            mTotalListener.setReceiveTotal(mOffset + total);
        }
    }

    public void setSendPos(long pos) {
        if (!mSendMode) {
            mOffset = pos;
        }
        mTotalListener.setSendPos(pos);
    }

    public void setReceivePos(long pos) {
        if (mSendMode) {
            mOffset = pos;
        }
        mTotalListener.setReceivePos(pos);
    }

    private void sended(long len) {
        if (!mSendMode) {
            mOffset += len;
        }
        mTotalListener.sended(len);
    }

    private void received(long len) {
        if (mSendMode) {
            mOffset += len;
        }
        mTotalListener.received(len);
    }

    class ChunkListaner implements IKscTransferListener {

        private FileTranceListener mParent;

        // private final long mSendStart;
        // private final long mReceiveStart;

        private long mSendOffset;
        private long mReceiveOffset;

        ChunkListaner(FileTranceListener parent, long sended, long received) {
            mParent = parent;
            // mSendStart = sended;
            // mReceiveStart = received;
            mSendOffset = 0;
            mReceiveOffset = 0;

            setSendPos(0);
            setReceivePos(0);
        }

        @Override
        public void setSendTotal(long total) {
            mParent.setSendTotal(total);
        }

        @Override
        public void setReceiveTotal(long total) {
            mParent.setReceiveTotal(total);
        }

        @Override
        public void setSendPos(long pos) {
            long dur = pos - mSendOffset;
            sended(dur);
            // mParent.setSendPos(pos + mSendStart);
        }

        @Override
        public void setReceivePos(long pos) {
            long dur = pos - mReceiveOffset;
            received(dur);
            // mParent.setReceivePos(pos + mReceiveStart);
        }

        @Override
        public void sended(long len) {
            mParent.sended(len);
            mSendOffset += len;
        }

        @Override
        public void received(long len) {
            mParent.received(len);
            mReceiveOffset += len;
        }

    }
}
