/*
 * File: bantam.lexer.Scanner.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the Scanner, which tokenizes the source file
 */

package proj9AbramsDeutschDurstJones.bantam.lexer;

import proj9AbramsDeutschDurstJones.bantam.util.Error;
import proj9AbramsDeutschDurstJones.bantam.util.ErrorHandler;


import java.io.Reader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class handles tokenizing a Java file, returning the next token when scan is called
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private char currentChar;
    private String currentSpelling;

    private static Set<Character> punctuation = Stream.of('.', ';', ':', ',').collect(Collectors.toSet());
    private static Set<Character> escapeCharacters = Stream.of('t', 'b', 'n', 'r', 'f', '\'',
            '"', '\\').collect(Collectors.toSet());
    private static Set<Character> brackets = Stream.of('(', ')', '{', '}', '[', ']').collect(Collectors.toSet());
    private static Set<Character> operators = Stream.of('+', '-','/', '=', '<', '>', '&', '|',
            '*', '%', '!', '^').collect(Collectors.toSet());

    /**
     * Constructor just getting the error handler
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = null;
    }

    /**
     * Constructor getting the file to tokenize and the error handler
     * @param filename the path to the file to tokenize
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
    }

    /**
     * Constructor getting a file reader and the error handler
     * @param reader the reader to initialize the SourceFile object with
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        errorHandler = handler;
        sourceFile = new SourceFile(reader);
    }

    public Token scan()
    {
        // initialize the spelling and kind of the token
        currentSpelling = "";
        Token.Kind kind;

        // initialize current char if this is the start of the file
        if (currentChar == ' ') {
            currentChar = sourceFile.getNextChar();
        }

        // munch whitespace
        while (Character.isWhitespace(currentChar)) {
            currentChar = sourceFile.getNextChar();
        }

        // identifier
        if (Character.isLetter(currentChar)) {
            kind = handleIdentifier();

        }
        // integer
        else if (Character.isDigit(currentChar)) {
            kind = handleInteger();
        }
        // string
        else if (currentChar == '"') {
            kind = handleString();
        }
        // punctuation
        else if (punctuation.contains(currentChar)) {
            kind = handlePunctuation();
        }
        // brackets
        else if (brackets.contains(currentChar)) {
            kind = handleBrace();
        }
        else if (operators.contains(currentChar)) {
            kind = handleOperator();
        }
        // EOF
        else if (currentChar == SourceFile.eof) {
            currentSpelling = "EOF";
            kind = Token.Kind.EOF;
        }
        // unsupported characters
        else {
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                    "Unsupported character");
            currentSpelling = Character.toString(currentChar);
            currentChar = sourceFile.getNextChar();
            kind = Token.Kind.ERROR;
        }

        // generate the token
        return new Token(kind, currentSpelling, sourceFile.getCurrentLineNumber());
    }

    private Token.Kind handleIdentifier() {
        while (Character.isLetterOrDigit(currentChar) || currentChar == '_') {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
        }
        return Token.Kind.IDENTIFIER;
    }

    private Token.Kind handleInteger() {
        while (Character.isDigit(currentChar)) {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
        }
        if (Integer.parseInt(currentSpelling) > (Math.pow(2, 31) - 1)) {
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                    "Integer constant too large");
            return Token.Kind.ERROR;
        }
        return Token.Kind.INTCONST;
    }

    private Token.Kind handleString() {
        currentSpelling += currentChar;
        currentChar = sourceFile.getNextChar();

        while (currentChar != '"') {
            currentSpelling += currentChar;
            // if escape character, add the next character to the string and continue
            if (currentChar == '\\') {
                currentChar = sourceFile.getNextChar();
                // check if escape character is supported
                if (!escapeCharacters.contains(currentChar)) {
                    errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                            sourceFile.getCurrentLineNumber(), "Unsupported escape character");
                    return Token.Kind.ERROR;
                }
                currentSpelling += currentChar;
            }
            // if eof or eol, string is invalid
            if (currentChar == SourceFile.eof || currentChar == SourceFile.eol) {
                errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                        "Unterminated String");
                return Token.Kind.ERROR;
            }
            currentChar = sourceFile.getNextChar();
        }

        // error if string greater than 5000 characters (not including start and end quotes)
        if (currentSpelling.length() > 5002) {
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                    "String constant too long");
            return Token.Kind.ERROR;
        }
        currentSpelling += currentChar;
        currentChar = sourceFile.getNextChar();
        return Token.Kind.STRCONST;
    }

    private Token.Kind handlePunctuation() {
        currentSpelling += currentChar;
        Token.Kind kind;
        switch(currentChar) {
            case '.':
                kind = Token.Kind.DOT;
                break;
            case ',':
                kind = Token.Kind.COMMA;
                break;
            case ';':
                kind = Token.Kind.SEMICOLON;
                break;
            default:
                kind = Token.Kind.COLON;
        }
        currentChar = sourceFile.getNextChar();
        return kind;
    }

    private Token.Kind handleBrace() {
        currentSpelling += currentChar;
        Token.Kind kind;
        switch (currentChar) {
            case '(':
                kind = Token.Kind.RPAREN;
                break;
            case ')':
                kind = Token.Kind.LPAREN;
                break;
            case '{':
                kind = Token.Kind.RCURLY;
                break;
            case '}':
                kind = Token.Kind.LCURLY;
                break;
            case '[':
                kind = Token.Kind.LBRACKET;
                break;
            default:
                kind = Token.Kind.RBRACKET;
        }
        currentChar = sourceFile.getNextChar();
        return kind;
    }

    private Token.Kind handleOperator() {
        currentSpelling += currentChar;
        Token.Kind kind;
        switch (currentChar) {
            case '*':
                kind = Token.Kind.MULDIV;
                currentChar = sourceFile.getNextChar();
                break;
            case '%':
                kind =Token.Kind.MULDIV;
                currentChar = sourceFile.getNextChar();
                break;
            case '^':
                kind = Token.Kind.MULDIV;
                currentChar = sourceFile.getNextChar();
                break;
            case '+':
                if (isRepeated()) {
                    kind = Token.Kind.UNARYINCR;
                }
                else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '-':
                if (isRepeated()) {
                    kind = Token.Kind.UNARYDECR;
                }
                else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '=':
                if (isRepeated()) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.ASSIGN;
                }
                break;
            case '&':
                if (isRepeated()) {
                    kind = Token.Kind.BINARYLOGIC;
                }
                else {
                    errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                            sourceFile.getCurrentLineNumber(), "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '|':
                if (isRepeated()) {
                    kind = Token.Kind.BINARYLOGIC;
                }
                else {
                    errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                            sourceFile.getCurrentLineNumber(), "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '!':
                if (isFollowedByEquals()) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind= Token.Kind.UNARYNOT;
                }
                break;
            case '<':
                if (isFollowedByEquals()) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            case '>':
                if (isFollowedByEquals()) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            default: kind = handleForwardSlash();
        }
        return kind;
    }

    private Token.Kind handleForwardSlash() {
        currentChar = sourceFile.getNextChar();
        if (currentChar == '/') {
            return handleSingleLineComment();
        }
        else if (currentChar == '*') {
            return handleMultiLineComment();
        }
        else {
            return Token.Kind.MULDIV;
        }
    }

    private Token.Kind handleSingleLineComment() {
        while (currentChar != SourceFile.eol && currentChar != SourceFile.eof) {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
        }
        return Token.Kind.COMMENT;
    }

    private Token.Kind handleMultiLineComment() {
        while (currentChar != SourceFile.eof) {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
            if (currentChar == '*') {
                currentSpelling += currentChar;
                currentChar = sourceFile.getNextChar();
                if (currentChar == '/') {
                    currentSpelling += currentChar;
                    currentChar = sourceFile.getNextChar();
                    return Token.Kind.COMMENT;
                }
            }
        }
        errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                "Unterminated block comment");
        return Token.Kind.ERROR;
    }

    private boolean isRepeated() {
        char checkChar = currentChar;
        currentChar = sourceFile.getNextChar();
        if (checkChar == currentChar) {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
            return true;
        }
        return false;
    }

    private boolean isFollowedByEquals() {
        currentChar = sourceFile.getNextChar();
        if (currentChar == '=') {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter files to scan");
        }

        ErrorHandler errorHandler = new ErrorHandler();
        for (int i = 0; i < args.length; i++) {
            Scanner scanner = new Scanner(args[i], errorHandler);
            System.out.println(args[i]);
            Token token = scanner.scan();
            while (token.kind != Token.Kind.EOF) {
                System.out.println(token.toString());
                token = scanner.scan();
            }
            System.out.println(token.toString());
            if (errorHandler.errorsFound()) {
                System.out.println(errorHandler.getErrorList().size() + " illegal tokens");
            }
            else {
                System.out.println("Scanning successful");
            }
            errorHandler.clear();
        }
    }
}
