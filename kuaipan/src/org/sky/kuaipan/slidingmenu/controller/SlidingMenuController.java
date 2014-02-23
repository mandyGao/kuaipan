package org.sky.kuaipan.slidingmenu.controller;

import org.sky.kuaipan.mvc.BaseMvcAdapter;
import org.sky.kuaipan.mvc.IController;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuItemModel;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuModel;
import org.sky.kuaipan.slidingmenu.view.SlidingMenuItemView;
import org.sky.kuaipan.slidingmenu.view.SlidingMenuView;

import android.view.ViewGroup;

public class SlidingMenuController implements IController<SlidingMenuView, SlidingMenuModel> {

  @Override
  public void bind(SlidingMenuView view, SlidingMenuModel model) {
    new MenuAccountController().bind(view.accountView, model.account);
    view.menuList.setAdapter(new BaseMvcAdapter<SlidingMenuItemView, SlidingMenuItemModel>() {

      @Override
      protected SlidingMenuItemView newView(int position, SlidingMenuItemModel model,
          ViewGroup parent) {
        return SlidingMenuItemView.newInstance(parent);
      }

      @Override
      protected IController<SlidingMenuItemView, SlidingMenuItemModel> newController(int position,
          SlidingMenuItemModel model) {
        return new SlidingMenuItemController();
      }
    });
  }
}
