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

/**
 * TypeCheckerVisitor goes through AST looking for Semantic issues
 * containing a main method
 *
 * @author Wyett MacDonald
 * @author Kyle Douglas
 */
public class TypeCheckerVisitor extends proj12MacDonaldDouglas.bantam.visitor.Visitor {

    private ClassTreeNode currentClass;
    private SymbolTable currentSymbolTable;
    private ErrorHandler errorHandler;

    /**
     * TypeCheckerVisitor constructor
     *
     * @param node ClassTreeNode
     * @param errorHandler ErrorHandler from SemanticAnalyzer
     */
    public TypeCheckerVisitor(ClassTreeNode node, ErrorHandler errorHandler) {
        this.currentClass = node;
        this.errorHandler = errorHandler;
        node.getASTNode().accept(this);

    }

    /**
     * Check if type is of a class type
     *
     * @param classType
     * @return true if the type is a class type, false if not
     */
    private boolean checkDefinedClass(String classType) {

        return currentClass.getClassMap().containsKey(classType);
    }

    /**
     * Check if type is within the class table or int or boolean or String
     *
     * @param nameType
     * @return true if the type is defined, false if not
     */
    private boolean checkDefined(String nameType) {
        return (currentClass.getClassMap().containsKey(nameType) || nameType.equals("int") ||
                nameType.equals("boolean") || nameType.equals("String"));
    }

    /**
     * Check if type is within the class table or int or boolean or String or var
     *
     * @param nameType
     * @return true if the type is defined, false if not
     */
    private boolean checkDefinedField(String nameType) {
        return (currentClass.getClassMap().containsKey(nameType) || nameType.equals("int") ||
                nameType.equals("boolean") || nameType.equals("String") || nameType.equals("var"));
    }

    /**
     * Check if type is of a class type
     *
     * @param node1 type of node1
     * @param node2 type of node2
     * @return true if type is defined, false if not
     */
    private boolean checkSubClass(String node1, String node2) {
        if(node1 == node2) {
            return true;
        }
        String nodeName = currentClass.getParent().getName();
        while (!nodeName.equals("Object")) {
            if(nodeName.equals(node2)) {
                return true;
            }
            nodeName = currentClass.getParent().getName();
        }
        return false;
    }

    /**
     * Check if node is in the VariableSymbolTable
     * UNUSED
     *
     * @param node
     * @return null
     */
    private Object checkInVarST(String node) {
        currentClass.getVarSymbolTable().enterScope();
        if(currentClass.getVarSymbolTable().lookup(node) != null) {
            return currentClass.getVarSymbolTable().lookup(node);
        }
        return null;
    }

    /**
     * Check if node is in the Method Symbol table
     * UNUSED
     *
     * @param node
     * @return null
     */
    private Object checkInMethodST(String node) {
        currentClass.getMethodSymbolTable().enterScope();
        if(currentClass.getMethodSymbolTable().lookup(node) != null) {
            currentClass.getMethodSymbolTable().exitScope();
            return currentClass.getMethodSymbolTable().lookup(node);
        }
        currentClass.getMethodSymbolTable().exitScope();
        return null;
    }

    /**
     * Visits a Class node
     * Enters and Exits scope
     *
     * @param node the class node
     * @return
     */
    public Object visit(Class_ node) {
        currentClass.getVarSymbolTable().enterScope();
        currentClass.getMethodSymbolTable().enterScope();
        node.getMemberList().accept(this);
        currentClass.getMethodSymbolTable().exitScope();
        currentClass.getVarSymbolTable().exitScope();
        return null;
    }

    /**
     * Visit a list node of members
     *
     * @param node the member list node
     * @return result of the visit
     */
    public Object visit(MemberList node) {
        for (ASTNode child : node)
            child.accept(this);
        return null;
    }

