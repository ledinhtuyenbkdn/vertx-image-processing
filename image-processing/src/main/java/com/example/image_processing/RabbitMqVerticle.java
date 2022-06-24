package com.example.image_processing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMqVerticle extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(RabbitMqVerticle.class);

  @Override
  public void start() throws Exception {
    RabbitMQOptions config = new RabbitMQOptions()
      .setUser("user")
      .setPassword("password")
      .setHost("localhost")
      .setPort(5672);

    RabbitMQClient client = RabbitMQClient.create(vertx, config);

    client.start()
      .onSuccess(v -> {
        logger.info("Success to start rabbitmq client");
        handleNewImage(client);
      })
      .onFailure(err -> {
        logger.error("Failed to start rabbitmq client", err);
      });

    vertx.eventBus().<JsonObject>consumer("rabbitmq", message -> {
      logger.info("Publish message to update_status queue: {}", message.body().encode());
      client.basicPublish("", "update_status", message.body().toBuffer());
    });
  }

  private void handleNewImage(RabbitMQClient client) {
    client.basicConsumer("new_image", ar -> {
      if (ar.succeeded()) {
        RabbitMQConsumer consumer = ar.result();
        consumer.handler(message -> {
          logger.info("Received message: {}", message.body().toString());
          vertx.eventBus().send("image.processing.worker", message.body().toJsonObject());
        });
      } else {
        logger.error("Failed to consume message", ar.cause());
      }
    });
  }
}
