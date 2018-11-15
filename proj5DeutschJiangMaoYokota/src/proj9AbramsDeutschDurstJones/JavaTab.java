/*
 * File: JavaTab.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file extends the Tab class to include a StyledJavaCodeArea within
 * a VirtualizedScrollPane on construction
 */

package proj9AbramsDeutschDurstJones;

import javafx.scene.control.Tab;
import org.fxmisc.flowless.VirtualizedScrollPane;

/**
 * This class extends the Tab class from JavaFx to include a
 * CodeArea on construction
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class JavaTab extends Tab {

    /**
     * Field storing whether the tab is saved or dirty
     */
    private boolean isSaved;

    /**
     * Constructor initializes the tab name and tab contents
     * @param name the title of the tab
     * @param contentString the initial contents of the tab
     */
    public JavaTab(String name, String contentString) {
        StyledJavaCodeArea newStyledCodeArea = new StyledJavaCodeArea(contentString);
        this.setText(name);
        this.setContent(new VirtualizedScrollPane<>(newStyledCodeArea));
        this.isSaved = true;

        // when text is changed, mark tab as dirty
        newStyledCodeArea.textProperty().addListener((obj, oldVal, newVal) -> {
            isSaved = false;
            this.setStyle("-fx-text-base-color: green");
        });
    }

    /**
     * Gets the Java code area in this tab
     * @return the CodeArea in the tab
     */
    public StyledJavaCodeArea getCodeArea() {
        return (StyledJavaCodeArea)((VirtualizedScrollPane)this.getContent()).getContent();
    }

    /**
     * Gets whether the file is saved or dirty
     * @return true if the file is saved, false if it is dirty
     */
    public boolean isSaved() {
        return this.isSaved;
    }

    /**
     * Sets the isSaved field to the given value and changes the color of the tab title accordingly
     * @param saved the new saved value
     */
    public void setSaved(boolean saved) {
        this.isSaved = saved;
        if(saved) {
            this.setStyle(null);
        }
        else {
            this.setStyle("-fx-text-base-color: green");
        }
    }
}