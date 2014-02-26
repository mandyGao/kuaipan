package org.sky.kuaipan.account;

import org.sky.kuaipan.Callback;
import org.sky.kuaipan.KuaipanApplication;

import android.os.AsyncTask;
import cn.kuaipan.android.sdk.internal.OAuthApi;
import cn.kuaipan.android.sdk.model.KuaipanUser;

public class KscAccountManager {

  private static KscAccountManager instance;

  private KuaipanUser currentUser;

  private KscAccountManager() {}

  public static KscAccountManager getInstance() {
    if (instance == null) {
      instance = new KscAccountManager();
    }
    return instance;
  }

  public KuaipanUser getCurrentUser() {
    return currentUser;
  }

  public void login(final String username, final String passwork,
      final Callback<KuaipanUser> callback) {
    new AsyncTask<Void, Void, KuaipanUser>() {

      private Throwable throwable;

      @Override
      protected KuaipanUser doInBackground(Void... params) {
        try {
          OAuthApi api = KuaipanApplication.getInstance().getKscApi();
          api.login(username, passwork);
          return api.getUserInfo();
        } catch (Throwable e) {
          throwable = e;
          return null;
        }
      }

      @Override
      protected void onPostExecute(KuaipanUser user) {
        if (user != null) {
          // set current user on ui thread
          currentUser = user;
          // callback
          callback.onSuccess(user);
        } else {
          callback.onFailed(throwable);
        }
      }
    }.execute();
  }

}
