/*
* TypeCheckerVisitor.java
* Editing Authors: Tia Zhang and Danqing Zhao
* Class: CS461
* Date: February 25, 2019
*/


package proj12ZhangZhao.bantam.semant;

import proj12ZhangZhao.bantam.util.*;
import proj12ZhangZhao.bantam.ast.*;
import proj12ZhangZhao.bantam.util.Error;
import proj12ZhangZhao.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;


import com.sun.source.tree.ClassTree;
import proj12ZhangZhao.proj12.SemanticAnalyzer;

import java.util.Hashtable;



/*
Visitor that looks sets the types of expressions and checks to make sure all the types in the program
are valid
*/

public class TypeCheckerVisitor extends Visitor {
    private ClassTreeNode currentClass;
    private SymbolTable currentSymbolTable;
    private ErrorHandler errorHandler;
    private String currentMethod;


    /*
    * Constructor for the Type Checker Visitor.
    * @param eh is the ErrorHandler that'll log any errors encountered in the course of type checking
    */
    public TypeCheckerVisitor(ErrorHandler eh){
        errorHandler = eh;
    }




    /*
    * Checks the types of variables, params expressions, etc in a given class
    * @param classNode is the ClassTreeNode of the class to be checked
    * If it's a built in type, does nothing
    */
    public void checkTypes (ClassTreeNode classNode){
        if(classNode.isBuiltIn()){
            return;
        }
        currentClass = classNode;
        currentSymbolTable = classNode.getVarSymbolTable();
        classNode.getASTNode().accept(this);
    }


    /**
     * @param type1 a string
     * @param type2 a string
     * @return boolean (if type2 is a subclass of type1)
     */
    private boolean isSubClass(String type1, String type2) {
        if (type1.equals(type2)) {
            return true;
        }
        Hashtable<String, ClassTreeNode> classMap = this.currentClass.getClassMap();
        ClassTreeNode classTree = classMap.get(type2);

        while (classTree.getParent() != null) {
            if (classTree.getParent().getName().equals(type1)) {
                return true;
            }
            classTree = classTree.getParent();
        }

        return false;
    }

