

/*
 * Name: ClassVisitor.java
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


public class ClassVisitor extends Visitor{
    Hashtable<String, ClassTreeNode> classMap;
    ErrorHandler errorHandler;
    String currentClass;

    public ClassVisitor(Hashtable<String, ClassTreeNode> map, ErrorHandler handler){
        classMap = map;
        errorHandler = handler;
    }

    public void makeTree(Program ast){
        ast.accept(this);

        classMap.forEach( (nodeName, node) -> {
            setParentAndChild(node);
            node.getMethodSymbolTable();
        });

        ArrayList<ClassTreeNode> cycleNodes = new ArrayList<ClassTreeNode>();
        classMap.forEach( (nodeName, node) -> {
            checkCycles(node, cycleNodes);
        });

        //Since nodes in a cycle don't have Object as a parent, change parent to Object to connect them to the tree
        //This won't remove the cycle (because I can't unset cycled nodes as each other's children), but that doesn't matter
        ClassTreeNode object = classMap.get("Object");
        cycleNodes.forEach(node ->{
            //System.out.println("Cycle node " + node.getName());
            node.setParent(object);
        });



        //Hardcoded lookup cause there doesn't seem to be a way to get the symbol table to print everything inside
//        System.out.println(classMap.get("Main").getMethodSymbolTable().lookup("bar"));
//        System.out.println(classMap.get("Main").getMethodSymbolTable().lookup("foo"));
//
//        System.out.println(classMap.get("B").getMethodSymbolTable().lookup("B"));
//        System.out.println(classMap.get("B").getMethodSymbolTable().lookup("main"));

        //Params
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("b"));
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("a"));
        System.out.println(classMap.get("B").getVarSymbolTable().lookup("a"));

        //Params
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("y"));
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("z"));

        //Local vars
        System.out.println(classMap.get("Main").getVarSymbolTable().lookup("m"));

    }


    public Object visit(Class_ node){
        ClassTreeNode treeNode = new ClassTreeNode(node, false, true, classMap);
        classMap.put(node.getName(), treeNode);
        treeNode.getMethodSymbolTable().enterScope();
        treeNode.getVarSymbolTable().enterScope();
        currentClass = node.getName();
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

    public Object visit(DeclStmt node){
        ClassTreeNode treeNode = classMap.get(currentClass);
        treeNode.getVarSymbolTable().add(node.getName(), null); //DeclStmt have null type at the start - does the type checker set it?
        System.out.println("Local var found: " + node.getName() + " added to " + currentClass);
        return null;
    }



    public void setParentAndChild(ClassTreeNode treeNode){
        if(treeNode.getName()== "Object") {
            return;
        }
        Class_ astNode = treeNode.getASTNode();
        String parent = astNode.getParent();
        ClassTreeNode parentNode;
        if(parent != null ) {
            parentNode = classMap.get(parent);
        }
        else{
            parentNode = classMap.get("Object");

        }

        if(parentNode != null) {
            treeNode.setParent(parentNode);
            //The number of descendants is auto-calculated by setParent, so no need to adjust
            //setParent also triggers addChild() automatically
        }
        else{
            System.out.println("No parent");
            errorHandler.register(Error.Kind.SEMANT_ERROR, astNode.getFilename(), astNode.getLineNum(),
                    "Parent class of " + treeNode.getName() +  " does not exist");
        }
    }




    private ArrayList<ClassTreeNode> checkCycles(ClassTreeNode root, ArrayList<ClassTreeNode> cycleNodes){
        Stack stack = new Stack();
        ArrayList<ClassTreeNode> visited = new ArrayList<ClassTreeNode>();
        dfs(root, stack, visited, cycleNodes);
        return cycleNodes;
    }

    private void dfs(ClassTreeNode node, Stack path, ArrayList<ClassTreeNode> visited, ArrayList<ClassTreeNode> cycleNodes){
        if(!visited.contains(node)){
            //System.out.println("Checking " + node.getName());
            if(path.search(node) > -1){
                Class_ astNode = node.getASTNode();
                errorHandler.register(Error.Kind.SEMANT_ERROR, astNode.getFilename(), astNode.getLineNum(),
                        "There is a cycle with class " + node.getName() +
                                ". Please check its inheritance structure. For now, it'll be changed to have Object as a parent");
                cycleNodes.add(node);
                //System.out.println("Detected cycle");
            }
            else{
                path.push(node);
                //System.out.println("Number of children is " + node.getNumDescendants());
                if(node.getNumDescendants() > 0) {
                    Iterator<ClassTreeNode> childrenIt = node.getChildrenList();
                    while (childrenIt.hasNext()) {
                        //System.out.println("Next child");
                        ClassTreeNode child = childrenIt.next();
                        //System.out.println("Moving onto " + child.getName());
                        dfs(child, path, visited, cycleNodes);
                    }
                }
                else{ //End of the path
                    //System.out.println("No children, popping");
                    path.pop();
                }
                visited.add(node);
            }

        }
    }

}
