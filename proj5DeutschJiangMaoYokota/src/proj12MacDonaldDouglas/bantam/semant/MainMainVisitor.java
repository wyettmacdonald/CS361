/*
 * File: bantam.semant.MainMainVisitor.java
 * CS361 Project 11
 * Names: Martin Deutsch and Wyett MacDonald
 * Date: 2/13/2019
 * This file extends Visitor to search a given AST for a Main class
 * containing a main method that has void return type and no parameters
 */

package proj12MacDonaldDouglas.bantam.semant;
import proj12MacDonaldDouglas.bantam.ast.*;
import proj12MacDonaldDouglas.bantam.visitor.Visitor;

/**
 * MainMainVisitor searches an AST for a Main class
 * containing a main method
 *
 * @author Martin Deutsch
 * @author Wyett MacDonald
 */
public class MainMainVisitor extends Visitor {

    private boolean mainMainFound = false;

    /**
     * Determine if the AST with the given root has a Main class
     * containing a main method with void return type and no parameters
     *
     * @param ast the Program node at the root of the AST
     * @return true if there is a Main class with void main method
     * with no parameters, otherwise false
     */
    public boolean hasMain(Program ast) {
        ast.getClassList().accept(this);
        return mainMainFound;
    }


    /**
     * Visit a list node of classes
     * Stop traversal if Main.main found
     *
     * @param node the class list node
     * @return result of the visit
     */
    public Object visit(ClassList node) {
        for (ASTNode aNode : node) {
            aNode.accept(this);
            if (mainMainFound) {
                break;
            }
        }
        return null;
    }

    /**
     * Visit a class node
     * Visit children only if name is Main
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        if (node.getName().equals("Main")) {
            node.getMemberList().accept(this);
        }
        return null;
    }

    /**
     * Visit a list node of members
     * Visit children only if they are methods
     *
     * @param node the member list node
     * @return result of the visit
     */
    public Object visit(MemberList node) {
        for (ASTNode child : node) {
            if (child instanceof Method) {
                child.accept(this);
            }
            if (mainMainFound) {
                break;
            }
        }
        return null;
    }

    /**
     * Visit a method node
     * If name is main, type is void, and has no parameters,
     * stop traversal
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        if (node.getName().equals("main") && node.getReturnType().equals("void")
                && node.getFormalList().getSize() == 0) {
            this.mainMainFound = true;
        }
        return null;
    }
}