/*
 * File: ToolBarController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj10AbramsDeutschDurstJones;
import javafx.application.Platform;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.event.Event;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.lexer.Scanner;
import proj10AbramsDeutschDurstJones.bantam.lexer.Token;
import proj10AbramsDeutschDurstJones.bantam.parser.Parser;
import proj10AbramsDeutschDurstJones.bantam.treedrawer.*;
import proj10AbramsDeutschDurstJones.bantam.util.CompilationException;
import proj10AbramsDeutschDurstJones.bantam.util.Error;
import proj10AbramsDeutschDurstJones.bantam.util.ErrorHandler;

import java.util.List;
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
     * Handles the Scan and parse button action.
     *
     * @param event Event object
     * @param file the Selected file
     */
    public void handleScanAndParseButtonAction(Event event, File file) {
        int userResponse = fileMenuController.checkSaveBeforeContinue();
        // user select cancel button
        if (userResponse == 2) {
            return;
        }
        // user select to save
        else if (userResponse == 1) {
            fileMenuController.handleSaveAction();
        }

        Thread scanAndParseThread = new Thread() {
            public void run() {
                handleScanAndParse(file);
            }
        };
        scanAndParseThread.start();
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

    /**
     * Helper method for printing the given list of Errors to the console
     *
     * @param errorList the list of Error objects to display
     */
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

    /**
     * Helper method for running the Scanner and displaying results.
     */
    private void handleScanAndParse(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
            });

            ErrorHandler errorHandler = new ErrorHandler();
            Parser parser = new Parser(errorHandler);

            // parse and display
            try {
                Program root = parser.parse(file.getAbsolutePath());
                Drawer drawer = new Drawer();
                drawer.draw(file.getName(), root);
                Platform.runLater(() -> {
                    this.console.appendText("Scanning and parsing completed successfully\n");
                });
            } catch (CompilationException e) {
                printErrorList(errorHandler.getErrorList());
            }
        }
        catch (Throwable e) {
            Platform.runLater(() -> {
                this.fileMenuController.createErrorDialog("Reading File",
                        "Please try again with another valid Bantam Java file.");
            });
        }
    }
}
