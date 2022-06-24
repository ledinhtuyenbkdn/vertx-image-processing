package com.example.image_storage.service;

import com.google.inject.Singleton;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public interface ImageService {

  void uploadImage(RoutingContext routingContext);

  void getAllImages(RoutingContext routingContext);

  void getIndex(RoutingContext routingContext);

  void updateStatus(Vertx vertx, JsonObject jsonObject);
}
