
package cn.kuaipan.android.kss.download;

import cn.kuaipan.android.kss.download.LoadMap.Space;

public class LoadRecorder {
    private LoadMap map;
    private final Space space;

    // private final long start;

    LoadRecorder(LoadMap map, Space space) {
        this.map = map;
        this.space = space;
        // start = space.start;
    }

    public void add(int size) {
        if (map == null) {
            throw new RuntimeException("The recoder has been recycled");
        }
        space.remove(size);

        map.onSpaceRemoved(size);
    }

    public boolean isCompleted() {
        return space.size() <= 0;
    }

    public void recycle() {
        if (map != null) {
            map.recycleRecorder(this);
            map = null;
        }
    }

    long getStart() {
        return space.getStart();
    }

    public long size() {
        return space.size();
    }

    Space getSpace() {
        return space;
    }

    @Override
    protected void finalize() throws Throwable {
        recycle();
        super.finalize();
    }
}
