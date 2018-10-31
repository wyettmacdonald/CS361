/*
 * File: Controller.java
 * CS361 Project 7
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/3/2018
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
import java.util.*;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.scene.layout.VBox;
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
    private CodeAreaTabPane codeAreaTabPane;
    /**
     * Tree of current directory
     */
    @FXML
    private TreeView<String> directoryTree;
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
     * Find button defined in Main.fxml
     */
    @FXML
    private Button findButton;
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
     * Replace button defined in Main.fxml
     */
    @FXML
    private Button replaceButton;
    /**
     * Replace field defined in Main.fxml
     */
    @FXML
    private TextField replaceTextEntry;
    /**
     * a HashMap mapping the tabs and the associated files
     */
    private Map<Tab, File> tabFileMap = new HashMap<>();
    /**
     * Stores CSS files for different color modes
     */
    private String lightModeCss =
            getClass().getResource("LightMode.css").toExternalForm();
    private String darkModeCss =
            getClass().getResource("DarkMode.css").toExternalForm();
    /**
     * The worker running the compile task
     */
    private ToolBarController.CompileWorker compileWorker;
    /**
     * The worker running the compile and run tasks
     */
    private ToolBarController.CompileRunWorker compileRunWorker;

    /**
     * Creates a reference to the ToolbarController and passes in window items
     * and other sub Controllers when necessary.
     */
    private void setupToolbarController() {
        this.toolbarController.setConsole(this.console);
        this.toolbarController.setFileMenuController(this.fileMenuController);
        this.toolbarController.initialize();
        this.compileWorker = this.toolbarController.getCompileWorker();
        this.compileRunWorker = this.toolbarController.getCompileRunWorker();

        // when the tab selection is change, matches is cleared
        this.codeAreaTabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, t, t1) -> this.toolbarController.clearMatches());
    }

    /**
     * Creates a reference to the FileMenuController and passes in window items
     * and other sub Controllers when necessary.
     */
    private void setupFileMenuController() {
        this.fileMenuController.setDirectoryController(this.directoryController);
        this.fileMenuController.setTabFileMap(this.tabFileMap);
        this.fileMenuController.setCodeAreaTabPane(this.codeAreaTabPane);
    }

    /**
     * Creates a reference to the EditMenuController and passes in window items
     * and other sub Controllers when necessary.
     */
    private void setupEditMenuController() {
        this.editMenuController.setCodeAreaTabPane(this.codeAreaTabPane);
        this.editMenuController.setFindTextEntry(this.findTextEntry);
        this.editMenuController.setNextMatchBtn(this.findNextBtn);
        this.editMenuController.setPrevMatchBtn(this.findPrevBtn);
        this.editMenuController.setReplaceTextEntryTextEntry(this.replaceTextEntry);
    }

    /**
     * Creates a reference to the CodeMenuController and passes in window items
     * nd other sub Controllers when necessary
     */
    private void setupCodeMenuController() {
        this.codeMenuController.setCodeAreaTabPane(this.codeAreaTabPane);
    }

    /**
     * Creates a reference to the DirectoryController and passes in the directory
     * tree for the controller to take ownership of.
     */
    private void setupDirectoryController() {
        this.directoryController.setDirectoryTree(directoryTree);
        this.directoryController.setTabFileMap(this.tabFileMap);
        this.directoryController.setTabPane(this.codeAreaTabPane);
        this.directoryController.setFileMenuController(this.fileMenuController);
    }

    /**
     * Creates a reference to the SettingMenuController.
     */
    private void setupSettingMenuController() {
        this.settingMenuController.setTabPane(this.codeAreaTabPane);
    }

    /**
     * Binds the Close, Save, Save As menu items of the File menu,
     * the Edit menu, with the condition whether the tab pane is empty.
     */
    private void setButtonBinding() {
        BooleanBinding ifTabPaneEmpty = Bindings.isEmpty(codeAreaTabPane.getTabs());
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
     * Initializes the tab file map with the default tab.
     * Sets up bindings.
     * Sets up references to the sub Controllers.
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

        // set up the sub controllers
        this.setupFileMenuController();
        this.setupEditMenuController();
        this.setupCodeMenuController();
        this.setupToolbarController();
        this.setupDirectoryController();
        this.setupSettingMenuController();

        this.setButtonBinding();
    }

    /**
     * Calls the method that handles the Compile button action from the
     * toolbarController.
     *
     * @param event Event object
     */
    @FXML
    private void handleCompileButtonAction(Event event) {
        Tab selectedTab = this.codeAreaTabPane.getSelectionModel().getSelectedItem();
        this.toolbarController.handleCompileButtonAction(
                event, this.tabFileMap.get(selectedTab));
    }

    /**
     * Calls the method that handles the CompileRun button action from the
     * toolbarController.
     *
     * @param event Event object
     */
    @FXML
    private void handleCompileRunButtonAction(Event event) {
        Tab selectedTab = this.codeAreaTabPane.getSelectionModel().getSelectedItem();
        this.toolbarController.handleCompileRunButtonAction(
                event, this.tabFileMap.get(selectedTab));
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
     * Calls the method that handles the simple Edit menu actions from the
     * editMenuController.
     *
     * @param event ActionEvent object
     */
    @FXML
    private void handleEditMenuAction(ActionEvent event) {
        this.editMenuController.handleEditMenuAction(event);
    }

    /**
     * Calls handleFindText() of the editController
     */
    @FXML
    public void handleFindText() {
        this.editMenuController.handleFindText(true);
    }

    /**
     * Calls handleHighlightPrevMatch() of the editController
     */
    @FXML
    public void handleHighlightPrevMatch() {
        this.editMenuController.handleHighlightPrevMatch();
    }

    /**
     * Calls handleHighlightNextMatch() of the editController
     */
    @FXML
    public void handleHighlightNextMatch() {
        this.editMenuController.handleHighlightNextMatch();
    }

    /**
     * Calls handleReplaceText() of the editController
     */
    @FXML
    public void handleReplaceText() {
        this.editMenuController.handleReplaceText();
    }

    /**
     * Focuses on the Find Text Entry Box
     */
    @FXML
    public void handleFocusOnFindTextEntry() {
        this.findTextEntry.requestFocus();
    }

    /**
     * Focuses on the Replace Text Extry Box
     */
    @FXML
    public void handleFocusOnReplaceTextEntry() {
        this.replaceTextEntry.requestFocus();
    }

    /**
     * Handles onAction for the Light Mode menu item to switch CSS for vBox to
     * LightMode.css
     */
    @FXML
    private void handleLightModeMenuAction() {
        vBox.getStylesheets().remove(darkModeCss);
        if (!vBox.getStylesheets().contains(lightModeCss)) {
            vBox.getStylesheets().add(lightModeCss);
        }
    }

    /**
     * Handles onAction for the Dark Mode menu item to switch CSS for vBox to
     * DarkMode.css
     */
    @FXML
    private void handleDarkModeMenuAction() {
        vBox.getStylesheets().remove(lightModeCss);
        if (!vBox.getStylesheets().contains(darkModeCss)) {
            vBox.getStylesheets().add(darkModeCss);
        }
    }

    /**
     * Calls the method that handles the Keyword color menu item from the settingMenuController.
     */
    @FXML
    public void handleKeywordColorAction() {
        this.settingMenuController.handleKeywordColorAction();
    }

    /**
     * Calls the method that handles the Parentheses/Brackets color menu item from the settingMenuController.
     */
    @FXML
    public void handleParenColorAction() {
        this.settingMenuController.handleParenColorAction();
    }

    /**
     * Calls the method that handles the String color menu item from the settingMenuController.
     */
    @FXML
    public void handleStrColorAction() {
        this.settingMenuController.handleStrColorAction();
    }

    /**
     * Calls the method that handles the Int color menu item from the settingMenuController.
     */
    @FXML
    public void handleIntColorAction() {
        this.settingMenuController.handleIntColorAction();
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
     * Calls the method that handles the Check Well Formed menu item action from the
     * codeMenuController
     */
    @FXML
    public void handleCheckWellFormedAction() {
        this.codeMenuController.handleCheckWellFormed();
    }
}
