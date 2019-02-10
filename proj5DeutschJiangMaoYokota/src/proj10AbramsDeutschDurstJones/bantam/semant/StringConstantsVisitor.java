/*
 * File: bantam.semant.StringConstantsVisitor.java
 * CS361 Project 11
 * Names: Martin Deutsch and Wyett MacDonald
 * Date: 2/13/2019
 * This file extends Visitor to find and store all the
 * string constants in a given AST
 */

package proj10AbramsDeutschDurstJones.bantam.semant;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

/**
 * StringConstantsVisitor searches an AST for string constants
 * and stores them in a hashmap
 *
 * @author Martin Deutsch
 * @author Wyett MacDonald
 */
public class StringConstantsVisitor extends Visitor {

    private Map<String, String> stringConstantsMap = new HashMap<>();
    private int num_consts = 0;

    /**
     * Returns a hashmap containing all the string constants in the
     * AST with given root
     *
     * @param ast the Program node at the root of the AST
     * @return a hashmap mapping string constants to unique names
     */
    public Map<String,String> getStringConstants(Program ast) {
        ast.getClassList().accept(this);
        return stringConstantsMap;
    }

    /**
     * Visit a method node but do not visit method parameters
     *
     * @param node the method node
     * @return result of the visit
     */
    public Object visit(Method node) {
        node.getStmtList().accept(this);
        return null;
    }

    /**
     * Visit a Constant String Expression node and inputs name and value in to HashMap
     *
     * @param node the string constant expression node
     * @return result of visit
     */
    public Object visit(ConstStringExpr node) {
        String const_name = "StringConst_" + num_consts;
        stringConstantsMap.put(node.getConstant(), const_name);
        num_consts++;
        return null;
    }
}