package org.sky.kuaipan.slidingmenu.controller;

import org.sky.kuaipan.mvc.IController;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuItemModel;
import org.sky.kuaipan.slidingmenu.view.SlidingMenuItemView;

public class SlidingMenuItemController
    implements IController<SlidingMenuItemView, SlidingMenuItemModel> {

  @Override
  public void bind(SlidingMenuItemView view, SlidingMenuItemModel model) {
    view.iconView.setImageDrawable(model.getIcon());
    view.nameView.setText(model.getName());
  }
}
