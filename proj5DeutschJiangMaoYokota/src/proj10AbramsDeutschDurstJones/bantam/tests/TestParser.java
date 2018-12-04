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
  }

  @Test
  public void TestHelloWorld() {
    String filepath = new File("").getAbsolutePath();
    filepath = filepath.concat("/proj10AbramsDeutschDurstJones/bantam/tests/test_bantam_files/Parser_Testfile.java");
    parser = new Parser(errorHandler);
    root = parser.parse(filepath);
    
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
    
    // void main(int z)
    Method mainFunc = (Method)members.get(2);
    ListNode mainFuncParams = (ListNode)mainFunc.getFormalList();
    Formal param = (Formal)mainFuncParams.get(0);
    assertEquals("int", mainFunc.getReturnType());
    assertEquals("main", mainFunc.getName());
    assertEquals("int", param.getType());
    assertEquals("z", param.getName());

    // get all statements
    ListNode statements = mainFunc.getStmtList();

    // var x = new Walrus()
    DeclStmt walrusStmnt = (DeclStmt)statements.get(0);
    assertEquals("x", walrusStmnt.getName());
    NewExpr newExpr = (NewExpr)walrusStmnt.getInit();
    assertEquals("Walrus", newExpr.getType());

    // var a = 10 instanceof bob
    DeclStmt bobStmt = (DeclStmt)statements.get(1);
    assertEquals("a", bobStmt.getName());
    InstanceofExpr bob = (InstanceofExpr)bobStmt.getInit();
    ConstExpr onezero = (ConstExpr)bob.getExpr();
    assertEquals("bob", bob.getType());
    assertEquals("10", onezero.getConstant());

    // var c = (int)(10)
    DeclStmt castStmt = (DeclStmt)statements.get(2);
    assertEquals("c", castStmt.getName());
    CastExpr cast = (CastExpr)castStmt.getInit();
    assertEquals("int", cast.getType());
    ConstExpr zeroone = (ConstExpr)cast.getExpr();
    assertEquals("10", zeroone.getConstant());

    // while
    WhileStmt whilee = (WhileStmt)statements.get(3);
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

    // for(i = 0; i < 10; i++) break;
    ForStmt forr = (ForStmt)statements.get(4);
    AssignExpr iEq0 = (AssignExpr)forr.getInitExpr();
    ConstExpr zero = (ConstExpr)iEq0.getExpr();
    BinaryExpr iLt10 = (BinaryExpr)forr.getPredExpr();
    VarExpr i2 = (VarExpr)iLt10.getLeftExpr();
    ConstExpr ten = (ConstExpr)iLt10.getRightExpr();
    UnaryExpr iPlusPlus = (UnaryExpr)forr.getUpdateExpr();
    VarExpr i3 = (VarExpr)iPlusPlus.getExpr();
    BreakStmt breakk = (BreakStmt)forr.getBodyStmt();
    assertEquals("i", iEq0.getName());
    assertEquals("0", zero.getConstant());
    assertEquals("i", i2.getName());
    assertEquals("<", iLt10.getOpName());
    assertEquals("10", ten.getConstant());
    assertEquals("i", i3.getName());
    assertEquals("++", iPlusPlus.getOpName());

    // if (true || false && true) return 10;
    IfStmt iff = (IfStmt)statements.get(5);
    BinaryExpr iffExpr = (BinaryExpr)iff.getPredExpr();
    ConstExpr iffExprLeft = (ConstExpr)iffExpr.getLeftExpr();
    BinaryExpr iffExprRight = (BinaryExpr)iffExpr.getRightExpr();
    ConstExpr iffExprRightLeft = (ConstExpr)iffExprRight.getLeftExpr();
    ConstExpr iffExprRightRight = (ConstExpr)iffExprRight.getRightExpr();
    assertEquals("true", iffExprLeft.getConstant());
    assertEquals("||", iffExpr.getOpName());
    assertEquals("false", iffExprRightLeft.getConstant());
    assertEquals("&&", iffExprRight.getOpName());
    assertEquals("true", iffExprRightRight.getConstant());
    ReturnStmt ret10 = (ReturnStmt)iff.getThenStmt();
    ConstExpr retd10 = (ConstExpr)ret10.getExpr();
    assertEquals("10", retd10.getConstant());

    // else return 9;
    ReturnStmt ret9 = (ReturnStmt)iff.getElseStmt();
    ConstExpr retd9 = (ConstExpr)ret9.getExpr();
    assertEquals("9", retd9.getConstant());
  }
}
