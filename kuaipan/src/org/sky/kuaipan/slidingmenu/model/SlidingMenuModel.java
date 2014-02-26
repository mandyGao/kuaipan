package org.sky.kuaipan.slidingmenu.model;

import java.util.List;

import org.sky.kuaipan.mvc.IModel;

public interface SlidingMenuModel extends IModel {

  MenuAccountModel getAccountModel();

  List<SlidingMenuItemModel> getMenuItemModels();
}
