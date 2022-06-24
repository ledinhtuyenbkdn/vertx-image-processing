package com.example.image_storage.service.impl;

import com.example.image_storage.model.Image;
import com.example.image_storage.repository.ImageRepository;
import com.example.image_storage.service.ImageService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ImageServiceImpl implements ImageService {

  private final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

  private final ImageRepository imageRepository;

  @Inject
  public ImageServiceImpl(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  @Override
  public void uploadImage(RoutingContext routingContext) {
    logger.info("Request to upload image");

    if (routingContext.fileUploads() == null || routingContext.fileUploads().isEmpty()) {
      routingContext.response().setStatusCode(400)
        .end(new JsonObject().put("message", "File upload is required").toBuffer());
      return;
    }

    FileUpload fileUpload = routingContext.fileUploads().iterator().next();
    String uploadedFile = fileUpload.uploadedFileName();

    Image image = new Image();
    image.setOriginalImageUrl(uploadedFile);
    image.setName(fileUpload.fileName());
    image.setStatus("PENDING");
    imageRepository.save(image)
      .compose(row -> {
        JsonObject json = new JsonObject()
          .put("id", row.getId())
          .put("name", row.getName())
          .put("original_image_url", row.getOriginalImageUrl())
          .put("target_image_url", row.getTargetImageUrl())
          .put("status", row.getStatus());

        routingContext.vertx().eventBus().send("rabbitmq", json);
        return Future.succeededFuture(json);
      })
      .onSuccess(json -> {
        routingContext.vertx().eventBus().publish("client.update.status", json);
        routingContext.response().end(json.toBuffer());
      })
      .onFailure(err -> {
        logger.error("Failed to save image record", err);
        routingContext.response().setStatusCode(400)
          .end(new JsonObject().put("message", "Failed to save image record").toBuffer());
      });
  }

  @Override
  public void getAllImages(RoutingContext routingContext) {
    logger.info("Request to get all images");
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/event-stream")
      .putHeader("Cache-Control", "no-cache")
      .setChunked(true);

    MessageConsumer<JsonObject> consumer = routingContext.vertx().eventBus().consumer("client.update.status");
    consumer.handler(msg -> {
      response.write("event: update\n");
      response.write("data: " + msg.body().encode() + "\n\n");
    });
  }

  @Override
  public void updateStatus(Vertx vertx, JsonObject jsonObject) {
    logger.info("Request to update image: {}", jsonObject.encode());
    Image image = new Image();
    image.setId(jsonObject.getLong("id"));
    image.setTargetImageUrl(jsonObject.getString("target_image_url"));
    image.setStatus(jsonObject.getString("status"));
    imageRepository.update(image);
    vertx.eventBus().publish("client.update.status", jsonObject);
  }

  @Override
  public void getIndex(RoutingContext routingContext) {
    logger.info("Request to get index html");
    routingContext.response().sendFile("index.html");
  }
}
