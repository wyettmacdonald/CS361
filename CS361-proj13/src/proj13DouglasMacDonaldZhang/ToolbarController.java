/*
 * File: ToolbarController.java
 * Names: Kevin Ahn, Matt Jones, Jackie Hang, Kevin Zhou
 * Class: CS 361
 * Project 4
 * Date: October 2, 2018
 * ---------------------------
 * Edited By: Zena Abulhab, Paige Hanssen, Kyle Slager, Kevin Zhou
 * Project 5
 * Date: October 12, 2018
 * ---------------------------
 * Edited By: Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * Project 6/7/9
 * Date: October 26, 2018/ November 3, 2018/ November 20, 2018
 *  ---------------------------
 * Edited By: Zeb Keith-Hardy, Danqing Zhao, Tia Zhang
 * Class: CS 461
 * Project 11
 * Date: February 13, 2019
 *  ---------------------------
 * Edited By: Tia Zhang and Danqing Zhao
 * Class: CS 461
 * Project 12
 * Date: February 25, 2019
 */

package proj13DouglasMacDonaldZhang;

import javafx.application.Platform;
import javafx.geometry.Side;
import org.fxmisc.richtext.CodeArea;
import proj13DouglasMacDonaldZhang.bantam.ast.Program;
import proj13DouglasMacDonaldZhang.bantam.parser.Parser;
import proj13DouglasMacDonaldZhang.bantam.semant.*;
import proj13DouglasMacDonaldZhang.bantam.treedrawer.Drawer;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj13DouglasMacDonaldZhang.bantam.util.CompilationException;
import proj13DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj13DouglasMacDonaldZhang.bantam.util.Error;
import proj13DouglasMacDonaldZhang.bantam.lexer.Scanner;
import proj13DouglasMacDonaldZhang.bantam.lexer.Token;
import proj13DouglasMacDonaldZhang.bantam.semant.SemanticAnalyzer;

import java.util.*;
import java.util.concurrent.*;

/**
 * This class is the controller for all of the toolbar functionality.
 * Specifically, the compile, compile and run, and stop buttons
 *
 * @author  Zeb Keith-Hardy, Michael Li, Iris Lian, Kevin Zhou
 * @author  Kevin Ahn, Jackie Hang, Matt Jones, Kevin Zhou
 * @author  Zena Abulhab, Paige Hanssen, Kyle Slager Kevin Zhou
 * @version 2.0
 * @since   10-3-2018
 *
 */
public class ToolbarController {

    private boolean scanIsDone;
    private boolean parseIsDone;
    private Console console;
    private CodeTabPane codeTabPane;
    private String tokenString;
    private Integer errorCounter = 0;
    private Program ast = null; //Stores the most recent ast generated by parsing - Tia addition
    private Hashtable<String, ClassTreeNode> classMap;


    /**
     * This is the constructor of ToolbarController.
     * @param console the console
     * @param codeTabPane the tab pane
     */
    public ToolbarController(Console console, CodeTabPane codeTabPane){
        this.console = console;
        this.codeTabPane = codeTabPane;
        this.scanIsDone = true;
        this.parseIsDone = true;
    }

    /**
     * Handles actions for scan or scanParse buttons
     * @param method type of button clicked on the toolbar
     */
    public void handleScanOrScanParse(String method){
        if(method.equals("scan")){
            this.handleScan();
        }else{
            this.handleParsing(method, true);
        }
    }

