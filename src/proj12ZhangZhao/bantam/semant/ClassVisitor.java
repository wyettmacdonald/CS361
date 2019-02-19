

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
        });

        ArrayList<ClassTreeNode> cycleNodes = new ArrayList<ClassTreeNode>();
        classMap.forEach( (nodeName, node) -> {
            checkCycles(node, cycleNodes);
        });

        //Since nodes in a cycle don't have Object as a parent, change parent to Object to connect them to the tree
        //This won't remove the cycle (because I can't unset cycled nodes as each other's children), but that doesn't matter
        ClassTreeNode object = classMap.get("Object");
        cycleNodes.forEach(node ->{
            node.setParent(object);
        });

    }


    public Object visit(Class_ node){
        ClassTreeNode treeNode = new ClassTreeNode(node, false, true, classMap);
        classMap.put(node.getName(), treeNode);
        currentClass = node.getName();
        super.visit(node);
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
            treeNode.getVarSymbolTable().setParent(parentNode.getVarSymbolTable());
            treeNode.getMethodSymbolTable().setParent(parentNode.getMethodSymbolTable());
            //The number of descendants is auto-calculated by setParent, so no need to adjust
            //setParent also triggers addChild() automatically
        }
        else{
            System.out.println("No parent");
            errorHandler.register(Error.Kind.SEMANT_ERROR, astNode.getFilename(), astNode.getLineNum(),
                    "Parent class of " + treeNode.getName() +  " does not exist");

            //Setting Object as a parent as a default
            parentNode = classMap.get("Object");
            treeNode.setParent(parentNode);
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
