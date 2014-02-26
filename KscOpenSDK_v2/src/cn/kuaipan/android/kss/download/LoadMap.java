
package cn.kuaipan.android.kss.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.kss.download.DownloadRequest.Block;

public class LoadMap {
    private static final String LOG_TAG = "LoadMap";

    private static final int MAX_VERIFY_COUNT = 2;
    private final static int MIN_OBTAIN_SIZE = 64 * 1024;

    private final static String KEY_BLOCKS = "blocks";
    private final static String KEY_BLOCK_START = "block_start";
    private final static String KEY_BLOCK_END = "block_end";
    private final static String KEY_SPACE = "space_info";

    enum VerifyState {
        NOT_VERIFY, VERIFING, VERIFIED
    }

    // private final long mTotal;
    // private final IKscTransferListener mListener;

    private final HashMap<Space, LoadRecorder> mRecorders;
    private final ArrayList<Space> mEmptySpaces;

    private final BlockSpace[] mBlocks;

    private IKscTransferListener mListener;

    public LoadMap(DownloadRequest request, IKscTransferListener listener) {
        // mTotal = request.getTotalSize();
        mRecorders = new HashMap<LoadMap.Space, LoadRecorder>();
        mEmptySpaces = new ArrayList<LoadMap.Space>();

        final int count = request.getBlockCount();
        mBlocks = new BlockSpace[count];

        int pos = 0;
        for (int i = 0; i < count; i++) {
            Block block = request.getBlock(i);

            BlockSpace blockSpace = new BlockSpace(block, pos);
            mBlocks[i] = blockSpace;

            mEmptySpaces.addAll(Arrays.asList(blockSpace.getAllSpaces()));
            pos += block.size;
        }

        mListener = listener;
        if (listener != null) {
            listener.setReceiveTotal(request.getTotalSize());
        }
    }

    // public synchronized List<BlockSpace> getNeedVerifyBlocks() {
    // ArrayList<BlockSpace> blocks = new ArrayList<BlockSpace>();
    // for (int i = 0; i <mBlocks.length; i++) {
    // BlockSpace block = mBlocks[i];
    // if (block.needVerify()) {
    // blocks.add(block);
    // }
    // }
    // return blocks;
    // }

    void verify(KssAccessor accessor, boolean increaseFailCount)
            throws IOException {
        for (int i = 0; i < mBlocks.length; i++) {
            BlockSpace block = mBlocks[i];
            if (!block.verify(accessor, increaseFailCount)) {
                resetBlock(i);
                if (mListener != null) {
                    mListener.received(block.start - block.end);
                }
            }
        }
    }

    /**
     * Obtain a {@link LoadRecorder} for recorder load process to
     * {@link LoadMap}. Return a new instance if success obtain, otherwise
     * return null. The returned recorder must call
     * {@link LoadRecorder#recycle()} if not needed.
     * 
     * @return a new LoadRecorder if success obtain, otherwise return null.
     */
    synchronized LoadRecorder obtainRecorder() {
        Space space = allocEmptySpace();
        if (space != null) {
            LoadRecorder recorder = new LoadRecorder(this, space);
            mRecorders.put(space, recorder);
            return recorder;
        }

        space = findMaxInUsedSpace();
        if (space == null || space.size() <= MIN_OBTAIN_SIZE) {
            return null;
        }

        space = space.halve();
        LoadRecorder recorder = new LoadRecorder(this, space);
        mRecorders.put(space, recorder);
        return recorder;
    }

    /**
     * Recycle the space in {@link LoadRecorder} to unused. Only after recycled,
     * the space can be full obtain. Otherwise only can do {@link Space#halve()}
     * when obtain.
     */
    synchronized void recycleRecorder(LoadRecorder recorder) {
        Space space = recorder.getSpace();
        if (mRecorders.remove(space) == null) {
            return;
        }

        if (space.tryMerge()) {
            return;
        }

        mEmptySpaces.add(space);
    }

    /**
     * Reset a Block space. After reset, the space of block will be empty and
     * can be obtain; all LoadRecorder obtained before should be stop.
     * 
     * @param index The index of block need to reset.
     */
    void resetBlock(int index) {
        if (index < 0 || index >= mBlocks.length) {
            throw new IndexOutOfBoundsException();
        }

        BlockSpace block = mBlocks[index];
        synchronized (block) {
            for (Space space : block.getAllSpaces()) {
                LoadRecorder recorder = mRecorders.remove(space);
                if (recorder != null) {
                    recorder.recycle();
                }

                mEmptySpaces.remove(space);
            }

            block.reset();
            mEmptySpaces.addAll(Arrays.asList(block.getAllSpaces()));
        }

        // if (mListener != null) {
        // mListener.setReceivePos(getSpaceSize());
        // }
    }

