package com.example.image_storage;

import com.example.image_storage.service.ImageService;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RabbitMqVerticle extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(RabbitMqVerticle.class);

  private final ImageService imageService;

  @Inject
  public RabbitMqVerticle(ImageService imageService) {
    logger.info("init rabbit verticle {}", imageService.hashCode());
    this.imageService = imageService;

  }

  @Override
  public void start() throws Exception {
    RabbitMQOptions config = new RabbitMQOptions()
      .setUser("user")
      .setPassword("password")
      .setHost("localhost")
      .setPort(5672);

    RabbitMQClient client = RabbitMQClient.create(vertx, config);

    client.addConnectionEstablishedCallback(promise -> {
      CompositeFuture.all(List.of(
        client.queueDeclare("new_image", true, false, false),
        client.queueDeclare("update_status", true, false, false)
        ))
        .onSuccess(f -> {
          logger.info("Create queue successfully");
          promise.complete();
        })
        .onFailure(err -> {
          logger.error("Failed to create queue");
          promise.fail(err);
        });
    });

    client.start()
      .onSuccess(v -> {
        logger.info("Success to start rabbitmq client");
        handleUpdateStatus(client);
      })
      .onFailure(err -> {
        logger.error("Failed to start rabbitmq client", err);
      });

    vertx.eventBus().<JsonObject>consumer("rabbitmq", message -> {
      logger.info("Publish message to new_image queue: {}", message.body().encode());
      client.basicPublish("", "new_image", message.body().toBuffer());
    });
  }

  private void handleUpdateStatus(RabbitMQClient client) {
    client.basicConsumer("update_status", ar -> {
      if (ar.succeeded()) {
        RabbitMQConsumer consumer = ar.result();
        consumer.handler(message -> {
          logger.info("Received message: {}", message.body().toString());
          vertx.eventBus().send("update.status", message.body().toJsonObject());
        });
      } else {
        logger.error("Failed to consume message", ar.cause());
      }
    });
  }
}
