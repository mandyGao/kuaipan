package org.sky.kuaipan.mvc;


public interface IController<V extends IView, M extends IModel> {

  void bind(V view, M model);
}
