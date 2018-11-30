/*
 * Authors: Haoyu Song and Dale Skrien
 * Date: Spring and Summer, 2018
 *
 * In the grammar below, the variables are enclosed in angle brackets.
 * The notation "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * All other symbols in the rules are terminals.
 * EMPTY indicates a rule with an empty right hand side.
 * All other terminal symbols that are in all caps correspond to keywords.
 */
package proj10AbramsDeutschDurstJones.bantam.parser;

import static proj10AbramsDeutschDurstJones.bantam.lexer.Token.Kind.*;
import proj10AbramsDeutschDurstJones.bantam.ast.*;
import proj10AbramsDeutschDurstJones.bantam.lexer.*;
import proj10AbramsDeutschDurstJones.bantam.util.*;
import proj10AbramsDeutschDurstJones.bantam.util.Error;

/**
 * This class constructs an AST from a legal Bantam Java program.  If the
 * program is illegal, then one or more error messages are displayed.
 */
public class Parser
{
    // instance variables
    private Scanner scanner;
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler;
    private String fileName;

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * parse the given file and return the root node of the AST
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) throws CompilationException {
        this.fileName = filename;
        this.scanner = new Scanner(filename, this.errorHandler);
        this.currentToken = this.scanner.scan();
        return parseProgram();
    }


    /*
     * <Program> ::= <Class> | <Class> <Program>
     */
    private Program parseProgram() throws CompilationException {
        int position = currentToken.position;
        ClassList classList = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            classList.addElement(aClass);
        }

        return new Program(position, classList);
    }


    /*
     * <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
     * <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
     * <MemberList> ::= EMPTY | <Member> <MemberList>
     */
    private Class_ parseClass() throws CompilationException {
        int position = currentToken.position;

        // handle CLASS
        if (currentToken.kind != CLASS) {
            registerError("Missing class");
        }
        // handle name
        advance();
        String name = parseIdentifier();
        // handle extends clause
        String parent = null;
        if (currentToken.kind == EXTENDS) {
            advance();
            parent = parseIdentifier();
        }
        // handle member list
        MemberList memberList = new MemberList(position);
        if (currentToken.kind != LCURLY) {
            registerError("Missing {");
        }
        advance();
        while (currentToken.kind != RCURLY) {
            Member aMember = parseMember();
            memberList.addElement(aMember);
        }
        advance();

        return new Class_(position, fileName, name, parent, memberList);
    }


    /* Fields and Methods
     * <Member> ::= <Field> | <Method>
     * <Method> ::= <Type> <Identifier> ( <Parameters> ) <Block>
     * <Field> ::= <Type> <Identifier> <InitialValue> ;
     * <InitialValue> ::= EMPTY | = <Expression>
     */
    private Member parseMember() throws CompilationException {
        int position = currentToken.position;

        String type = parseType();
        String name = parseIdentifier();

        // handle method
        if (currentToken.kind == LPAREN) {
            advance();
            FormalList formalList = parseParameters();
            if (currentToken.kind != RPAREN) {
                registerError("Missing )");
            }
            advance();
            StmtList stmtList = ((BlockStmt) parseBlock()).getStmtList();
            return new Method(position, type, name, formalList, stmtList);
        }

        // handle field
        Expr init = null;
        if (currentToken.kind == ASSIGN) {
            advance();
            init = parseExpression();
        }
        if (currentToken.kind != SEMICOLON) {
            registerError("Missing ;");
        }
        advance();

        return new Field(position, type, name, init);
    }


    //-----------------------------------

    /* Statements
     *  <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <DeclStmt>
     *              | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
     */
    private Stmt parseStatement() throws CompilationException {
        Stmt stmt;

        switch (currentToken.kind) {
            case IF:
                stmt = parseIf();
                break;
            case LCURLY:
                stmt = parseBlock();
                break;
            case VAR:
                stmt = parseDeclStmt();
                break;
            case RETURN:
                stmt = parseReturn();
                break;
            case FOR:
                stmt = parseFor();
                break;
            case WHILE:
                stmt = parseWhile();
                break;
            case BREAK:
                stmt = parseBreak();
                break;
            default:
                stmt = parseExpressionStmt();
        }

        return stmt;
    }


    /*
     * <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
     */
    private Stmt parseWhile() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != WHILE) {
            registerError("Missing while");
        }
        advance();

        if (currentToken.kind != LPAREN) {
            registerError("Missing (");
        }
        advance();
        Expr expr = parseExpression();
        if (currentToken.kind != RPAREN) {
            registerError("Missing )");
        }
        advance();

        Stmt stmt = parseStatement();

        return new WhileStmt(position, expr, stmt);
    }


    /*
     * <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
     */
    private Stmt parseReturn() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != RETURN) {
            registerError("Missing return");
        }
        advance();

        Expr expr = parseExpression();
        if (currentToken.kind != SEMICOLON) {
            registerError("Missing ;");
        }
        advance();

        return new ReturnStmt(position, expr);
    }


    /*
     * BreakStmt> ::= BREAK ;
     */
    private Stmt parseBreak() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != BREAK) {
            registerError("Missing break");
        }
        advance();
        if (currentToken.kind != SEMICOLON) {
            registerError("Missing ;");
        }
        advance();

        return new BreakStmt(position);
    }


    /*
     * <ExpressionStmt> ::= <Expression> ;
     */
    private ExprStmt parseExpressionStmt() throws CompilationException {
        int position = currentToken.position;

        Expr expr = parseExpression();
        if (currentToken.kind != SEMICOLON) {
            registerError("Missing ;");
        }
        advance();

        return new ExprStmt(position, expr);
    }


    /*
     * <DeclStmt> ::= VAR <Identifier> = <Expression> ;
     * every local variable must be initialized
     */
    private Stmt parseDeclStmt() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != VAR) {
            registerError("Missing var");
        }
        advance();

        String name = parseIdentifier();

        if (currentToken.kind != ASSIGN) {
            registerError("Missing =");
        }
        advance();

        Expr initExpr = parseExpression();

        if (currentToken.kind != SEMICOLON) {
            registerError("Missing ;");
        }
        advance();

        return new DeclStmt(position, name, initExpr);
    }


    /*
     * <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
     * <Start>     ::= EMPTY | <Expression>
     * <Terminate> ::= EMPTY | <Expression>
     * <Increment> ::= EMPTY | <Expression>
     */
    private Stmt parseFor() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != FOR) {
            registerError("Missing for");
        }
        advance();

        if (currentToken.kind != LPAREN) {
            registerError("Missing (");
        }
        advance();

        Expr start = null;
        if (currentToken.kind != SEMICOLON) {
            start = parseExpression();
            if (currentToken.kind != SEMICOLON) {
                registerError("Missing ;");
            }
        }
        advance();

        Expr terminate = null;
        if (currentToken.kind != SEMICOLON) {
            terminate = parseExpression();
            if (currentToken.kind != SEMICOLON) {
                registerError("Missing ;");
            }
        }
        advance();

        Expr increment = null;
        if (currentToken.kind != SEMICOLON) {
            increment = parseExpression();
            if (currentToken.kind != SEMICOLON) {
                registerError("Missing ;");
            }
        }
        advance();

        if (currentToken.kind != RPAREN) {
            registerError("Missing )");
        }
        advance();

        Stmt stmt = parseStatement();

        return new ForStmt(position, start, terminate, increment, stmt);
    }


    /*
     * <BlockStmt> ::= { <Body> }
     * <Body> ::= EMPTY | <Stmt> <Body>
     */
    private Stmt parseBlock() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != LCURLY) {
            registerError("Missing {");
        }
        advance();

        StmtList stmtList = new StmtList(position);

        while (currentToken.kind != RCURLY) {
            Stmt aStmt = parseStatement();
            stmtList.addElement(aStmt);
        }
        advance();

        return new BlockStmt(position, stmtList);
    }


    /*
     * <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
     */
    private Stmt parseIf() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != IF) {
            registerError("Missing if");
        }
        advance();

        if (currentToken.kind != LPAREN) {
            registerError("Missing (");
        }
        advance();

        Expr predExpr = parseExpression();
        if (currentToken.kind != RPAREN) {
            registerError("Missing )");
        }
        advance();

        Stmt thenStmt = parseStatement();

        Stmt elseStmt = null;
        if (currentToken.kind == ELSE) {
            advance();
            elseStmt = parseStatement();
        }

        return new IfStmt(position, predExpr, thenStmt, elseStmt);
    }


    //-----------------------------------------
    // Expressions
    //Here we introduce the precedence to operations

    /*
     * <Expression> ::= <LogicalOrExpr> <OptionalAssignment>
     * <OptionalAssignment> ::= EMPTY | = <Expression>
     */
    private Expr parseExpression() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseOrExpr();
        if (currentToken.kind == ASSIGN) {
            advance();
            Expr right = parseExpression();
            // get variable being assigned
            if (!(left instanceof VarExpr)) {
                registerError("Expected identifier");
            }
            VarExpr leftVar = (VarExpr) left;
            // get reference to variable
            String leftRef = null;
            if (leftVar.getRef() != null) {
                if (!(leftVar.getRef() instanceof VarExpr)) {
                    registerError("Expected reference identifier");
                }
                leftRef = ((VarExpr) leftVar.getRef()).getName();
            }

            left = new AssignExpr(position, leftRef, leftVar.getName(), right);
        }

        return left;
    }


    /*
     * <LogicalOR> ::= <logicalAND> <LogicalORRest>
     * <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
     */
    private Expr parseOrExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseAndExpr();
        while (currentToken.spelling.equals("||")) {
            advance();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
    }


    /*
     * <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
     * <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
     */
    private Expr parseAndExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseEqualityExpr();
        while (currentToken.spelling.equals("&&")) {
            advance();
            Expr right = parseEqualityExpr();
            left = new BinaryLogicAndExpr(position, left, right);
        }

        return left;
    }


    /*
     * <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
     *                     <RelationalExpr>
     * <equalOrNotEqual> ::=  == | !=
     */
    private Expr parseEqualityExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseRelationalExpr();

        if (currentToken.spelling.equals("==")) {
            advance();
            Expr right = parseRelationalExpr();
            left = new BinaryCompEqExpr(position, left, right);
        }
        else if (currentToken.spelling.equals("!=")) {
            advance();
            Expr right = parseRelationalExpr();
            left = new BinaryCompNeExpr(position, left, right);
        }

        return left;
    }


    /*
     * <RelationalExpr> ::=<AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
     * <ComparisonOp> ::=  < | > | <= | >= | INSTANCEOF
     */
    private Expr parseRelationalExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseAddExpr();
        if (currentToken.kind == COMPARE) {
            String comparisonOp = currentToken.spelling;
            advance();
            Expr right = parseAddExpr();
            switch(comparisonOp) {
                case "<":
                    left = new BinaryCompLtExpr(position, left, right);
                    break;
                case ">":
                    left = new BinaryCompGtExpr(position, left, right);
                    break;
                case "<=":
                    left = new BinaryCompLeqExpr(position, left, right);
                    break;
                case ">=":
                    left = new BinaryCompGeqExpr(position, left, right);
                    break;
                default:
                    if (!(right instanceof VarExpr)) {
                        registerError("Expected variable name");
                    }
                    left = new InstanceofExpr(position, left, ((VarExpr) right).getName());
            }
        }

        return left;
    }


    /*
     * <AddExpr>::＝ <MultExpr> <MoreMultExpr>
     * <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
     */
    private Expr parseAddExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseMultExpr();

        while (currentToken.kind == PLUSMINUS) {
            String op = currentToken.spelling;
            advance();
            Expr right = parseMultExpr();
            switch(op) {
                case "+":
                    left = new BinaryArithPlusExpr(position, left, right);
                    break;
                case "-":
                    left = new BinaryArithMinusExpr(position, left, right);
                    break;
            }
        }

        return left;
    }


    /*
     * <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
     * <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
     *               / <NewCastOrUnary> <MoreNCU> |
     *               % <NewCastOrUnary> <MoreNCU> |
     *               EMPTY
     */
    private Expr parseMultExpr() throws CompilationException {
        int position = currentToken.position;

        Expr left = parseNewCastOrUnary();

        while (currentToken.kind == MULDIV) {
            String op = currentToken.spelling;
            advance();
            Expr right = parseNewCastOrUnary();
            switch(op) {
                case "*":
                    left = new BinaryArithTimesExpr(position, left, right);
                    break;
                case "/":
                    left = new BinaryArithDivideExpr(position, left, right);
                    break;
                case "%":
                    left = new BinaryArithModulusExpr(position, left, right);
                    break;
            }
        }

        return left;
    }

    /*
     * <NewCastOrUnary> ::= < NewExpression> | <CastExpression> | <UnaryPrefix>
     */
    private Expr parseNewCastOrUnary() throws CompilationException {
        Expr expr;

        switch(currentToken.kind) {
            case NEW:
                expr = parseNew();
                break;
            case CAST:
                expr = parseCast();
                break;
            default:
                expr = parseUnaryPrefix();
                break;
        }

        return expr;
    }


    /*
     * <NewExpression> ::= NEW <Identifier> ( ) | NEW <Identifier> [ <Expression> ]
     */
    private Expr parseNew() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != NEW) {
            registerError("Missing new");
        }
        advance();

        String identifier = parseIdentifier();

        if (currentToken.kind == LPAREN) {
            advance();
            if (currentToken.kind != RPAREN) {
                registerError("Missing )");
            }
            registerError("Missing (");
        } else if (currentToken.kind != LBRACKET) {
            advance();
            parseExpression();
            if (currentToken.kind == RBRACKET) {
                registerError("Missing ]");
            }
        } else {
            registerError("Missing ( or [");
        }

        return new NewExpr(position, identifier);
    }


    /*
     * <CastExpression> ::= CAST ( <Type> , <Expression> )
     */
    private Expr parseCast() throws CompilationException {
        int position = currentToken.position;

        if (currentToken.kind != LPAREN) {
            registerError("Missing (");
        }
        advance();

        String type = parseType();

        if (currentToken.kind != COMMA) {
            registerError("Missing ,");
        }
        advance();

        Expr expr = parseExpression();

        if (currentToken.kind != RPAREN) {
            registerError("Missing )");
        }
        advance();

        return new CastExpr(position, type, expr);
    }


    /*
     * <UnaryPrefix> ::= <PrefixOp> <UnaryPrefix> | <UnaryPostfix>
     * <PrefixOp> ::= - | ! | ++ | --
     */
    private Expr parseUnaryPrefix() throws CompilationException {
        int position = currentToken.position;

        if (!currentToken.spelling.equals("-") &&
                !currentToken.spelling.equals("!") &&
                !currentToken.spelling.equals("++") &&
                !currentToken.spelling.equals("--" )) {
            return parseUnaryPostfix();
        }

        String op = currentToken.spelling;
        advance();
        switch(op) {
            case "-":
                return new UnaryNegExpr(position, parseUnaryPrefix());
            case "!":
                return new UnaryNotExpr(position, parseUnaryPrefix());
            case "++":
                return new UnaryIncrExpr(position, parseUnaryPrefix(), false);
            default:
                return new UnaryDecrExpr(position, parseUnaryPrefix(), false);
        }
    }

    /*
     * <UnaryPostfix> ::= <Primary> <PostfixOp>
     * <PostfixOp> ::= ++ | -- | EMPTY
     */
    private Expr parseUnaryPostfix() throws CompilationException {
        int position = currentToken.position;

        Expr expr = parsePrimary();
        if (currentToken.spelling.equals("++")) {
            return new UnaryIncrExpr(position, expr, true);
        }
        if (currentToken.spelling.equals("--")) {
            return new UnaryDecrExpr(position, expr, true);
        }

        return expr;
    }


    /*
     * <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> | <VarExpr> | <DispatchExpr>
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
    private Expr parsePrimary() throws CompilationException {
        int position = currentToken.position;

        switch(currentToken.kind) {
            case LPAREN:
                advance();
                Expr expr = parseExpression();
                if (currentToken.kind != RPAREN) {
                    registerError("Missing )");
                }
                return expr;
            case INTCONST:
                return parseIntConst();
            case BOOLEAN:
                return parseBoolean();
            case STRCONST:
                return parseStringConst();
            default:
                Expr varOrDispatch;
                // get prefix for var or dispatch
                Expr prefix = null;
                if (currentToken.spelling.equals("SUPER") ||
                currentToken.spelling.equals("THIS")) {
                    prefix = new VarExpr(position, null, currentToken.spelling);
                    advance();
                    if (this.currentToken.kind != DOT) {
                        registerError("Missing .");
                    }
                    advance();
                }
                else if (currentToken.kind != IDENTIFIER) {
                    prefix = parsePrimary();
                    if (this.currentToken.kind != DOT) {
                        registerError("Missing .");
                    }
                    advance();
                }

                String name = parseIdentifier();

                // parse dispatch expression
                if (currentToken.kind == LPAREN) {
                    ExprList exprList = parseArguments();
                    if (currentToken.kind != RPAREN) {
                        registerError("Missing )");
                    }
                    varOrDispatch = new DispatchExpr(position, prefix, name, exprList);
                }

                // parse var expression
                else {
                    if (currentToken.kind == LBRACKET) {
                        parseExpression();
                        if (currentToken.kind != RBRACKET) {
                            registerError("Missing ]");
                        }
                    }
                    varOrDispatch = new VarExpr(position, prefix, name);
                }
                return varOrDispatch;
        }
    }


    /*
     * <Arguments> ::= EMPTY | <Expression> <MoreArgs>
     * <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
     */
    private ExprList parseArguments() throws CompilationException {
        int position = currentToken.position;
        ExprList exprList = new ExprList(position);

        while (currentToken.kind == COMMA) {
            Expr expr = parseExpression();
            exprList.addElement(expr);
        }

        return exprList;
    }


    /*
     * <Parameters>  ::= EMPTY | <Formal> <MoreFormals>
     * <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
     */
    private FormalList parseParameters() throws CompilationException {
        int position = currentToken.position;

        FormalList formalList = new FormalList(position);
        while (currentToken.kind == COMMA) {
            Formal formal = parseFormal();
            formalList.addElement(formal);
        }
        return formalList;
    }


    /*
     * <Formal> ::= <Type> <Identifier>
     */
    private Formal parseFormal() throws CompilationException {
        int position = currentToken.position;

        String type = parseType();
        String identifier = parseIdentifier();

        return new Formal(position, type, identifier);
    }


    /*
     * <Type> ::= <Identifier> <Brackets>
     * <Brackets> ::= EMPTY | [ ]
     */
    private String parseType() throws CompilationException {
        String identifier = parseIdentifier();
        if (currentToken.kind == LBRACKET){
            advance();
            if (currentToken.kind != RBRACKET){
                registerError("Missing ]");
            }
            advance();
        }
        return identifier;
    }


    //----------------------------------------
    //Terminals

    private String parseOperator() {
         String spelling = currentToken.spelling;
         advance();
         return spelling;
    }


    private String parseIdentifier() {
        String spelling = currentToken.spelling;
        advance();
        return spelling;
    }


    private ConstStringExpr parseStringConst() {
         int position = currentToken.position;
         String spelling = currentToken.spelling;
         advance();
         return new ConstStringExpr(position, spelling);
    }


    private ConstIntExpr parseIntConst() {
         int position = currentToken.position;
         String spelling = currentToken.spelling;
         advance();
         return new ConstIntExpr(position, spelling);
    }


    private ConstBooleanExpr parseBoolean() {
         int position = currentToken.position;
         String spelling = currentToken.spelling;
         advance();
         return new ConstBooleanExpr(position, spelling);
    }


    private void registerError(String errorMessage) throws CompilationException {
         errorHandler.register(Error.Kind.PARSE_ERROR, fileName,
                     currentToken.position, errorMessage);
         throw new CompilationException(errorMessage);
    }

    private void advance() {
         currentToken = scanner.scan();
         // cycle through comments
         while (currentToken.kind == COMMENT) {
             currentToken = scanner.scan();
         }
    }
}