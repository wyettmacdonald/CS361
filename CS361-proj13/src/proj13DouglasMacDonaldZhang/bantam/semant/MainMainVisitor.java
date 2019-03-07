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

public class MainMainVisitor extends Visitor {
    private boolean hasMainClassAndMethod; //tracks whether a Main class with a main method has been found yet

    /**
     * Takes in a Syntax Tree and searches the tree for a main class with a main method
     * @param ast AST to search for main method
     * @return boolean of whether the main method is there
     */
    public boolean hasMain(Program ast){
        hasMainClassAndMethod = false;
        ast.accept(this);
        return hasMainClassAndMethod;
    }

    /**
     * visits class node and checks if it is main class
     * @param node the class node
     */
    public Object visit(Class_ node){
        if(node.getName().equals("Main")){
            node.getMemberList().accept(this);
        }
        return null;
    }

    /**
     * bypass fields
     * @param node the field node
     */
    public Object visit(Field node){
        return null;
    }

    /**
     * updated visitor so that if main method is found in main class, stops searching through
     * the member list
     * @param node the member list node
     */
    public Object visit(MemberList node){
        for (ASTNode child : node){
            child.accept(this);
            if(hasMainClassAndMethod){
                return null;
            }
        }
        return null;
    }

    /**
     * if in main class, searches for main method with correct format.
     * @param node the method node
     */
    public Object visit(Method node){
        //must be called main, have void return, and no parameters
        if(node.getName().equals("main") && node.getReturnType().equals("void")
                && node.getFormalList().getSize() == 0){
            hasMainClassAndMethod = true;
        }
        return null;
    }

}
