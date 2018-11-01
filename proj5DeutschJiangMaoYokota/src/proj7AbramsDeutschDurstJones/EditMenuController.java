/*
 * File: EditMenuController.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj7AbramsDeutschDurstJones;
import javafx.event.Event;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;

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
    private CodeAreaTabPane codeAreaTabPane;
    private TextField findTextEntry;
    // fields relating to string finding
    private String fileTextSearched;
    private ArrayList<Integer> matchStartingIndices;
    private int curMatchLength;
    private int curMatchHighlightedIdx;
    private Button prevMatchBtn;
    private Button nextMatchBtn;
    private TextField replaceTextEntry;

    /**
     * Sets the tab pane.
     *
     * @param codeAreaTabPane TabPane defined in Main.fxml
     */
    public void setCodeAreaTabPane(CodeAreaTabPane codeAreaTabPane) {
        this.codeAreaTabPane = codeAreaTabPane;
    }

    /**
     * Sets the find field.
     *
     * @param findTextEntry TextField defined in Main.fxml
     */
    public void setFindTextEntry(TextField findTextEntry) {
        this.findTextEntry = findTextEntry;
        this.matchStartingIndices = new ArrayList<>();
    }

    /**
     * Sets the replace field.
     *
     * @param replaceTextEntry TextField defined in Main.fxml
     */
    public void setReplaceTextEntryTextEntry(TextField replaceTextEntry) {
        this.replaceTextEntry = replaceTextEntry;
    }

    /**
     * Sets the tab pane.
     *
     * @param prevMatchBtn Previous button defined in Main.fxml
     */
    public void setPrevMatchBtn(Button prevMatchBtn) {
        this.prevMatchBtn = prevMatchBtn;
    }

    /**
     * Sets the tab pane.
     *
     * @param nextMatchBtn Next button defined in Main.fxml
     */
    public void setNextMatchBtn(Button nextMatchBtn) {
        this.nextMatchBtn = nextMatchBtn;
    }

    /**
     * Handles the Undo menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleUndo(Event event) {
        this.codeAreaTabPane.getActiveCodeArea().undo();
    }

    /**
     * Handles the Redo menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleRedo(Event event) {
        this.codeAreaTabPane.getActiveCodeArea().redo();
    }

    /**
     * Handles the Cut menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleCut(Event event) {
        this.codeAreaTabPane.getActiveCodeArea().cut();
    }

    /**
     * Handles the Copy menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleCopy(Event event) {
        this.codeAreaTabPane.getActiveCodeArea().copy();
    }

    /**
     * Handles the Paste menu action.
     *
     *  @param event ActionEvent object
     */
    public void handlePaste(Event event) {
        this.codeAreaTabPane.getActiveCodeArea().paste();
    }

    /**
     * Handles the Select all menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleSelectAll(Event event) {
        this.codeAreaTabPane.getActiveCodeArea().selectAll();
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


    /**
     * searches for the text entered in the "Find" TextField
     * shows appropriate error message if nothing found or provided as search string
     * enables the Previous and Next buttons if more than one match is found
     */
    public void handleFindText(Boolean showNumMatchesAlert) {

        CodeArea curJavaCodeArea = codeAreaTabPane.getActiveCodeArea();
        if (curJavaCodeArea == null) {
            showAlert("NO FILES OPEN");
            resetFindMatchingStringData();
            return;
        }

        String textToFind = this.findTextEntry.getText();
        int textToFindLength = textToFind.length();

        // check if some text was searched for
        if (textToFindLength > 0) {

            // get current file's text
            String openFileText = curJavaCodeArea.getText();

            // get index of first match, -1 if no matches
            int index = openFileText.indexOf(textToFind);

            // check if any match was found
            if (index != -1) {

                // build list of starting match indices
                this.matchStartingIndices.clear();
                while (index >= 0) {
                    this.matchStartingIndices.add(index);
                    index = openFileText.indexOf(textToFind, index + 1);

                }

                // save text of searched file
                this.fileTextSearched = openFileText;

                // first match is at the first index of the match starting indices array
                this.curMatchHighlightedIdx = 0;

                // save length of valid match
                this.curMatchLength = textToFindLength;

                // get starting index in file of first found match
                int highlightStartIdx = this.matchStartingIndices.get(0);

                // highlight first found match
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);

                if (showNumMatchesAlert) {
                    // notify the user of search results
                    showAlert(this.matchStartingIndices.size() + " MATCHES FOUND");
                }

                // enable the Previous and Next buttons if more than 1 match is found
                if (this.matchStartingIndices.size() > 1) {
                    this.setMatchNavButtonsClickable(true);
                }
                else this.setMatchNavButtonsClickable(false);

                return;
            }
            resetFindMatchingStringData();
            showAlert("NO MATCH FOUND");
            return;
        }
        resetFindMatchingStringData();
        showAlert("NOTHING TO SEARCH FOR");
    }


    /**
     * highlights the match preceding the currently highlighted match if there are
     * multiple matches found in the file
     */
    public void handleHighlightPrevMatch() {

        if (this.canHighlightMatches()) {

            CodeArea curJavaCodeArea = codeAreaTabPane.getActiveCodeArea();
            if (curJavaCodeArea == null) {
                showAlert("NO FILES OPEN");
                return;
            }

            // if first match highlighted, highlight the last match
            if (this.curMatchHighlightedIdx == 0) {

                // get index of match located last in file
                int highlightStartIdx = this.matchStartingIndices.get(
                        this.matchStartingIndices.size()-1);

                // highlight this last match
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);

                // update the index of the currently highlighted match
                this.curMatchHighlightedIdx = this.matchStartingIndices.size()-1;
            }
            // otherwise highlight the previous match
            else {
                // decrement index of highlighted match
                this.curMatchHighlightedIdx--;

                // get starting index in file of preceding match
                int highlightStartIdx = this.matchStartingIndices.get(
                        this.curMatchHighlightedIdx);

                // highlight match preceding currently highlighted match
                curJavaCodeArea.selectRange( highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);
            }
        }
    }

    /**
     * Highlights the next matched word available
     */
    public void handleHighlightNextMatch() {

        if (this.canHighlightMatches()) {

            CodeArea curJavaCodeArea = codeAreaTabPane.getActiveCodeArea();
            if (curJavaCodeArea == null) {
                showAlert("NO FILES OPEN");
                return;
            }

            // if last match in file highlighted, wrap around to highlight the first match
            if (this.curMatchHighlightedIdx == this.matchStartingIndices.size()-1) {
                // get index of match located last in file

                int highlightStartIdx = this.matchStartingIndices.get(0);
                // highlight the match located first in the file
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);

                // update the index of the currently highlighted match
                this.curMatchHighlightedIdx = 0;
            }
            // otherwise highlight the previous match
            else {
                // increment index of highlighted match
                this.curMatchHighlightedIdx++;

                // get starting index in file of next match
                int highlightStartIdx = this.matchStartingIndices.get(
                        this.curMatchHighlightedIdx);

                // highlight match after currently highlighted match
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);
            }
        }
    }

    /**
     *
     * @return true if any matches from Find can currently be highlighted, else false
     */
    private boolean canHighlightMatches() {
        CodeArea curJavaCodeArea = codeAreaTabPane.getActiveCodeArea();
        if (curJavaCodeArea == null) {
            showAlert("NO FILES OPEN");
            return false;
        }
        String openFileText = curJavaCodeArea.getText();

        // check if anything searched for
        if (this.fileTextSearched == null || this.curMatchHighlightedIdx == -1
                || this.curMatchLength == -1) {
            showAlert("MUST FIND MATCHING TEXT");
            return false;
        }
        // check if any matches found
        if (this.matchStartingIndices.size() == 0) {
            showAlert("NO MATCHES FOUND");
            return false;
        }
        // check if the file has been changed since the last search
        if (!this.fileTextSearched.equals(openFileText)) {
            showAlert("FILE HAS BEEN CHANGED SINCE PREVIOUS SEARCH, FIND AGAIN");
            setMatchNavButtonsClickable(false);
            return false;
        }
        return true;
    }

    /**
     * resets the fields used for string searching in the file when no match is found
     */
    private void resetFindMatchingStringData() {
        this.fileTextSearched = null;
        this.curMatchLength = -1;
        this.curMatchHighlightedIdx = -1;
        this.setMatchNavButtonsClickable(false);
    }

    /**
     * enables or disables the Previous and Next match navigation buttons
     * @param enable boolean denoting whether or not the Previous & Next buttons
     *               are enabled
     */
    private void setMatchNavButtonsClickable(boolean enable) {
        this.prevMatchBtn.setDisable(!enable);
        this.nextMatchBtn.setDisable(!enable);
    }

    /**
     * if there is a highlighted match, this will replace it with the text from the
     * Replace text entry
     */
    public void handleReplaceText() {

        // check that there were matches & the file has not been changed since last search
        if (this.canHighlightMatches()) {

            String textToReplaceMatch = this.replaceTextEntry.getText();

            // check that there is some text in the replace text entry
            if (textToReplaceMatch.length() > 0) {


                // get idx of currently highlighted match
                int curHighlightedMatchStartingIdx =
                        this.matchStartingIndices.get(this.curMatchHighlightedIdx);


                // replace current highlighted mach with the replaced text
                codeAreaTabPane.getActiveCodeArea().replaceText(curHighlightedMatchStartingIdx,
                        curHighlightedMatchStartingIdx+this.curMatchLength,
                        textToReplaceMatch);
                /* call find method to update the indices of the found matches
                 * to account for the changed text */
                this.handleFindText(false);
                return;
            }
            showAlert("ENTER REPLACEMENT TEXT");
        }
    }

    /**
     * creates and displays an informational alert
     *
     * @param header the content of the alert
     */
    private void showAlert(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.show();
    }
}