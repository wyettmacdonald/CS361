/*
 * File: Main.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 */

package proj7AbramsDeutschDurstJones;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * This class creates a stage, as specified in Main.fxml, that contains a
 * set of tabs, embedded in a tab pane, with each tab window containing a
 * code area; a menu bar containing File and Edit menu; and a toolbar of
 * buttons for compiling, running, and stopping code; and a program console
 * that takes in standard input, displays standard output and program message.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class Main extends Application {
    private static Parent parentRoot;

    /**
     * Creates a stage as specified in Main.fxml, that contains a set of tabs,
     * embedded in a tab pane, with each tab window containing a code area; a menu
     * bar containing File and Edit menu; and a toolbar of buttons for compiling,
     * running, and stopping code; and a program console that takes in standard
     * input, displays standard output and program message.
     *
     * @param stage The stage that contains the window content
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/proj7AbramsDeutschDurstJones/Main.fxml"));
        Parent root = loader.load();
        Main.parentRoot = root;

        // initialize a scene and add features specified in the css file to the scene
        Scene scene = new Scene(root, 1000, 650);
        scene.getStylesheets().add(getClass().getResource(
                "/proj7AbramsDeutschDurstJones/LightMode.css").toExternalForm());

        // configure the stage
        stage.setTitle("AbramsDeutschDurstJones's Project 6");
        stage.sizeToScene();
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> ((proj7AbramsDeutschDurstJones.Controller)
                loader.getController()).handleExitAction(event));
        stage.show();
    }

    /**
     * Gets the parent root of the main program.
     *
     * @return the parent root of the main program
     */
    static public Parent getParentRoot() {
        return Main.parentRoot;
    }

    /**
     * main function of Main class
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