    /**
     * Handles scanning the current CodeArea, prints results to a new code Area.
     */
    public void handleScan(){
        this.scanIsDone = false;
        //declare a new thread and assign it with the work of scanning the current tab
        new Thread(()-> {
            ScanTask scanTask = new ScanTask();
            FutureTask<String> curFutureTask = new FutureTask<>(scanTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
        }).start();
    }


    //TODO update every time it's saved
    public void handleSuggestions(){

        CodeArea curCodeArea = codeTabPane.getCodeArea();
        String beginning = curCodeArea.getSelectedText();
        if(beginning.length() < 1){
            return;
        }
        handleParsing("semanticCheck", false);
        if(ast != null) {
            System.out.println("AST isn't null");
            FindIDsVisitor idVisitor = new FindIDsVisitor(classMap);
            ArrayList<IdentifierInfo> idList = idVisitor.collectIdentifiers(ast);
            System.out.println("Found " + idList.size() + " ids");
            findPossibleVars(idList, beginning, curCodeArea); //TODO - MAKE SURE THIS WORKS WHEN YOU SWITCH TABS
        }

    }


    public void findPossibleVars(ArrayList<IdentifierInfo> idList, String beginning, CodeArea curCodeArea){
        ArrayList<IdentifierInfo> possibleNames = new ArrayList<IdentifierInfo>();
        idList.forEach((idInfo) -> {
               if(idInfo.getName().startsWith(beginning)){
                    possibleNames.add(idInfo);
                    System.out.println("Suggestions " + idInfo.getName());
                }
            }
         );

        //TODO add a way to dismiss the context menu
        SuggestionsContextMenu suggestionsMenu = new SuggestionsContextMenu(possibleNames, curCodeArea);
        suggestionsMenu.show(curCodeArea, Side.RIGHT, curCodeArea.getCaretPosition(), curCodeArea.getCaretColumn());

    }

    /**
     * creates a new finder thread for parsing the AST.
     * Once AST is parsed, checks the type of finder and then passes the AST to the correct one.
     * default case draws the AST
     * @param method type of finder being executed
     */
    public void handleParsing(String method, boolean errorMode){
        this.parseIsDone = false;
        new Thread (()->{
            ParseTask parseTask = new ParseTask();
            FutureTask<Program> curFutureTask = new FutureTask<Program>(parseTask);
            ExecutorService curExecutor = Executors.newFixedThreadPool(1);
            curExecutor.execute(curFutureTask);
            try{
                Program AST = curFutureTask.get();
                if(AST != null){
                    ast = AST;
                    switch(method){

                        //scan, parse, and check button clicked
                        case "semanticCheck":
                            ErrorHandler errorHandler = new ErrorHandler();
                            SemanticAnalyzer semantAnalyzer = new SemanticAnalyzer(errorHandler);
                            semantAnalyzer.analyze(AST);
                            classMap = semantAnalyzer.getClassMap();
                            Hashtable<String, ClassTreeNode> map = semantAnalyzer.getClassMap();
                            //Useful debugging code, but it crashes if there's a cycle
                            /*map.forEach( (nodeName, node) -> {

                                Platform.runLater(()->this.console.writeToConsole(
                                        "The name is " + nodeName + "\n",
                                        "Output"));
                                if(!nodeName.equals("Object")) {
                                    Platform.runLater(() -> this.console.writeToConsole(
                                            "Parent is " + node.getParent().getName() + "\n",
                                            "Output"));
                                }
                                if(node.getNumDescendants() > 0) {
                                    Iterator<ClassTreeNode> childrenIt = node.getChildrenList();
                                    while (childrenIt.hasNext()) {
                                        ClassTreeNode child = childrenIt.next();
                                        Platform.runLater(() -> this.console.writeToConsole(
                                                "One child of " + nodeName + " is " + child.getName() + "\n",
                                                "Output"));
                                    }
                                }
                                else{
                                    Platform.runLater(() -> this.console.writeToConsole(
                                            "No children" + "\n",
                                            "Output"));
                                }

                            });*/




                            if(errorMode) {
                                Platform.runLater(() -> {
                                    ToolbarController.this.console.writeToConsole("Semantic Analysis Failed\n", "Error");
                                    ToolbarController.this.console.writeToConsole("There were: " +
                                            errorHandler.getErrorList().size() + " semantic errors in " +
                                            ToolbarController.this.codeTabPane.getFileName() + "\n", "Output");

                                    if (errorHandler.errorsFound()) {
                                        List<Error> errorList = errorHandler.getErrorList();
                                        Iterator<Error> errorIterator = errorList.iterator();
                                        ToolbarController.this.console.writeToConsole("\n", "Error");
                                        while (errorIterator.hasNext()) {
                                            ToolbarController.this.console.writeToConsole(errorIterator.next().toString() +
                                                    "\n", "Error");
                                        }
                                    }
                                });
                            }
                            break;

                        case "parseOnly":
                            System.out.println("ASt is " + ast + " " + AST);
                            break;
                        //scan and parse clicked, build AST image
                        default:
                            Drawer drawer = new Drawer();
                            drawer.draw(this.codeTabPane.getFileName(),AST);
                            break;
                    }
                }
                this.parseIsDone = true;
            }catch(InterruptedException| ExecutionException e){
                Platform.runLater(()-> this.console.writeToConsole("Parsing failed \n", "Error"));
            }
        }).start();
    }

    /**
     * Check if the scan task is still running.
     * @return true if this task is done, and false otherwise
     */
    public boolean scanIsDone(){
        return this.scanIsDone;
    }

    /**
     * Check if the parse task is still running.
     * @return true if this task is done, and false otherwise
     */
    public boolean parseIsDone(){
        return this.parseIsDone;
    }


    //TODO Have Wyett change this to call handleScanOrScanParse instead to get the save check in
    public void handleFindUsesButtonAction() { //Original params were Event event, File file
//        int userResponse = fileMenuController.checkSaveBeforeContinue();
//        // user select cancel button
//        if (userResponse == 2) {
//            return;
//        }
//        // user select to save
//        else if (userResponse == 1) {
//            fileMenuController.handleSaveAction();
//        }
        // run scan and parse in new thread

        handleParsing("semanticCheck", false);
        Thread findUsesThread = new Thread() {
            public void run() {
                Program root = ast;

                if (root != null) {
//                    drawTree(root, file);
                    String selectedText = codeTabPane.getCodeArea().getSelectedText();
                    FindDeclarationUsesVisitor findDeclarationUses = new FindDeclarationUsesVisitor();
                    findDeclarationUses.setJavaTabPane(selectedText);
                    findDeclarationUses.handleFindUses(root);
                    Platform.runLater(() -> {
                        console.appendText("Uses of " + selectedText + ": \n" + findDeclarationUses.getUses());
                        console.appendText("Finding uses completed successfully \n");
                    });
                }
            }
        };
        findUsesThread.start();
    }







    public void handleFindUnusedButtonAction() {
        handleParsing("semanticCheck", false);

        Program root = ast;

        if (root != null) {
//                    drawTree(root, file);
            FindUnusedVisitor findUnused = new FindUnusedVisitor(classMap);
            String unused = findUnused.checkForUnused(ast);
            ToolbarController.this.console.writeToConsole(unused,  "output");

        }
    }




    /**
     * An inner class used to parse a file in a separate thread.
     * Prints error info to the console
     */
    private class ParseTask implements Callable{

        /**
         * Create a Parser and use it to create an AST
         * @return AST tree created by a parser
         */
        @Override
        public Program call(){
            ErrorHandler errorHandler = new ErrorHandler();
            Parser parser = new Parser(errorHandler);
            String filename = ToolbarController.this.codeTabPane.getFileName();
            Program AST = null;
            try{
                AST = parser.parse(filename);
                Platform.runLater(()->ToolbarController.this.console.writeToConsole(
                        "Parsing Successful.\n", "Output"));
            }
            catch (CompilationException e){
                Platform.runLater(()-> {
                    ToolbarController.this.console.writeToConsole("Parsing Failed\n","Error");
                    ToolbarController.this.console.writeToConsole("There were: " +
                            errorHandler.getErrorList().size() + " errors in " +
                            ToolbarController.this.codeTabPane.getFileName() + "\n", "Output");

                    if (errorHandler.errorsFound()) {
                        List<Error> errorList = errorHandler.getErrorList();
                        Iterator<Error> errorIterator = errorList.iterator();
                        ToolbarController.this.console.writeToConsole("\n", "Error");
                        while (errorIterator.hasNext()) {
                            ToolbarController.this.console.writeToConsole(errorIterator.next().toString() +
                                    "\n", "Error");
                        }
                    }
                });
            }
            return AST;
        }
    }

    /**
     * A private inner class used to scan a file in a separate thread
     * Print error messages to the console and write tokens in a new tab
     */
    private class ScanTask implements Callable {
        /**
         * Start the process by creating a scanner and use it to scan the file
         * @return a result string containing information about all the tokens
         */
        @Override
        public String call(){
            ErrorHandler errorHandler = new ErrorHandler();
            Scanner scanner = new Scanner(ToolbarController.this.codeTabPane.getFileName(), errorHandler);
            Token token = scanner.scan();
            StringBuilder tokenString = new StringBuilder();

            while(token.kind != Token.Kind.EOF){
                tokenString.append(token.toString() + "\n");
                token = scanner.scan();
            }
            String resultString = tokenString.toString();
            Platform.runLater(()-> {
                ToolbarController.this.console.writeToConsole("There were: " +
                        errorHandler.getErrorList().size() + " errors in " +
                        ToolbarController.this.codeTabPane.getFileName() + "\n","Output");
                if(errorHandler.errorsFound()){
                    List<Error> errorList= errorHandler.getErrorList();
                    Iterator<Error> errorIterator = errorList.iterator();
                    ToolbarController.this.console.writeToConsole("\n","Error");

                    while(errorIterator.hasNext()){
                        ToolbarController.this.console.writeToConsole(
                                errorIterator.next().toString() + "\n","Error");
                    }
                }
                ToolbarController.this.codeTabPane.createTabWithContent(resultString);
                ToolbarController.this.scanIsDone = true;
            });
            return tokenString.toString();
        }
    }
}