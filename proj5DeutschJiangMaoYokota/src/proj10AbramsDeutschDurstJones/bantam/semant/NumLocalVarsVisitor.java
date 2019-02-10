/*
 * File: bantam.semant.NumLocalVarsVisitor.java
 * CS361 Project 11
 * Names: Martin Deutsch and Wyett MacDonald
 * Date: 2/13/2019
 * This file extends Visitor to find and store the local variables
 * in each method in a given AST
 */

package proj10AbramsDeutschDurstJones.bantam.semant;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * NumLocalVarsVisitor searches an AST for local variables
 * and stores them in a hashmap
 *
 * @author Martin Deutsch
 * @author Wyett MacDonald
 */
public class NumLocalVarsVisitor extends Visitor {

    private Map<String, Integer> localVarsMap = new HashMap<>();

    /**
     * Returns a hashmap containing all the local variables in
     * each method in the AST with given root
     *
     * @param ast the Program node at the root of the AST
     * @return a hashmap mapping local variables to class.method names
     */
    public Map<String,Integer> getNumLocalVars(Program ast) {
        ast.getClassList().accept(this);
        return localVarsMap;
    }
}