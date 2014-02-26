package org.sky.kuaipan.slidingmenu.controller;

import java.util.List;

import org.sky.kuaipan.Callback;
import org.sky.kuaipan.account.KscAccountManager;
import org.sky.kuaipan.mvc.BaseMvcAdapter;
import org.sky.kuaipan.mvc.IController;
import org.sky.kuaipan.slidingmenu.ModelFactory;
import org.sky.kuaipan.slidingmenu.model.MenuAccountModel;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuItemModel;
import org.sky.kuaipan.slidingmenu.model.SlidingMenuModel;
import org.sky.kuaipan.slidingmenu.view.SlidingMenuItemView;
import org.sky.kuaipan.slidingmenu.view.SlidingMenuView;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import cn.kuaipan.android.sdk.model.KuaipanUser;

public class SlidingMenuController implements IController<SlidingMenuView, SlidingMenuModel> {

  private final BaseMvcAdapter<SlidingMenuItemView, SlidingMenuItemModel> adapter =
      new BaseMvcAdapter<SlidingMenuItemView, SlidingMenuItemModel>() {

        @Override
        protected SlidingMenuItemView newView(int position, SlidingMenuItemModel model,
            ViewGroup parent) {
          return SlidingMenuItemView.newInstance(parent);
        }

        @Override
        protected IController<SlidingMenuItemView, SlidingMenuItemModel> newController(
            int position,
            SlidingMenuItemModel model) {
          return new SlidingMenuItemController();
        }
      };

  private SlidingMenuView view;
  private SlidingMenuModel model;

  @Override
  public void bind(SlidingMenuView view, SlidingMenuModel model) {
    this.view = view;
    this.model = model;

    if (model.getAccountModel() == null) {
      view.accountView.setVisibility(View.GONE);
      view.loginView.setVisibility(View.VISIBLE);
      bindLoginView();
      return;
    }
    view.accountView.setVisibility(View.VISIBLE);
    view.loginView.setVisibility(View.GONE);
    new MenuAccountController().bind(view.accountView, model.getAccountModel());
    view.menuList.setAdapter(adapter);
    adapter.setData(model.getMenuItemModels());
  }

  private void bindLoginView() {
    view.getView().setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        login();
      }
    });
  }

  private void login() {
    KscAccountManager.getInstance().login("453981192@qq.com", "890607lt",
        new Callback<KuaipanUser>() {

          @Override
          public void onSuccess(KuaipanUser result) {
            bind(view, createNewModel(result));
          }

          @Override
          public void onFailed(Throwable throwable) {
            Toast.makeText(view.getContext(), "login failed", Toast.LENGTH_SHORT).show();
          }
        });
  }

  private SlidingMenuModel createNewModel(final KuaipanUser user) {
    final List<SlidingMenuItemModel> itemModels = model.getMenuItemModels();
    return new SlidingMenuModel() {

      @Override
      public List<SlidingMenuItemModel> getMenuItemModels() {
        return itemModels;
      }

      @Override
      public MenuAccountModel getAccountModel() {
        return ModelFactory.convertMenuAccountModel(user);
      }
    };
  }
}
