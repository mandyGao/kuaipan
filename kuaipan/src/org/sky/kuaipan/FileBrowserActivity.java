package org.sky.kuaipan;

import org.sky.kuaipan.slidingmenu.ModelFactory;
import org.sky.kuaipan.slidingmenu.controller.SlidingMenuController;
import org.sky.kuaipan.slidingmenu.view.SlidingMenuView;

import android.os.Bundle;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivity;

public class FileBrowserActivity extends SlidingActivity {

  private SlidingMenuView menuView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // set the content view
    setContentView(R.layout.activity_file_browser);
    setTitle(R.string.my_cloud);
    // set the Behind View
    menuView = SlidingMenuView.newInstance(getSlidingMenu());
    setBehindContentView(menuView);
    // configure the SlidingMenu
    SlidingMenu menu = getSlidingMenu();
    menu.setShadowWidthRes(R.dimen.shadow_width);
    menu.setShadowDrawable(R.drawable.shadow);
    menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
    menu.setFadeDegree(0.35f);
    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
    // action bar for sliding view
    setSlidingActionBarEnabled(false);
    // the back action
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    bindSlidingMenu();
  }

  private void bindSlidingMenu() {
    new SlidingMenuController().bind(menuView, ModelFactory.convertSlidingMenuModel());
  }

}
