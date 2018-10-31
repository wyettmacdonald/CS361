/*
 * File: EditMenuController.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj7AbramsDeutschDurstJones;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
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
    @FXML private CodeAreaTabPane codeAreaTabPane;

    /**
     * Sets the tab pane.
     *
     * @param codeAreaTabPane TabPane defined in Main.fxml
     */
    public void setCodeAreaTabPane(CodeAreaTabPane codeAreaTabPane) {
        this.codeAreaTabPane = codeAreaTabPane;
    }

    /**
     * Handles the Edit menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleEditMenuAction(Event event) {
        // get the code area embedded in the selected tab window
        CodeArea activeCodeArea = this.codeAreaTabPane.getActiveCodeArea();
        MenuItem clickedItem = (MenuItem)event.getTarget();
        switch(clickedItem.getId()) {
            case "undoMenuItem":
                activeCodeArea.undo();
                break;
            case "redoMenuItem":
                activeCodeArea.redo();
                break;
            case "cutMenuItem":
                activeCodeArea.cut();
                break;
            case "copyMenuItem":
                activeCodeArea.copy();
                break;
            case "pasteMenuItem":
                activeCodeArea.paste();
                break;
            case "selectMenuItem":
                activeCodeArea.selectAll();
                break;
            case "indentTextMenuItem":
                this.handleIndentText();
            case "unindentTextMenuItem":
                this.handleUnindentText();
            default:
        }
    }

    /*
     *Indents all highlighted text by one tab per line
     */
    public void handleIndentText() {
        CodeArea activeCodeArea = this.codeAreaTabPane.getActiveCodeArea();
        String selectedText = activeCodeArea.getSelectedText();
        String selectedTextTabbed = selectedText.replace("\n", "\n\t");
        activeCodeArea.replaceSelection("\t" + selectedTextTabbed);
    }

    /*
     * Unindents all highlighted text by one tab per line if there is at least one tab on the line
     * If there's no tab, nothing happens on that line
     */
    public void handleUnindentText() {
        CodeArea activeCodeArea = this.codeAreaTabPane.getActiveCodeArea();
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