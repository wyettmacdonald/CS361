/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the FindUnusedVisitor class, which highlights unused variables, fields, and methods
 */


package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;

import java.util.Hashtable;

public class FindUnusedVisitor extends Visitor {


    Hashtable<String, ClassTreeNode> classMap;
    String currentClass;
    Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>> classUsesMap;
    Hashtable<String, IdentifierInfo> currentUsesMap;
    String unused; //This is a field because it's the only way I've found so far to handle modifying in a for each while using a lambda

    /*
     * Constructor for SymbolTableBuildingVisitor
     * @param map is the class map from which to get the class nodes which need symbol tables
     * @param handler is the error handler which will log any errors found along the way
     */
    public FindUnusedVisitor(Hashtable<String, ClassTreeNode> map){
        classMap = map;
        classUsesMap = new Hashtable<ClassTreeNode, Hashtable<String, IdentifierInfo>>();
    }


    /*
     */
    public String checkForUnused(Program ast){
        ast.accept(this);
        unused = "Here are the unused things in this program:\n";
        classUsesMap.forEach( (classNode, usesMap) ->{
                usesMap.forEach( (name, identifierInfo) ->{
                        if(identifierInfo.getNumUses() == 0){
                            unused += identifierInfo.getType() + " " + identifierInfo.getName() + " was never used\n";
                        }
                    }
                );
            }

        );
        return unused;

    }

    /*
     * Visits Class_ nodes
     * @param node is the Class_ node to be visited
     */
    public Object visit(Class_ node){
        currentClass = node.getName();
        ClassTreeNode treeNode = classMap.get(currentClass);
        currentUsesMap = new Hashtable<String, IdentifierInfo>();
        classUsesMap.put(treeNode, currentUsesMap);

        treeNode.getMethodSymbolTable().enterScope();
        treeNode.getVarSymbolTable().enterScope();

        super.visit(node);
        //treeNode.getMethodSymbolTable().exitScope();
        treeNode.getVarSymbolTable().exitScope();
        return null;
    }

    /*
     * Visits Field nodes
     * @param node is the Field node to be visited
     */
    public Object visit(Field node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String fieldName  = node.getName();
        IdentifierInfo fieldInfo = new IdentifierInfo(node.getName(), treeNode, "Var",
                treeNode.getVarSymbolTable().getCurrScopeLevel());
        currentUsesMap.putIfAbsent(fieldName + " " + fieldInfo.getScopeLevel(), fieldInfo );
        return null;
    }


    //TODO figure out how to handle methods of other objects - I don't think that will work until semantic analysis
    /*
     * Visits Method nodes
     * @param node is the Method node to be visited
     */
    public Object visit(Method node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String methodName = node.getName();
        currentUsesMap.putIfAbsent(methodName, new IdentifierInfo(methodName, treeNode, "Method", 0));

        treeNode.getVarSymbolTable().enterScope();
        super.visit(node);
        return null;
    }

    public Object visit(Formal node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String paramName  = node.getName();
        IdentifierInfo paramInfo = new IdentifierInfo(node.getName(), treeNode, "Var",
                treeNode.getVarSymbolTable().getCurrScopeLevel());
        currentUsesMap.putIfAbsent(paramName + " " + paramInfo.getScopeLevel(), paramInfo );
        return null;
    }



    public Object visit(ForStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getVarSymbolTable().enterScope();
        return null;
    }

    public Object visit(WhileStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getVarSymbolTable().enterScope();
        super.visit(node);
        return null;
    }


    public Object visit(DeclStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        IdentifierInfo varInfo = new IdentifierInfo(node.getName(), treeNode, "Var",
                treeNode.getVarSymbolTable().getCurrScopeLevel());
        System.out.println("It's been entered as " + varName + " " + varInfo.getScopeLevel());
        currentUsesMap.putIfAbsent(varName + " " + varInfo.getScopeLevel(), varInfo );
        super.visit(node);
        return null;
    }


    public Object visit(AssignExpr node){
        super.visit(node);
        ClassTreeNode treeNode = classMap.get(currentClass);
        System.out.println("Got to assign node. " + node.getName() +  " " + node.getExprType().getClass());

        //Dummy code while I try to figure out why getName isn't a VarExpr

        String varName = node.getName();
        int scopeLevel = treeNode.getVarSymbolTable().getCurrScopeLevel();
        IdentifierInfo varInfo;
        while(scopeLevel > -1) {
            System.out.println("Checking " + varName  + " " + scopeLevel);
            varInfo = currentUsesMap.get(varName + " " + scopeLevel);
            if(varInfo != null){
                System.out.println("Incrementing for " + varName);
                varInfo.setNumUses(varInfo.getNumUses() + 1);
                break;
            }
            scopeLevel --;

        }



        return null;
    }

    public Object visit(VarExpr node){
        System.out.println("Got to a var node");
        ClassTreeNode treeNode = classMap.get(currentClass);
        String varName = node.getName();
        int scopeLevel = treeNode.getVarSymbolTable().getCurrScopeLevel();
        IdentifierInfo varInfo;
        while(scopeLevel > -1) {
            System.out.println("Checking " + varName  + " " + scopeLevel);
            varInfo = currentUsesMap.get(varName + " " + scopeLevel);
            if(varInfo != null){
                System.out.println("Incrementing for " + varName);
                varInfo.setNumUses(varInfo.getNumUses() + 1);
                break;
            }
            scopeLevel --;

        }



        return null;
    }


    public Object visit(DispatchExpr node){
        //This currently won't work if a method is invoked outside of its class
        String methodName = node.getMethodName();
        IdentifierInfo methodInfo = currentUsesMap.get(methodName);
        if(methodInfo != null){
            methodInfo.setNumUses(methodInfo.getNumUses() + 1);
        }
        //It's not the job of this visitor to report methods that don't exist
        super.visit(node);
        return null;
    }











}
