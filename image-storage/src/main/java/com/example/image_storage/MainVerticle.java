package com.example.image_storage;

import com.example.image_storage.config.ApplicationModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    Injector injector = Guice.createInjector(
      new ApplicationModule(vertx)
    );
    HttpServerVerticle httpServerVerticle = injector.getInstance(HttpServerVerticle.class);
    RabbitMqVerticle rabbitMqVerticle = injector.getInstance(RabbitMqVerticle.class);

    vertx.deployVerticle(httpServerVerticle);
    vertx.deployVerticle(rabbitMqVerticle);
  }
}
