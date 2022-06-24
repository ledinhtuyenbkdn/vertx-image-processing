package com.example.image_processing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.deployVerticle(RabbitMqVerticle.class.getName());
    vertx.deployVerticle(ImageProcessingWorkerVerticle.class.getName(), new DeploymentOptions().setWorker(true));
  }
}
