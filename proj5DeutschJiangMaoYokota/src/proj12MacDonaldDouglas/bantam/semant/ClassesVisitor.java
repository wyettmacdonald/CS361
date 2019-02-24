/*
 * File: bantam.semant.ClassesVisitor.java
 * CS361 Project 12
 * Names: Wyett MacDonald
 * Date: 2/19/2019
 * This file extends Visitor to search a given AST for all classes
 * in the AST.
 */

package proj12MacDonaldDouglas.bantam.semant;
import com.sun.source.tree.ClassTree;
import proj12MacDonaldDouglas.bantam.ast.*;
import proj12MacDonaldDouglas.bantam.util.ClassTreeNode;
import proj12MacDonaldDouglas.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * ClassesVisitor searches the tree for all classes
 *
 * @author Wyett MacDonald
 * @author Kyle Douglas
 */
public class ClassesVisitor extends Visitor {

    private Hashtable<String, ClassTreeNode> classMap;
    private String currentClass;

    public ClassesVisitor(Hashtable<String, ClassTreeNode> classMap) {
        this.classMap = classMap;
    }

    /**
     * Determine if the AST with the given root has a Main class
     * containing a main method with void return type and no parameters
     *
     * @param ast the Program node at the root of the AST
     * @return true if there is a Main class with void main method
     * with no parameters, otherwise false
     */
    public Hashtable<String, ClassTreeNode> getAllClasses(Program ast) {
        ast.getClassList().accept(this);
        return classMap;
//        return classesMap;
    }


    /**
     * Visit a list node of classes
     *
     * @param node the class list node
     * @return result of the visit
     */
    public Object visit(ClassList node) {
        for (ASTNode aNode : node) {
            aNode.accept(this);
        }
        return null;
    }

    /**
     * Visit a class node
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        if(classMap.contains(node.getName())) {
            System.out.println("Duplicate class");
            return null;
        }
        currentClass = node.getName();
        classMap.put(node.getName(), new ClassTreeNode(node, false,
                true, classMap));
//        classesMap.put(node, true);
        node.getMemberList().accept(this);
        System.out.println(currentClass);
        classMap.get(currentClass).getMethodSymbolTable().dump();
        classMap.get(currentClass).getVarSymbolTable().dump();
        return null;
    }

    public Object visit(MemberList node) {
        for (ASTNode child : node) {
            child.accept(this);
        }
        return null;
    }

    public Object visit(Field node) {
        classMap.get(currentClass).getVarSymbolTable().enterScope();
        classMap.get(currentClass).getVarSymbolTable().add(node.getName(), node.getType());
        classMap.get(currentClass).getVarSymbolTable().exitScope();
        return null;
    }

    public Object visit(Method node) {
        classMap.get(currentClass).getMethodSymbolTable().enterScope();
        classMap.get(currentClass).getMethodSymbolTable().add(node.getName(), node.getReturnType());
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        classMap.get(currentClass).getMethodSymbolTable().exitScope();
        return null;
    }

    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); ((Iterator) it).hasNext();) {
            Formal aNode = ((Formal) it.next());
            aNode.accept(this);
        }
        return null;
    }

    public Object visit(Formal node) {
        classMap.get(currentClass).getVarSymbolTable().enterScope();
        classMap.get(currentClass).getVarSymbolTable().add(node.getName(), node.getType());
        classMap.get(currentClass).getVarSymbolTable().exitScope();
        return null;
    }

    public Object visit(StmtList node) {
        for (Iterator it = node.iterator(); it.hasNext();) {
                ((Stmt) it.next()).accept(this);
        }
        return null;
    }

//    public Object visit(DeclStmt node) {
//        System.out.println(node.getName());
//        classMap.get(currentClass).getVarSymbolTable().enterScope();
//        classMap.get(currentClass).getVarSymbolTable().add(node.getName(), node.getType());
//        classMap.get(currentClass).getVarSymbolTable().exitScope();
//        return null;
//    }

    // needs to be implemented
    public Object visit(IfStmt node) {
        return null;
    }

    public Object visit(WhileStmt node) {
        classMap.get(currentClass).getVarSymbolTable().enterScope();
        node.getPredExpr().accept(this);
        node.getBodyStmt().accept(this);
        classMap.get(currentClass).getVarSymbolTable().exitScope();
        return null;
    }

    public Object visit(BlockStmt node) {
        classMap.get(currentClass).getVarSymbolTable().enterScope();
        node.getStmtList().accept(this);
        classMap.get(currentClass).getVarSymbolTable().exitScope();
        return null;
    }

    public Object visit(ExprList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
            ((Expr) it.next()).accept(this);
        }
        return null;
    }

    public Object visit(AssignExpr node) {
        node.getExpr().accept(this);
        return null;
    }

}
