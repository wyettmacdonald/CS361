/* Bantam Java Compiler and Language Toolset.

   Copyright (C) 2009 by Marc Corliss (corliss@hws.edu) and 
                         David Furcy (furcyd@uwosh.edu) and
                         E Christopher Lewis (lewis@vmware.com).
   ALL RIGHTS RESERVED.

   The Bantam Java toolset is distributed under the following 
   conditions:

     You may make copies of the toolset for your own use and 
     modify those copies.

     All copies of the toolset must retain the author names and 
     copyright notice.

     You may not sell the toolset or distribute it in 
     conjunction with a commerical product or service without 
     the expressed written consent of the authors.

   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS 
   OR IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE 
   IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A 
   PARTICULAR PURPOSE.

   This file was modified by Dale Skrien, February, 2019.
*/

package proj12MacDonaldDouglas.bantam.semant;

import proj12MacDonaldDouglas.bantam.ast.*;
import proj12MacDonaldDouglas.bantam.lexer.Token;
import proj12MacDonaldDouglas.bantam.util.*;
import proj12MacDonaldDouglas.bantam.util.Error;
import proj12MacDonaldDouglas.bantam.visitor.Visitor;

import java.util.*;

/**
 * The <tt>SemanticAnalyzer</tt> class performs semantic analysis.
 * In particular this class is able to perform (via the <tt>analyze()</tt>
 * method) the following tests and analyses: (1) legal inheritence
 * hierarchy (all classes have existing parent, no cycles), (2)
 * legal class member declaration, (3) there is a correct bantam.Main class
 * and main() method, and (4) each class member is correctly typed.
 * <p>
 * This class is incomplete and will need to be implemented by the student.
 */
public class SemanticAnalyzer
{
    /**
     * reserved words that are tokens of type ID, but cannot be declared as the
     * names of (a) classes, (b) methods, (c) fields, (d) variables.
     * These words are:  null, this, super, void, int, boolean.
     * However, class names can be used as variable names.
     */
    public static final Set<String> reservedIdentifiers = new HashSet<>(Arrays.asList(
            "null", "this", "super", "void", "int", "boolean"));

    /**
     * Root of the AST
     */
    private Program program;

    /**
     * Root of the class hierarchy tree
     */
    private ClassTreeNode root;

    /**
     * Maps class names to ClassTreeNode objects representing the class
     */
    private Hashtable<String, ClassTreeNode> classMap = new Hashtable<String,
            ClassTreeNode>();

    /**
     * error handling
     */
    private ErrorHandler errorHandler;

    /**
     * type checking
     */
    private TypeCheckerVisitor typeCheckerVisitor;

    /**
     * Maximum number of inherited and non-inherited fields that can be defined for any
     * one class
     */
    private final int MAX_NUM_FIELDS = 1500;

