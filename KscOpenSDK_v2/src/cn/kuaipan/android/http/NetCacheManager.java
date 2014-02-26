package cn.kuaipan.android.http;

import java.io.File;

import org.sky.base.utils.FileUtils;
import org.sky.base.utils.TwoKeyHashMap;

import android.content.Context;
import android.text.TextUtils;
import cn.kuaipan.android.sdk.exception.ErrorCode;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;

public class NetCacheManager {
  private static final String CACHE_DIR = "net_cache";
  private static final String CACHE_FILE = "%08d.tmp";

  private static final TwoKeyHashMap<Boolean, String, NetCacheManager> sCaches =
      new TwoKeyHashMap<Boolean, String, NetCacheManager>();

  public static synchronized NetCacheManager getInstance(Context context,
      boolean external) {

    return getInstance(context, external, null);
  }

  public static synchronized NetCacheManager getInstance(Context context,
      boolean external, String dirName) {
    if (TextUtils.isEmpty(dirName)) {
      dirName = CACHE_DIR;
    }
    NetCacheManager result = sCaches.get(external, dirName);

    if (result == null) {
      result = new NetCacheManager(context, external, dirName);
      sCaches.put(external, dirName, result);
    }
    return result;
  }

  private final Context mContext;
  private final boolean mExternal;
  private final String mDirName;

  private String mFolderPath;
  private int mLatestId = 0;

  private NetCacheManager(Context context, boolean external, String dirName) {
    if (context == null) {
      throw new NullPointerException("Context can't be null.");
    }
    mContext = context;
    mExternal = external;
    mDirName = dirName;

    final File folder = FileUtils.getCacheDir(context, dirName, external);
    if (folder != null) {
      mFolderPath = folder.getAbsolutePath();
      new Thread() {
        public void run() {
          FileUtils.deleteChildren(folder);
        }
      }.start();
    }
  }

  public File assignCache() {
    File result = null;
    do {
      result = getNextCache();
    } while (result.exists());

    result.deleteOnExit();
    return result;
  }

  private File getNextCache() {
    int id;
    synchronized (this) {
      id = ++mLatestId;
    }
    String fileName = String.format(CACHE_FILE, id);
    File folder = FileUtils.getCacheDir(mContext, mDirName, mExternal);
    if (folder == null) {
      throw new KscRuntimeException(ErrorCode.LIMIT_NO_SPACE);
    }
    mFolderPath = folder.getAbsolutePath();
    return new File(folder, fileName);
  }

  public void releaseCache(File file) {
    if (file == null || !TextUtils.equals(mFolderPath, file.getParent())) {
      return;
    }
    file.delete();
  }
}
