/*
 * File: CodeMenuController.java
 * CS361 Project 6
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 10/27/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj6AbramsDeutschDurstJones;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
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
    @FXML private TabPane tabPane;

    /**
     * Sets the tab pane.
     *
     * @param tabPane TabPane defined in Main.fxml
     */
    public void setTabPane(TabPane tabPane) { this.tabPane = tabPane; }

    /**
     * Toggles commenting for each line in currently selected code block
     * @param event the Event object
     */
    public void handleToggleComment(Event event) {
        CodeArea activeCodeArea = this.getActiveCodeArea();

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
        CodeArea activeCodeArea = this.getActiveCodeArea();
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
        CodeArea activeCodeArea = this.getActiveCodeArea();
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
        CodeArea activeCodeArea = this.getActiveCodeArea();
        String line1 = activeCodeArea.getText(p1);
        String line2 = activeCodeArea.getText(p2);
        activeCodeArea.replaceText(p1, 0, p2, activeCodeArea.getParagraphLength(p2), line2+"\n"+line1);
    }

    /**
     * Check that the number of left-facing and right-facing parenthesis, brackets
     * and braces matches
     */
    public void handleCheckWellFormed() {
        CodeArea activeCodeArea = this.getActiveCodeArea();
        // get the text of the current codeArea
        String text = activeCodeArea.getText();
        // now go through the text to check if it is malformed
        long nOpenBraces = text.chars().filter(ch -> ch == '{').count();
        long nCloseBraces= text.chars().filter(ch -> ch == '}').count();

        long nOpenParens = text.chars().filter(ch -> ch == '(').count();
        long nCloseParens = text.chars().filter(ch -> ch == ')').count();

        long nOpenBrackets = text.chars().filter(ch -> ch == '[').count();
        long nCloseBrackets = text.chars().filter(ch -> ch == ']').count();

        String bracesMessage = this.wellFormedMessage(nOpenBraces, nCloseBraces, "braces");
        String parensMessage = this.wellFormedMessage(nOpenParens, nCloseParens, "parenthesis");
        String bracketsMessage = this.wellFormedMessage(nOpenBrackets, nCloseBrackets, "brackets");

        // show messages
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Code formation report");
        alert.setHeaderText("Checking brackets, parentheses and braces");
        alert.setContentText(bracesMessage + parensMessage + bracketsMessage);
        alert.showAndWait();
    }

    /**
     *
     * @param nOpen
     * @param nClosed
     * @param type
     * @return
     */
    private String wellFormedMessage(long nOpen, long nClosed, String type) {
        String message;
        if (nOpen > nClosed) {
            message = "Missing " + (nOpen - nClosed) + " close " + type + "\n";
        }
        else if (nOpen < nClosed) {
            message = "Missing " + (nClosed - nOpen) + " open " + type + "\n";
        }
        else {
            message = type.substring(0, 1).toUpperCase() + type.substring(1) + " are well formed\n";
        }
        return message;
    }

    /**
     * Duplicates the line with the caret on it
     * @param event the Event object
     */
    public void handleDuplicateLine(Event event) {
        CodeArea activeCodeArea = this.getActiveCodeArea();
        int paragraphIndex = activeCodeArea.getCurrentParagraph();
        String newText = activeCodeArea.getText(paragraphIndex) + "\n" + activeCodeArea.getText(paragraphIndex);
        activeCodeArea.replaceText(paragraphIndex, 0, paragraphIndex,
                activeCodeArea.getParagraphLength(paragraphIndex), newText);
    }

    /**
     * Gets the code area from the active tab
     * @return the currently active code area
     */
    private CodeArea getActiveCodeArea() {
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        return (CodeArea)((VirtualizedScrollPane)selectedTab.getContent()).getContent();
    }
}