    /**
     * Visit a field node
     *
     * @param node the field node
     * @return null
     */
    @Override
    public Object visit(Field node) {
        // The fields should have already been added to the symbol table by the
        // SemanticAnalyzer so the only thing to check is the compatibility of the init
        // expr's type with the field's type.
        this.currentSymbolTable = currentClass.getVarSymbolTable();
        Object objType = currentClass.getVarSymbolTable().peek(node.getName());
        if (!checkDefinedField(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the field "
                            + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            if(!checkSubClass(initExpr.getExprType(), node.getType())) {
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
    @Override
    public Object visit(Method node) {
        if (!checkDefined(node.getReturnType()) && !node.getReturnType().equals("void")) {
//        if (...the node's return type is not a defined type and not "void"...) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The return type " + node.getReturnType() + " of the method "
                            + node.getName() + " is undefined.");
        }
        //create a new scope for the method body
        currentSymbolTable = currentClass.getVarSymbolTable();
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
    @Override
    public Object visit(Formal node) {
        if (!checkDefined(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the formal" +
                            " parameter " + node.getName() + " is undefined.");
        }
        // add it to the current scope
//        currentSymbolTable.add(node.getName(), node.getType());
        return null;
    }

    /**
     * Visit a while statement node
     *
     * @param node the while statement node
     * @return null
     */
    @Override
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
    @Override
    public Object visit(BlockStmt node) {
        currentSymbolTable.enterScope();
        node.getStmtList().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit a new expression node
     * Check if node type is defined
     *
     * @param node the new expression node
     * @return null
     */
    @Override
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

    /**
     * Visit a DeclStmt node
     * Check if node type is defined
     * Check if initExpr is a subtype of node type
     *
     * @param node the declaration statement node
     * @return null
     */
    @Override
    public Object visit(DeclStmt node) {
        if ((!checkDefinedField(node.getType()))) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The declared type " + node.getType() + " of the DeclStmt "
                            + node.getName() + " is undefined.");
        }
        Expr initExpr = node.getInit();
        if (initExpr != null) {
            initExpr.accept(this);
            if(!checkSubClass(initExpr.getExprType(), node.getType())) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "The type of the initializer is " + initExpr.getExprType()
                                + " which is not compatible with the " + node.getName() +
                                " DeclStmt's type " + node.getType());
            }
            node.getInit().setExprType("Object");
        }
        //Note: if there is no initExpr, then leave it to the Code Generator to
        //      initialize it to the default value since it is irrelevant to the
        //      SemanticAnalyzer.
        return null;
    }

    /**
     * Visit a binary arithmetic divide expression node
     * Check if both expressions are of type int
     *
     * @param node the binary arithmetic divide expression node
     * @return null
     */
    @Override
    public Object visit(BinaryArithDivideExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being divided is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a binary arithmetic minus expression node
     * Check if both expressions are of type int
     *
     * @param node the binary arithmetic minus expression node
     * @return null
     */
    @Override
    public Object visit(BinaryArithMinusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being subtracted is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a BinaryArithModulusExpr node
     * Check if both expressions are of type int
     *
     * @param node the binary arithmetic modulus expression node
     * @return null
     */
    @Override
    public Object visit(BinaryArithModulusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values in the modulus is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a BinaryArithPlusExpr node
     * Check if both expressions are of type int
     *
     * @param node the binary arithmetic plus expression node
     * @return null
     */
    @Override
    public Object visit(BinaryArithPlusExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being added is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a BinaryArithTimesExpr node
     * Check if both expressions are of type int
     *
     * @param node the binary arithmetic times expression node
     * @return null
     */
    @Override
    public Object visit(BinaryArithTimesExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being multiplied is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a BinaryCompGeqExpr node
     * Check if both expressions are of type int
     *
     * @param node the binary comparison greater to or equal to expression node
     * @return null
     */
    @Override
    public Object visit(BinaryCompGeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Geq is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a BinaryCompGtExpr node
     * check if both expressions are of type int
     *
     * @param node the binary comparison greater than expression node
     * @return null
     */
    @Override
    public Object visit(BinaryCompGtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Gt is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a BinaryCompLeqExpr node
     * Check if both expressions are of type int
     *
     * @param node the binary comparison less than or equal to expression node
     * @return null
     */
    @Override
    public Object visit(BinaryCompLeqExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Leq is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit BinaryCompLtExpr node
     * Check if both expressions are of type int
     *
     * @param node the binary comparison less than expression node
     * @return null
     */
    @Override
    public Object visit(BinaryCompLtExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "int" || type2 != "int") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the two values being compared Lt is not an int.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit BinaryCompNeExpr node
     * Check if expressions are subtypes of eachother
     *
     * @param node the binary comparison not equals expression node
     * @return null
     */
    @Override
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
     * Check if expressions are subtypes of eachother
     *
     * @param node the binary comparison equals expression node
     * @return null
     */
    @Override
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

    /**
     * Visit BinaryLogicAndExpr node
     * Check that both expressions are of type boolean
     *
     * @param node the binary logical AND expression node
     * @return null
     */
    @Override
    public Object visit(BinaryLogicAndExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "boolean" || type2 != "boolean") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the expressions in the AND expression is not a boolean.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit BinaryLogicOrExpr node
     * Check that both expressions are of type boolean
     *
     * @param node the binary logical OR expression node
     * @return null
     */
    @Override
    public Object visit(BinaryLogicOrExpr node) {
        node.getLeftExpr().accept(this);
        node.getRightExpr().accept(this);
        String type1 = node.getLeftExpr().getExprType();
        String type2 = node.getRightExpr().getExprType();
        if(type1 != "boolean" || type2 != "boolean") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "One of the expressions in the OR expression is not a boolean.");
        }
        node.setExprType("boolean");
        return null;
    }

    /**
     * Visit CastExpr node
     * Check that node is a defined type
     *
     * @param node the cast expression node
     * @return null
     */
    @Override
    public Object visit(CastExpr node) {
        if(!checkDefined(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Casting to an undefined type.");
        }
        return null;
    }

    // TODO: need to implement Dispatch
    public Object visit(DispatchExpr node) {
        if(node.getRefExpr() != null) {
            node.getRefExpr().accept(this);
            if (currentClass.getMethodSymbolTable().lookup(node.getMethodName()) != null) {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Dispatch Expr error");
            }
            node.setExprType(node.getRefExpr().getExprType());
        }
        node.getActualList().accept(this);
        return null;
    }

    // TODO: need to implement ExprList
    public Object visit(ExprList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Expr) it.next()).accept(this);
        return null;
    }

    /**
     * Visit ExprStmt node
     *
     * @param node the expression statement node
     * @return null
     */
    public Object visit(ExprStmt node) {
        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit FormalList node
     *
     * @param node the formal list node
     * @return null
     */
    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Formal) it.next()).accept(this);
        return null;
    }

    /**
     * Visit ForStmt node
     * Check that initExpr is of type int
     * Check that predExpr is of type boolean
     * Check that updateExpr is of type int
     *
     * @param node the for statement node
     * @return null
     */
    @Override
    public Object visit(ForStmt node) {
        Expr node1 = node.getInitExpr();
        if(node1 != null) {
            node1.accept(this);
            if(node1.getExprType() != "int") {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Expression type of UpdateExpr is " + node1.getExprType() +
                                ", not int.");
            }
            node1.setExprType("int");
        }
        Expr node2 = node.getPredExpr();
        node2.accept(this);
        if(node2.getExprType() != "boolean") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Expression type of PredExpr is " + node2.getExprType() +
                            ", not boolean.");
        }
        Expr node3 = node.getUpdateExpr();
        if(node3 != null) {
            node3.accept(this);
            if(node1.getExprType() != "int") {
                errorHandler.register(Error.Kind.SEMANT_ERROR,
                        currentClass.getASTNode().getFilename(), node.getLineNum(),
                        "Expression type of UpdateExpr is " + node3.getExprType() +
                        ", not int.");
            }
            node3.setExprType("int");
        }
        node2.setExprType("boolean");
        currentSymbolTable.enterScope();
        node.getBodyStmt().accept(this);
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit an IfStmt node
     * Check that predExpr is of type boolean
     *
     * @param node the if statement node
     * @return null
     */
    @Override
    public Object visit(IfStmt node) {
        node.getPredExpr().accept(this);
        if(node.getPredExpr().getExprType() != "boolean") {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "IfStmt is of type " + node.getPredExpr().getExprType() + ", not boolean.");
        }
        node.getPredExpr().setExprType("boolean");
        currentSymbolTable.enterScope();
        node.getThenStmt().accept(this);
        if(node.getElseStmt() != null) {
            node.getElseStmt().accept(this);
        }
        currentSymbolTable.exitScope();
        return null;
    }

    /**
     * Visit an InstanceofExpr node
     * Check that the node type is defined
     * Check that ExprType is a subclass of nodeType
     *
     * @param node the instanceof expression node
     * @return null
     */
    @Override
    public Object visit(InstanceofExpr node) {
        node.getExpr().accept(this);
        String type1 = node.getExprType();
        String type2 = node.getType();
        if(!checkDefined(type2) || !checkSubClass(type1, type2)) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Type is not defined type or Expr type is not subclass of type.");
                    // need to make method for super types - can be a super type
        }
        return null;
    }

