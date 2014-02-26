package org.sky.kuaipan.slidingmenu;

import java.util.Collections;
import java.util.List;

import org.sky.kuaipan.account.KscAccountManager;
import org.sky.kuaipan.slidingmenu.model.MenuAccountModel;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuItemModel;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuModel;

import cn.kuaipan.android.sdk.model.KuaipanUser;

public class ModelFactory {

  public static SlidingMenuModel convertSlidingMenuModel() {
    return new SlidingMenuModel() {

      @Override
      public List<SlidingMenuItemModel> getMenuItemModels() {
        return Collections.emptyList();
      }

      @Override
      public MenuAccountModel getAccountModel() {
        KuaipanUser user = KscAccountManager.getInstance().getCurrentUser();
        if (user != null) {
          return convertMenuAccountModel(user);
        }
        return null;
      }
    };
  }

  public static MenuAccountModel convertMenuAccountModel(final KuaipanUser user) {
    int index = user.user_name.indexOf('@');
    final String nickname = index != -1 ? user.user_name.substring(0, index) : user.user_name;
    final String desc = index != -1 ? user.user_name.substring(index) : "";
    return new MenuAccountModel() {

      @Override
      public String getNickname() {
        return nickname;
      }

      @Override
      public String getDescription() {
        return desc;
      }

      @Override
      public String getAvatar() {
        return null;
      }
    };
  }

}
