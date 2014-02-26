package cn.kuaipan.android.kss;

import java.io.File;

import org.json.JSONException;
import org.sky.base.utils.SQLUtility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import cn.kuaipan.android.http.IKscTransferListener;
import cn.kuaipan.android.http.KscHttpTransmitter;
import cn.kuaipan.android.kss.download.KssDownloaderImpl;
import cn.kuaipan.android.kss.upload.KssHelper;
import cn.kuaipan.android.kss.upload.KssUploaderImpl;
import cn.kuaipan.android.kss.upload.UploadFileInfo;
import cn.kuaipan.android.kss.upload.UploadRequest;
import cn.kuaipan.android.sdk.exception.KscException;
import cn.kuaipan.android.sdk.exception.KscRuntimeException;
import cn.kuaipan.android.sdk.exception.ServerMsgException;
import cn.kuaipan.android.utils.OAuthTimeUtils;
import cn.kuaipan.android.utils.SyncAccessor;

public class KssMaster implements KssDef {
  private static final String LOG_TAG = "KssMaster";

  private final IKssRequestor mRequestor;
  private final KscHttpTransmitter mTransmitter;

  private final DBHelper mDBHelper;
  private final KssUploader mUploader;
  private final KssDownloader mDownloader;

  public KssMaster(Context context, IKssRequestor requestor) {
    // XXX use api transmitter is better
    mTransmitter = new KscHttpTransmitter(context);
    mRequestor = requestor;
    mDBHelper = DBHelper.getInstance(context);
    mUploader = new KssUploaderImpl(mTransmitter, this, requestor);
    mDownloader = new KssDownloaderImpl(mTransmitter, this, requestor);
  }

  public KscHttpTransmitter getTransmitter() {
    return mTransmitter;
  }

  // Use for test or fix a upload error
  public void resetUploadPos(File localFile, String remotePath)
      throws KscRuntimeException, KscException, InterruptedException {
    final String token = mRequestor.getUserToken();
    final UploadFileInfo fileInfo = KssHelper.getUploadFileInfo(localFile);
    final int taskHash = getUploadHash(token, localFile, remotePath,
        fileInfo);
    KssUploadInfo info = getStoredUploadInfo(taskHash);
    if (info != null) {
      updateUploadInfo(taskHash, info, 0);
    }
  }

  public void resetUploadChunkSize() {
    if (mUploader instanceof KssUploaderImpl) {
      ((KssUploaderImpl) mUploader).resetChunkSize();
    }
  }

  public void cleanDownload(File savePath) {
    mDownloader.clean(savePath);
  }

  // Save path
  public File download(String remotePath, int rev, File savePath,
      boolean append, IKscTransferListener listener) throws Exception {
    // IKssDownloadRequestResult requestResult = mRequestor.requestDownload(
    // remotePath, rev);

    File file = mDownloader.download(remotePath, rev, savePath, append,
        listener);

    if (file != null && !file.exists()) {
      file = null;
    }

    return file;
  }

