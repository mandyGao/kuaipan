package org.sky.kuaipan.mvc;

import java.util.List;

import org.sky.base.adapter.DataAdapter;
import org.sky.kuaipan.R;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseMvcAdapter<V extends IView, M extends IModel> extends DataAdapter<M> {

  public static final String TAG = "BaseListAdapter";

  private static final int TAG_KEY_CONTROLLER = R.id._controller;

  @Override
  public void setData(List<M> modelList) {
    super.setData(modelList);
    if (modelList == null || modelList.isEmpty()) {
      Log.d(TAG, "set data : list is null or empty");
    } else {
      Log.d(TAG, "set data : model class is " + modelList.get(0).getClass().getSimpleName());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    IController<V, M> controller;
    V baseView;
    if (convertView instanceof IView) {
      baseView = (V) convertView;
      controller = (IController<V, M>) baseView.getView().getTag(TAG_KEY_CONTROLLER);
    } else {
      baseView = newView(position, getItem(position), parent);
      controller = newController(position, getItem(position));
      baseView.getView().setTag(TAG_KEY_CONTROLLER, controller);
    }
    controller.bind(baseView, getItem(position));
    return baseView.getView();
  }

  protected abstract V newView(int position, M model, ViewGroup parent);

  protected abstract IController<V, M> newController(int position, M model);
}
