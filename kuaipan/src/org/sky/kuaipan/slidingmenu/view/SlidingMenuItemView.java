package org.sky.kuaipan.slidingmenu.view;

import org.sdk.utils.ViewUtils;
import org.sky.kuaipan.R;
import org.sky.kuaipan.mvc.IView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SlidingMenuItemView extends RelativeLayout implements IView {

  public ImageView iconView;
  public TextView nameView;

  public SlidingMenuItemView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public SlidingMenuItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SlidingMenuItemView(Context context) {
    super(context);
  }

  @Override
  protected void onFinishInflate() {
    iconView = (ImageView) findViewById(R.id.icon);
    nameView = (TextView) findViewById(R.id.name);
  }

  @Override
  public View getView() {
    return this;
  }

  public static SlidingMenuItemView newInstance(ViewGroup parent) {
    return (SlidingMenuItemView) ViewUtils.newInstance(parent, R.layout.view_silding_menu_item);
  }

}
