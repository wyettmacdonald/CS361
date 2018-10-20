/*
 * File: EditMenuController.java
 * CS361 Project 6
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 10/27/2018
 * This file contains the EditMenuController class, handling Edit menu related actions.
 */

package proj6AbramsDeutschDurstJones;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

/**
 * Main controller handles Edit menu related actions.
 *
 * @author Liwei Jiang
 * @author Martin Deutsch
 * @author Tatsuya Yokota
 * @author Melody Mao
 */
public class EditMenuController {
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
     * Handles the Edit menu action.
     *
     *  @param event ActionEvent object
     */
    public void handleEditMenuAction(ActionEvent event) {
        // get the code area embedded in the selected tab window
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        CodeArea activeCodeArea = (CodeArea)((VirtualizedScrollPane)selectedTab.getContent()).getContent();
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
            default:
        }
    }
}