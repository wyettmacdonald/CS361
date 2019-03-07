/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the IdentifierInfo class, which stores information about identifiers
 */



package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;

public class IdentifierInfo {
    private String name;
    private ClassTreeNode classNode;
    private String type; //It should be either Method or Var
    private int scopeLevel;
    private int numUses;
    private int lineNum;


    public IdentifierInfo(String name, ClassTreeNode classNode, String type, int scopeLevel, int lineNum){
        this.name = name;
        this.classNode = classNode;
        this.type = type; //It should be either Method or Var
        this.scopeLevel = scopeLevel;
        this.numUses = 0;
        this.lineNum = lineNum;
    }


    public String getName() {
        return name;
    }

    public ClassTreeNode getClassNode() {
        return classNode;
    }

    public String getType() {
        return type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public int getNumUses() {
        return numUses;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setNumUses(int numUses) {
        this.numUses = numUses;
    }
}
