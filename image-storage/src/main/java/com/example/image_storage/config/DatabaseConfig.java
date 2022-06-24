package com.example.image_storage.config;

import io.vertx.core.Vertx;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;

public class DatabaseConfig {

  public PgPool createDbPool(Vertx vertx) {
    return PgPool.pool(
      vertx,
      new PgConnectOptions()
        .setHost("localhost")
        .setPort(5432)
        .setDatabase("postgres")
        .setUser("postgres")
        .setPassword("secret"),
      new PoolOptions().setMaxSize(4)
    );
  }
}
