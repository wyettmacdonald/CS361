/*
 * File: DirectoryController.java
 * CS361 Project 6
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 10/27/2018
 * This file contains the DirectoryController class, handling the file directory portion of the GUI.
 */

// https://stackoverflow.com/questions/35070310/javafx-representing-directories
package proj6AbramsDeutschDurstJones;
import java.io.File;
import java.util.Map;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;

/**
 * This controller handles directory related actions.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class DirectoryController {

    /**
     * the tree view representing the directory
     */
    private TreeView directoryTree;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap;
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;

    /**
     * Sets the directory tree from Main.fxml
     * @param tv the directory tree
     */
    public void setDirectoryTree(TreeView tv) {
        this.directoryTree = tv;
    }

    /**
     * Sets the tabFileMap.
     *
     * @param tabFileMap HashMap mapping the tabs and the associated files
     */
    public void setTabFileMap(Map<Tab,File> tabFileMap) {
        this.tabFileMap = tabFileMap;
    }

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(TabPane tabPane) {
        this.tabPane = tabPane;
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            this.buildTree();
        });
    }

    /**
     * Returns the directory tree for the given file
     * @param fl the file
     * @return the root TreeItem of the tree
     */
    private TreeItem<String> getNode(File fl) {
        // create root, which is returned at the end
        TreeItem<String> root = new TreeItem<String>(fl.getName());
        for (File f : fl.listFiles()) {
            if (f.isDirectory()) {
                // recursively traverse file directory
                root.getChildren().add(getNode(f));
            } else {
                root.getChildren().add(new TreeItem<String>(f.getName()));
            }
        }
        return root;
    }

    /**
     * Adds the directory tree for the current file to the GUI
     */
    private void buildTree() {
        // capture current file
        File fl = this.tabFileMap.get(this.tabPane.getSelectionModel().getSelectedItem());
        // create the directory tree
        if (fl != null) {
            this.directoryTree.setRoot(this.getNode(fl.getParentFile()));
            this.directoryTree.getRoot().setExpanded(true);
        }
    }
}
