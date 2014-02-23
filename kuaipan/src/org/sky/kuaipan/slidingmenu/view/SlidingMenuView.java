package org.sky.kuaipan.slidingmenu.view;

import org.sdk.utils.ViewUtils;
import org.sky.kuaipan.R;
import org.sky.kuaipan.mvc.IView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class SlidingMenuView extends RelativeLayout implements IView {

  public MenuAccountView accountView;
  public ListView menuList;

  public SlidingMenuView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public SlidingMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SlidingMenuView(Context context) {
    super(context);
  }

  @Override
  protected void onFinishInflate() {
    accountView = (MenuAccountView) findViewById(R.id.account);
    menuList = (ListView) findViewById(R.id.menu_list);
  }

  @Override
  public View getView() {
    return this;
  }

  public static SlidingMenuView newInstance(ViewGroup parent) {
    return (SlidingMenuView) ViewUtils.newInstance(parent, R.layout.view_sliding_menu);
  }
}
