/*
 * File: ToolBarController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj9AbramsDeutschDurstJones;
import javafx.application.Platform;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.event.Event;
import proj9AbramsDeutschDurstJones.bantam.lexer.Scanner;
import proj9AbramsDeutschDurstJones.bantam.lexer.Token;
import proj9AbramsDeutschDurstJones.bantam.util.Error;
import proj9AbramsDeutschDurstJones.bantam.util.ErrorHandler;

import java.util.List;
import java.util.concurrent.*;
import java.io.*;

/**
 * ToolbarController handles Toolbar related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class ToolBarController {
    /**
     * Console defined in Main.fxml
     */
    private StyleClassedTextArea console;
    /**
     * Tab pane defined in Main.fxml
     */
    private JavaTabPane tabPane;
    /**
     * The FileMenuController
     */
    private FileMenuController fileMenuController;

    /**
     * Sets the console pane
     *
     * @param console StyleClassedTextArea defined in Main.fxml
     */
    public void setConsole(StyleClassedTextArea console) {
        this.console = console;
        this.console.setEditable(false);
    }

    /**
     * Sets the tab pane
     *
     * @param tabPane StyleClassedTextArea defined in Main.fxml
     */
    public void setTabPane(JavaTabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * Sets the FileMenuController.
     *
     * @param fileMenuController FileMenuController created in main Controller.
     */
    public void setFileMenuController(FileMenuController fileMenuController) {
        this.fileMenuController = fileMenuController;
    }

    /**
     * Handles the Scan button action.
     *
     * @param event Event object
     * @param file the Selected file
     */
    public void handleScanButtonAction(Event event, File file) {
        int userResponse = fileMenuController.checkSaveBeforeContinue();
        // user select cancel button
        if (userResponse == 2) {
            event.consume();
            return;
        }
        // user select to save
        else if (userResponse == 1) {
            fileMenuController.handleSaveAction();
        }

        Thread scanThread = new Thread() {
            public void run() {
                handleScan(file);
            }
        };
        scanThread.start();
    }


    /**
     * Helper method for running the Scanner and displaying results.
     */
    private void handleScan(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
            });

            ErrorHandler errorHandler = new ErrorHandler();
            Scanner scanner = new Scanner(file.getAbsolutePath(), errorHandler);

            String scannedTokens = "";
            Token token = scanner.scan();
            while(token.kind != Token.Kind.EOF) {
                scannedTokens += token.toString() + "\n";
                token = scanner.scan();
            }
            scannedTokens += token.toString();
            final String scanResults = scannedTokens;
            Platform.runLater(() -> {
                tabPane.createNewTab("Scan Results", scanResults, null);
            });

            printErrorList(errorHandler.getErrorList());

        } catch (Throwable e) {
            Platform.runLater(() -> {
                this.fileMenuController.createErrorDialog("Reading File",
                        "Please try again with another valid Java File.");
            });
        }
    }

    private void printErrorList(List<Error> errorList) {
        String errors = "";
        for (Error e : errorList) {
            errors += e.toString() + "\n";
        }
        final String output = errors + "Illegal tokens found: " + errorList.size() + "\n";
        Platform.runLater(() -> {
            this.console.appendText(output);
        });
    }
}