    /**
     * SemanticAnalyzer constructor
     *
     * @param errorHandler the ErrorHandler to use for reporting errors
     */
    public SemanticAnalyzer(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Analyze the AST checking for semantic errors and annotating the tree
     * Also builds an auxiliary class hierarchy tree
     *
     * @param program root of the AST to be checked
     * @return root of the class hierarchy tree (needed for code generation)
     * <p>
     * Must add code to do the following:
     * 1 - add built-in classes in classMap (already done)
     * 2 - add user-defined classes and build the inheritance tree of ClassTreeNodes
     * 3 - build the environment for each class (add class members only) and check
     *     that members are declared properly
     * 4 - check that the Main class and main method are declared properly
     * 5 - type check everything
     * See the lab manual for more details on each of these steps.
     */
    public ClassTreeNode analyze(Program program) {
        this.program = program;
        this.classMap.clear();

        // step 1:  add built-in classes to classMap
        addBuiltins();

        // remove the following statement
//        throw new RuntimeException("Semantic analyzer unimplemented");

        // add user defined classes
        addUserDefined();

        // implement step 3 - build the environment
        buildClassEnvironment();

        // check for Main class and main method
        MainMainVisitor mainVisitor = new MainMainVisitor();
        boolean mainResult = mainVisitor.hasMain(program);
        if (!mainResult) {
            errorHandler.register(Error.Kind.SEMANT_ERROR, "No Main.main");
        }

        for (Map.Entry<String, ClassTreeNode> entry : classMap.entrySet()) {
            return null;
        }

        // uncomment the following statement
        return root;
    }

    /**
     * @return the ErrorHandler for this Parser
     */
    public ErrorHandler getErrorHandler() { return errorHandler; }

    /**
     * Add built-in classes to the classMap.
     * These are the classes Object, String, Sys, and TextIO
     */
    private void addBuiltins() {
        // create AST node for object
        Class_ astNode = new Class_(-1, "<built-in class>", "Object", null,
                (MemberList) (new MemberList(-1)).addElement(new Method(-1, "Object",
                        "clone", new FormalList(-1),
                        (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1,
                                new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "boolean", "equals", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "Object", "o")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstBooleanExpr(-1, "false"))))).addElement(new Method(-1, "String", "toString", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))));
        // create a class tree node for object, save in variable root
        root = new ClassTreeNode(astNode, /*built-in?*/true, /*extendable?*/true,
                classMap);
        // add object class tree node to the mapping
        classMap.put("Object", root);

        // note: String, TextIO, and Sys all have fields that are not shown below.
        // Because these classes cannot be extended and their fields are protected,
        // they cannot be
        // accessed by other classes, so they do not have to be included in the AST.

        // create AST node for String
        astNode = new Class_(-1, "<built-in class>", "String", "Object",
                (MemberList) (new MemberList(-1)).addElement(new Field(-1, "int",
                        "length", /*0 by default*/null))
                /* note: str is the character sequence -- no applicable type for a
               character sequence so it is just made an int.  it's OK to
               do this since this field is only accessed (directly) within
               the runtime system */.addElement(new Method(-1, "int", "length",
                                new FormalList(-1),
                                (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))).addElement(new Method(-1, "boolean", "equals", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "Object", "str")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstBooleanExpr(-1, "false"))))).addElement(new Method(-1, "String", "toString", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "String", "substring", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "int", "beginIndex")).addElement(new Formal(-1, "int", "endIndex")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "String", "concat", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "str")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))));
        // create class tree node for String, add it to the mapping
        classMap.put("String", new ClassTreeNode(astNode, /*built-in?*/true,
                /*extendable?*/false, classMap));

        // create AST node for TextIO
        astNode = new Class_(-1, "<built-in class>", "TextIO", "Object",
                (MemberList) (new MemberList(-1)).addElement(new Field(-1, "int",
                        "readFD", /*0 by default*/null)).addElement(new Field(-1, "int"
                        , "writeFD", new ConstIntExpr(-1, "1"))).addElement(new Method(-1, "void", "readStdin", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "readFile", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "readFile")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "writeStdout", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "writeStderr", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "void", "writeFile", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "writeFile")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, null)))).addElement(new Method(-1, "String", "getString", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "int", "getInt", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))).addElement(new Method(-1, "TextIO", "putString", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "String", "str")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))).addElement(new Method(-1, "TextIO", "putInt", (FormalList) (new FormalList(-1)).addElement(new Formal(-1, "int", "n")), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new VarExpr(-1, null, "null"))))));
        // create class tree node for TextIO, add it to the mapping
        classMap.put("TextIO", new ClassTreeNode(astNode, /*built-in?*/true,
                /*extendable?*/false, classMap));

        // create AST node for Sys
        astNode = new Class_(-1, "<built-in class>", "Sys", "Object",
                (MemberList) (new MemberList(-1)).addElement(new Method(-1, "void",
                        "exit",
                        (FormalList) (new FormalList(-1)).addElement(new Formal(-1,
                                "int", "status")),
                        (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1,
                                null))))
                /* MC: time() and random() requires modifying SPIM to add a time system
                 call
               (note: random() does not need its own system call although it uses the time
               system call).  We have a version of SPIM with this system call available,
               otherwise, just comment out. (For x86 and jvm there are no issues.)
               */.addElement(new Method(-1, "int", "time", new FormalList(-1),
                                (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))).addElement(new Method(-1, "int", "random", new FormalList(-1), (StmtList) (new StmtList(-1)).addElement(new ReturnStmt(-1, new ConstIntExpr(-1, "0"))))));
        // create class tree node for Sys, add it to the mapping
        classMap.put("Sys", new ClassTreeNode(astNode, /*built-in?*/true, /*extendable
        ?*/false, classMap));
    }

    /**
     * Add user-defined classes to the classMap.
     * Not fully implemented - need to figure out extendable
     */
    private void addUserDefined() {

        ClassesVisitorBuilder classesVisitor = new ClassesVisitorBuilder();
        classesVisitor.start();
    }

    /**
     * ClassesVisitorBuilder searches the tree for all classes
     *
     * @author Wyett MacDonald
     * @author Kyle Douglas
     */
    private class ClassesVisitorBuilder extends Visitor {

//        private Hashtable<String, ClassTreeNode> classMap;
//        private String currentClass;

        public void start() {
            program.accept(this);
        }

        /**
         * Visit a class node
         *
         * @param node the class node
         * @return result of the visit
         */
        public Object visit(Class_ node) {

            ClassTreeNode classTreeNode = new ClassTreeNode(node, false, true, classMap);

            // num of descendants
            int numOfDescendants = classMap.get("Object").getNumDescendants();

            // parent
            System.out.println(node.getParent());
            if(node.getParent() == null) {
                classTreeNode.setParent(classMap.get("Object"));
            }
            else {
                classTreeNode.setParent(classMap.get(node.getParent()));
            }

            // inheritance cycle
            if (numOfDescendants == classMap.get("Object").getNumDescendants()) {
                classTreeNode.getParent().setParent(classMap.get("Object"));
                classTreeNode.setParent(classMap.get("Object"));
                //error handler for cycle
                // TODO: Handle cycle for Class Hierarchy

            }

            classMap.put(node.getName(), classTreeNode);
            return null;
        }
    }

    private void buildClassEnvironment() {
        BuildEnvironment buildEnvironment = new BuildEnvironment();
        buildEnvironment.start();
    }

    private class BuildEnvironment extends Visitor {

        private ClassTreeNode currentClass;

        public void start() {
            currentClass = null;
            program.accept(this);
        }

        @Override
        public Object visit(Class_ node) {

            currentClass = classMap.get(node.getName());
            currentClass.getMethodSymbolTable().enterScope();
            currentClass.getVarSymbolTable().enterScope();
            // enter scope of both

            node.getMemberList().accept(this);

            //exit scope
            currentClass.getMethodSymbolTable().exitScope();
            currentClass.getVarSymbolTable().exitScope();

            return null;
        }

        @Override
        public Object visit(Field node) {

            if(reservedIdentifiers.contains(node.getName())) {
                // register error
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Field has a Reserved Identifer name, " + node.getName());
            }

            if(currentClass.getVarSymbolTable().peek(node.getName()) != null) {
                // return error that field has already been declared
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Field " + node.getName() + " has already been declared.");
            }
            else {
                currentClass.getVarSymbolTable().add(node.getName(), node.getType());
                System.out.println(currentClass.getName() + ": Var Symbol");
                currentClass.getVarSymbolTable().dump();
            }
            return null;
        }

        @Override
        public Object visit(Method node) {
            // if reserved identifiers
            if(reservedIdentifiers.contains(node.getName())) {
                // register error
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Method has a Reserved Identifier name, " + node.getName());
            }

            if(currentClass.getVarSymbolTable().peek(node.getName()) != null) {
                // return error that method has already been declared
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Method " + node.getName() + " has already been declared.");
            }

            else {
                currentClass.getMethodSymbolTable().add(node.getName(), node);
                System.out.println(currentClass.getName() + ": Method Table");
                currentClass.getMethodSymbolTable().dump();
                currentClass.getVarSymbolTable().enterScope();
                super.visit(node);
                currentClass.getVarSymbolTable().exitScope();
            }
            return null;
        }

        @Override
        public Object visit(Formal node) {
            if(reservedIdentifiers.contains(node.getName())) {
                // register error
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Formal has a Reserved Identifer name, " + node.getName());
            }

            if(currentClass.getVarSymbolTable().peek(node.getName()) != null) {
                // return error that field has already been declared
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Formal " + node.getName() + " has already been declared.");
            }
            else {
                currentClass.getVarSymbolTable().add(node.getName(), node.getType());
                currentClass.getVarSymbolTable().dump();
            }
            return null;
        }

        @Override
        public Object visit(DeclStmt node) {
            if(reservedIdentifiers.contains(node.getName())) {
                // register error
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Formal has a Reserved Identifier name, " + node.getName());
            }

            if(currentClass.getVarSymbolTable().peek(node.getName()) != null) {
                // return error that field has already been declared
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Formal " + node.getName() + " has already been declared.");
            }
            else {
                // TODO: type is null for some reason
                // type here is null for some reason
                currentClass.getVarSymbolTable().add(node.getName(), node.getType());
                super.visit(node);
            }
            return null;
        }

        @Override
        public Object visit(ForStmt node) {
            currentClass.getVarSymbolTable().enterScope();
            super.visit(node);
            currentClass.getVarSymbolTable().exitScope();
            return null;
        }

        @Override
        public Object visit(WhileStmt node) {
            currentClass.getVarSymbolTable().enterScope();
            super.visit(node);
            currentClass.getVarSymbolTable().exitScope();
            return null;
        }
    }
}
