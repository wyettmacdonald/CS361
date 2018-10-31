/*
 * File: CodeAreaTabPane.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file extends the TabPane class to include a getActiveCodeArea method
 */

package proj7AbramsDeutschDurstJones;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

/**
 * This class extends the TabPane class from RichTextFx to handle
 * getting the code area from the active tab.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class CodeAreaTabPane extends TabPane {

    /**
     * Gets the code area from the active tab
     * @return the currently active code area
     */
    public CodeArea getActiveCodeArea() {
        if (this.getTabs().isEmpty()) {
            return null;
        }

        Tab selectedTab = this.getSelectionModel().getSelectedItem();
        return (CodeArea)((VirtualizedScrollPane)selectedTab.getContent()).getContent();
    }
}
