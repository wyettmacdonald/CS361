/*
 * File: SettingMenuController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the SettingMenuController class, handling Setting menu related actions.
 */

package proj9AbramsDeutschDurstJones;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
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
import java.util.HashMap;
import java.util.Map;


/**
 * SettingMenuController handles Setting menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Chris Marcello
 */
public class SettingMenuController {

    /**
     * Stores CSS files for different color modes
     */
    private String lightModeCss;
    private String darkModeCss;

    /**
     * Main VBox of the stage
     */
    private VBox vBox;

    /**
     * Map of styleClass (keyword, paren, string, integer) to color
     */
    private Map<String, String> styleClassColorMap;

    /**
     * Constructor initializes fields
     */
    public SettingMenuController() {
        this.lightModeCss = getClass().getResource("resources/LightMode.css").toExternalForm();
        this.darkModeCss = getClass().getResource("resources/DarkMode.css").toExternalForm();
        this.styleClassColorMap = new HashMap<>();
        this.initializeLightMode();
    }

    /**
     * Sets the main VBox of the GUI
     * @param vBox the VBox defined in Main.fxml
     */
    public void setVBox(VBox vBox) {
        this.vBox = vBox;
    }

    /**
     * Handles onAction for the Light Mode menu item to switch CSS for vBox to
     * LightMode.css and stores the default styleClass colors if necessary
     */
    public void handleLightMode() {
        vBox.getStylesheets().clear();
        vBox.getStylesheets().add(lightModeCss);
        this.initializeLightMode();
    }

    /**
     * Handles onAction for the Dark Mode menu item to switch CSS for vBox to
     * DarkMode.css and stores the default styleClass colors if necessary
     */
    public void handleDarkMode() {
        vBox.getStylesheets().clear();
        vBox.getStylesheets().add(darkModeCss);
        this.initializeDarkMode();
    }

    /**
     * Add default light mode colors to color map
     */
    private void initializeLightMode() {
        this.styleClassColorMap.put("keyword", "purple");
        this.styleClassColorMap.put("paren", "teal");
        this.styleClassColorMap.put("string", "blue");
        this.styleClassColorMap.put("integer", "red");
        this.updateColors();
    }

    /**
     * Add default dark mode colors to color map
     */
    private void initializeDarkMode() {
        this.styleClassColorMap.put("keyword", "yellow");
        this.styleClassColorMap.put("paren", "orange");
        this.styleClassColorMap.put("string", "pink");
        this.styleClassColorMap.put("integer", "red");
        this.updateColors();
    }

    /**
     * Handles Color menu item action for the given style class.
     * @param styleClass the style class to change the color of
     */
    public void handleColorAction(String styleClass) {
        ChoiceBox colorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Purple", "Black", "Blue", "Teal", "Pink", "Yellow", "Orange", "Red"));
        String color = this.styleClassColorMap.get(styleClass);
        color = color.substring(0,1).toUpperCase() + color.substring(1);
        colorCB.setValue(color);
        this.createColorChoiceWindow(styleClass, colorCB, color);
    }

    /**
     * Creates a window allowing the user to choose the color for the selected element type
     * @param styleClass the style class of the element to change the color of
     * @param colorCB the choicebox listing the color options
     */
    private void createColorChoiceWindow(String styleClass, ChoiceBox colorCB, String origColor) {
        String element = styleClass.substring(0,1).toUpperCase() + styleClass.substring(1);
        Stage colorWin = new Stage();
        colorWin.setTitle(element + " Color");

        VBox colorRoot = new VBox();
        colorRoot.setAlignment(Pos.CENTER);
        colorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75, Color.web(origColor));
        Text message = new Text(element + " Color");
        message.setFill(Color.web(origColor));

        colorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                String color = colorCB.getValue().toString();
                rect.setFill(Color.web(color));
                message.setFill(Color.web(color));
                styleClassColorMap.put(styleClass, color);
                updateColors();
            }
        });

        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        colorRoot.getChildren().addAll(message, rect, colorCB);
        Scene colorScene = new Scene(colorRoot, 200, 200);
        colorWin.setScene(colorScene);
        colorWin.show();
    }

    /**
     * Set all elements of the style classes in the color map to their respective colors
     */
    private void updateColors() {
        Parent root = Main.getParentRoot();
        String newStyles = "";
        for (String styleClass : this.styleClassColorMap.keySet()) {
            newStyles += styleClass + "-color: " + this.styleClassColorMap.get(styleClass) + ";\n";
        }
        if (root != null) {
            root.setStyle(newStyles);
        }
    }
}