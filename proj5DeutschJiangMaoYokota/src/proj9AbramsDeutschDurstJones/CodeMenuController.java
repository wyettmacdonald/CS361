/*
 * File: CodeMenuController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj9AbramsDeutschDurstJones;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.fxmisc.richtext.CaretSelectionBind;
import org.fxmisc.richtext.CodeArea;
import javafx.event.Event;

/**
 * This controller handles Code menu related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class CodeMenuController {
    /**
     * TabPane defined in Main.fxml
     */
    @FXML private JavaTabPane tabPane;

    /**
     * Sets the tab pane.
     *
     * @param tabPane TabPane defined in Main.fxml
     */
    public void setTabPane(JavaTabPane tabPane) {
        this.tabPane = tabPane;
    }

    /**
     * Toggles commenting for each line in currently selected code block
     * @param event the Event object
     */
    public void handleToggleComment(Event event) {
        CodeArea activeCodeArea = tabPane.getActiveCodeArea();
        if (activeCodeArea == null) {
            return;
        }
        // get selected lines of text
        CaretSelectionBind<?, ?, ?> selection = activeCodeArea.getCaretSelectionBind();
        int startParagraph = selection.getStartParagraphIndex();
        int endParagraph = selection.getEndParagraphIndex();
        int startCol = selection.getStartColumnPosition();
        int endCol = selection.getEndColumnPosition();

        // toggle comment on each line
        String newCodeBlock = "";
        for (int i = startParagraph; i < endParagraph; i++) {
            String line = activeCodeArea.getText(i);
            newCodeBlock += this.toggleCommentLine(line) + "\n";
        }
        newCodeBlock += this.toggleCommentLine(activeCodeArea.getText(endParagraph));

        // add lines with comments toggled back to code area
        activeCodeArea.replaceText(startParagraph, 0, endParagraph,
                activeCodeArea.getParagraphLength(endParagraph), newCodeBlock);

        // determine new bounds of selection
        if (activeCodeArea.getText(startParagraph).startsWith("//")) {
            startCol += 2;
        }
        else if (startCol != 0) {
            startCol -= 2;
        }

        if (activeCodeArea.getText(endParagraph).startsWith("//")) {
            endCol += 2;
        }
        else {
            endCol -= 2;
        }
        // select previously selected text
        activeCodeArea.selectRange(startParagraph, startCol, endParagraph, endCol);
    }

    /**
     * Appends a line comment to front of a string if not already there, otherwise removes line comment
     * @param line the String to comment or uncomment
     * @return the line with comment toggled
     */
    private String toggleCommentLine(String line) {
        if(line.startsWith("//")) {
            line = line.substring(2);
        }
        else {
            line = "//" + line;
        }
        return line;
    }

    /**
     * Moves the line with the cursor on it up one line
     * @param event the Event object
     */
    public void handleMoveUp(Event event) {
        CodeArea activeCodeArea = tabPane.getActiveCodeArea();
        if (activeCodeArea == null) {
            return;
        }
        int paragraphIndex = activeCodeArea.getCurrentParagraph();
        int caretCol = activeCodeArea.getCaretColumn();
        int numLines = activeCodeArea.getParagraphs().size();
        // if the line cannot be moved up, return
        if(numLines < 2 || paragraphIndex == 0) {
            return;
        }
        // swap the given line and the line before it
        this.swapLines(paragraphIndex-1, paragraphIndex);
        // move the caret to its previous position on the moved line
        activeCodeArea.displaceCaret(activeCodeArea.getAbsolutePosition(paragraphIndex-1, caretCol));
    }

    /**
     * Moves the line with the cursor on it down one line
     * @param event the Event object
     */
    public void handleMoveDown(Event event) {
        CodeArea activeCodeArea = tabPane.getActiveCodeArea();
        if (activeCodeArea == null) {
            return;
        }
        int paragraphIndex = activeCodeArea.getCurrentParagraph();
        int caretCol = activeCodeArea.getCaretColumn();
        int numLines = activeCodeArea.getParagraphs().size();
        // if the line cannot be moved down, return
        if(numLines < 2 || paragraphIndex == numLines-1) {
            return;
        }
        // swap the given line and the line after it
        this.swapLines(paragraphIndex, paragraphIndex+1);
        // move the caret to its previous position on the moved line
        activeCodeArea.displaceCaret(activeCodeArea.getAbsolutePosition(paragraphIndex+1, caretCol));

    }

    /**
     * Swaps the given consecutive line numbers in the active code area
     * @param p1 the line number of the first paragraph
     * @param p2 the line number of the second paragraph
     */
    private void swapLines(int p1, int p2) {
        CodeArea activeCodeArea = tabPane.getActiveCodeArea();
        if (activeCodeArea == null) {
            return;
        }
        String line1 = activeCodeArea.getText(p1);
        String line2 = activeCodeArea.getText(p2);
        activeCodeArea.replaceText(p1, 0, p2, activeCodeArea.getParagraphLength(p2), line2+"\n"+line1);
    }

    /**
     * Duplicates the line with the caret on it
     * @param event the Event object
     */
    public void handleDuplicateLine(Event event) {
        CodeArea activeCodeArea = tabPane.getActiveCodeArea();
        if (activeCodeArea == null) {
            return;
        }
        // replace current line with current line duplicated
        int paragraphIndex = activeCodeArea.getCurrentParagraph();
        String newText = activeCodeArea.getText(paragraphIndex) + "\n" + activeCodeArea.getText(paragraphIndex);
        activeCodeArea.replaceText(paragraphIndex, 0, paragraphIndex,
                activeCodeArea.getParagraphLength(paragraphIndex), newText);
    }
}