package com.example.image_storage.model;

public class Image {

  private Long id;

  private String name;

  private String originalImageUrl;

  private String targetImageUrl;

  private String status; // PENDING, PROCESSING, COMPLETED, ERROR

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOriginalImageUrl() {
    return originalImageUrl;
  }

  public void setOriginalImageUrl(String originalImageUrl) {
    this.originalImageUrl = originalImageUrl;
  }

  public String getTargetImageUrl() {
    return targetImageUrl;
  }

  public void setTargetImageUrl(String targetImageUrl) {
    this.targetImageUrl = targetImageUrl;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public String toString() {
    return "Image{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", originalImageUrl='" + originalImageUrl + '\'' +
      ", targetImageUrl='" + targetImageUrl + '\'' +
      ", status='" + status + '\'' +
      '}';
  }
}
