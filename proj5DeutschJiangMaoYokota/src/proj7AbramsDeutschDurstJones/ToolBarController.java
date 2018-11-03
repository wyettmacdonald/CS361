/*
 * File: ToolBarController.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file contains the ToolBarController class, handling Toolbar related actions.
 */

package proj7AbramsDeutschDurstJones;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.StyleClassedTextArea;
import javafx.event.Event;

import java.util.concurrent.*;
import java.io.*;

import javafx.concurrent.Task;
import javafx.concurrent.Service;

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
     * Process currently compiling or running a Java file
     */
    private Process curProcess;
    /**
     * Thread representing the Java program input stream
     */
    private Thread inThread;
    /**
     * Thread representing the Java program output stream
     */
    private Thread outThread;
    /**
     * Mutex lock to control input and output threads' access to console
     */
    private Semaphore consoleMutex;
    /**
     * The consoleLength of the output on the console
     */
    private int consoleLength;
    /**
     * The FileMenuController
     */
    private FileMenuController fileMenuController;
    /**
     * A CompileWorker object compiles a Java file in a separate thread.
     */
    private CompileWorker compileWorker;
    /**
     * A CompileRunWorker object compiles and runs a Java file in a separate thread.
     */
    private CompileRunWorker compileRunWorker;

    /**
     * OutputType defines the four different expected types of output the ide may produce
     * and thus the console output may have to deal with.
     */
    private enum OutputType {
        OUTPUT,
        ERROR,
        PROCESS_INFO,
        DEFAULT,
    }

    /**
     * Initializes the ToolBarController controller.
     * Sets the Semaphore, the CompileWorker and the CompileRunWorker.
     */
    public void initialize() {
        this.consoleMutex = new Semaphore(1);
        this.compileWorker = new CompileWorker();
        this.compileRunWorker = new CompileRunWorker();
        this.consoleLength = 0;
    }

    /**
     * Sets the console pane and the event filter to prevent modifying previous console output.
     *
     * @param console StyleClassedTextArea defined in Main.fxml
     */
    public void setConsole(StyleClassedTextArea console) {
        this.console = console;

        // prevent the user from backspacing any previous contents of the console
        this.console.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (console.getCaretPosition() == consoleLength && event.getCode() == KeyCode.BACK_SPACE) {
                event.consume();
            }
        });

        // set styling for user typed input
        this.console.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            if (event.getCode() != KeyCode.ENTER) {
                this.console.setStyleClass(consoleLength, this.console.getLength(), "default");
            }
        });
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
     * Gets the CompileWorker.
     *
     * @return CompileWorker
     */
    public CompileWorker getCompileWorker() {
        return this.compileWorker;
    }

    /**
     * Gets the CompileRunWorker.
     *
     * @return CompileRunWorker
     */
    public CompileRunWorker getCompileRunWorker() {
        return this.compileRunWorker;
    }

    /**
     * Helper method for running Java Compiler.
     */
    private boolean compileJavaFile(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
                this.consoleLength = 0;
            });

            ProcessBuilder pb = new ProcessBuilder("javac", file.getAbsolutePath());
            this.curProcess = pb.start();

            this.outputToConsole();

            // true if compiled without compile-time error, else false
            return this.curProcess.waitFor() == 0;
        } catch (Throwable e) {
            Platform.runLater(() -> {
                this.fileMenuController.createErrorDialog("File Compilation", "Error compiling.\n" +
                        "Please try again with another valid Java File.");
            });
            return false;
        }
    }

    /**
     * Helper method for running Java Program.
     */
    private boolean runJavaFile(File file) {
        try {
            Platform.runLater(() -> {
                this.console.clear();
                consoleLength = 0;
            });
            ProcessBuilder pb = new ProcessBuilder("java", file.getName().substring(0,
                    file.getName().length() - 5));
            pb.directory(file.getParentFile());
            this.curProcess = pb.start();

            // Start output and input in different threads to avoid deadlock
            this.outThread = new Thread() {
                public void run() {
                    try {
                        // start output thread first
                        consoleMutex.acquire();
                        outputToConsole();
                    } catch (Throwable e) {
                        Platform.runLater(() -> {
                            // print stop message if other thread hasn't
                            if (consoleLength == console.getLength()) {
                                writeToConsole( "\nProgram exited unexpectedly\n", OutputType.ERROR);
                            }
                        });
                    }
                }
            };
            outThread.start();

            inThread = new Thread() {
                public void run() {
                    try {
                        inputFromConsole();
                    } catch (Throwable e) {
                        Platform.runLater(() -> {
                            // print stop message if other thread hasn't
                            if (consoleLength == console.getLength()) {
                                writeToConsole( "\nProgram exited unexpectedly\n", OutputType.ERROR);
                            }
                        });
                    }
                }
            };
            inThread.start();

            // true if compiled without compile-time error, else false
            return curProcess.waitFor() == 0;
        } catch (Throwable e) {
            Platform.runLater(() -> {
                this.fileMenuController.createErrorDialog("File Running", "Error running "
                        + file.getName() + ".");
            });
            return false;
        }
    }

    /**
     * Helper method for getting program output
     */
    private void outputToConsole() throws java.io.IOException, java.lang.InterruptedException {
        InputStream stdout = this.curProcess.getInputStream();
        InputStream stderr = this.curProcess.getErrorStream();

        BufferedReader outputReader = new BufferedReader(new InputStreamReader(stdout));
        printOutput(outputReader, OutputType.OUTPUT);

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(stderr));
        printOutput(errorReader, OutputType.ERROR);
    }

    /**
     * Helper method for getting program input
     */
    private void inputFromConsole() throws java.io.IOException, java.lang.InterruptedException {
        OutputStream stdin = curProcess.getOutputStream();
        BufferedWriter inputWriter = new BufferedWriter(new OutputStreamWriter(stdin));

        while (curProcess.isAlive()) {
            // wait until signaled by output thread
            this.consoleMutex.acquire();
            // write input to program
            writeInput(inputWriter);
            // signal output thread
            this.consoleMutex.release();
            // wait for output to acquire consoleMutex
            Thread.sleep(1);
        }
        inputWriter.close();
    }
    
    /**
     * Adds a new, separate line of text to the console.
     * Used in ToolbarController when printing to the console.
     * @param newString the string to add to the console
     * @param type the content type added to the console
     */
    public void writeToConsole(String newString, OutputType type){
        int fromIndex = this.console.getText().length();
        this.console.appendText(newString);

        // Style the texts differently base on their source provided
        int toIndex = this.console.getText().length();
        switch(type) {
            case OUTPUT:
                this.console.setStyleClass(fromIndex, toIndex, "output");
                break;
            case ERROR:
                this.console.setStyleClass(fromIndex, toIndex, "error");
                break;
            case PROCESS_INFO:
                this.console.setStyleClass(fromIndex, toIndex, "processInfo");
                break;
            case DEFAULT: // intentional default state
            default: // non-intentional default state
                // more elegant way of dealing with DEFAULT type
                // than just reseting type at the end of this function
                this.console.setStyleClass(fromIndex, toIndex, "default");
        }

        // replicated previous group's: this.console.moveCaretToEnd();
        // which is a method on the console class they had that moved
        // the caret to the end of the output
        this.console.moveTo(toIndex);
        this.console.requestFollowCaret();
        this.console.setStyleClass(toIndex, toIndex, "default");
    }

    /**
     * Helper method for printing to console
     *
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    private void printOutput(BufferedReader reader, OutputType type) throws java.io.IOException, java.lang.InterruptedException {
        // if the output stream is paused, signal the input thread
        if (!reader.ready()) {
            this.consoleMutex.release();
        }

        int intch;
        // read in program output one character at a time
        while ((intch = reader.read()) != -1) {
            this.consoleMutex.tryAcquire();
            char ch = (char) intch;
            String out = Character.toString(ch);
            Platform.runLater(() -> writeToConsole( Character.toString(ch), type));
            // update console length tracker to include output character
            this.consoleLength++;

            // if the output stream is paused, signal the input thread
            if (!reader.ready()) {
                this.consoleMutex.release();
            }
            // wait for input thread to acquire consoleMutex if necessary
            Thread.sleep(1);
        }
        this.consoleMutex.release();
        reader.close();
    }

    
    /**
     * Helper function to write user input
     */
    private void writeInput(BufferedWriter writer) throws java.io.IOException {
        // wait for user to input line of text
        while (true) {
            // do not allow the user to edit the previous console text
            if (this.console.getCaretPosition() < this.consoleLength) {
                this.console.setEditable(false);
            }
            else {
                this.console.setEditable(true);
            }

            // if the user has entered text
            if (this.console.getLength() > this.consoleLength) {
                // check if user has hit enter
                if (this.console.getText().substring(this.consoleLength).contains("\n")) {
                    break;
                }
            }
        }
        // write user-entered text to program input
        writer.write(this.console.getText().substring(this.consoleLength));
        writer.flush();

        // update console length to include user input
        this.consoleLength = this.console.getLength();
    }


    /**
     * A CompileWorker subclass handling Java program compiling in a separated thread in the background.
     * CompileWorker extends the javafx Service class.
     */
    protected class CompileWorker extends Service<Boolean> {
        /**
         * the selected file to be compiled.
         */
        private File file;
        /**
         * Sets the selected file.
         *
         * @param file the selected file to be compiled.
         */
        private void setFile(File file) {
            this.file = file;
        }

        /**
         * Overrides the createTask method in Service class.
         * Compiles the file embedded in the selected tab, if appropriate.
         *
         * @return true if the program compiles successfully;
         *         false otherwise.
         */
        @Override protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                /**
                 * Called when we execute the start() method of a CompileRunWorker object
                 * Compiles the file.
                 *
                 * @return true if the program compiles successfully;
                 *         false otherwise.
                 */
                @Override protected Boolean call() {
                    Boolean compileResult = compileJavaFile(file);
                    if (compileResult) {
                        Platform.runLater(() -> writeToConsole( "Compilation was successful!\n", OutputType.PROCESS_INFO));
                    }
                    return compileResult;
                }
            };
        }
    }

    /**
     * A CompileRunWorker subclass handling Java program compiling and running in a separated thread in the background.
     * CompileWorker extends the javafx Service class.
     */
    protected class CompileRunWorker extends Service<Boolean> {
        /**
         * the selected tab in which the embedded file is to be compiled.
         */
        private File file;
        /**
         * Sets the selected tab.
         *
         * @param file the file to be compiled.
         */
        private void setFile(File file) {
            this.file = file;
        }

        /**
         * Overrides the createTask method in Service class.
         * Compiles and runs the file embedded in the selected tab, if appropriate.
         *
         * @return true if the program runs successfully;
         *         false otherwise.
         */
        @Override protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                /**
                 * Called when we execute the start() method of a CompileRunWorker object.
                 * Compiles the file and runs it if compiles successfully.
                 *
                 * @return true if the program runs successfully;
                 *         false otherwise.
                 */
                @Override protected Boolean call() {
                    if (compileJavaFile(file)) {
                        return runJavaFile(file);
                    }
                    return false;
                }
            };
        }
    }

    /**
     * Handles the Compile button action.
     *
     * @param event Event object
     * @param file the Selected file
     */
    public void handleCompileButtonAction(Event event, File file) {
        // user select cancel button
        if (fileMenuController.checkSaveBeforeCompile() == 2) {
            event.consume();
        }
        else {
            compileWorker.setFile(file);
            compileWorker.restart();
        }
    }

    /**
     * Handles the CompileRun button action.
     *
     * @param event Event object
     * @param file the selected File
     */
    public void handleCompileRunButtonAction(Event event, File file) {
        // user select cancel button
        if (fileMenuController.checkSaveBeforeCompile() == 2) {
            event.consume();
        }
        else {
            compileRunWorker.setFile(file);
            compileRunWorker.restart();
        }
    }

    /**
     * Handles the Stop button action.
     */
    public void handleStopButtonAction() {
        try {
            if (this.curProcess.isAlive()) {
                this.inThread.interrupt();
                this.outThread.interrupt();
                this.curProcess.destroy();
                writeToConsole( "\nProcess terminated.\n", OutputType.PROCESS_INFO);
            }
        } catch (Throwable e) {
            this.fileMenuController.createErrorDialog("Program Stop",
                    "Error stopping the Java program.");
        }
    }
}
