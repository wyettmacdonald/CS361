/*
 * Name: ClassVisitor.java
 * Authors: Tia Zhang and Danqing Zhao
 * Class: CS461
 * Date: February 25, 2019
 */



package proj13DouglasMacDonaldZhang.bantam.semant;

import proj13DouglasMacDonaldZhang.bantam.ast.*;
import proj13DouglasMacDonaldZhang.bantam.util.ErrorHandler;
import proj13DouglasMacDonaldZhang.bantam.util.Error;
import proj13DouglasMacDonaldZhang.bantam.util.SymbolTable;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;
import proj13DouglasMacDonaldZhang.bantam.util.ClassTreeNode;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;



/*
Visitor that visits classes to construct the inheritance tree
*/
public class ClassVisitor extends Visitor{
    Hashtable<String, ClassTreeNode> classMap;
    ErrorHandler errorHandler;
    String currentClass;


    /**
    * Constructor for the ClassVisitor.
    * @param map is the class map that it'll be adding class nodes to
    * @param handler is the error handler that'll be logging any errors along the way
    */
    public ClassVisitor(Hashtable<String, ClassTreeNode> map, ErrorHandler handler){
        classMap = map;
        errorHandler = handler;
    }


    /**
    * Builds the class inheritance tree
    * @param ast is the Program node containing the classes from which to build the tree
    */
    public void makeTree(Program ast){
        ast.accept(this);

        classMap.forEach( (nodeName, node) -> {
            setParentAndChild(node);
        });

        ArrayList<ClassTreeNode> cycleNodes = new ArrayList<ClassTreeNode>();
        classMap.forEach( (nodeName, node) -> {
            checkCycles(node, cycleNodes);
        });



        //Since nodes in a cycle don't have Object as a parent, change parent to Object
        // to connect them to the tree and remove them as children of each other
        ClassTreeNode object = classMap.get("Object");
        cycleNodes.forEach(node ->{
            //System.out.println("Removing cycle in " + node.getName());
            ClassTreeNode oldParent = node.getParent();
            node.setParent(object);
            oldParent.removeChild(node);
        });

    }

    /**
    * Visits Class_ nodes
    * @param node is the Class_ node to be visited
    */
    public Object visit(Class_ node){
        ClassTreeNode treeNode = new ClassTreeNode(node, false, true, classMap);
        classMap.put(node.getName(), treeNode);
        currentClass = node.getName();
        super.visit(node);
        return null;
    }


    /**
    * Connects a class tree node to its parent and its parent to it
    * If the declared parent doesn't exist, Object is used as the parent
    * @param treeNode is the class tree node to be connected
    */
    private void setParentAndChild(ClassTreeNode treeNode){
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
            errorHandler.register(Error.Kind.SEMANT_ERROR, astNode.getFilename(), astNode.getLineNum(),
                    "Parent class of " + treeNode.getName() +  " does not exist");

            //Setting Object as a parent as a default
            parentNode = classMap.get("Object");
            treeNode.setParent(parentNode);
        }
    }



    /**
    * Looks for cycles in a node's inheritance structure
    * @param node is the ClassTreeNode which should be checked for cycles
    * @param cycleNodes is the ArrayList in which to store any nodes in a cycle
    */
    private ArrayList<ClassTreeNode> checkCycles(ClassTreeNode node, ArrayList<ClassTreeNode> cycleNodes){
        Stack stack = new Stack();
        ArrayList<ClassTreeNode> visited = new ArrayList<ClassTreeNode>();
        dfs(node, stack, visited, cycleNodes);
        return cycleNodes;
    }

    /**
    * Helper method for checkCycles which runs a depth first search on a node to find cycles.
    * @param node is the node on which to run depth-first search
    * @param visited is an empty list which will store which nodes have already been fully visited
    * @param cycleNodes is a list which will have all the nodes found in a cycle added to it
    */
    private void dfs(ClassTreeNode node, Stack path, ArrayList<ClassTreeNode> visited, ArrayList<ClassTreeNode> cycleNodes){
        if(!visited.contains(node)){
            //System.out.println("Checking " + node.getName());
            if(path.search(node) > -1){
                Class_ astNode = node.getASTNode();
                errorHandler.register(Error.Kind.SEMANT_ERROR, astNode.getFilename(), astNode.getLineNum(),
                        "There is a cycle with class " + node.getName() +
                                ". Please check its inheritance structure. For now, it'll be changed to have Object as a parent");
                cycleNodes.add(node);
                //System.out.println("Detected cycle in " + node.getName());
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
