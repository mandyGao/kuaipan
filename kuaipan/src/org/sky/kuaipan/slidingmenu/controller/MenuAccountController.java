package org.sky.kuaipan.slidingmenu.controller;

import org.sky.kuaipan.R;
import org.sky.kuaipan.mvc.IController;
import org.sky.kuaipan.slidingmenu.model.MenuAccountModel;
import org.sky.kuaipan.slidingmenu.view.MenuAccountView;

import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MenuAccountController implements IController<MenuAccountView, MenuAccountModel> {

  private MenuAccountView view;
  private MenuAccountModel model;

  @Override
  public void bind(MenuAccountView view, MenuAccountModel model) {
    this.view = view;
    this.model = model;

    if (!TextUtils.isEmpty(model.getAvatar())) {
      ImageLoader.getInstance().displayImage(model.getAvatar(), view.avatarView);
    } else {
      view.avatarView.setImageResource(R.drawable.ic_defalut_avatar);
    }
    view.nicknameView.setText(model.getNickname());
    view.descriptionView.setText(model.getDescription());
  }
}