    /**
     * Checks if the given type exists (is declared in the file or is a built-in class)
     *
     * @param objectName is a String indicating the name of the type it's checking for
     * @param lineNum    is the line number containing the statement which has a type to be checked
     * @return the ClassTreeNode of the type if the class exists. Otherwise, return null
     * For arrays, since they do not have a class tree node, Object node's is returned
     * For primitive types, a temporary class tree node that does not get connected to the tree is returned
     */
    private ClassTreeNode checkTypeExistence(String objectName, int lineNum) {
        if( ("int".equals(objectName)) || ("int[]".equals(objectName)) ||
                ("boolean".equals(objectName)) || "boolean[]".equals(objectName) ) {
            return new ClassTreeNode(null, true, false, null);
            //Returning a node unattached to the tree just to confirm the type exists
        }
        Hashtable<String, ClassTreeNode> classMap = currentClass.getClassMap();
        System.out.println("Object name " + objectName);
        ClassTreeNode objectNode = classMap.get(objectName);
        if (objectNode == null) {
            //Check to see if it's an array
            if(objectName.length() > 3) {
                String objectAsArray = objectName.substring(0, objectName.length() - 3);
                objectNode = classMap.get(objectAsArray);

                if (objectNode == null) {
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), lineNum,
                            "The class " + objectName + " does not exist in this file");
                } else { //If it's an array, use Object (because Dispatch needs to check Object methods for arrays)
                    // TODO CHECK DISPATCH IS THE ONLY ONE THIS MATTERS FOR
                    objectNode = classMap.get("Object");
                }
            }
            else{ //If it's shorter than size 3, it can't be an array anyways, it already failed
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), lineNum,
                        "The class " + objectName + " does not exist in this file");
            }

        }
        return objectNode;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return null
     */

    public Object visit(Field node) {
        // The fields should have already been added to the symbol table by the
        // SemanticAnalyzer so the only thing to check is the compatibility of the init
        // expr's type with the field's type.
        String type = node.getType();
        checkTypeExistence(type, node.getLineNum());

        Expr initExpr = node.getInit();

        if (initExpr != null) {
            initExpr.accept(this);
            String exprType = initExpr.getExprType();
            ClassTreeNode exprTypeNode = checkTypeExistence(exprType, node.getLineNum());
            ClassTreeNode varTypeNode = checkTypeExistence(type, node.getLineNum());
            if(exprTypeNode != null && varTypeNode != null){
                if (!isSubClass(initExpr.getExprType(), type)) {
                    //...the initExpr's type is not a subtype of the node's type...
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), node.getLineNum(),
                            "The type of the initializer is " + initExpr.getExprType()
                                    + " which is not compatible with the " + node.getName() +
                                    " field's type " + node.getType());
                }
            }
        }
        //Note: if there is no initExpr, then leave it to the Code Generator to
        //      initialize it to the default value since it is irrelevant to the
        //      SemanticAnalyzer.
        return null;
    }


    /**
     * Visit a method node
     *
     * @param node the Method node to visit
     * @return null
     */

    public Object visit(Method node) {
        String type = node.getReturnType();
        if(!type.equals("void")) {
            checkTypeExistence(type, node.getLineNum());
        }

        currentMethod = node.getName();
        //create a new scope for the method body
        currentSymbolTable.enterScope();
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }


    /**
     * Visit a formal parameter node
     *
     * @param node the Formal node
     * @return null
     */

    public Object visit(Formal node) {
        String type = node.getType();
        checkTypeExistence(type, node.getLineNum());
        // add it to the current scope
        // I already added it in the symbol table builder visitor TODO VERIFY THIS WORKS
        //currentSymbolTable.add(node.getName(), node.getType());
        return null;
    }

    /*
    *
    */
    private String checkIDExistence(String id, String ref, int lineNum){
        String idType = null;
        if(ref != null) {
            if(ref.equals("this")){
                if ( (idType = (String) currentSymbolTable.lookup(id, 0)) == null) {
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), lineNum,
                            "The field " + id + " does not exist");
                }
            }
            else if (ref.equals("super")){
                if ( (idType = (String) currentClass.getParent().getVarSymbolTable().lookup(id, 0)) == null){ //It has to be a field
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), lineNum,
                            "The parent class does not have the field " + id);

                }
            }
        }
        else{ //Check the local scope and the fields
            idType = (String) currentSymbolTable.lookup(id);
            if ( idType == null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), lineNum,
                        "The variable " + id + " does not exist");
            }
        }

        return idType;
    }


    /**
     * Visits the ArrayAssignExpr node
     * @param node the ArrayAssignExpr node
     * @return null
     */
    public Object visit(ArrayAssignExpr node){
        //TODO did we already check to make sure the ref expression is super or this only?

        Expr index = node.getIndex();
        index.accept(this);
        node.getExpr().accept(this);
        if(!index.getExprType().equals("int")){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The size expression in the array is not valid");
        }

        String id = node.getName();
        String ref = node.getRefName();

        String idType = checkIDExistence(id, ref, node.getLineNum());
        //The type should've already been validated by the new array expr

        if(idType != null){
            node.setExprType(idType);
        }
        else{
            node.setExprType("void"); //Dummy type to avoid throwing errors if not defined
        }

        return null;
    }


    /**
     * Visits the ArrayExpr node
     * @param node the ArrayExpr node
     * @return null
     */
    public Object visit(ArrayExpr node){
        if(node.getRef() != null){
            node.getRef().accept(this);
        }
        Expr index = node.getIndex();
        index.accept(this);
        if(!index.getExprType().equals("int")){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Size expression in array expression is not valid");
        }
        String id = node.getName();
        //If the name is this or super, since they're reserved, it'll be null
        if(checkIDExistence(null, id, node.getLineNum()) != null) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The array " + id + " does not exist");
        }

        //It's not actually null, but I have no way of seeing whether it's "super" or "this" right now
        // TODO FIND SOME WAY TO TELL
        String idType = checkIDExistence(id, null, node.getLineNum());
        //The type should've already been validated by the new array expr

        if(idType != null){
            node.setExprType(idType);
        }
        else{
            node.setExprType("void"); //Dummy type to avoid throwing errors if not defined
        }
        return null;
    }
    /**
     *
     * @param node the cast expression node
     * @return
     */
    public Object visit(CastExpr node){
        String target = node.getType();
        String exprType = node.getExpr().getExprType();
        checkTypeExistence(target, node.getLineNum());
        checkTypeExistence(exprType, node.getLineNum());
        if(!isSubClass(target, exprType) && !isSubClass(exprType, target)){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Cast from " + exprType + " to " + target +" is not allowed");
        }
        return null;
    }

    /**
     * Visits the assignment expression node
     * @param node the assignment expression node
     * @return null
     */
    public Object visit(AssignExpr node) {
        node.getExpr().accept(this);

        String type1 = checkIDExistence(node.getRefName(), node.getName(), node.getLineNum());
        String type2 = node.getExpr().getExprType();
        //System.out.println("Type 2 " + type2 + " type one " + type1);
        if(type1 != null) {
            ClassTreeNode type1Class = checkTypeExistence(type1, node.getLineNum());
            ClassTreeNode type2Class = checkTypeExistence(type2, node.getLineNum());
            if ((type1Class != null) && (type2Class != null)) {
                if (!isSubClass(type1, type2)) {
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), node.getLineNum(),
                            "cannot assign type " + type2 + " to " + type1);
                }
            }
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return null
     */

    public Object visit(WhileStmt node) {
        node.getPredExpr().accept(this);
        if (!node.getPredExpr().getExprType().equals("boolean")) {
            //...the predExpr's type is not "boolean"...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type of the predicate is " + node.getPredExpr().getExprType()
                            + " which is not boolean.");
        }
        currentSymbolTable.enterScope();
        node.getBodyStmt().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }


    /**
     * Visit a block statement node
     *
     * @param node the block statement node
     * @return null
     */

    public Object visit(BlockStmt node) {
        currentSymbolTable.enterScope();
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }


    /**
     * Visit a new expression node
     *
     * @param node the new expression node
     * @return null
     */

    public Object visit(NewExpr node) {
        String type = node.getType();
        checkTypeExistence(type, node.getLineNum());
        node.setExprType(node.getType());
        return null;
    }

    /**
     * visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return
     */

    public Object visit(BinaryArithDivideExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "can only divide between integers");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return
     */
    public Object visit(BinaryArithMinusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "can only minus between integers");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * visit a binary arithmetic modulus expression node
     *
     * @param node the binary arithmetic modulus expression node
     * @return
     */
    public Object visit(BinaryArithModulusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "can only use modulus between integers");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * visit a binary arithmetic plus expression node
     *
     * @param node the binary arithmetic plus expression node
     * @return
     */
    public Object visit(BinaryArithPlusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "can only add between integers");
        }
        node.setExprType("int");
        return null;
    }


    /**
     * visit a binary arithmetic times expression node
     *
     * @param node the binary arithmetic times expression node
     * @return
     */
    public Object visit(BinaryArithTimesExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        //System.out.println("Type 1 "  + type1 + " type2 " + type2);

        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Can only multiply between integers");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return null
     */

    public Object visit(BinaryCompGeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared are not integers.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison greater than expression node
     *
     * @param node the binary comparison greater than expression node
     * @return null
     */
    public Object visit(BinaryCompGtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        ClassTreeNode type1Class = checkTypeExistence(type1, node.getLineNum());
        ClassTreeNode type2Class = checkTypeExistence(type2, node.getLineNum());
        if( (type1Class != null) && (type2Class != null)) {
            if (!(isSubClass(type1, type2) || isSubClass(type2, type1))) {
                //...if neither type1 nor type2 is a subtype of the other...
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The two values being compared are not integers.");
            }
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison less than expression node
     *
     * @param node the binary comparison less than expression node
     * @return null
     */
    public Object visit(BinaryCompLtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        if (!type1.equals("int") || !type2.equals("int")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared are not integers");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison equal to expression node
     *
     * @param node the binary comparison equal to expression node
     * @return null
     */
    public Object visit(BinaryCompEqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        ClassTreeNode type1Class = checkTypeExistence(type1, node.getLineNum());
        ClassTreeNode type2Class = checkTypeExistence(type2, node.getLineNum());
        if( (type1Class != null) && (type2Class != null)) {
            if (!(isSubClass(type1, type2) || isSubClass(type2, type1))) {
                //...if neither type1 nor type2 is a subtype of the other...
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The two values being compared for equality are not compatible types.");
            }
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison not equal expression node
     *
     * @param node the binary comparison not equal expression node
     * @return null
     */
    public Object visit(BinaryCompNeExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        ClassTreeNode type1Class = checkTypeExistence(type1, node.getLineNum());
        ClassTreeNode type2Class = checkTypeExistence(type2, node.getLineNum());
        if( (type1Class != null) && (type2Class != null)) {
            if (!(isSubClass(type1, type2) || isSubClass(type2, type1))) {
                //...if neither type1 nor type2 is a subtype of the other...
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The two values being compared for equality are not compatible types.");
            }
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * visit a logical AND expression
     *
     * @param node the binary logical AND expression node
     * @return
     */
    public Object visit(BinaryLogicAndExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        if (!type1.equals("boolean") || !type2.equals("boolean")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The logic and expression needs boolean type on both sides");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * visit a logical OR expression node
     *
     * @param node the binary logical OR expression node
     * @return
     */
    public Object visit(BinaryLogicOrExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();

        if (!type1.equals("boolean") || !type2.equals("boolean")) {
            //...if neither type1 nor type2 is a subtype of the other...
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The logic or expression needs boolean type on both sides");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return null
     */

    public Object visit(UnaryNotExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not (!) operator applies only to boolean expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return null
     */

    public Object visit(ConstIntExpr node) {
        node.setExprType("int");
        return null;
    }


    /**
     * Visit a boolean constant expression node
     *
     * @param node the boolean constant expression node
     * @return null
     */

    public Object visit(ConstBooleanExpr node) {
        node.setExprType("boolean");
        return null;
    }


    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */

    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }








    /**
     * Visit a DeclStmt expression node
     *
     * @param node the DeclStmt expression node
     * @return null
     */

    public Object visit(DeclStmt node) {
        node.getInit().accept(this);
        String id = node.getName();
        Object existingDef = currentSymbolTable.lookup(id);
        if ((existingDef != null) && (currentSymbolTable.lookup(id, 0) == null)) {
            //If it's in the table and it's not a field
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable name " + id + " has already been defined in this scope");
        }
        if (SemanticAnalyzer.reservedIdentifiers.contains(id)){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    id + " is a reserved word in Bantam Java and can't be used as an identifier");
        }


       //I don't think I need to check for = null yet, null isn't an expression and banned as an identifier
        //It would throw an error as undefined identifier if used elsewhere
        String varType = node.getInit().getExprType();
        ClassTreeNode varClassNode = checkTypeExistence(varType, node.getLineNum());
        if(varClassNode != null){
            currentSymbolTable.add(id, varType);//TODO FIGURE OUT IF NON EXISTENT TYPE SHOULD STILL BE SET IN SYMBOL TABLE
        }
        return null;

    }






    /**
     * Visit a DispatchExpr expression node
     *
     * @param node the DispatchExpr expression node
     * @return null
     */

    public Object visit(DispatchExpr node) {
        System.out.println(node.getRefExpr() + " " + node.getMethodName());
        //I think the reference expression could be null if you're running a method from your own class
        Expr ref = node.getRefExpr();
        String objectName;
        if(ref!= null) {
            ref.accept(this);
            //System.out.println("Got it from ref! " + ref.getExprType() + " " + ref);

            objectName = ref.getExprType();
            System.out.println(node.getMethodName());
            if(ref instanceof DispatchExpr){
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Method chaining is not legal");
                return null;
            }
            //Temporary patch for a bug I can't figure out
            // where things without references are somehow getting references -Tia
            if(objectName == null) {
                objectName = currentClass.getName();
            }
        }
        else{
            objectName = currentClass.getName();
            //System.out.println("Got it from current class!" + objectName + " " + currentClass + " " + currentClass.getName());
        }
        node.getActualList().accept(this);


        ClassTreeNode objectNode = checkTypeExistence(objectName, node.getLineNum());
        if(objectNode != null) {
            String methodName = node.getMethodName();
            objectNode.getMethodSymbolTable().enterScope();
            Method method = (Method) objectNode.getMethodSymbolTable().lookup(methodName);
            if (method == null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The method " + methodName + " does not exist in the class");
                //TODO figure out what to set the type of the DispatchNode to if the method doesn't exist
            }
            else {
                String type = method.getReturnType();
                if(type == null){ //Void return
                    type = "void";
                }
                node.setExprType(type);
            }
        }

        return null;
    }


    /**
     * Visit an IfStmt expression node
     *
     * @param node the IfStmt expression node
     * @return null
     */

    public Object visit(ForStmt node) {

        //Init expr is allowed to be null, I think because you could init the variable outside the for loop
        Expr initExpr = node.getInitExpr();
        if(initExpr != null){
            node.getInitExpr().accept(this);
            String type = initExpr.getExprType();
            if(!"int".equals(type)){
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Error: for loop's initialization needs to be of type int");
            }

        }

        node.getPredExpr().accept(this);
        Expr midExpr = node.getPredExpr();
        if(midExpr.getExprType() != "boolean"){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Error: for loop's predicate must be a boolean");
        }



        node.getUpdateExpr().accept(this);
        String type = node.getUpdateExpr().getExprType();
        if(!"int".equals(type)){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Error: for loop's updating expression needs to be of type int");




        }

        //Set up new scope
        currentSymbolTable.enterScope();
        node.getBodyStmt().accept(this);
        currentSymbolTable.exitScope();

        return null;
    }



    /**
     * Visit an IfStmt expression node
     *
     * @param node the IfStmt expression node
     * @return null
     */

    public Object visit(IfStmt node) {
        Expr condition = node.getPredExpr();

        condition.accept(this);
        if (condition.getExprType() != "boolean") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Error: if statement's condition must be a boolean");
        }
        currentSymbolTable.enterScope();
        node.getThenStmt().accept(this);
        node.getElseStmt().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }


    /**
     * Visit an InstanceOfExpr expression node
     *
     * @param node the InstanceOfExpr expression node
     * @return null
     */

    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        String type = node.getType();
        checkTypeExistence(type, node.getLineNum());
        node.setExprType("boolean");
        return null;
    }



    /**
     * Visit a NewArrayExpr expression node
     *
     * @param node the NewArrayExpr expression node
     * @return null
     */

    public Object visit(NewArrayExpr node) {
        node.getSize().accept(this);
        String sizeType = node.getSize().getExprType();
        if (!"int".equals(sizeType)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The size expression for an array must be an integer," +
                            " not " + sizeType);
        }

        String type = node.getType();
        System.out.println("type " + type);
        type = type.substring(0, type.length()-3); //Cut off the brackets []
        System.out.println("type " + type);
        ClassTreeNode arrayType = checkTypeExistence(type, node.getLineNum());
        if(arrayType == null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type of the array cannot be found.");
        }

        node.setExprType(type); //Even if the type doesn't exist, let's pretend so I can get on with analysis

        return null;
    }






    /**
     * Visit a ReturnStmt expression node
     *
     * @param node the ReturnStmt expression node
     * @return null
     */

    public Object visit(ReturnStmt node) {
        Expr returnExpr = node.getExpr();
        String type = "void";
        if(returnExpr != null){
            returnExpr.accept(this);
            type = node.getExpr().getExprType();
        }

        //The type shouldn't need to be validated cause it's determined automatically.
        // Only declared return needs to be checked

        Method method = (Method) currentClass.getMethodSymbolTable().lookup(currentMethod);
        String returnType = method.getReturnType();
        if(returnType == null){
            type = "void";
        }
        if(!returnType.equals(type)){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The returned type of the method " + currentMethod + " does not equal the declared return type");
        }

        return null;
    }








    /**
     * Visit a unary DECR expression node
     *
     * @param node the unary DECR expression node
     * @return null
     */

    public Object visit(UnaryDecrExpr node) {

        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if (!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The decrement (--) operator applies only to integer expressions," +
                            " not " + type + " expressions.");
        }

        node.setExprType("int");
        return null;
    }


    /**
     * Visit a unary INCR expression node
     *
     * @param node the unary INCR expression node
     * @return null
     */

    public Object visit(UnaryIncrExpr node) {

        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
            if (!type.equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The increment (++) operator applies only to integer expressions," +
                                " not " + type + " expressions.");
            }

        node.setExprType("int");
        return null;
    }


    /**
     * Visit a unary NEG expression node
     *
     * @param node the unary NEG expression node
     * @return null
     */

    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
            if (!type.equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The negative (-) operator applies only to integer expressions," +
                                " not " + type + " expressions.");
            }

        node.setExprType("int"); //Type needs to be set to int even if expression is missing
        return null;
    }



    /**
     * Visit a VarExpr expression node
     *
     * @param node VarExpr expression node
     * @return null
     */

    public Object visit(VarExpr node) {
        Expr ref = node.getRef();
        if (ref != null){
            ref.accept(this);
        }
        String varName = node.getName();
        //TODO if the expression = this or super, how can you tell so you can make sure to only check those symbol tables?
        String type = (String) currentSymbolTable.lookup(varName);

        if( (type == null)&& (!"super".equals(varName) && (!"this".equals(varName))) ){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable " + varName + " does not exist in this scope");
            node.setExprType("void"); //Setting a dummy type to avoid getting errors
        }
        else {
            node.setExprType(type);

        }
        return null;
    }

}
