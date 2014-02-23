package org.sky.kuaipan.slidingmenu.view;

import org.sky.kuaipan.R;
import org.sky.kuaipan.mvc.IView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuAccountView extends RelativeLayout implements IView {

  public ImageView avatarView;
  public TextView nicknameView;
  public TextView descriptionView;

  public MenuAccountView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public MenuAccountView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MenuAccountView(Context context) {
    super(context);
  }

  @Override
  protected void onFinishInflate() {
    avatarView = (ImageView) findViewById(R.id.avatar);
    nicknameView = (TextView) findViewById(R.id.nickname);
    descriptionView = (TextView) findViewById(R.id.description);
  }

  @Override
  public View getView() {
    return this;
  }
}
