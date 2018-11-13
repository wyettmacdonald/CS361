/*
 * File: EditMenuController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj7AbramsDeutschDurstJones;
import javafx.event.Event;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;

/**
 * This controller handles Edit menu related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class EditMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    private JavaTabPane javaTabPane;

    /**
     * Sets the tab pane.
     *
     * @param javaTabPane TabPane defined in Main.fxml
     */
    public void setJavaTabPane(JavaTabPane javaTabPane) {
        this.javaTabPane = javaTabPane;
    }

    /**
     * Handles the Undo menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleUndo(Event event) {
        this.javaTabPane.getActiveCodeArea().undo();
    }

    /**
     * Handles the Redo menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleRedo(Event event) {
        this.javaTabPane.getActiveCodeArea().redo();
    }

    /**
     * Handles the Cut menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleCut(Event event) {
        this.javaTabPane.getActiveCodeArea().cut();
    }

    /**
     * Handles the Copy menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleCopy(Event event) {
        this.javaTabPane.getActiveCodeArea().copy();
    }

    /**
     * Handles the Paste menu action.
     *
     *  @param event ActionEvent object
     */
    public void handlePaste(Event event) {
        this.javaTabPane.getActiveCodeArea().paste();
    }

    /**
     * Handles the Select all menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleSelectAll(Event event) {
        this.javaTabPane.getActiveCodeArea().selectAll();
    }

    /*
     *Indents all highlighted text by one tab per line
     */
    public void handleIndentText() {
        CodeArea activeCodeArea = this.javaTabPane.getActiveCodeArea();
        String selectedText = activeCodeArea.getSelectedText();
        String selectedTextTabbed = selectedText.replace("\n", "\n\t");
        activeCodeArea.replaceSelection("\t" + selectedTextTabbed);
    }

    /*
     * Unindents all highlighted text by one tab per line if there is at least one tab on the line
     * If there's no tab, nothing happens on that line
     */
    public void handleUnindentText() {
        CodeArea activeCodeArea = this.javaTabPane.getActiveCodeArea();
        String selectedText = activeCodeArea.getSelectedText();
        String selectedTextUntabbed = selectedText.replace("\n\t", "\n");
        //The first line won't have a new line char and has to be handled separately
        String firstLine = selectedText.split("(?<=\n)")[0];
        int firstLineLength = firstLine.length();
        //replaceFirst in case there are multiple tabs on the first line
        String firstLineUntabbed = firstLine.replaceFirst("\t", "");
        selectedTextUntabbed = firstLineUntabbed + selectedTextUntabbed.substring(firstLineLength);
        activeCodeArea.replaceSelection(selectedTextUntabbed);
    }
}