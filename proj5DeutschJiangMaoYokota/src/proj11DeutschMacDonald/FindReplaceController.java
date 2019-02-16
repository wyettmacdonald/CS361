/*
 * File: FindReplaceController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the FindReplaceController class, handling find and replace related actions.
 */

package proj11DeutschMacDonald;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;

/**
 * This controller handles Find and Replace related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class FindReplaceController {

    /**
     * TabPane defined in Main.fxml
     */
    private JavaTabPane javaTabPane;

    // find and replace related fields
    private TextField findTextEntry;
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
     * @param javaTabPane TabPane defined in Main.fxml
     */
    public void setJavaTabPane(JavaTabPane javaTabPane) {
        this.javaTabPane = javaTabPane;
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
                javaTabPane.getActiveCodeArea().replaceText(curHighlightedMatchStartingIdx,
                        curHighlightedMatchStartingIdx+this.curMatchLength,
                        textToReplaceMatch);
                /* call find method to update the indices of the found matches
                 * to account for the changed text */
                this.handleFindText();
                return;
            }
            showAlert("ENTER REPLACEMENT TEXT");
        }
    }

    /**
     * searches for the text entered in the "Find" TextField
     * shows appropriate error message if nothing found or provided as search string
     * enables the Previous and Next buttons if more than one match is found
     */
    public void handleFindText() {
        CodeArea codeArea = this.javaTabPane.getActiveCodeArea();

        if (codeArea == null) {
            showAlert("NO FILES OPEN");
            resetFindMatchingStringData();
            return;
        }

        String textToFind = findTextEntry.getText();
        int textToFindLength = textToFind.length();

        // check if some text was searched for
        if (textToFindLength > 0) {

            // get current file's text
            String openFileText = codeArea.getText();

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
                codeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);
                codeArea.requestFollowCaret();

                // notify the user of search results
                showAlert(this.matchStartingIndices.size() + " MATCHES FOUND");

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

            CodeArea curJavaCodeArea = javaTabPane.getActiveCodeArea();
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
                curJavaCodeArea.requestFollowCaret();

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
                curJavaCodeArea.requestFollowCaret();
            }
        }
    }

    /**
     * Highlights the next matched word available
     */
    public void handleHighlightNextMatch() {
        CodeArea curJavaCodeArea = javaTabPane.getActiveCodeArea();
        if (curJavaCodeArea == null) {
            showAlert("NO FILES OPEN");
            return;
        }

        if (this.canHighlightMatches()) {

            // if last match in file highlighted, wrap around to highlight the first match
            if (this.curMatchHighlightedIdx == this.matchStartingIndices.size()-1) {
                // get index of match located last in file

                int highlightStartIdx = this.matchStartingIndices.get(0);
                // highlight the match located first in the file
                curJavaCodeArea.selectRange(highlightStartIdx,
                        highlightStartIdx+this.curMatchLength);
                curJavaCodeArea.requestFollowCaret();

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
                curJavaCodeArea.requestFollowCaret();
            }
        }
    }

    /**
     *
     * @return true if any matches from Find can currently be highlighted, else false
     */
    private boolean canHighlightMatches() {
        CodeArea curJavaCodeArea = javaTabPane.getActiveCodeArea();
        if (curJavaCodeArea == null) {
            return false;
        }
        String openFileText = curJavaCodeArea.getText();

        // check if anything searched for
        if (this.fileTextSearched == null || this.curMatchHighlightedIdx == -1
                || this.curMatchLength == -1) {
            return false;
        }
        // check if any matches found
        if (this.matchStartingIndices.size() == 0) {
            return false;
        }
        // check if the file has been changed since the last search
        if (!this.fileTextSearched.equals(openFileText)) {
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
     * creates and displays an informational alert
     *
     * @param header the content of the alert
     */
    private void showAlert(String header) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(header);
        a.show();
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
}