    private Space allocEmptySpace() {
        long maxSize = -1;
        int maxIndex = -1;

        for (int i = 0; i < mEmptySpaces.size(); i++) {
            Space space = mEmptySpaces.get(i);
            long size = space.size();
            if (maxSize < size) {
                maxSize = size;
                maxIndex = i;
            }
        }

        if (maxIndex >= 0) {
            return mEmptySpaces.remove(maxIndex);
        } else {
            return null;
        }
    }

    // XXX only call in init, otherwise the size can be changed by threads
    public long getSpaceSize() {
        long result = 0;
        for (BlockSpace space : mBlocks) {
            result += space.size();
        }
        return result;
    }

    public void onSpaceRemoved(int size) {
        if (mListener != null) {
            mListener.received(size);
        }
    }

    private Space findMaxInUsedSpace() {
        long maxSize = -1;
        Space result = null;
        for (Space space : mRecorders.keySet()) {
            long size = space.size();
            if (maxSize < size) {
                maxSize = size;
                result = space;
            }
        }
        return result;
    }

    public void initSize(long length) {
        synchronized (this) {
            final int blockConut = mBlocks.length;
            int pos = 0;
            mEmptySpaces.clear();

            if (mListener != null) {
                mListener.setReceivePos(0);
            }

            for (int i = 0; i < blockConut; i++) {
                BlockSpace block = mBlocks[i];
                block.reset();
                long size = block.size();
                if (length >= pos + size) {
                    block.setSpaces(new long[0]);
                    if (mListener != null) {
                        mListener.received(block.end - block.start);
                    }
                } else {
                    // long start = Math.max(pos, length);
                    long start = pos;
                    long end = pos + size;

                    block.setSpaces(new long[] {
                            start, end
                    });
                }
                mEmptySpaces.addAll(Arrays.asList(block.getAllSpaces()));
                pos += size;
            }
        }
    }

    public boolean load(Bundle b) {
        if (b == null) {
            return false;
        }

        try {
            ArrayList<Bundle> blockData = b.getParcelableArrayList(KEY_BLOCKS);

            final int blockConut = blockData.size();
            if (blockConut != mBlocks.length) {
                Log.w(LOG_TAG,
                        "Block count is wrong in kinfo, ignore saved map");
                return false;
            }

            for (int i = 0; i < blockConut; i++) {
                Bundle bundle = blockData.get(i);
                long start = bundle.getLong(KEY_BLOCK_START);
                long end = bundle.getLong(KEY_BLOCK_END);
                BlockSpace block = mBlocks[i];
                if (block.start != start || block.end != end) {
                    Log.w(LOG_TAG,
                            "Block start/ends is wrong in kinfo, ignore saved map");
                    return false;
                }
                // XXX check all space is in block one by one
            }

            synchronized (this) {
                mEmptySpaces.clear();

                if (mListener != null) {
                    mListener.setReceivePos(0);
                }

                long initSize = 0;
                for (int i = 0; i < blockConut; i++) {
                    Bundle bundle = blockData.get(i);
                    BlockSpace block = mBlocks[i];
                    block.reset();
                    long[] spaces = bundle.getLongArray(KEY_SPACE);
                    block.setSpaces(spaces);
                    mEmptySpaces.addAll(Arrays.asList(block.getAllSpaces()));

                    if (mListener != null) {
                        long size = block.end - block.start - block.size();
                        initSize += size;
                    }
                }

                if (mListener != null && initSize != 0) {
                    mListener.received(initSize);
                }
            }
            return true;
        } catch (Throwable t) {
            Log.w(LOG_TAG,
                    "Meet exception Block count is wrony in kinfo, ignore saved map");
            return false;
        }
    }

    public void save(Bundle b) {
        if (b == null) {
            return;
        }

        final int blockConut = mBlocks.length;
        ArrayList<Bundle> blockData = new ArrayList<Bundle>(blockConut);
        for (int i = 0; i < blockConut; i++) {
            BlockSpace block = mBlocks[i];
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_BLOCK_START, block.start);
            bundle.putLong(KEY_BLOCK_END, block.end);

            ArrayList<Space> spaces = block.spaces;
            int conut = spaces.size();

            long[] pos = new long[2 * conut];
            for (int j = 0; j < conut; j++) {
                Space space = spaces.get(j);

                pos[2 * j] = space.start;
                pos[2 * j + 1] = space.end;
            }
            bundle.putLongArray(KEY_SPACE, pos);
            blockData.add(bundle);
        }

