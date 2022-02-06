package com.client.pak;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class CellRenderer implements Callback<ListView<UserCell>, ListCell<UserCell>> {

  @Override
  public ListCell<UserCell> call(ListView<UserCell> stringListView) {
    ListCell<UserCell> cell = new ListCell<>() {
      @Override
      protected void updateItem(UserCell user, boolean empty) {
        super.updateItem(user, empty);
        setGraphic(null);
        setText(null);
        if (user != null) {
          HBox hBox = new HBox();
          hBox.setSpacing(10);
          hBox.setPadding(new Insets(5));

          VBox vBox = new VBox();
          vBox.setSpacing(5);
          vBox.setAlignment(Pos.CENTER_LEFT);
          Text nameText = new Text(user.getName());
          nameText.setFont(Font.font("Ubuntu", FontWeight.BOLD, 15));
          Text statusText = new Text(user.getStatus());
          vBox.getChildren().addAll(nameText, statusText);

          UserPicture userPicture = new UserPicture();
          hBox.getChildren().addAll(userPicture, vBox);
          hBox.setAlignment(Pos.CENTER_LEFT);
          setGraphic(hBox);
        }
      }
    };
    return cell;
  }
}
