/*
* TypeCheckerVisitor.java
* Editing Authors: Tia Zhang and Danqing Zhao
* Class: CS461
* Date: February 25, 2019
*/


package proj12ZhangZhao.bantam.semant;

import com.sun.source.tree.ClassTree;
import proj12ZhangZhao.bantam.util.*;
import proj12ZhangZhao.bantam.ast.*;
import proj12ZhangZhao.bantam.util.Error;
import proj12ZhangZhao.bantam.visitor.Visitor;
import proj12ZhangZhao.proj12.SemanticAnalyzer;

import java.util.Hashtable;


public class TypeCheckerVisitor extends Visitor {
    private ClassTreeNode currentClass;
    private SymbolTable currentSymbolTable;
    private ErrorHandler errorHandler;
    private String currentMethod;


    public TypeCheckerVisitor(Hashtable<String, ClassTreeNode> map){
        //classMap = map;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return null
     *//*

    public Object visit(Field node) {
        // The fields should have already been added to the symbol table by the
        // SemanticAnalyzer so the only thing to check is the compatibility of the init
        // expr's type with the field's type.
        if (...node's type is not a defined type...) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the field "
                            + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            if(...the initExpr's type is not a subtype of the node's type...) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the initializer is " + initExpr.getExprType()
                         + " which is not compatible with the " + node.getName() +
                         " field's type " + node.getType());
            }
        }
        //Note: if there is no initExpr, then leave it to the Code Generator to
        //      initialize it to the default value since it is irrelevant to the
        //      SemanticAnalyzer.
        return null;
    }

    */
/**
 * Visit a method node
 *
 * @param node the Method node to visit
 * @return null
 *//*

    public Object visit(Method node) {
        if (...the node's return type is not a defined type and not "void"...) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The return type " + node.getReturnType() + " of the method "
                            + node.getName() + " is undefined.");
        }

        //create a new scope for the method body
        currentSymbolTable.enterScope();
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

    */
/**
 * Visit a formal parameter node
 *
 * @param node the Formal node
 * @return null
 *//*

    public Object visit(Formal node) {
        if (...the node's type is not a defined type...) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the formal" +
                            " parameter " + node.getName() + " is undefined.");
        }
        // add it to the current scope
        currentSymbolTable.add(node.getName(), node.getType());
        return null;
    }

    */
/**
 * Visit a while statement node
 *
 * @param node the while statement node
 * @return null
 *//*

    public Object visit(WhileStmt node) {
        node.getPredExpr().accept(this);
        if(...the predExpr's type is not "boolean"...) {
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

    */
/**
 * Visit a block statement node
 *
 * @param node the block statement node
 * @return null
 *//*

    public Object visit(BlockStmt node) {
        currentSymbolTable.enterScope();
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

    */
/**
 * Visit a new expression node
 *
 * @param node the new expression node
 * @return null
 *//*

    public Object visit(NewExpr node) {
        if(...the node's type is not a defined class type...) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The type " + node.getType() + " does not exist.");
            node.setExprType("Object"); // to allow analysis to continue
        }
        else {
            node.setExprType(node.getType());
        }
        return null;
    }

    */
/**
 * Visit a binary comparison equals expression node
 *
 * @param node the binary comparison equals expression node
 * @return null
 *//*

    public Object visit(BinaryCompEqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(...if neither type1 nor type2 is a subtype of the other...) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                currentClass.getASTNode().getFilename(), node.getLineNum(),
                "The two values being compared for equality are not compatible types.");
        }
        node.setExprType("boolean");
        return null;
    }

    */
/**
 * Visit a unary NOT expression node
 *
 * @param node the unary NOT expression node
 * @return null
 *//*

    public Object visit(UnaryNotExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not (!) operator applies only to boolean expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("boolean");
        return null;
    }

    */
/**
 * Visit an int constant expression node
 *
 * @param node the int constant expression node
 * @return null
 *//*

    public Object visit(ConstIntExpr node) {
        node.setExprType("int");
        return null;
    }

    */
/**
 * Visit a boolean constant expression node
 *
 * @param node the boolean constant expression node
 * @return null
 *//*

    public Object visit(ConstBooleanExpr node) {
        node.setExprType("boolean");
        return null;
    }

    */
