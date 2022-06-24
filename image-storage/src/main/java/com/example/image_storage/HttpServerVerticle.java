package com.example.image_storage;

import com.example.image_storage.service.ImageService;
import com.google.inject.Inject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerVerticle extends AbstractVerticle {

  public static final int PORT = 8080;

  private final Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);

  private final ImageService imageService;

  @Inject
  public HttpServerVerticle(ImageService imageService) {
    logger.info("init http verticle {}", imageService.hashCode());
    this.imageService = imageService;
    logger.info("started http verticle");
  }

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);
    configureRouting(router);
    vertx.createHttpServer()
      .requestHandler(router)
      .exceptionHandler(err -> {
        logger.error("Unhandled error", err);
      })
      .listen(PORT)
      .onSuccess(httpServer -> {
        logger.info("HTTP server has started on port: {}", httpServer.actualPort());
      })
      .onFailure(err -> {
        logger.error("HTTP server could not to start", err);
      });

    vertx.eventBus().<JsonObject>consumer("update.status", message -> imageService.updateStatus(vertx, message.body()));
  }

  private void configureRouting(Router router) {
    router.post("/images").handler(BodyHandler.create().setHandleFileUploads(true)).handler(imageService::uploadImage);
    router.get("/images").handler(imageService::getAllImages);
    router.get("/").handler(imageService::getIndex);
  }
}