  public void upload(File localFile, String remotePath,
      IKscTransferListener listener) throws Exception {
    final String token = mRequestor.getUserToken();

    final UploadFileInfo fileInfo = KssHelper.getUploadFileInfo(localFile);
    final int taskHash = getUploadHash(token, localFile, remotePath,
        fileInfo);

    boolean errInCommit = false;
    while (true) {
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }

      KssUploadInfo info = mUploader.upload(localFile, remotePath,
          listener, taskHash, fileInfo);
      if (info == null) {
        // not need to commit
        return;
      } else if (info.request != null && !info.request.hasEmptyBlock()) {
        // need commit
        try {
          mRequestor
              .commitUpload(info.stub, info.request.getCommit());
          deleteUploadInfo(taskHash);
          return;
        } catch (ServerMsgException e) {
          if (!errInCommit) {
            deleteUploadInfo(taskHash);
            errInCommit = true;
            continue;
          }
          throw e;
        }
      }
      // need re-upload.
    }
  }

  private static volatile Looper sLooper;

  private static Looper getCommonLooper() {
    Looper result = sLooper;
    Thread t = result == null ? null : result.getThread();
    if (t == null || !t.isAlive()) {
      result = null;
    }
    if (result == null) {
      synchronized (KssMaster.class) {
        result = sLooper;
        t = result == null ? null : result.getThread();
        if (t == null || !t.isAlive()) {
          result = null;
        }
        if (result == null) {
          HandlerThread ht = new HandlerThread(
              "KssMaster - UploadRecorder",
              android.os.Process.THREAD_PRIORITY_BACKGROUND);
          ht.start();
          result = ht.getLooper();
        }
      }
    }

    return result;
  }

  private static final int MSG_UPDATE = 0;
  private static final int MSG_DELETE = 1;
  private static final int MSG_GET_POS = 2;
  private static final int MSG_GET_INFO = 3;
  private final SyncAccessor mAccessor = new SyncAccessor(getCommonLooper()) {
    @Override
    public Object handleAccess(int what, Object... objs) {
      Object result = null;
      switch (what) {
        case MSG_UPDATE: {
          Integer hash = (Integer) objs[0];
          KssUploadInfo info = (KssUploadInfo) objs[1];
          Long pos = (Long) objs[2];
          mDBHelper.update(hash, info, pos);
          break;
        }
        case MSG_DELETE: {
          Integer hash = (Integer) objs[0];
          mDBHelper.delete(hash);
          break;
        }
        case MSG_GET_POS: {
          Integer hash = (Integer) objs[0];
          result = mDBHelper.queryPos(hash);
          break;
        }
        case MSG_GET_INFO: {
          Integer hash = (Integer) objs[0];
          mDBHelper.deleteBefore(OAuthTimeUtils.currentTime()
              - CHUNK_VALIDATE_DUR);

          try {
            result = mDBHelper.queryKss(hash);
          } catch (Throwable t) {
            Log.w(LOG_TAG,
                "Meet exception when parser kss from db", t);
          }
          break;
        }
        default:
          result = super.handleAccess(what, objs);
      }
      return result;
    }
  };

  public KssUploadInfo getStoredUploadInfo(int hash)
      throws InterruptedException {
    return (KssUploadInfo) mAccessor.access(MSG_GET_INFO, hash);
    // mDBHelper.deleteBefore(OAuthTimeUtils.currentTime()
    // - CHUNK_VALIDATE_DUR);
    //
    // try {
    // return mDBHelper.queryKss(hash);
    // } catch (Throwable t) {
    // Log.w(LOG_TAG, "Meet exception when parser kss from db", t);
    // return null;
    // }
  }

  public void updateUploadInfo(int hash, KssUploadInfo info, long pos)
      throws InterruptedException {
    mAccessor.access(MSG_UPDATE, hash, info, pos);

    // mDBHelper.update(hash, info, pos);
  }

  public void deleteUploadInfo(int hash) throws InterruptedException {
    mAccessor.access(MSG_DELETE, hash);
    // mDBHelper.delete(hash);
  }

  public long getUploadPos(int hash) throws InterruptedException {
    return (Long) mAccessor.access(MSG_GET_POS, hash);
    // return mDBHelper.queryPos(hash);
  }

  private static int getUploadHash(String token, File localFile,
      String remotePath, UploadFileInfo fileInfo) {
    String sha1 = fileInfo == null ? "" : fileInfo.getSha1();

    StringBuilder builder = new StringBuilder();
    builder.append(token);
    builder.append(":");
    builder.append(localFile);
    builder.append(":");
    builder.append(remotePath);
    builder.append(":");
    builder.append(sha1);
    return builder.toString().hashCode();
  }

  private static class DBHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = "DBHelper";

    private static final String DATABASE = "ksssdk_infos.db";
    private static final String TABLE_UPLOAD = "upload_chunks";
    private static final int DB_VERSION = 1;

    private static final String TASK_HASH = "task_hash";
    private static final String STUB = "stub";
    private static final String KSS_FILE_INFO = "kss_file_info";
    private static final String KSS_REQUEST = "kss_request";
    private static final String CHUNK_POS = "chunk_pos";
    private static final String GEN_TIME = "gen_time";

    private static volatile DBHelper sInstance;

    public static DBHelper getInstance(Context context) {
      DBHelper helper = sInstance;
      if (helper == null) {
        synchronized (DBHelper.class) {
          helper = sInstance;
          if (helper == null) {
            if (context == null) {
              throw new NullPointerException(
                  "Context should not be null.");
            }
            helper = new DBHelper(context);
            sInstance = helper;
          }
        }
      }
      return helper;
    }

    private DBHelper(Context context) {
      super(context, DATABASE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      StringBuilder sb = new StringBuilder();
      sb.append(BaseColumns._ID).append(
          " INTEGER PRIMARY KEY AUTOINCREMENT, ");
      sb.append(TASK_HASH).append(" INTEGER NOT NULL UNIQUE, ");
      sb.append(STUB).append(" STRING NOT NULL, ");
      sb.append(KSS_REQUEST).append(" STRING NOT NULL, ");
      sb.append(KSS_FILE_INFO).append(" STRING NOT NULL, ");
      sb.append(CHUNK_POS).append(" LONG NOT NULL DEFAULT 0, ");
      sb.append(GEN_TIME).append(" LONG NOT NULL DEFAULT 0");
      SQLUtility.createTable(db, TABLE_UPLOAD, sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      if (oldVersion != DB_VERSION) {
        Log.w(LOG_TAG, "Destroying all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOAD);
        onCreate(db);
      }
    }

    @SuppressWarnings("unused")
    public void onDowngrade(SQLiteDatabase db, int oldVersion,
        int newVersion) {
      if (oldVersion != DB_VERSION) {
        Log.w(LOG_TAG, "Destroying all old data.");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOAD);
        onCreate(db);
      }
    }

    private static final String WHERE_DEL = SQLUtility
        .getSelectionWithTemplete(SQLUtility.WHERE_LESS, GEN_TIME);
    private static final String WHERE_QUERY = SQLUtility
        .getSelection(TASK_HASH);
    private static final String[] QUERY_POS = new String[] {
        CHUNK_POS
    };
    private static final String[] QUERY_KSS = new String[] {
        STUB, KSS_REQUEST, KSS_FILE_INFO
    };

    public void deleteBefore(long time) {
      SQLiteDatabase db = getWritableDatabase();
      db.delete(TABLE_UPLOAD, WHERE_DEL, new String[] {
          String.valueOf(time)
      });
    }

    public long queryPos(int taskHash) {
      SQLiteDatabase db = getReadableDatabase();
      Cursor c = db.query(TABLE_UPLOAD, QUERY_POS, WHERE_QUERY,
          new String[] {
          String.valueOf(taskHash)
          }, null, null, null);
      long result = -1;
      try {
        if (c != null && c.moveToFirst()) {
          result = c.getInt(c.getColumnIndex(CHUNK_POS));
        }
        return result;
      } finally {
        if (c != null) {
          c.close();
        }
      }
    }

    public KssUploadInfo queryKss(int taskHash) throws KscException,
        JSONException {
      SQLiteDatabase db = getReadableDatabase();
      Cursor c = db.query(TABLE_UPLOAD, QUERY_KSS, WHERE_QUERY,
          new String[] {
          String.valueOf(taskHash)
          }, null, null, null);
      KssUploadInfo result = null;
      try {
        if (c != null && c.moveToFirst()) {
          String stub = c.getString(c.getColumnIndex(STUB));
          String requestStr = c.getString(c
              .getColumnIndex(KSS_REQUEST));
          String fileInfoStr = c.getString(c
              .getColumnIndex(KSS_FILE_INFO));
          if (TextUtils.isEmpty(stub)
              || TextUtils.isEmpty(requestStr)
              || TextUtils.isEmpty(fileInfoStr)) {
            return null;
          }
          UploadRequest request = new UploadRequest(requestStr);
          UploadFileInfo fileInfo = new UploadFileInfo(fileInfoStr);
          result = new KssUploadInfo(fileInfo, stub, request);
        }
        return result;
      } finally {
        if (c != null) {
          c.close();
        }
      }
    }

    public void update(int taskHash, KssUploadInfo info, long pos) {
      if (info == null) {
        return;
      }

      ContentValues values = new ContentValues();
      values.put(TASK_HASH, taskHash);
      values.put(STUB, info.stub);
      values.put(KSS_FILE_INFO, info.fileInfo.toString());
      values.put(KSS_REQUEST, info.request.toString());
      values.put(CHUNK_POS, pos);
      values.put(GEN_TIME, info.request.generateTime);

      SQLiteDatabase db = getWritableDatabase();

      db.replace(TABLE_UPLOAD, null, values);
    }

    public void delete(int taskHash) {
      SQLiteDatabase db = getWritableDatabase();

      db.delete(TABLE_UPLOAD, WHERE_QUERY, new String[] {
          String.valueOf(taskHash)
      });
    }
  }
}
