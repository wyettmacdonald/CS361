/*
 * File: DirectoryController.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the DirectoryController class, handling the file directory portion of the GUI.
 */

package proj7AbramsDeutschDurstJones;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

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
     * A HashMap mapping the TreeItems and associated files
     */
    private Map<TreeItem<String>, File> treeItemFileMap;
    /**
     * TabPane defined in Main.fxml
     */
    private JavaTabPane tabPane;
    /**
     * FileMenuController defined in main controller
     */
    private FileMenuController fileMenuController;

    /**
     * Sets the directory tree from Main.fxml
     *
     * @param tv the directory tree
     */
    public void setDirectoryTree(TreeView tv) {
        this.directoryTree = tv;
        this.treeItemFileMap = new HashMap<>();

        // add listener to listen for clicks in the directory tree
        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> handleDirectoryItemClicked(event);
        this.directoryTree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
    }

    /**
     * Sets the tabPane.
     *
     * @param tabPane TabPane
     */
    public void setTabPane(JavaTabPane tabPane) {
        this.tabPane = tabPane;
        // add listener to tab selection to switch directories based on open file
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
                this.createDirectoryTree());
    }

    /**
     * Sets the FileMenuController.
     *
     * @param fileMenuController FileMenuController created in main Controller.
     */
    public void setFileMenuController(FileMenuController fileMenuController) {
        this.fileMenuController = fileMenuController;
    }

    /**
     * Returns the directory tree for the given file
     *
     * @param file the file
     * @return the root TreeItem of the tree
     */
    private TreeItem<String> getNode(File file) {
        // create root, which is returned at the end
        TreeItem<String> root = new TreeItem<>(file.getName());
        treeItemFileMap.put(root, file);

        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                // recursively traverse file directory
                root.getChildren().add(getNode(f));
            } else {
                TreeItem<String> leaf = new TreeItem<>(f.getName());
                root.getChildren().add(leaf);
                treeItemFileMap.put(leaf, f);
            }
        }
        return root;
    }

    /**
     * Adds the directory tree for the current file to the GUI
     */
    public void createDirectoryTree() {
        Thread directoryCreationThread = new Thread() {
            public void run() {
                // capture current file
                File file = tabPane.getFileFromTab(tabPane.getSelectedTab());
                // create the directory tree
                if (file != null) {
                    TreeItem headNode = getNode(file.getParentFile());
                    Platform.runLater(() -> {
                        directoryTree.setRoot(headNode);
                        directoryTree.getRoot().setExpanded(true);
                    });
                }
            }
        };
        directoryCreationThread.start();
    }

    /**
     * Event handler to open a file selected from the directory
     *
     * @param event a MouseEvent object
     */
    private void handleDirectoryItemClicked(MouseEvent event) {
        // only open file if double clicked
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            event.consume();
            TreeItem selectedItem = (TreeItem) directoryTree.getSelectionModel().getSelectedItem();
            // if no item selected, do nothing
            if (selectedItem == null) {
                return;
            }
            // Open the file, if it is a java file
            String fileName = (String) selectedItem.getValue();
            if (fileName.endsWith(".java")) {
                this.fileMenuController.handleOpenFile(this.treeItemFileMap.get(selectedItem));
            }
        }
    }
}
