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
import org.fxmisc.flowless.VirtualizedScrollPane;


/**
 * SettingMenuController handles Setting menu related actions.
 *
 * @author Liwei Jiang
 * @author Tracy Quan
 * @author Chris Marcello
 */
public class SettingMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * Handles Keyword Color menu item action.
     * Pops up a window displaying the color preference for the keywords.
     * By selecting a color from the drop-down menu, the color of the keywords will change accordingly.
     */
    public void handleKeywordColorAction() {
        Stage keywordColorWin = new Stage();
        keywordColorWin.setTitle("Keyword Color");

        Parent root = Main.getParentRoot();

        VBox keywordColorRoot = new VBox();
        keywordColorRoot.setAlignment(Pos.CENTER);
        keywordColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75, Color.PURPLE);

        ChoiceBox keywordColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Purple", "Black", "Blue", "Green", "Pink", "Yellow", "Red"));
        keywordColorCB.setValue("Purple");

        // load temporary CSS files into strings
        String kwBlack = getClass().getResource("KeywordColorCSS/KeywordBlack.css").toExternalForm();
        String kwRed = getClass().getResource("KeywordColorCSS/KeywordRed.css").toExternalForm();
        String kwBlue = getClass().getResource("KeywordColorCSS/KeywordBlue.css").toExternalForm();
        String kwYellow = getClass().getResource("KeywordColorCSS/KeywordOrange.css").toExternalForm();
        String kwPink = getClass().getResource("KeywordColorCSS/KeywordPink.css").toExternalForm();
        String kwGreen = getClass().getResource("KeywordColorCSS/KeywordGreen.css").toExternalForm();

        Text message = new Text("Keyword Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.PURPLE);

        keywordColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (keywordColorCB.getValue() == "Purple") {
                    rect.setFill(Color.PURPLE);
                    message.setFill(Color.PURPLE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                } else if (keywordColorCB.getValue() == "Black") {
                    rect.setFill(Color.BLACK);
                    message.setFill(Color.BLACK);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                    root.getStylesheets().add(kwBlack);
                } else if (keywordColorCB.getValue() == "Blue") {
                    rect.setFill(Color.ROYALBLUE);
                    message.setFill(Color.ROYALBLUE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                    root.getStylesheets().add(kwBlue);
                } else if (keywordColorCB.getValue() == "Red") {
                    rect.setFill(Color.FIREBRICK);
                    message.setFill(Color.FIREBRICK);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                    root.getStylesheets().add(kwRed);
                } else if (keywordColorCB.getValue() == "Yellow") {
                    rect.setFill(Color.ORANGE);
                    message.setFill(Color.ORANGE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                    root.getStylesheets().add(kwYellow);
                } else if (keywordColorCB.getValue() == "Pink") {
                    rect.setFill(Color.ORCHID);
                    message.setFill(Color.ORCHID);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                    root.getStylesheets().add(kwPink);
                } else if (keywordColorCB.getValue() == "Green") {
                    rect.setFill(Color.TEAL);
                    message.setFill(Color.TEAL);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGreen);
                    root.getStylesheets().add(kwGreen);
                }
            }
        });

        keywordColorRoot.getChildren().addAll(message, rect, keywordColorCB);
        Scene keywordColorScene = new Scene(keywordColorRoot, 200, 200);
        keywordColorWin.setScene(keywordColorScene);
        keywordColorWin.show();

    }

    /**
     * Handles Parentheses/Brackets Color menu item action.
     */
    public void handleParenColorAction() {
        Stage parenColorWin = new Stage();
        parenColorWin.setTitle("Parentheses/Brackets Color");

        Parent root = Main.getParentRoot();

        VBox parenColorRoot = new VBox();
        parenColorRoot.setAlignment(Pos.CENTER);
        parenColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75, Color.TEAL);

        ChoiceBox parenColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Teal", "Black", "Blue", "Grey", "Pink", "Yellow", "Red"));
        parenColorCB.setValue("Teal");

        // load temporary CSS files into strings
        String kwBlack = getClass().getResource("ParenColorCSS/ParenBlack.css").toExternalForm();
        String kwRed = getClass().getResource("ParenColorCSS/ParenRed.css").toExternalForm();
        String kwBlue = getClass().getResource("ParenColorCSS/ParenBlue.css").toExternalForm();
        String kwYellow = getClass().getResource("ParenColorCSS/ParenYellow.css").toExternalForm();
        String kwPink = getClass().getResource("ParenColorCSS/ParenPink.css").toExternalForm();
        String kwGrey = getClass().getResource("ParenColorCSS/ParenGrey.css").toExternalForm();

        Text message = new Text("Parentheses/Brackets Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.TEAL);

        parenColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (parenColorCB.getValue() == "Teal") {
                    rect.setFill(Color.TEAL);
                    message.setFill(Color.TEAL);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                } else if (parenColorCB.getValue() == "Black") {
                    rect.setFill(Color.BLACK);
                    message.setFill(Color.BLACK);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                    root.getStylesheets().add(kwBlack);
                } else if (parenColorCB.getValue() == "Blue") {
                    rect.setFill(Color.ROYALBLUE);
                    message.setFill(Color.ROYALBLUE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                    root.getStylesheets().add(kwBlue);
                } else if (parenColorCB.getValue() == "Red") {
                    rect.setFill(Color.FIREBRICK);
                    message.setFill(Color.FIREBRICK);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                    root.getStylesheets().add(kwRed);
                } else if (parenColorCB.getValue() == "Yellow") {
                    rect.setFill(Color.ORANGE);
                    message.setFill(Color.ORANGE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                    root.getStylesheets().add(kwYellow);
                } else if (parenColorCB.getValue() == "Pink") {
                    rect.setFill(Color.ORCHID);
                    message.setFill(Color.ORCHID);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                    root.getStylesheets().add(kwPink);
                } else if (parenColorCB.getValue() == "Grey") {
                    rect.setFill(Color.SILVER);
                    message.setFill(Color.SILVER);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwRed, kwYellow, kwPink, kwGrey);
                    root.getStylesheets().add(kwGrey);
                }
            }
        });

        parenColorRoot.getChildren().addAll(message, rect, parenColorCB);
        Scene keywordColorScene = new Scene(parenColorRoot, 230, 200);
        parenColorWin.setScene(keywordColorScene);
        parenColorWin.show();
    }


    /**
     * Handles String Color menu item action.
     */
    public void handleStrColorAction() {
        Stage strColorWin = new Stage();
        strColorWin.setTitle("String Color");

        Parent root = Main.getParentRoot();

        VBox strColorRoot = new VBox();
        strColorRoot.setAlignment(Pos.CENTER);
        strColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75, Color.BLUE);

        ChoiceBox strColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Blue", "Black", "Green", "SkyBlue", "Pink", "Yellow", "Red"));
        strColorCB.setValue("Blue");

        // load temporary CSS files into strings
        String kwBlack = getClass().getResource("StrColorCSS/StrColorBlack.css").toExternalForm();
        String kwRed = getClass().getResource("StrColorCSS/StrColorRed.css").toExternalForm();
        String kwGreen = getClass().getResource("StrColorCSS/StrColorGreen.css").toExternalForm();
        String kwYellow = getClass().getResource("StrColorCSS/StrColorYellow.css").toExternalForm();
        String kwPink = getClass().getResource("StrColorCSS/StrColorPink.css").toExternalForm();
        String kwSkyBlue = getClass().getResource("StrColorCSS/StrColorSkyBlue.css").toExternalForm();

        Text message = new Text("String Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.BLUE);

        strColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (strColorCB.getValue() == "Blue") {
                    rect.setFill(Color.BLUE);
                    message.setFill(Color.BLUE);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                } else if (strColorCB.getValue() == "Black") {
                    rect.setFill(Color.BLACK);
                    message.setFill(Color.BLACK);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwBlack);
                } else if (strColorCB.getValue() == "Green") {
                    rect.setFill(Color.TEAL);
                    message.setFill(Color.TEAL);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwGreen);
                } else if (strColorCB.getValue() == "Red") {
                    rect.setFill(Color.FIREBRICK);
                    message.setFill(Color.FIREBRICK);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwRed);
                } else if (strColorCB.getValue() == "Yellow") {
                    rect.setFill(Color.ORANGE);
                    message.setFill(Color.ORANGE);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwYellow);
                } else if (strColorCB.getValue() == "Pink") {
                    rect.setFill(Color.ORCHID);
                    message.setFill(Color.ORCHID);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwPink);
                } else if (strColorCB.getValue() == "SkyBlue") {
                    rect.setFill(Color.SKYBLUE);
                    message.setFill(Color.SKYBLUE);
                    root.getStylesheets().removeAll(kwBlack, kwGreen, kwRed, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwSkyBlue);
                }
            }
        });

        strColorRoot.getChildren().addAll(message, rect, strColorCB);
        Scene keywordColorScene = new Scene(strColorRoot, 230, 200);
        strColorWin.setScene(keywordColorScene);
        strColorWin.show();
    }


    /**
     * Handles int Color menu item action.
     */
    public void handleIntColorAction() {
        Stage intColorWin = new Stage();
        intColorWin.setTitle("int Color");

        Parent root = Main.getParentRoot();

        VBox intColorRoot = new VBox();
        intColorRoot.setAlignment(Pos.CENTER);
        intColorRoot.setSpacing(10);

        final Rectangle rect = new Rectangle(75, 75, Color.FIREBRICK);

        ChoiceBox intColorCB = new ChoiceBox(FXCollections.observableArrayList(
                "Red", "Black", "Blue", "SkyBlue", "Pink", "Yellow", "Teal"));
        intColorCB.setValue("Red");

        // load temporary CSS files into strings
        String kwBlack = getClass().getResource("IntColorCSS/IntBlack.css").toExternalForm();
        String kwGreen = getClass().getResource("IntColorCSS/IntGreen.css").toExternalForm();
        String kwBlue = getClass().getResource("IntColorCSS/IntBlue.css").toExternalForm();
        String kwYellow = getClass().getResource("IntColorCSS/IntYellow.css").toExternalForm();
        String kwPink = getClass().getResource("IntColorCSS/IntPink.css").toExternalForm();
        String kwSkyBlue = getClass().getResource("IntColorCSS/IntSkyBlue.css").toExternalForm();

        Text message = new Text("Integer(int) Color");
        message.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        message.setFill(Color.FIREBRICK);

        intColorCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                if (intColorCB.getValue() == "Red") {
                    rect.setFill(Color.FIREBRICK);
                    message.setFill(Color.FIREBRICK);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                } else if (intColorCB.getValue() == "Black") {
                    rect.setFill(Color.BLACK);
                    message.setFill(Color.BLACK);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwBlack);
                } else if (intColorCB.getValue() == "Blue") {
                    rect.setFill(Color.ROYALBLUE);
                    message.setFill(Color.ROYALBLUE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwBlue);
                } else if (intColorCB.getValue() == "Green") {
                    rect.setFill(Color.TEAL);
                    message.setFill(Color.TEAL);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwGreen);
                } else if (intColorCB.getValue() == "Yellow") {
                    rect.setFill(Color.ORANGE);
                    message.setFill(Color.ORANGE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwYellow);
                } else if (intColorCB.getValue() == "SkyBlue") {
                    rect.setFill(Color.SKYBLUE);
                    message.setFill(Color.SKYBLUE);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwSkyBlue);
                } else if (intColorCB.getValue() == "Pink") {
                    rect.setFill(Color.ORCHID);
                    message.setFill(Color.ORCHID);
                    root.getStylesheets().removeAll(kwBlack, kwBlue, kwGreen, kwYellow, kwPink, kwSkyBlue);
                    root.getStylesheets().add(kwPink);
                }
            }
        });

        intColorRoot.getChildren().addAll(message, rect, intColorCB);
        Scene keywordColorScene = new Scene(intColorRoot, 230, 200);
        intColorWin.setScene(keywordColorScene);
        intColorWin.show();
    }
}
