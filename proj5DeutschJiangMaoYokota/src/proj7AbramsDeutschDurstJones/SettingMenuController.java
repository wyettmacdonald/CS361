/*
 * File: SettingMenuController.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file contains the SettingMenuController class, handling Setting menu related actions.
 */

package proj7AbramsDeutschDurstJones;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.Set;


/**
 * SettingMenuController handles Setting menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Chris Marcello
 */
public class SettingMenuController {

    /**
     * Handles Color menu item action.
     * Pops up a window displaying the color preference.
     * By selecting a color from the drop-down menu, the color of the given styleClass will change accordingly.
     *
     * @param styleClass the styleClass to change the color of - keyword, paren, string, or integer
     */
    public void handleColorAction(String styleClass) {
        String element = styleClass.substring(0,1).toUpperCase() + styleClass.substring(1);
        Stage colorWin = new Stage();
        colorWin.setTitle(element + " Color");

        Parent root = Main.getParentRoot();

        VBox colorRoot = new VBox();
        colorRoot.setAlignment(Pos.CENTER);
        colorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75, Color.WHITE);

        ChoiceBox colorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Purple", "Black", "Blue", "Teal", "Pink", "Yellow", "Red"));
        // set initial value to blank
        colorCB.setValue("");

        Text message = new Text(element + " Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Set<Node> nodes = root.lookupAll("." + styleClass);

        if (!nodes.isEmpty()) {
            colorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    if (colorCB.getValue() == "Purple") {
                        rect.setFill(Color.PURPLE);
                        message.setFill(Color.PURPLE);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: purple;");
                        }
                    } else if (colorCB.getValue() == "Black") {
                        rect.setFill(Color.BLACK);
                        message.setFill(Color.BLACK);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: black;");
                        }
                    } else if (colorCB.getValue() == "Blue") {
                        rect.setFill(Color.ROYALBLUE);
                        message.setFill(Color.ROYALBLUE);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: blue;");
                        }
                    } else if (colorCB.getValue() == "Red") {
                        rect.setFill(Color.FIREBRICK);
                        message.setFill(Color.FIREBRICK);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: firebrick;");
                        }
                    } else if (colorCB.getValue() == "Yellow") {
                        rect.setFill(Color.ORANGE);
                        message.setFill(Color.ORANGE);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: yellow;");
                        }
                    } else if (colorCB.getValue() == "Pink") {
                        rect.setFill(Color.ORCHID);
                        message.setFill(Color.ORCHID);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: orchid;");
                        }
                    } else if (colorCB.getValue() == "Teal") {
                        rect.setFill(Color.TEAL);
                        message.setFill(Color.TEAL);
                        for (Node node : nodes) {
                            node.setStyle("-fx-fill: teal;");
                        }
                    }
                }
            });
        }

        colorRoot.getChildren().addAll(message, rect, colorCB);
        Scene colorScene = new Scene(colorRoot, 200, 200);
        colorWin.setScene(colorScene);
        colorWin.show();
    }
}