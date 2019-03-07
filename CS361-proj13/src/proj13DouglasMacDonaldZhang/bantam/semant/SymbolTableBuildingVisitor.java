/*
 * Name: SymbolTableBuildingVisitor.java
 * Authors: Tia Zhang and Danqing Zhao
 * Class: CS461
 * Date: February 25, 2019
 */



package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj13DouglasMacDonaldZhang.bantam.util.Error;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;
//import proj13DouglasMacDonaldZhang.proj12.SemanticAnalyzer;

import java.util.Hashtable;
import java.util.List;


/*
A Visitor that builds the method symbol table and part of the var symbol table for class tree nodes
*/

public class SymbolTableBuildingVisitor extends Visitor{
    Hashtable<String, ClassTreeNode> classMap;
    ErrorHandler errorHandler;
    String currentClass;

    /*
    * Constructor for SymbolTableBuildingVisitor
    * @param map is the class map from which to get the class nodes which need symbol tables
    * @param handler is the error handler which will log any errors found along the way
    */
    public SymbolTableBuildingVisitor(Hashtable<String, ClassTreeNode> map, ErrorHandler handler){
        classMap = map;
        errorHandler = handler;
    }


    /*
    * Makes method symbol tables and begins the var symbol tables f
    * or all classes in the class map given at initialization
    */
    public void makeTables(){

        classMap.forEach( (nodeName, node) -> {
            node.getASTNode().accept(this);
            setParentAndChild(node);
        });


        ClassTreeNode mainClassNode = classMap.get("Main");
        if(mainClassNode == null){
            errorHandler.register(Error.Kind.SEMANT_ERROR, classMap.get(currentClass).getASTNode().getFilename(), 0,
                    "There is no Main class in this file");
        }
        else{
            //System.out.println("Scope size " + classMap.get("Main").getMethodSymbolTable().getSize());
            boolean mainMainExists = (classMap.get("Main").getMethodSymbolTable().lookup("main") != null);
            //System.out.println(mainMainExists);
            if(!mainMainExists){
                //Using 0 as the line number cause that doesn't really have a specific line num
                errorHandler.register(Error.Kind.SEMANT_ERROR, classMap.get(currentClass).getASTNode().getFilename(), 0,
                        "There is no Main class with a main method in this file");

            }
        }


    }

    /*
    * Visits Class_ nodes
    * @param node is the Class_ node to be visited
    */
    public Object visit(Class_ node){
        currentClass = node.getName();
        ClassTreeNode treeNode = classMap.get(currentClass);

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
        if(SemanticAnalyzer.reservedIdentifiers.contains(fieldName)){
            errorHandler.register(Error.Kind.SEMANT_ERROR, classMap.get(currentClass).getASTNode().getFilename(),
                    node.getLineNum(),
                    "Reserved word " + fieldName + " cannot be used as an identifier");

        }
        treeNode.getVarSymbolTable().add(node.getName(), node.getType());
        //System.out.println("Field found: " + node.getName() + " added to " + currentClass);
        return null;
    }

    /*
     * Visits Method nodes
     * @param node is the Method node to be visited
     */
    public Object visit(Method node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getMethodSymbolTable().add(node.getName(), node);
        //System.out.println("Adding method " + node.getName() + " to " + currentClass);
        treeNode.getVarSymbolTable().enterScope();
        super.visit(node);
        return null;
    }

    public Object visit(Formal node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        String paramName  = node.getName();
        if(SemanticAnalyzer.reservedIdentifiers.contains(paramName)){
            errorHandler.register(Error.Kind.SEMANT_ERROR, classMap.get(currentClass).getASTNode().getFilename(),
                    node.getLineNum(),
                    "Reserved word " + paramName + " cannot be used as an identifier");

        }
        treeNode.getVarSymbolTable().add(node.getName(), node.getType());
        //System.out.println("Param found: " + node.getName() + " added to " + currentClass);
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
