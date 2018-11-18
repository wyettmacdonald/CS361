package proj9AbramsDeutschDurstJones.bantam.tests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import proj9AbramsDeutschDurstJones.bantam.lexer.Scanner;
import proj9AbramsDeutschDurstJones.bantam.lexer.Token;
import proj9AbramsDeutschDurstJones.bantam.lexer.Token.Kind;
import proj9AbramsDeutschDurstJones.bantam.util.Error;
import proj9AbramsDeutschDurstJones.bantam.util.ErrorHandler;

public class TestHelloWorld {
    static Scanner scanner;
    static ErrorHandler errorHandler;

    // called before any tests are executed
    @BeforeClass
    static public void startTest() {
      errorHandler = new ErrorHandler();
      String filepath = new File("").getAbsolutePath();
      filepath = filepath.concat("/proj9AbramsDeutschDurstJones/bantam/tests/test_bantam_files/HelloWorld.java"); 
      scanner = new Scanner(filepath, errorHandler);
    }

    @Test
    public void scanFile() {
      class Testobj {
        Token.Kind kind;
        String spelling;

        Testobj(Token.Kind k, String s) {
          this.kind = k;
          this.spelling = s;
        }
      }
      
     Testobj[] expected = {
      new Testobj(Token.Kind.IDENTIFIER, "public"),
      new Testobj(Token.Kind.CLASS, "class"),
      new Testobj(Token.Kind.IDENTIFIER, "HelloWorld"),
      new Testobj(Token.Kind.LCURLY, "{"),
      new Testobj(Token.Kind.IDENTIFIER, "public"),
      new Testobj(Token.Kind.IDENTIFIER, "static"),
      new Testobj(Token.Kind.IDENTIFIER, "void"),
      new Testobj(Token.Kind.IDENTIFIER, "main"),
      new Testobj(Token.Kind.LPAREN, "("),
      new Testobj(Token.Kind.IDENTIFIER, "String"),
      new Testobj(Token.Kind.LBRACKET, "["),
      new Testobj(Token.Kind.RBRACKET, "]"),
      new Testobj(Token.Kind.IDENTIFIER, "args"),
      new Testobj(Token.Kind.RPAREN, ")"),
      new Testobj(Token.Kind.LCURLY, "{"),
      new Testobj(Token.Kind.IDENTIFIER, "int"),
      new Testobj(Token.Kind.IDENTIFIER, "b"),
      new Testobj(Token.Kind.ASSIGN, "="),
      new Testobj(Token.Kind.INTCONST, "2"),
      new Testobj(Token.Kind.PLUSMINUS, "+"),
      new Testobj(Token.Kind.INTCONST, "2"),
      new Testobj(Token.Kind.SEMICOLON, ";"),
      new Testobj(Token.Kind.IDENTIFIER, "boolean"),
      new Testobj(Token.Kind.IDENTIFIER, "f"),
      new Testobj(Token.Kind.ASSIGN, "="),
      new Testobj(Token.Kind.BOOLEAN, "true"),
      new Testobj(Token.Kind.SEMICOLON, ";"),
      new Testobj(Token.Kind.COMMENT, "// begin fake java"),
      new Testobj(Token.Kind.BINARYLOGIC, "&&"),
      new Testobj(Token.Kind.MULDIV, "*"),
      new Testobj(Token.Kind.COMPARE, ">"),
      new Testobj(Token.Kind.UNARYINCR, "++"),
      new Testobj(Token.Kind.UNARYDECR, "--"),
      new Testobj(Token.Kind.UNARYNOT, "!"),
      new Testobj(Token.Kind.COMMA, ","),
      new Testobj(Token.Kind.BREAK, "break"),
      new Testobj(Token.Kind.CAST, "cast"),
      new Testobj(Token.Kind.COMMENT, "/*return to real java*/"),
      new Testobj(Token.Kind.IDENTIFIER, "String"),
      new Testobj(Token.Kind.IDENTIFIER, "s"),
      new Testobj(Token.Kind.ASSIGN, "="),
      new Testobj(Token.Kind.ERROR, "\"\n"),
      new Testobj(Token.Kind.ERROR, "\";\n"),
      new Testobj(Token.Kind.IDENTIFIER, "System"),
      new Testobj(Token.Kind.DOT, "."),
      new Testobj(Token.Kind.IDENTIFIER, "out"),
      new Testobj(Token.Kind.DOT, "."),
      new Testobj(Token.Kind.IDENTIFIER, "println"),
      new Testobj(Token.Kind.LPAREN, "("),
      new Testobj(Token.Kind.STRCONST, "\"Hello World\""),
      new Testobj(Token.Kind.RPAREN, ")"),
      new Testobj(Token.Kind.SEMICOLON, ";"),
      new Testobj(Token.Kind.RCURLY, "}"),
      new Testobj(Token.Kind.RCURLY, "}"),
     };
     
     Token token;
     for (Testobj to : expected) {
      token = scanner.scan();
      System.out.printf("Expected: %s and Received: %s\n", to.spelling, token.spelling);
      assertEquals(to.spelling, token.spelling);
      assertEquals(to.kind, token.kind);
     }
    }
}

