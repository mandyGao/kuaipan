package org.sky.kuaipan.slidingmenu.model;

import org.sky.kuaipan.mvc.IModel;

import android.graphics.drawable.Drawable;

public interface SlidingMenuItemModel extends IModel {

  Drawable getIcon();

  CharSequence getName();
}
