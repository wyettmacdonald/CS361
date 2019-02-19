/*
 * Name: SymbolTableBuildingVisitor.java
 * Authors: Tia Zhang and Danqing Zhao
 * Class: CS461
 * Date: February 25, 2019
 */



package proj12ZhangZhao.bantam.semant;

import proj12ZhangZhao.bantam.ast.*;
import proj12ZhangZhao.bantam.util.ErrorHandler;
import proj12ZhangZhao.bantam.util.Error;
import proj12ZhangZhao.bantam.util.SymbolTable;
import proj12ZhangZhao.bantam.visitor.Visitor;
import proj12ZhangZhao.bantam.util.ClassTreeNode;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;


public class SymbolTableBuildingVisitor extends Visitor{
    Hashtable<String, ClassTreeNode> classMap;
    ErrorHandler errorHandler;
    String currentClass;

    public SymbolTableBuildingVisitor(Hashtable<String, ClassTreeNode> map, ErrorHandler handler){
        classMap = map;
        errorHandler = handler;
    }

    public void makeTables(){

        classMap.forEach( (nodeName, node) -> {
            node.getASTNode().accept(this);
            setParentAndChild(node);
        });
        //Hardcoded lookup cause there doesn't seem to be a way to get the symbol table to print everything inside
        System.out.println(classMap.get("Main").getMethodSymbolTable().lookup("bar"));
//        System.out.println(classMap.get("Main").getMethodSymbolTable().lookup("foo"));
//
//        System.out.println(classMap.get("B").getMethodSymbolTable().lookup("B"));
//        System.out.println(classMap.get("B").getMethodSymbolTable().lookup("main"));

        //System.out.println("Is there a main method? " + classMap.get("Main").getMethodSymbolTable().lookup("main"));

        //Params
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("b"));
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("a"));
        System.out.println(classMap.get("B").getVarSymbolTable().lookup("a"));

        //Params
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("y"));
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("z"));

        //Local vars are the job of the TypeCheckerVisitor

        System.out.print("Is there a main method? ");
        //System.out.println(classMap.get("Main").getMethodSymbolTable().getSize());
        boolean mainMainExists = (classMap.get("Main").getMethodSymbolTable().lookup("main") != null);
        System.out.println(mainMainExists);
        if(!mainMainExists){
            //TODO WHAT LINE NUM SHOULD NO MAIN CLASS HAVE
            errorHandler.register(Error.Kind.SEMANT_ERROR, classMap.get(currentClass).getASTNode().getFilename(), 0,
                    "There is no Main class with a main method in this file");

        }

    }


    public Object visit(Class_ node){
        currentClass = node.getName();
        ClassTreeNode treeNode = classMap.get(currentClass);

        treeNode.getMethodSymbolTable().enterScope();
        treeNode.getVarSymbolTable().enterScope();

        super.visit(node);
        return null;
    }

    public Object visit(Field node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getVarSymbolTable().add(node.getName(), node.getType());
        System.out.println("Field found: " + node.getName() + " added to " + currentClass);
        return null;
    }

    public Object visit(Method node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getMethodSymbolTable().add(node.getName(), node);
        System.out.println("Adding method " + node.getName() + " to " + currentClass);
        treeNode.getVarSymbolTable().enterScope();
        super.visit(node);
        return null;
    }

    public Object visit(Formal node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getVarSymbolTable().add(node.getName(), node.getType());
        System.out.println("Param found: " + node.getName() + " added to " + currentClass);
        return null;
    }



    //DeclStmts won't be handled until TypeCheckerVisitor

    public void setParentAndChild(ClassTreeNode treeNode){
        if(treeNode.getName()== "Object") {
            return;
        }
        ClassTreeNode parentNode = treeNode.getParent();
        treeNode.getVarSymbolTable().setParent(parentNode.getVarSymbolTable());
        treeNode.getMethodSymbolTable().setParent(parentNode.getMethodSymbolTable());

    }

}
