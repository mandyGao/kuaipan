package org.sky.kuaipan.slidingmenu.controller;

import org.sky.kuaipan.mvc.IController;
import org.sky.kuaipan.slidingmenu.model.MenuAccountModel;
import org.sky.kuaipan.slidingmenu.view.MenuAccountView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class MenuAccountController implements IController<MenuAccountView, MenuAccountModel> {

  @Override
  public void bind(MenuAccountView view, MenuAccountModel model) {
    ImageLoader.getInstance().displayImage(model.avatar, view.avatarView);
    view.nicknameView.setText(model.nickname);
    view.descriptionView.setText(model.description);
  }
}
