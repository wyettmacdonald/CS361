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
    private int num_vars;
    private String class_name;
    private String method_name;

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

    /**
     * Visit a class node and store the class node name
     *
     * @param node the class node
     * @return result of the visit
     */
    public Object visit(Class_ node) {
        class_name = node.getName();
        super.visit(node);
        return null;
    }

    /**
     * Visit a method node and store the method node name
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        method_name = node.getName();
        super.visit(node);
        return null;
    }

    /**
     * Visit a list node of Formals and increments num_vars
     *
     * @param node the formal list node
     * @return result of visit
     */
    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
//            ((Formal) it.next()).accept(this);
//            System.out.println(it.next().toString());
            it.next();
            num_vars++;
        }
        return null;
    }

    /**
     * Visit a list node of Statements and increments num_vars
     *
     * @param node the statement list node
     * @return result of visit
     */
    public Object visit(StmtList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
//            ((Stmt) it.next()).accept(this);
//            System.out.println(it.next().toString());
            if (it.next() instanceof DeclStmt) {
                num_vars++;
            }
        }
        String key_input = class_name + "." + method_name;
        localVarsMap.put(key_input, num_vars);
        num_vars = 0;
        return null;
    }
}