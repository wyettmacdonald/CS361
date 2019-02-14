/*
* Name: ClassVisitor.java
* Authors: Tia Zhang and Danqing Zhao
* Class: CS461
* Date: February 25, 2019
*/



package proj12ZhangZhao.bantam.semant;

import proj12ZhangZhao.bantam.ast.Program;
import proj12ZhangZhao.bantam.util.ErrorHandler;
import proj12ZhangZhao.bantam.util.Error;
import proj12ZhangZhao.bantam.visitor.Visitor;
import proj12ZhangZhao.bantam.ast.Class_;
import proj12ZhangZhao.bantam.util.ClassTreeNode;

import java.util.Hashtable;



public class ClassVisitor extends Visitor{
    Hashtable<String, ClassTreeNode> classMap;
    ErrorHandler errorHandler;

    public ClassVisitor(Hashtable<String, ClassTreeNode> map, ErrorHandler handler){
        classMap = map;
        errorHandler = handler;
    }

    public void makeTree(Program ast){
        ast.accept(this);

        classMap.forEach( (nodeName, node) -> {
            //ClassTreeNode node = classMap.get(nodeName);
            System.out.println("Node " + nodeName);
            setParentAndChild(node);
        });



        checkCycles();
    }


    public Object visit(Class_ node){
        ClassTreeNode treeNode = new ClassTreeNode(node, false, true, classMap);
        classMap.put(node.getName(), treeNode);

        return null;
    }


    public void setParentAndChild(ClassTreeNode treeNode){
        if(treeNode.getName()== "Object") {
            return;
        }
        Class_ astNode = treeNode.getASTNode();
        String parent = astNode.getParent();
        //System.out.println(classMap.keySet() + " " + classMap.keySet().toArray()[0].getClass());
        ClassTreeNode parentNode;
        if(parent != null ) {
            parentNode = classMap.get(parent);
            System.out.println("Inheritance! " + parent + " " + parent.getClass());
            classMap.keySet().forEach(key -> {
                System.out.print(key + " " + parent);
                System.out.print(" Does it match? ");
                System.out.print(parent.equals(key));
                System.out.println();
            });
        }
        else{
            parentNode = classMap.get("Object");

        }

        if(parentNode != null) {
            //System.out.println("The parent is " + parentNode.getName());
            treeNode.setParent(parentNode);
            //The number of descendants is auto-calculated by setParent, so no need to adjust
            parentNode.addChild(treeNode);
        }
        else{
            System.out.println("No parent");
            errorHandler.register(Error.Kind.SEMANT_ERROR, astNode.getFilename(), astNode.getLineNum(),
                    "Parent class does not exist");
        }
    }




    public void checkCycles(){
        return;
    }

}