    /**
     * Visit a NewArrayExpr node
     * Check that node type is defined
     *
     * @param node the new array expression node
     * @return null
     */
    @Override
    public Object visit(NewArrayExpr node) {
        node.getSize().accept(this);
        if(!checkDefined(node.getType())) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Array of type " + node.getType() + " is not of a " +
                            "defined type.");
            node.setExprType("Object");
        }
        // set expr type?
        return null;
    }

    /**
     * Visit an AssignExpr node
     * Check if name reference is equal to this or super
     *
     * @param node the assignment expression node
     * @return null
     */
    @Override
    public Object visit(AssignExpr node) {
        node.getExpr().accept(this);
        if(node.getRefName() != "this" && node.getRefName() != "super" && node.getRefName() != null) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "Type " + node.getRefName() + " is not allowed for " +
                            "AssignExpr");
        }
//        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a ReturnStmt node
     * Check that return type is a defined type
     *
     * @param node the return statement node
     * @return null
     */
    @Override
    public Object visit(ReturnStmt node) {
        if (node.getExpr() != null) {
            node.getExpr().accept(this);

            String type = node.getExpr().getExprType();
            if (type != null && !checkDefined(type)) {
//            node.getExpr().accept(this);
//            if(!checkDefined(node.getExpr().getExprType())) {
                if (!currentClass.getMethodSymbolTable().peek(node.getExpr().getExprType(),
                        currentSymbolTable.getCurrScopeLevel()).toString().equals(node.getExpr().getExprType())) {
                    errorHandler.register(Error.Kind.SEMANT_ERROR,
                            currentClass.getASTNode().getFilename(), node.getLineNum(),
                            "Return type of " + node.getExpr().getExprType() + " is not of a " +
                                    "defined type.");
                    node.getExpr().setExprType("Object");
                }
            }
        }
//        node.getExpr().accept(this);
        return null;
    }

    /**
     * Visit a StmtList node
     *
     * @param node the statement list node
     * @return null
     */
    public Object visit(StmtList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
            ((Stmt) it.next()).accept(this);
        }
        return null;
    }

    /**
     * Visit a UnaryDecrExpr node
     * Check that it is of type int
     *
     * @param node the unary decrement expression node
     * @return null
     */
    @Override
    public Object visit(UnaryDecrExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not -- operator applies only to int expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a UnaryIncrExpr node
     * Check that it is of type int
     *
     * @param node the unary increment expression node
     * @return null
     */
    @Override
    public Object visit(UnaryIncrExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not ++ operator applies only to int expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a UnaryNegExpr node
     * Check that it is of type int
     *
     * @param node the unary negation expression node
     * @return null
     */
    @Override
    public Object visit(UnaryNegExpr node) {
        node.getExpr().accept(this);
        String type = node.getExpr().getExprType();
        if(!type.equals("int")) {
            errorHandler.register(Error.Kind.SEMANT_ERROR,
                    currentClass.getASTNode().getFilename(), node.getLineNum(),
                    "The not - operator applies only to int expressions," +
                            " not " + type + " expressions.");
        }
        node.setExprType("int");
        return null;
    }

    /**
     * Visit a unary NOT expression node
     *
     * @param node the unary NOT expression node
     * @return null
     */
    @Override
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
     * Visit a VarExpr node
     *
     * @param node the variable expression node
     * @return null
     */
    public Object visit(VarExpr node) {
        if (node.getRef() != null) {
            node.getRef().accept(this);
        }
        return null;
    }

    /**
     * Visit an int constant expression node
     *
     * @param node the int constant expression node
     * @return null
     */
    @Override
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
    @Override
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
    @Override
    public Object visit(ConstStringExpr node) {
        node.setExprType("String");
        return null;
    }

    /**
     * Visit a break statment node
     *
     * @param node the break statement node
     * @return null
     */
    public Object visit(BreakStmt node) {
//        node.accept(this);
        return null;
    }



}
