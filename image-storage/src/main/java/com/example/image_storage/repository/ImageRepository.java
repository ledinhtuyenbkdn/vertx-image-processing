package com.example.image_storage.repository;

import com.example.image_storage.model.Image;
import com.google.inject.Inject;
import io.vertx.core.Future;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

public class ImageRepository {

  private final PgPool pool;

  @Inject
  public ImageRepository(PgPool pool) {
    this.pool = pool;
  }

  public Future<Image> save(Image image) {
    return pool.preparedQuery("INSERT INTO image(name, original_image_url, target_image_url, status) " +
        "VALUES($1, $2, $3, $4) " +
        "RETURNING id, name, original_image_url, target_image_url, status")
      .execute(Tuple.of(image.getName(), image.getOriginalImageUrl(), image.getTargetImageUrl(), image.getStatus()))
      .map(rs -> {
        Row row = rs.iterator().next();
        Image result = new Image();
        result.setId(row.getLong("id"));
        result.setName(row.getString("name"));
        result.setOriginalImageUrl(row.getString("original_image_url"));
        result.setTargetImageUrl(row.getString("target_image_url"));
        result.setStatus(row.getString("status"));
        return result;
      });
  }

  public Future<Void> update(Image image) {
    return pool.preparedQuery("UPDATE image SET target_image_url=$1, status =$2 WHERE id=$3")
      .execute(Tuple.of(image.getTargetImageUrl(), image.getStatus(), image.getId()))
      .flatMap(v -> Future.succeededFuture());
  }
}