        b.putParcelableArrayList(KEY_BLOCKS, blockData);
    }

    public boolean isCompleted() {
        for (BlockSpace block : mBlocks) {
            if (block.size() > 0 || !block.isVerified()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return Arrays.toString(mBlocks);
    }

    class Space {
        private final BlockSpace block;
        private long start;
        private long end;

        public Space(BlockSpace block, long start, long end) {
            if (end < start) {
                throw new IndexOutOfBoundsException();
            }
            this.block = block;
            this.start = start;
            this.end = end;
        }

        long getStart() {
            return start;
        }

        void remove(int size) {
            // if (mListener != null) {
            // mListener.received(Math.min(size, end - start));
            // }
            synchronized (block) {
                start = Math.min(start + size, end);
            }
        }

        private Space halve() {
            long newStart = start + (end - start) / 2;
            if ((newStart % 1024) > 0) {
                newStart = (newStart / 1024 + 1) * 1024;
            }

            Space result = new Space(block, newStart, end);
            block.spaces.add(this);

            end = newStart;
            return result;
        }

        long size() {
            synchronized (block) {
                return end - start;
            }
        }

        private boolean tryMerge() {
            return block.tryMerge(this);
        }

        private boolean tryMerge(Space space) {
            if (space.start != end) {
                return false;
            }

            end = space.end;
            return true;
        }

        @Override
        public String toString() {
            return start + "-" + end;
        }
    }

    class BlockSpace {
        private final String sha1;
        private final long start;
        private final long end;
        private final ArrayList<Space> spaces;

        private VerifyState verifyState;
        private int verifyFailCount;

        public BlockSpace(Block block, long start) {
            sha1 = block.sha1;
            this.start = start;
            this.end = start + block.size;
            spaces = new ArrayList<LoadMap.Space>();
            verifyFailCount = 0;
            reset();
        }

        public boolean isVerified() {
            return verifyState == VerifyState.VERIFIED;
        }

        public synchronized void setSpaces(long[] spaceCfg) {
            spaces.clear();
            verifyState = VerifyState.NOT_VERIFY;
            if (spaceCfg == null || spaceCfg.length % 2 != 0) {
                spaces.add(new Space(this, start, end));
            }

            final int count = spaceCfg.length / 2;
            for (int i = 0; i < count; i++) {
                spaces.add(new Space(this, spaceCfg[2 * i], spaceCfg[2 * i + 1]));
            }
        }

        private synchronized void reset() {
            verifyState = VerifyState.NOT_VERIFY;
            spaces.clear();
            spaces.add(new Space(this, start, end));
        }

        private synchronized boolean tryMerge(Space space) {
            if (space.size() <= 0) {
                spaces.remove(space);
                return true;
            }

            for (Space child : spaces) {
                if (child != space && child.tryMerge(space)) {
                    return true;
                }
            }

            return false;
        }

        private synchronized Space[] getAllSpaces() {
            return spaces.toArray(new Space[spaces.size()]);
        }

        private synchronized long size() {
            long result = 0;
            for (Space child : spaces) {
                result += child.size();
            }
            return result;
        }

        private synchronized boolean verify(KssAccessor accessor,
                boolean increaseFailCount) throws IOException {
            if (verifyState != VerifyState.NOT_VERIFY || size() > 0
                    || verifyFailCount >= MAX_VERIFY_COUNT) {
                return true;
            }

            verifyState = VerifyState.VERIFING;
            boolean verifyPassed = false;
            try {
                verifyPassed = _verify(accessor);
                if (!verifyPassed) {
                    if (increaseFailCount) {
                        verifyFailCount++;
                    }

                    if (verifyFailCount >= MAX_VERIFY_COUNT) {
                        throw new IOException(
                                "Sha1 verify failed more than MAX_VERIFY_COUNT");
                    }
                }
                return verifyPassed;
            } finally {
                if (verifyPassed) {
                    verifyState = VerifyState.VERIFIED;
                } else {
                    verifyState = VerifyState.NOT_VERIFY;
                }
            }
        }

        private boolean _verify(KssAccessor accessor) {
            boolean result = false;
            accessor.lock();
            try {
                String sha1 = accessor.sha1(start, end - start);
                result = sha1 == null ? false : sha1
                        .equalsIgnoreCase(this.sha1);
            } catch (Exception e) {
                Log.w(LOG_TAG, "Meet exception when verify sha1.", e);
            } finally {
                accessor.unlock();
            }

            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Block(");
            builder.append(start);
            builder.append("-");
            builder.append(end);
            builder.append("):");
            if (spaces.isEmpty()) {

                builder.append(verifyState);
            } else {
                builder.append(Arrays.toString(spaces.toArray()));
            }

            return builder.toString();
        }
    }

    public long getBlockStart(long start) {
        if (start < 0) {
            throw new IndexOutOfBoundsException();
        }
        long result = -1;
        for (BlockSpace block : mBlocks) {
            if (start >= block.start && start < block.end) {
                result = block.start;
                break;
            }
        }
        if (result < 0) {
            throw new IndexOutOfBoundsException();
        }

        return result;
    }

}
