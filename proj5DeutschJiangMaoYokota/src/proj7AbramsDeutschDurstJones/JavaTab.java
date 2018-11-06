/*
 * File: JavaTabPane.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
 * This file extends the TabPane class to include a getActiveCodeArea method
 */

package proj7AbramsDeutschDurstJones;

import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

/**
 * This class extends the Tab class from JavaFx to handle
 * getting the code area from the active tab.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class JavaTab extends Tab {

    /**
     * Constructor initializes the tab name and tab contents
     * @param name the title of the tab
     * @param contentString the initial contents of the tab
     */
    public JavaTab(String name, String contentString) {
        StyledJavaCodeArea newStyledCodeArea = new StyledJavaCodeArea(contentString);
        this.setText(name);
        this.setContent(new VirtualizedScrollPane<>(newStyledCodeArea));
    }

    /**
     * Gets the Java code area in this tab
     * @return the CodeArea in the tab
     */
    public CodeArea getCodeArea() {
        return (CodeArea)((VirtualizedScrollPane)this.getContent()).getContent();
    }
}