/**
 * Visit a string constant expression node
 *
 * @param node the string constant expression node
 * @return null
 *//*

    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

}






}




*/






    /**
     * Visit a ConstIntExpr expression node
     *
     * @param node the ConstIntExpr expression node
     * @return null
     */

    public Object visit(ConstIntExpr node) {
        node.setExprType("int");
        return null;
    }


    /**
     * Visit a ConstIntExpr expression node
     *
     * @param node the ConstIntExpr expression node
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
        node.accept(this);
        String id = node.getName();
        Object existingDef = currentSymbolTable.lookup(id);
        if ((existingDef != null) && (currentSymbolTable.lookup(id, 0) == null)) {
            //If it's in the table and it's not a field
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "This variable name has already been defined in this scope");
        }
        if (SemanticAnalyzer.reservedIdentifiers.contains(id)){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                     id + " is a reserved word in Bantam Java and can't be used as an identifier");
        }

        Expr initExpr = node.getInit();
        if (initExpr == null) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable has not been initialized");
        } else {
            String varType = initExpr.getExprType();
            ClassTreeNode varClassNode = checkTypeExistence(varType, node.getLineNum());
            if(varClassNode != null){
              currentSymbolTable.add(id, varType);//TODO FIGURE OUT IF NON EXISTENT TYPE SHOULD STILL BE SET IN SYMBOL TABLE
            }
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
        node.accept(this);
        Expr objectExpr = node.getRefExpr(); //TODO CHECK IF EXPRESSION BEING NULL IS LEGAL
        if (objectExpr == null) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The object whose method you are trying to call is null");
        }
        else {
            String objectName = objectExpr.getExprType();
            ClassTreeNode objectNode = checkTypeExistence(objectName, node.getLineNum());
            if(objectNode != null) {
                String methodName = node.getMethodName();
                Method method = (Method) objectNode.getMethodSymbolTable().lookup(methodName);
                if (method == null) {
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), node.getLineNum(),
                            "The method " + methodName + " does not exist in the class");
                    //TODO figure out what to set the type of the DispatchNode to if the method doesn't exist
                }
                else {
                    node.setExprType(method.getReturnType());
                }
            }

        }

        return null;
    }





    /**
    * Checks if the given type exists (is declared in the file or is a built-in class)
    * @param objectName is a String indicating the name of the type it's checking for
    * @param lineNum is the line number containing the statement which has a type to be checked
    * @return the ClassTreeNode of the type if the class exists. Otherwise, return null
    * For arrays, since they do not have a class tree node, Object node's is returned
    */
    private ClassTreeNode checkTypeExistence(String objectName, int lineNum) {
        Hashtable<String, ClassTreeNode> classMap = currentClass.getClassMap();
        ClassTreeNode objectNode = classMap.get(objectName);
        if (objectNode == null) {
            String objectAsArray = objectName.substring(0, objectName.length() - 2);
            objectNode = classMap.get(objectAsArray);
            if (objectNode == null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), lineNum,
                        "The class " + objectName + " does not exist in this file");
            }
            else{ //If it's an array, use Object (because Dispatch needs to check Object methods for arrays)
                // TODO CHECK DISPATCH IS THE ONLY ONE THIS MATTERS FOR
                objectNode = classMap.get("Object");
            }
        }
        return objectNode;
    }




    /**
     * Visit a NewArrayExpr expression node
     *
     * @param node the NewArrayExpr expression node
     * @return null
     */

    public Object visit(NewArrayExpr node) {
        node.accept(this); //Do I only need to visit size?
        Expr size = node.getSize();
        if(size == null){ //I think this should be possible, if you put in a variable that has a value of null, for instance
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Array requires a size when initialized");
        }
        else {
            String sizeType = size.getExprType();
            if (!"int".equals(sizeType)) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The size expression for an array must be an integer," +
                                " not " + sizeType);
            }
        }
        String type = node.getType();
        type = type.substring(0, type.length()-2); //Cut off the brackets []
        Hashtable<String, ClassTreeNode> classMap = currentClass.getClassMap();
        ClassTreeNode arrayType = classMap.get(type);
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
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(type != null) {
            checkTypeExistence(type, node.getLineNum());
        }
        else{
            type = "void";
        }

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
        Expr expr = node.getExpr();
        if(expr == null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The decrement (--) operator is missing its expression.");
        }
        else {
            String type = expr.getExprType();
            if (!type.equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The decrement (--) operator applies only to integer expressions," +
                                " not " + type + " expressions.");
            }
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
        Expr expr = node.getExpr();
        if(expr == null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The increment (++) operator is missing its expression.");
        }
        else {
            String type = expr.getExprType();
            if (!type.equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The increment (++) operator applies only to integer expressions," +
                                " not " + type + " expressions.");
            }
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
        Expr expr = node.getExpr(); //In case the expression returns null
        if(expr == null){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The negative (-) operator is missing its expression.");
        }
        else {
            String type = expr.getExprType();
            if (!type.equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The negative (-) operator applies only to integer expressions," +
                                " not " + type + " expressions.");
            }
        }
        node.setExprType("int"); //Type needs to be set to int even if expression is missing
        return null;
    }



    /**
     * Visit a string constant expression node
     *
     * @param node the string constant expression node
     * @return null
     */

    public Object visit(VarExpr node) {
        node.accept(this);
        String varName = node.getName();
        //TODO double check on the usage of this and super
        String type = (String) currentSymbolTable.lookup(varName, currentSymbolTable.getCurrScopeLevel());
        if( (type == null)&& (!"super".equals(varName) && (!"this".equals(varName))) ){
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The variable " + varName + " does not exist in this scope");

        }
        else {
            node.setExprType(type); //TODO how to handle it if the variable hasn't been defined - there is no type!
            // Leave it null? Put in the string "null" or non-existent?
        }
        return null;
    }

}
