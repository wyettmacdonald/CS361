/*
 * File: bantam.semant.MainMainVisitor.java
 * CS361 Project 11
 * Names: Martin Deutsch and Wyett MacDonald
 * Date: 2/13/2019
 * This file extends Visitor to search a given AST for a Main class
 * containing a main method that has void return type and no parameters
 */

package proj10AbramsDeutschDurstJones.bantam.semant;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.visitor.Visitor;

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
}