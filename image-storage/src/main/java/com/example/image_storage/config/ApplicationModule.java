package com.example.image_storage.config;

import com.example.image_storage.service.ImageService;
import com.example.image_storage.service.impl.ImageServiceImpl;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgPool;

public class ApplicationModule implements Module {

  private final Vertx vertx;

  public ApplicationModule(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void configure(Binder binder) {
    binder.bind(ImageService.class).to(ImageServiceImpl.class);
  }

  @Provides
  @Singleton
  public PgPool getPgPool() {
    DatabaseConfig databaseConfig = new DatabaseConfig();
    return databaseConfig.createDbPool(vertx);
  }
}
