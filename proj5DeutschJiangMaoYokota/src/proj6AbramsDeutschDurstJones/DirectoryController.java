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
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab,File> tabFileMap;
    /**
     * TabPane defined in Main.fxml
     */
    private TabPane tabPane;
    /**
     * FileMenuController defined in main controller
     */
    private FileMenuController fileMenuController;

    /**
     * Sets the directory tree from Main.fxml
     * @param tv the directory tree
     */
    public void setDirectoryTree(TreeView tv) {
        this.directoryTree = tv;
        // add listener to listen for clicks in the directory tree
        EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> handleDirectoryItemClicked(event);
        this.directoryTree.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle);
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
        // add listener to tab selection to switch directories based on open file
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) ->
                this.buildTree());
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
     * @param file the file
     * @return the root TreeItem of the tree
     */
    private TreeItem<String> getNode(File file) {
        // create root, which is returned at the end
        TreeItem<String> root = new TreeItem<>(file.getName());
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                // recursively traverse file directory
                root.getChildren().add(getNode(f));
            } else {
                root.getChildren().add(new TreeItem<>(f.getName()));
            }
        }
        return root;
    }

    /**
     * Adds the directory tree for the current file to the GUI
     */
    private void buildTree() {
        // capture current file
        File file = this.tabFileMap.get(this.tabPane.getSelectionModel().getSelectedItem());
        // create the directory tree
        if (file != null) {
            this.directoryTree.setRoot(this.getNode(file.getParentFile()));
            this.directoryTree.getRoot().setExpanded(true);
        }
    }

    /**
     * Event handler to open a file selected from the directory
     * @param event a MouseEvent object
     */
    private void handleDirectoryItemClicked(MouseEvent event) {
        // only open file if double clicked
        if (event.getClickCount() == 2 && !event.isConsumed()) {
            event.consume();
            TreeItem selectedItem = (TreeItem) directoryTree.getSelectionModel().getSelectedItem();
            String fileName = (String) selectedItem.getValue();
            if(fileName.endsWith(".java")) {
                this.fileMenuController.handleOpenFile(new File(this.getItemPath(selectedItem)));
            }
        }
    }

    /**
     * Gets the path to a file selected from the directory tree
     * @param item the TreeItem selected
     * @return the path String
     */
    private String getItemPath(TreeItem item) {
        // get the path here
        return "";
    }
}
