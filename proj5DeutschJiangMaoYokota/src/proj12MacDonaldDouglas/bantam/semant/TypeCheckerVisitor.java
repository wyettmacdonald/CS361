package proj12MacDonaldDouglas.bantam.semant;

import proj12MacDonaldDouglas.bantam.ast.Expr;
import proj12MacDonaldDouglas.bantam.ast.Field;
import proj12MacDonaldDouglas.bantam.ast.Method;
import proj12MacDonaldDouglas.bantam.ast.*;
import proj12MacDonaldDouglas.bantam.util.ClassTreeNode;
import proj12MacDonaldDouglas.bantam.util.Error;
import proj12MacDonaldDouglas.bantam.util.ErrorHandler;
import proj12MacDonaldDouglas.bantam.util.SymbolTable;

import java.util.Iterator;

public class TypeCheckerVisitor extends proj12MacDonaldDouglas.bantam.visitor.Visitor {

    private ClassTreeNode currentClass;
    private SymbolTable currentSymbolTable;
    private ErrorHandler errorHandler;

    /**
     * Check if type is of a class type
     *
     * @param classType
     * @return true if the type is a class type, false if not
     */
    public boolean checkDefinedClass(String classType) {
        return currentClass.getClassMap().containsKey(classType);
    }

    /**
     * Check if type is within the class table or int or boolean
     *
     * @param nameType
     * @return true if the type is defined, false if not
     */
    public boolean checkDefined(String nameType) {
        return (currentClass.getClassMap().containsKey(nameType) || nameType.equals("int") ||
                nameType.equals("boolean"));
    }

    public boolean checkSubClass(String node1, String node2) {
        String nodeName = node1;
        while (!currentClass.getClassMap().get(nodeName).getParent().getName().equals("Object")) {
            nodeName = currentClass.getClassMap().get(nodeName).getParent().getName();
            if(nodeName.equals(node2)) {
                return true;
            }
        }
        return false;
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
        // if (...node's type is not a defined type...) {
        if ((!checkDefined(node.getType()))) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the field "
                            + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            if(!checkSubClass(initExpr.getExprType(), node.getType())) {
//                if(...the initExpr's type is not a subtype of the node's type...) {
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

    /**
     * Visit a method node
     *
     * @param node the Method node to visit
     * @return null
     */
    public Object visit(Method node) {
        if (!checkDefined(node.getReturnType()) && !node.getReturnType().equals("void")) {
//        if (...the node's return type is not a defined type and not "void"...) {
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

    /**
     * Visit a formal parameter node
     *
     * @param node the Formal node
     * @return null
     */
    public Object visit(Formal node) {
//        if (...the node's type is not a defined type...) {
        if (!checkDefined(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the formal" +
                            " parameter " + node.getName() + " is undefined.");
        }
        // add it to the current scope
        currentSymbolTable.add(node.getName(), node.getType());
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
        if(!node.getPredExpr().getExprType().equals("boolean")) {
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
        if(!checkDefinedClass(node.getType())) {
//        if(...the node's type is not a defined class type...) {
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

    public Object visit(DeclStmt node) {
        if ((!checkDefinedClass(node.getType()))) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the DeclStmt "
                            + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            if(!checkSubClass(initExpr.getExprType(), node.getType())) {
//                if(...the initExpr's type is not a subtype of the node's type...) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the initializer is " + initExpr.getExprType()
                                + " which is not compatible with the " + node.getName() +
                                " DeclStmt's type " + node.getType());
            }
        }
        //Note: if there is no initExpr, then leave it to the Code Generator to
        //      initialize it to the default value since it is irrelevant to the
        //      SemanticAnalyzer.
        return null;
    }

    // finish this, what types can be here
    /**
     * Visit a binary arithmetic divide expression node
     *
     * @param node the binary arithmetic divide expression node
     * @return null
     */
    public Object visit(BinaryArithDivideExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being divided is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     *
     * @param node the binary arithmetic minus expression node
     * @return null
     */
    public Object visit(BinaryArithMinusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being subtracted is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryArithModulusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values in the modulus is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryArithPlusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being added is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryArithTimesExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being multiplied is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryCompGeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Geq is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryCompGtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Gt is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryCompLeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Leq is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryCompLtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("int") || !type2.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Lt is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    public Object visit(BinaryCompNeExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!checkSubClass(type1, type2) && !checkSubClass(type2, type1)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The two values being compared for non-equality are not compatible types.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit a binary comparison equals expression node
     *
     * @param node the binary comparison equals expression node
     * @return null
     */
    public Object visit(BinaryCompEqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!checkSubClass(type1, type2) && !checkSubClass(type2, type1)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                currentClass.getASTNode().getFilename(), node.getLineNum(),
                "The two values being compared for equality are not compatible types.");
        }
        node.setExprType("boolean");
        return null;
    }

    public Object visit(BinaryLogicAndExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("boolean") || !type2.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the expressions in the AND expression is not a boolean.");
        }
        node.setExprType("boolean");
        return null;
    }

    public Object visit(BinaryLogicOrExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(!type1.equals("boolean") || !type2.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the expressions in the OR expression is not a boolean.");
        }
        node.setExprType("boolean");
        return null;
    }

    public Object visit(CastExpr node) {
        if(!checkDefined(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Casting to an undefined type.");
        }
        return null;
    }

    // need to implement Dispatch
    public Object visit(DispatchExpr node) {
        if(node.getRefExpr() != null)
            node.getRefExpr().accept(this);
        node.getActualList().accept(this);
        return null;
    }

    // need to implement ExprList
    public Object visit(ExprList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Expr) it.next()).accept(this);
        return null;
    }

    public Object visit(ExprStmt node) {
        node.getExpr().accept(this);
        return null;
    }

    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Formal) it.next()).accept(this);
        return null;
    }

    public Object visit(ForStmt node) {
        Expr node1 = node.getInitExpr();
        if(node1 != null) {
            node1.accept(this);
            if(!node1.getExprType().equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Expression type of UpdateExpr is " + node1.getExprType() +
                                ", not int.");
            }
        }
        Expr node2 = node.getPredExpr();
        if(node2 != null) {
            node2.accept(this);
            if(!node2.getExprType().equals("boolean")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Expression type of UpdateExpr is " + node2.getExprType() +
                                ", not boolean.");
            }
        }
        Expr node3 = node.getUpdateExpr();
        if(node3 != null) {
            node3.accept(this);
            if(!node3.getExprType().equals("int")) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Expression type of UpdateExpr is " + node3.getExprType() +
                        ", not int.");
            }
        }
        node1.setExprType("int");
        node2.setExprType("boolean");
        node3.setExprType("int");
        node.getBodyStmt().accept(this);
        return null;
    }

    public Object visit(IfStmt node) {
        node.getPredExpr().accept(this);
        String type1 = node.getPredExpr().getExprType();
        if(!type1.equals("boolean")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "IfStmt is of type " + type1 + ", not boolean.");
        }
        node.getThenStmt().accept(this);
        if(node.getElseStmt() != null) {
            node.getElseStmt().accept(this);
        }
        node.getPredExpr().setExprType("boolean");
        return null;
    }

    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        String type1 = node.getExprType();
        String type2 = node.getType();
        if(!checkDefinedClass(type2) || !checkSubClass(type1, type2)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Type is not defined type or Expr type is not subclass of type.");
                    // need to make method for super types - can be a super type
        }
        return null;
    }

    public Object visit(MemberList node) {
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
        if(!type.equals("boolean")) {
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



}
