package proj10AbramsDeutschDurstJones.bantam.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import proj10AbramsDeutschDurstJones.bantam.util.Error;
import proj10AbramsDeutschDurstJones.bantam.util.ErrorHandler;
import proj10AbramsDeutschDurstJones.bantam.util.CompilationException;
import proj10AbramsDeutschDurstJones.bantam.parser.Parser;
import proj10AbramsDeutschDurstJones.bantam.ast.*;

public class TestParser {
  static Parser parser;
  static Program root;
  static ErrorHandler errorHandler;

  @BeforeClass
  static public void startTest() {
    errorHandler = new ErrorHandler();
    String filepath = new File("").getAbsolutePath();
    filepath = filepath.concat("/proj10AbramsDeutschDurstJones/bantam/tests/test_bantam_files/Parser_HelloWorld_Testfile.java");
    parser = new Parser(errorHandler);
    try {
      root = parser.parse(filepath);
    } catch (CompilationException e) {
      System.out.println("ISSUE:");
      System.out.println(e);
    }
  }

  @Test
  public void traverseAST() {
    // get all classes
    // class Parser_HelloWorld_TestFile
    ListNode childNodes = (ListNode)root.getClassList();
    Class_ classs = (Class_)childNodes.get(0);
    assertEquals("Parser_HelloWorld_Testfile", classs.getName());

    // get all members
    // { member* }
    ListNode members = (ListNode)classs.getMemberList();
    
    // int y = 4;
    Field field = (Field)members.get(0);
    assertEquals("int", field.getType());
    assertEquals("y", field.getName());
    ConstExpr fourExpr = (ConstExpr)field.getInit();
    assertEquals("4", fourExpr.getConstant());
    
    // void main
    Method mainFunc = (Method)members.get(1);
    assertEquals("void", mainFunc.getReturnType());
    assertEquals("main", mainFunc.getName());

    // get all statements
    ListNode statements = mainFunc.getStmtList();

    // var x = 3
    DeclStmt threeStmnt = (DeclStmt)statements.get(0);
    assertEquals("x", threeStmnt.getName());
    ConstExpr threeExpr = (ConstExpr)threeStmnt.getInit();
    assertEquals("3", threeExpr.getConstant());

    // while
    WhileStmt whilee = (WhileStmt)statements.get(1);
    BinaryExpr lte = (BinaryExpr)whilee.getPredExpr();
    ExprStmt incxExpr = (ExprStmt)whilee.getBodyStmt();
    AssignExpr incx = (AssignExpr)incxExpr.getExpr();

    // x <= 5
    VarExpr leftLte = (VarExpr)lte.getLeftExpr();
    ConstExpr rightLte = (ConstExpr) lte.getRightExpr();
    assertEquals("x", leftLte.getName());
    assertEquals("<=", lte.getOpName());
    assertEquals("5", rightLte.getConstant());

    // x = x + y
    BinaryExpr xPlusY = (BinaryExpr)incx.getExpr();
    VarExpr x = (VarExpr)xPlusY.getLeftExpr();
    VarExpr y = (VarExpr)xPlusY.getRightExpr();
    assertEquals("x", incx.getName());
    assertEquals("x", x.getName());
    assertEquals("+", xPlusY.getOpName());
    assertEquals("y", y.getName());
  }
}
