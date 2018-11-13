/*
 * File: JavaTabPane.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file extends the TabPane class to handle mapping tabs to
 * files and opening and closing tabs
 */

package proj7AbramsDeutschDurstJones;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.io.File;
import java.util.*;

/**
 * This class extends the TabPane class from JavaFx to handle
 * getting the code area from the active tab.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class JavaTabPane extends TabPane {

    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab, File> tabFileMap;

    /**
     * the fileMenuController object
     */
    private FileMenuController fileMenuController;

    /**
     * Constructor initializes the tab-file map
     */
    public JavaTabPane() {
        this.tabFileMap = new HashMap<>();
    }

    /**
     * Sets the fileMenuController created in Controller
     * @param fileMenuController the fileMenuController object
     */
    public void setFileMenuController(FileMenuController fileMenuController) {
        this.fileMenuController = fileMenuController;
    }

    /**
     * Returns the list of all tabs
     * @return the Set of tabs
     */
    public Set<Tab> getTabList() {
        return this.tabFileMap.keySet();
    }

    /**
     * Gets the code area from the active tab
     * @return the currently active code area
     */
    public StyledJavaCodeArea getActiveCodeArea() {
        if (this.getTabs().isEmpty()) {
            return null;
        }
        JavaTab selectedTab = (JavaTab) this.getSelectionModel().getSelectedItem();
        return selectedTab.getCodeArea();
    }

    /**
     * Gets the file corresponding to the given tab
     * @param tab the tab to get the corresponding file of
     * @return the File object matching the tab
     */
    public File getFileFromTab(Tab tab) {
        return this.tabFileMap.get(tab);
    }

    /**
     * Creates a new JavaTab
     * @param fileName the name of the new tab
     * @param contents the contents of the new tab
     * @param file the File being opened in the new tab, if it exists
     */
    public void createNewTab(String fileName, String contents, File file) {
        JavaTab newTab = new JavaTab(fileName, contents);
        newTab.setOnCloseRequest(event -> this.fileMenuController.handleCloseAction(event));
        this.tabFileMap.put(newTab, file);
        this.getTabs().add(newTab);
        this.getSelectionModel().select(newTab);
    }

    /**
     * Removes the given tab from the tab pane
     * @param tab the tab to remove
     */
    public void removeTab(Tab tab) {
        this.tabFileMap.remove(tab);
        this.getTabs().remove(tab);
    }

    /**
     * Associate the given tab and file
     * @param tab the Tab to map
     * @param file the File to map
     */
    public void mapTabToFile(Tab tab, File file) {
        this.tabFileMap.put(tab, file);
    }

    /**
     * Gets whether the given file is open in a tab
     * @param file the file to check for
     * @return true if the file is open, false otherwise
     */
    public boolean containsFile(File file) {
        for (Map.Entry<Tab, File> entry : this.tabFileMap.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue().equals(file)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Select the given tab in the tab pane
     * @param tab the Tab to select
     */
    public void selectTab(Tab tab) {
        this.getSelectionModel().select(tab);
    }

    /**
     * Select the tab holding the given file in the tab pane
     * @param file the File whose tab to select
     */
    public void selectTabFromFile(File file) {
        for (Map.Entry<Tab, File> entry : this.tabFileMap.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getValue().equals(file)) {
                    this.selectTab(entry.getKey());
                    return;
                }
            }
        }
    }

    /**
     * Get the currently selected tab
     * @return the selected JavaTab
     */
    public JavaTab getSelectedTab() {
        return (JavaTab) this.getSelectionModel().getSelectedItem();
    }
}
