/*
 * File: NumLocalVarsVisitor.java
 * Names: Zeb Keith-Hardy, Tia Zhang, Danqing Zhao
 * Class: CS 461
 * Project 11
 * Date: February 13, 2018
 */

package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.HashMap;
import java.util.Map;

public class NumLocalVarsVisitor extends Visitor {
    private String className; //Name of the current class the Visitor is visiting
    private String methodName; //Name of the current method the Visitor is visiting
    private int numLocalVars = 0; //Number of local vars in current method
    private HashMap<String, Integer> varMap; //Map storing [Class].[Method] to number of local vars + params


    /**
     * searches an AST for the number of local vars in each method
     * @param node AST to be searched
     */
    public Map<String, Integer> getNumLocalVars(Program node){
        varMap = new HashMap<>();
        node.accept(this);
        return varMap;
    }


    /**
     * grabs the name of the class we are in for storing in the HashMap
     * @param node the class node
     */
    public Object visit(Class_ node) {
        className = node.getName();
        super.visit(node);
        return null;
    }


    /**
     * skip fields
     * @param node the field node
     */
    public Object visit(Field node){
        return null;
    }

    /**
     * grabs the name of the method for storage
     * after searching children of node, places method name and num local vars in HashMap
     * @param node the method node
     */
    public Object visit(Method node){
        numLocalVars = 0;
        methodName = node.getName();
        super.visit(node);
        varMap.put(className + "." + methodName,numLocalVars + node.getFormalList().getSize());
        return null;

    }

    /**
     * local variable declarations need to be counted
     * @param node the declaration statement node
     */
    public Object visit(DeclStmt node){
        numLocalVars++;
        return null;
    }

    /**
     * skip return statements
     * @param node the return statement node
     */
    public Object visit(ReturnStmt node){
        return null;
    }


    /**
     * skip dispatch expressions
     * @param node the dispatch expression node
     */
    public Object visit(DispatchExpr node){
        return null;
    }

    /**
     * skip cast expressions
     * @param node the cast expression node
     */
    public Object visit(CastExpr node){
        return null;
    }


    /**
     * skip variable expressions
     * @param node the variable expression node
     */
    public Object visit(VarExpr node){
        return null;
    }
}
