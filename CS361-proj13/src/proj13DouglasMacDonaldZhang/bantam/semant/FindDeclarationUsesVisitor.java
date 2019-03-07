/*
 * File: FindDeclarationUsesVisitor.java
 * CS461 Project 13
 * Names: Wyett MacDonald, Kyle Douglas, Tia Zhang
 * Data: 2/28/19
 * This file contains the FindDelcarationUses class, handling the ability to find method, field and var uses.
 */
package proj13DouglasMacDonaldZhang.bantam.semant;

import javafx.scene.control.Button;
import proj13DouglasMacDonaldZhang.bantam.ast.Program;
import proj13DouglasMacDonaldZhang.bantam.visitor.Visitor;
import proj13DouglasMacDonaldZhang.bantam.ast.*;

import java.util.*;

public class FindDeclarationUsesVisitor extends Visitor {

    /**
     * Root of the AST
     */
    private Program program;

    // find declaration fields
    private String declarationName;
    private Hashtable<Integer, String> usagesFound = new Hashtable<>();

    /**
     * Sets the tab pane.
     *
     * @param declarationName Selected declaration
     */
    public void setJavaTabPane(String declarationName) {
        this.declarationName = declarationName;
    }

    /**
     * If there is a usage, it will be found
     */
    public void handleFindUses(Program program) {
        this.program = program;
        findDeclarationUses();
//        program.accept(this);
//            getSelectedText();
    }

    public String getUses() {
        String theUses = "";
        Set<Integer> lineNumbers = usagesFound.keySet();
        for (Integer key : lineNumbers) {
            theUses += "Line " + key + ", type " + usagesFound.get(key) + "\n";
        }
        return theUses;
    }

    public void findDeclarationUses() {
        program.accept(this);
    }

    public Object visit(ClassList node) {
        for (ASTNode aNode : node) {
            aNode.accept(this);
        }
        return null;
    }

    public Object visit(Class_ node) {
        node.getMemberList().accept(this);
        return null;
    }

    public Object visit(MemberList node) {
        for (ASTNode child : node) {
            child.accept(this);
        }
        return null;
    }

    public Object visit(Field node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getType());
        }
        return null;
    }

    public Object visit(Method node) {
        node.getFormalList().accept(this);
        node.getStmtList().accept(this);
        return null;
    }

    public Object visit(FormalList node) {
        for (Iterator it = node.iterator(); it.hasNext(); )
            ((Formal) it.next()).accept(this);
        return null;
    }

    public Object visit(Formal node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getType());
        }
        return null;
    }

    public Object visit(DeclStmt node) {
        node.getInit().accept(this);
        super.visit(node);
        return null;
    }

    public Object visit(StmtList node) {
        for (Iterator it = node.iterator(); it.hasNext(); ) {
            ((Stmt) it.next()).accept(this);
        }
        return null;
    }

    public Object visit(AssignExpr node) {
        if(node.getName().equals(declarationName)) {
            usagesFound.put(node.getLineNum(), node.getExprType());
        }
        return null;
    }

    public Object visit(ForStmt node) {
//        node.accept(this);
        super.visit(node);
        return null;
    }

    public Object visit(WhileStmt node) {
        super.visit(node);
//        node.accept(this);
        return null;
    }
}