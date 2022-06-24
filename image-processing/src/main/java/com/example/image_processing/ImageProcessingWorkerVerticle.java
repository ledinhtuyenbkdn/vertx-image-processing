package com.example.image_processing;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

public class ImageProcessingWorkerVerticle extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(ImageProcessingWorkerVerticle.class);

  private static final String IMAGE_DIR = "../image-storage";

  @Override
  public void start() throws Exception {
    vertx.eventBus().<JsonObject>consumer("image.processing.worker", message -> {
      runImageProcessing(message.body());
    });
  }

  private void runImageProcessing(JsonObject image) {
    // send update status to PROCESSING via rabbitmq
    updateStatus(new JsonObject(image.toBuffer()).put("status", "PROCESSING"));
    // scale image
    String originalImageUrl = String.format("%s/%s", IMAGE_DIR, image.getString("original_image_url"));
    String uuid = UUID.randomUUID().toString();

    String targetImageUrl = String.format("resized" + File.separator + uuid + ".jpg");
    try {
      logger.info("Processing image {}", image.getLong("id"));
      makeDir();
      resizeImage(originalImageUrl, IMAGE_DIR + File.separator + targetImageUrl, 100, 100);
      logger.info("Completed processing image {}", image.getLong("id"));
      // send update status to COMPLETED via rabbitmq
      updateStatus(new JsonObject(image.toBuffer()).put("status", "COMPLETED").put("target_image_url", targetImageUrl));;
    } catch (IOException exception) {
      logger.error("Failed to process image", exception);
      // send update status to ERROR via rabbitmq
      updateStatus(new JsonObject(image.toBuffer()).put("status", "ERROR"));
    }
  }

  private void makeDir() {
    String targetDir = String.format("%s/%s", IMAGE_DIR, "resized");
    File outputDir = new File(targetDir);
    if (!outputDir.exists()) {
      outputDir.mkdir();
    }
  }

  private void updateStatus(JsonObject message) {
    vertx.eventBus().send("rabbitmq", message);
  }

  public void resizeImage(String originalImageUrl, String targetImageUrl, int targetWidth, int targetHeight) throws IOException {
    BufferedImage originalImage = ImageIO.read(new File(originalImageUrl));
    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics2D = resizedImage.createGraphics();
    graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
    graphics2D.dispose();
    FileOutputStream outputFile = new FileOutputStream(targetImageUrl);
    ImageIO.write(resizedImage, "jpg", outputFile);
    outputFile.close();
  }
}
