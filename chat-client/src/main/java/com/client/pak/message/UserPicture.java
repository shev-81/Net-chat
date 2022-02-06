package com.client.pak.message;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class UserPicture extends Group {

  private static final int RADIUS = 27;

  public UserPicture() {
    Circle circle = new Circle();
    circle.setRadius(RADIUS);
    circle.setFill(new ImagePattern(new Image(getClass().getResourceAsStream("/images/default.png"))));
    getChildren().addAll(circle);
  }
}
