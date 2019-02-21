/*
 * File: bantam.semant.ClassesVisitor.java
 * CS361 Project 12
 * Names: Wyett MacDonald
 * Date: 2/19/2019
 * This file extends Visitor to search a given AST for all classes
 * in the AST.
 */

package proj12MacDonaldDouglas.bantam.semant;
import proj12MacDonaldDouglas.bantam.ast.*;
import proj12MacDonaldDouglas.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassesVisitor searches the tree for all classes
 *
 * @author Wyett MacDonald
 * @author Kyle Douglas
 */
public class ClassesVisitor extends Visitor {

    private Map<Class_, Boolean> classesMap = new HashMap<>();

    /**
     * Determine if the AST with the given root has a Main class
     * containing a main method with void return type and no parameters
     *
     * @param ast the Program node at the root of the AST
     * @return true if there is a Main class with void main method
     * with no parameters, otherwise false
     */
    public Map<Class_, Boolean> getAllClasses(Program ast) {
        ast.getClassList().accept(this);
        return classesMap;
    }


    /**
     * Visit a list node of classes
     * Stop traversal if Main.main found
     *
     * @param node the class list node
     * @return result of the visit
     */
    public Object visit(ClassList node) {
        node.accept(this);
        return null;
    }

    /**
     * Visit a class node
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        classesMap.put(node, true);
        return null;
    }
}
