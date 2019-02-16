/*
 * File: ToolBarController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj12MacDonaldDouglas;
import javafx.application.Platform;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.event.Event;
import proj12MacDonaldDouglas.bantam.ast.*;
import proj12MacDonaldDouglas.bantam.lexer.Scanner;
import proj12MacDonaldDouglas.bantam.lexer.Token;
import proj12MacDonaldDouglas.bantam.parser.Parser;
import proj12MacDonaldDouglas.bantam.semant.*;
import proj12MacDonaldDouglas.bantam.treedrawer.*;
import proj12MacDonaldDouglas.bantam.util.CompilationException;
import proj12MacDonaldDouglas.bantam.util.Error;
import proj12MacDonaldDouglas.bantam.util.ErrorHandler;
import java.io.*;
import java.util.*;

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
     * Scans the given file and displays the tokens in a new tab
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

        // run scan in new thread
        Thread scanThread = new Thread() {
            public void run() {
                Platform.runLater(() -> {
                    console.clear();
                });

                try {
                    ErrorHandler errorHandler = new ErrorHandler();
                    Scanner scanner = new Scanner(file.getAbsolutePath(), errorHandler);
                    String tokens = getTokens(scanner);
                    displayTokens(tokens);

                    List<Error> errorList = errorHandler.getErrorList();
                    printErrorList(errorList);
                    Platform.runLater(() -> {
                        console.appendText("Illegal tokens found: " + errorList.size() + "\n");
                    });
                } catch(Throwable e) {
                    fileMenuController.createErrorDialog("Reading File",
                            "Please try again with another valid Java File.");
                }
            }
        };
        scanThread.start();
    }

    /**
     * Scans and parses the given file and draws its AST
     *
     * @param event Event object
     * @param file the selected file
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

        // run scan and parse in new thread
        Thread scanAndParseThread = new Thread() {
            public void run() {
                Program root = getParseTree(file);

                if (root != null) {
                    drawTree(root, file);
                    Platform.runLater(() -> {
                        console.appendText("Scanning and parsing completed successfully\n");
                    });
                }
            }
        };
        scanAndParseThread.start();
    }

    /**
     * Scans parses and checks the given file
     *
     * @param event Event object
     * @param file Saved file
     */
    public void handleScanParseAndCheckButtonAction(Event event, File file) {
        int userResponse = fileMenuController.checkSaveBeforeContinue();
        // user select cancel button
        if (userResponse == 2) {
            return;
        }
        // user select to save
        else if (userResponse == 1) {
            fileMenuController.handleSaveAction();
        }
        // run scan and parse in new thread
        Thread scanAndParseThread = new Thread() {
            public void run() {
                Program root = getParseTree(file);

                if (root != null) {
                    drawTree(root, file);
                    Platform.runLater(() -> {
                        console.appendText("Scanning and parsing completed successfully\n");
                    });
                }
            }
        };
        scanAndParseThread.start();
    }

    /**
     * Scans and parses the given file and checks for a Main.main method
     *
     * @param event Event object
     * @param file the Selected file
     */
    public void handleCheckMainButtonAction(Event event, File file) {
        MainMainVisitor mainMainVisitor = new MainMainVisitor();

        Thread checkMainThread = new Thread() {
            public void run() {
                Program root = getParseTree(file);
                if (root == null) {
                    return;
                }
                boolean result = mainMainVisitor.hasMain(root);
                Platform.runLater(() -> {
                    console.appendText("Valid Main.main method exists: " + result + "\n");
                });
            }
        };
        checkMainThread.start();
    }

    /**
     * Scans and parses the given file and shows the string constants in each method
     *
     * @param event Event object
     * @param file the Selected file
     */
    public void handleCheckStringConstantsButtonAction(Event event, File file) {
        StringConstantsVisitor stringConstantsVisitor = new StringConstantsVisitor();

        Thread checkStringConstsThread = new Thread() {
            public void run() {
                Program root = getParseTree(file);
                if (root == null) {
                    return;
                }
                Map<String, String> result = stringConstantsVisitor.getStringConstants(root);
                String contents = "String constants in program: \n";
                for (Map.Entry<String, String> entry : result.entrySet()) {
                    contents += entry.getKey() + ": " + entry.getValue() + "\n";
                }
                final String displayString = contents;
                Platform.runLater(() -> {
                    console.appendText(displayString);
                });
            }
        };
        checkStringConstsThread.start();
    }

    /**
     * Scans and parses the given file and counts the number of local variables
     * in each method
     *
     * @param event Event object
     * @param file the Selected file
     */
    public void handleCheckLocalVarsButtonAction(Event event, File file) {
        NumLocalVarsVisitor numLocalVarsVisitor = new NumLocalVarsVisitor();

        Thread checkLocalVarsThread = new Thread() {
            public void run() {
                Program root = getParseTree(file);
                if (root == null) {
                    return;
                }
                Map<String, Integer> result = numLocalVarsVisitor.getNumLocalVars(root);
                String contents = "Local variables per method:\n";
                for (Map.Entry<String, Integer> entry : result.entrySet()) {
                    contents += entry.getKey() + ": " + entry.getValue() + "\n";
                }
                final String displayString = contents;
                Platform.runLater(() -> {
                    console.appendText(displayString);
                });
            }
        };
        checkLocalVarsThread.start();
    }

    /**
     * Helper method for running the Scanner and displaying results.
     */
    private String getTokens(Scanner scanner) {
        String scannedTokens = "";
        Token token = scanner.scan();
        while (token.kind != Token.Kind.EOF) {
            scannedTokens += token.toString() + "\n";
            token = scanner.scan();
        }
        scannedTokens += token.toString();
        return scannedTokens;
    }

    /**
     * Helper method for running the Scanner and displaying results.
     */
    private Program getParseTree(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
            });

            ErrorHandler errorHandler = new ErrorHandler();
            Parser parser = new Parser(errorHandler);

            // parse and display
            try {
                return parser.parse(file.getAbsolutePath());
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
        return null;
    }

    /**
     * Helper method for displaying a new tab with all the scanned tokens
     * @param scanResults the String of all the tokens to display
     */
    private void displayTokens(String scanResults) {
        Platform.runLater(() -> {
            tabPane.createNewTab("Scan Results", scanResults, null);
        });
    }

    /**
     * Helper function for drawing an AST
     * @param root the Program node at the root of the AST
     * @param file the file to draw an AST for
     */
    private void drawTree(Program root, File file) {
        Drawer drawer = new Drawer();
        drawer.draw(file.getName(), root);
    }

    /**
     * Helper method for printing the given list of Errors to the console
     *
     * @param errorList the list of Error objects to display
     */
    private void printErrorList(List<Error> errorList) {
        StringBuilder errors = new StringBuilder();
        for (Error e : errorList) {
            errors.append(e.toString());
            errors.append("\n");
        }

        Platform.runLater(() -> {
            this.console.appendText(errors.toString());
        });
    }
}