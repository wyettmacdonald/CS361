/*
 * File: Controller.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the Main controller class, handling actions evoked by the
 * Main window.
 */

package proj7AbramsDeutschDurstJones;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

/**
 * Main controller handles actions evoked by the Main window.
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class Controller {
    /**
     * ToolbarController handling toolbar actions
     */
    private ToolBarController toolbarController;
    /**
     * FindReplaceController handling find and replace actions
     */
    private FindReplaceController findReplaceController;
    /**
     * FileMenuController handling File menu actions
     */
    private FileMenuController fileMenuController;
    /**
     * EditMenuController handling Edit menu actions
     */
    private EditMenuController editMenuController;
    /**
     * CodeMenuController handling Code menu actions
     */
    private CodeMenuController codeMenuController;
    /**
     * DirectoryController handling directory tree actions
     */
    private DirectoryController directoryController;
    /**
     * SettingMenuController handling Setting menu actions
     */
    private SettingMenuController settingMenuController;
    /**
     * treeStructure View Controller handling the current file's treeStructure View
     */
    private StructureViewController structureViewController;
    /**
     * VBox defined in Main.fxml
     */
    @FXML
    private VBox vBox;
    /**
     * Compile button defined in Main.fxml
     */
    @FXML
    private Button compileButton;
    /**
     * CompileRun button defined in Main.fxml
     */
    @FXML
    private Button compileRunButton;
    /**
     * Stop button defined in Main.fxml
     */
    @FXML
    private Button stopButton;
    /**
     * TabPane defined in Main.fxml
     */
    @FXML
    private JavaTabPane tabPane;
    /**
     * Tree of current directory
     */
    @FXML
    private TreeView<String> directoryTree;
    /**
     * Tree of current file structure
     */
    @FXML
    private TreeView fileTree;
    /**
     * the console pane defined in Main.fxml
     */
    @FXML
    private StyleClassedTextArea console;
    /**
     * Close menu item of the File menu defined in Main.fxml
     */
    @FXML
    private MenuItem closeMenuItem;
    /**
     * Save menu item of the File menu defined in Main.fxml
     */
    @FXML
    private MenuItem saveMenuItem;
    /**
     * Save As menu item of the File menu defined in Main.fxml
     */
    @FXML
    private MenuItem saveAsMenuItem;
    /**
     * Edit menu defined in Main.fxml
     */
    @FXML
    private Menu editMenu;
    /**
     * Edit menu defined in Main.fxml
     */
    @FXML
    private Menu codeMenu;
    /**
     * Find field defined in Main.fxml
     */
    @FXML
    private TextField findTextEntry;
    /**
     * Find previous button defined in Main.fxml
     */
    @FXML
    private Button findPrevBtn;
    /**
     * Find next button defined in Main.fxml
     */
    @FXML
    private Button findNextBtn;
    /**
     * Replace field defined in Main.fxml
     */
    @FXML
    private TextField replaceTextEntry;
    /**
     * The worker running the compile task
     */
    private ToolBarController.CompileWorker compileWorker;
    /**
     * The worker running the compile and run tasks
     */
    private ToolBarController.CompileRunWorker compileRunWorker;

    /**
     * Passes in relevant items to ToolbarController.
     */
    private void setupToolbarController() {
        this.toolbarController.setConsole(this.console);
        this.toolbarController.setFileMenuController(this.fileMenuController);
        this.toolbarController.initialize();
        this.compileWorker = this.toolbarController.getCompileWorker();
        this.compileRunWorker = this.toolbarController.getCompileRunWorker();
    }

    /**
     * Passes in relevant items to FindReplaceController
     */
    private void setupFindReplaceController() {
        this.findReplaceController.setJavaTabPane(this.tabPane);
        this.findReplaceController.setFindTextEntry(this.findTextEntry);
        this.findReplaceController.setNextMatchBtn(this.findNextBtn);
        this.findReplaceController.setPrevMatchBtn(this.findPrevBtn);
        this.findReplaceController.setReplaceTextEntryTextEntry(this.replaceTextEntry);
    }

    /**
     *  Passes in relevant items to FileMenuController.
     */
    private void setupFileMenuController() {
        this.fileMenuController.setDirectoryController(this.directoryController);
        this.fileMenuController.setTabPane(this.tabPane);
        this.tabPane.setFileMenuController(this.fileMenuController);
    }

    /**
     *  Passes in relevant items to EditMenuController.
     */
    private void setupEditMenuController() {
        this.editMenuController.setJavaTabPane(this.tabPane);
    }

    /**
     *  Passes in relevant items to CodeMenuController.
     */
    private void setupCodeMenuController() {
        this.codeMenuController.setTabPane(this.tabPane);
    }

    /**
     *  Passes in relevant items to SettingMenuController.
     */
    private void setupSettingMenuController() {
        this.settingMenuController.setVBox(this.vBox);
    }

    /**
     * Passes in relevant items to DirectoryController.
     */
    private void setupDirectoryController() {
        this.directoryController.setDirectoryTree(directoryTree);
        this.directoryController.setTabPane(this.tabPane);
        this.directoryController.setFileMenuController(this.fileMenuController);
    }

    /**
     * Passes in relevant items to StructureViewController
     */
    private void setupStructureViewController() {
        this.structureViewController.setTreeView(this.fileTree);

        // Updates the file structure view whenever a key is typed
        this.tabPane.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            this.updateStructureView();
        });

        // Updates the file structure view whenever the tab selection changes
        // e.g., open tab, remove tab, select another tab
        this.tabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            this.updateStructureView();
        });
    }

    /**
     * Binds the Close, Save, Save As menu items of the File menu,
     * the Edit menu, with the condition whether the tab pane is empty.
     */
    private void setButtonBinding() {
        BooleanBinding ifTabPaneEmpty = Bindings.isEmpty(tabPane.getTabs());
        ReadOnlyBooleanProperty ifCompiling = this.compileWorker.runningProperty();
        ReadOnlyBooleanProperty ifCompilingRunning =
                this.compileRunWorker.runningProperty();

        this.closeMenuItem.disableProperty().bind(ifTabPaneEmpty);
        this.saveMenuItem.disableProperty().bind(ifTabPaneEmpty);
        this.saveAsMenuItem.disableProperty().bind(ifTabPaneEmpty);
        this.editMenu.disableProperty().bind(ifTabPaneEmpty);
        this.codeMenu.disableProperty().bind(ifTabPaneEmpty);

        this.stopButton.disableProperty().bind(
                ((ifCompiling.not()).and(ifCompilingRunning.not())).or(ifTabPaneEmpty));
        this.compileButton.disableProperty().bind(
                ifCompiling.or(ifCompilingRunning).or(ifTabPaneEmpty));
        this.compileRunButton.disableProperty().bind(
                ifCompiling.or(ifCompilingRunning).or(ifTabPaneEmpty));
    }

    /**
     * This function is called after the FXML fields are populated.
     * Sets up the sub Controllers and button bindings.
     */
    @FXML
    public void initialize() {
        // initialize sub controllers
        this.fileMenuController = new FileMenuController();
        this.editMenuController = new EditMenuController();
        this.codeMenuController = new CodeMenuController();
        this.toolbarController = new ToolBarController();
        this.directoryController = new DirectoryController();
        this.settingMenuController = new SettingMenuController();
        this.structureViewController = new StructureViewController();
        this.findReplaceController = new FindReplaceController();

        // set up the sub controllers
        this.setupFileMenuController();
        this.setupEditMenuController();
        this.setupCodeMenuController();
        this.setupToolbarController();
        this.setupDirectoryController();
        this.setupSettingMenuController();
        this.setupStructureViewController();
        this.setupFindReplaceController();

        this.setButtonBinding();
    }

    /**
     * Parses and generates the structure view for the currently open code area
     */
    private void updateStructureView() {
        CodeArea currentCodeArea = this.tabPane.getActiveCodeArea();
        File currentFile = this.tabPane.getFileFromTab(this.tabPane.getSelectionModel().getSelectedItem());

        // if the code area is open
        if (currentCodeArea != null) {
            // if this is not an unsaved file
            if (currentFile != null) {
                String fileName = currentFile.getName();
                // if this is a java file
                if (fileName.endsWith(".java")) {
                    // Re-generates the tree
                    this.structureViewController.generateStructureTree(currentCodeArea.getText());
                }
            } else {
                // Gets rid of open structure view
                this.resetStructureView();
            }
        }
    }

    /**
     * Jump to the line where the selected class/method/field is declared.
     */
    @FXML
    private void handleFileTreeItemClicked()
    {
        TreeItem selectedTreeItem = (TreeItem) this.fileTree.getSelectionModel().getSelectedItem();
        CodeArea currentCodeArea = this.tabPane.getActiveCodeArea();
        if (selectedTreeItem != null)
        {
            int lineNum = this.structureViewController.getTreeItemLineNum(selectedTreeItem);
            if (currentCodeArea != null) currentCodeArea.showParagraphAtTop(lineNum - 1);
        }
    }

    /**
     * Clears the currently open structure view of all nodes
     */
    private void resetStructureView() {
        this.structureViewController.resetRootNode();
    }

    /**
     * Calls the method that handles the Compile button action from the
     * toolbarController.
     *
     * @param event Event object
     */
    @FXML
    private void handleCompileButtonAction(Event event) {
        Tab selectedTab = this.tabPane.getSelectionModel().getSelectedItem();
        this.toolbarController.handleCompileButtonAction(
                event, this.tabPane.getFileFromTab(selectedTab));
    }

    /**
     * Calls the method that handles the CompileRun button action from the
     * toolbarController.
     *
     * @param event Event object
     */
    @FXML
    private void handleCompileRunButtonAction(Event event) {
        Tab selectedTab = this.tabPane.getSelectedTab();
        this.toolbarController.handleCompileRunButtonAction(
                event, this.tabPane.getFileFromTab(selectedTab));
    }

    /**
     * Calls the method that handles the Stop button action from the
     * toolbarController.
     */
    @FXML
    private void handleStopButtonAction() {
        this.toolbarController.handleStopButtonAction();
    }

    /**
     * Calls the method that handles About menu item action from the
     * fileMenuController.
     */
    @FXML
    private void handleAboutAction() {
        this.fileMenuController.handleAboutAction();
    }

    /**
     * Calls the method that handles the New menu item action from the
     * fileMenuController.
     */
    @FXML
    private void handleNewAction() {
        this.fileMenuController.handleNewAction();
    }

    /**
     * Calls the method that handles the Open menu item action from the
     * fileMenuController.
     */
    @FXML
    private void handleOpenAction() {
        this.fileMenuController.handleOpenAction();
    }

    /**
     * Calls the method that handles the Close menu item action from the
     * fileMenuController.
     *
     * @param event Event object
     */
    @FXML
    private void handleCloseAction(Event event) {
        this.fileMenuController.handleCloseAction(event);
    }

    /**
     * Calls the method that handles the Save As menu item action from the
     * fileMenuController.
     */
    @FXML
    private void handleSaveAsAction() {
        this.fileMenuController.handleSaveAsAction();
    }

    /**
     * Calls the method that handles the Save menu item action from the
     * fileMenuController.
     */
    @FXML
    private void handleSaveAction() {
        this.fileMenuController.handleSaveAction();
    }

    /**
     * Calls the method that handles the Exit menu item action from the
     * fileMenuController.
     *
     * @param event Event object
     */
    @FXML
    public void handleExitAction(Event event) {
        this.fileMenuController.handleExitAction(event);
    }

    /**
     * Calls the method that handles the Undo menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleUndoAction(ActionEvent event) {
        this.editMenuController.handleUndo(event);
    }

    /**
     * Calls the method that handles the Redo menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleRedoAction(ActionEvent event) {
        this.editMenuController.handleRedo(event);
    }

    /**
     * Calls the method that handles the Cut menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleCutAction(ActionEvent event) {
        this.editMenuController.handleCut(event);
    }

    /**
     * Calls the method that handles the Copy menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleCopyAction(ActionEvent event) {
        this.editMenuController.handleCopy(event);
    }

    /**
     * Calls the method that handles the Paste menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handlePasteAction(ActionEvent event) {
        this.editMenuController.handlePaste(event);
    }

    /**
     * Calls the method that handles the Select All menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleSelectAllAction(ActionEvent event) {
        this.editMenuController.handleSelectAll(event);
    }

    /**
     * Calls the method that handles the Indent menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleIndentAction(ActionEvent event) {
        this.editMenuController.handleIndentText();
    }

    /**
     * Calls the method that handles the Unindent menu action from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleUnindentAction(ActionEvent event) {
        this.editMenuController.handleUnindentText();
    }

    /**
     * Calls handleFindText() of the findReplaceController
     */
    @FXML
    public void handleFindText() {
        this.findReplaceController.handleFindText();
    }

    /**
     * Calls handleHighlightPrevMatch() of the findReplaceController
     */
    @FXML
    public void handleHighlightPrevMatch() {
        this.findReplaceController.handleHighlightPrevMatch();
    }

    /**
     * Calls handleHighlightNextMatch() of the findReplaceController
     */
    @FXML
    public void handleHighlightNextMatch() {
        this.findReplaceController.handleHighlightNextMatch();
    }

    /**
     * Calls handleReplaceText() of the findReplaceController
     */
    @FXML
    public void handleReplaceText() {
        this.findReplaceController.handleReplaceText();
    }

    /**
     * Focuses on the Find Text Entry Box
     */
    @FXML
    public void handleFocusOnFindTextEntry() {
        this.findTextEntry.requestFocus();
    }

    /**
     * Focuses on the Replace Text Entry Box
     */
    @FXML
    public void handleFocusOnReplaceTextEntry() {
        this.replaceTextEntry.requestFocus();
    }

    /**
     * Handles onAction for the Light Mode menu item
     */
    @FXML
    private void handleLightModeMenuAction() {
        this.settingMenuController.handleLightMode();
    }

    /**
     * Handles onAction for the Dark Mode menu item
     */
    @FXML
    private void handleDarkModeMenuAction() {
        this.settingMenuController.handleDarkMode();
    }

    /**
     * Calls the method that handles the Keyword color menu item from the settingMenuController.
     */
    @FXML
    public void handleKeywordColorAction() {
        this.settingMenuController.handleColorAction("keyword");
    }

    /**
     * Calls the method that handles the Parentheses/Brackets color menu item from the settingMenuController.
     */
    @FXML
    public void handleParenColorAction() {
        this.settingMenuController.handleColorAction("paren");
    }

    /**
     * Calls the method that handles the String color menu item from the settingMenuController.
     */
    @FXML
    public void handleStrColorAction() {
        this.settingMenuController.handleColorAction("string");
    }

    /**
     * Calls the method that handles the Int color menu item from the settingMenuController.
     */
    @FXML
    public void handleIntColorAction() {
        this.settingMenuController.handleColorAction("integer");
    }

    /**
     * Calls the method that handles the Toggle Comments menu item action from the
     * codeMenuController.
     *
     * @param event Event object
     */
    @FXML
    public void handleToggleCommentAction(Event event) {
        this.codeMenuController.handleToggleComment(event);
    }

    /**
     * Calls the method that handles the Move Line Up menu item action from the
     * codeMenuController.
     *
     * @param event Event object
     */
    @FXML
    public void handleMoveUpAction(Event event) {
        this.codeMenuController.handleMoveUp(event);
    }

    /**
     * Calls the method that handles the Move Line Down menu item action from the
     * codeMenuController.
     *
     * @param event Event object
     */
    @FXML
    public void handleMoveDownAction(Event event) {
        this.codeMenuController.handleMoveDown(event);
    }

    /**
     * Calls the method that handles the Duplicate Line menu item action from the
     * codeMenuController.
     *
     * @param event Event object
     */
    @FXML
    public void handleDuplicateLineAction(Event event) {
        this.codeMenuController.handleDuplicateLine(event);
    }

    /**
     * Calls the method that handles the Check Same Number menu item action from the
     * codeMenuController
     */
    @FXML
    public void handleCheckSameNumberGroupingsAction() {
        this.codeMenuController.handleCheckSameNumberGoupings();
    }
}
