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

    private static Set<Character> punctuation = Stream.of('.', ';', ':', ',').collect(Collectors.toSet());
    private static Set<Character> escapeCharacters = Stream.of('t', 'b', 'n', 'r', 'f', '\'',
            '"', '\\').collect(Collectors.toSet());
    private static Set<Character> brackets = Stream.of('(', ')', '{', '}', '[', ']').collect(Collectors.toSet());
    private static Set<Character> operators = Stream.of('+', '-','/', '=', '<', '>', '&', '|',
            '*', '%', '!', '^').collect(Collectors.toSet());

    /**
     * Constructor just taking the error handler
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(ErrorHandler handler) {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = null;
    }

    /**
     * Constructor taking the file to tokenize and the error handler
     * @param filename the path to the file to tokenize
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(String filename, ErrorHandler handler) {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = new SourceFile(filename);
    }

    /**
     * Constructor taking a file reader and the error handler
     * @param reader the reader to initialize the SourceFile object with
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        this.errorHandler = handler;
        this.sourceFile = new SourceFile(reader);
    }

    public Token scan()
    {
        // go to next meaningful character
        this.goToNonWhitespaceChar();

        // initialize the spelling, kind and position of the token
        StringBuilder spelling = new StringBuilder();
        Token.Kind kind;
        int position = this.sourceFile.getCurrentLineNumber();

        // identifier
        if (Character.isLetter(this.currentChar)) {
            kind = this.handleIdentifier(spelling);
        }
        // integer
        else if (Character.isDigit(this.currentChar)) {
            kind = this.handleInteger(spelling);
        }
        // string
        else if (this.currentChar == '"') {
            kind = this.handleString(spelling);
        }
        // punctuation
        else if (punctuation.contains(this.currentChar)) {
            kind = this.handlePunctuation(spelling);
        }
        // brackets
        else if (brackets.contains(this.currentChar)) {
            kind = this.handleBrace(spelling);
        }
        // operators
        else if (operators.contains(this.currentChar)) {
            kind = this.handleOperator(spelling);
        }
        // EOF
        else if (this.currentChar == SourceFile.eof) {
            kind = this.handleEOF(spelling);
        }
        // unsupported characters
        else {
            kind = this.handleUnsupportedChar(spelling);
        }

        // generate the token
        return new Token(kind, spelling.toString(), position);
    }

    private Token.Kind handleIdentifier(StringBuilder spelling) {
        while (Character.isLetterOrDigit(this.currentChar) || this.currentChar == '_') {
            this.appendAndAdvance(spelling);
        }
        return Token.Kind.IDENTIFIER;
    }

    private Token.Kind handleInteger(StringBuilder spelling) {
        int start = this.sourceFile.getCurrentLineNumber();

        while (Character.isDigit(this.currentChar)) {
            this.appendAndAdvance(spelling);
        }
        if (Long.parseLong(spelling.toString()) > Integer.MAX_VALUE) {
            this.registerError(start, "Integer constant too large");
            return Token.Kind.ERROR;
        }
        return Token.Kind.INTCONST;
    }

    private Token.Kind handleString(StringBuilder spelling) {
        int start = this.sourceFile.getCurrentLineNumber();
        this.appendAndAdvance(spelling);

        while (this.currentChar != '"') {
            // if escape character, add the next character to the string and continue
            if (this.currentChar == '\\') {
                this.appendAndAdvance(spelling);
                // check if escape character is supported
                if (!escapeCharacters.contains(this.currentChar)) {
                    this.appendAndAdvance(spelling);
                    this.registerError(start, "Unsupported escape character");
                    return Token.Kind.ERROR;
                }
            }
            this.appendAndAdvance(spelling);

            // if eof or eol, string is invalid
            if (this.currentChar == SourceFile.eof || this.currentChar == SourceFile.eol) {
                this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                        start, "Unterminated String");
                return Token.Kind.ERROR;
            }
        }

        // error if string greater than 5000 characters (not including start and end quotes)
        if (spelling.length() > 5002) {
            this.registerError(start, "String constant too long");
            return Token.Kind.ERROR;
        }
        this.appendAndAdvance(spelling);
        return Token.Kind.STRCONST;
    }

    private Token.Kind handlePunctuation(StringBuilder spelling) {
        Token.Kind kind;
        switch(this.currentChar) {
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
        this.appendAndAdvance(spelling);
        return kind;
    }

    private Token.Kind handleBrace(StringBuilder spelling) {
        Token.Kind kind;
        switch (this.currentChar) {
            case '(':
                kind = Token.Kind.LPAREN;
                break;
            case ')':
                kind = Token.Kind.RPAREN;
                break;
            case '{':
                kind = Token.Kind.LCURLY;
                break;
            case '}':
                kind = Token.Kind.RCURLY;
                break;
            case '[':
                kind = Token.Kind.LBRACKET;
                break;
            default:
                kind = Token.Kind.RBRACKET;
        }
        this.appendAndAdvance(spelling);
        return kind;
    }

    private Token.Kind handleOperator(StringBuilder spelling) {
        int start = this.sourceFile.getCurrentLineNumber();

        Token.Kind kind;
        switch (this.currentChar) {
            case '*':
                kind = Token.Kind.MULDIV;
                this.appendAndAdvance(spelling);
                break;
            case '%':
                kind =Token.Kind.MULDIV;
                this.appendAndAdvance(spelling);
                break;
            case '^':
                kind = Token.Kind.MULDIV;
                this.appendAndAdvance(spelling);
                break;
            case '+':
                if (this.isFollowedBy('+', spelling)) {
                    kind = Token.Kind.UNARYINCR;
                }
                else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '-':
                if (this.isFollowedBy('-', spelling)) {
                    kind = Token.Kind.UNARYDECR;
                }
                else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '=':
                if (this.isFollowedBy('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.ASSIGN;
                }
                break;
            case '&':
                if (this.isFollowedBy('&', spelling)) {
                    kind = Token.Kind.BINARYLOGIC;
                }
                else {
                    this.registerError(start, "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '|':
                if (this.isFollowedBy('|', spelling)) {
                    kind = Token.Kind.BINARYLOGIC;
                }
                else {
                    this.registerError(start, "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '!':
                if (isFollowedBy('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind= Token.Kind.UNARYNOT;
                }
                break;
            case '<':
                if (isFollowedBy('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            case '>':
                if (isFollowedBy('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            default: kind = handleForwardSlash(spelling);
        }
        return kind;
    }

    private Token.Kind handleForwardSlash(StringBuilder spelling) {
        this.appendAndAdvance(spelling);
        if (this.currentChar == '/') {
            return handleSingleLineComment(spelling);
        }
        else if (this.currentChar == '*') {
            return handleMultiLineComment(spelling);
        }
        else {
            return Token.Kind.MULDIV;
        }
    }

    private Token.Kind handleSingleLineComment(StringBuilder spelling) {
        while (this.currentChar != SourceFile.eol && this.currentChar != SourceFile.eof) {
            this.appendAndAdvance(spelling);
        }
        return Token.Kind.COMMENT;
    }

    private Token.Kind handleMultiLineComment(StringBuilder spelling) {
        int start = this.sourceFile.getCurrentLineNumber();

        while (this.currentChar != SourceFile.eof) {
            this.appendAndAdvance(spelling);
            if (this.currentChar == '*') {
                this.appendAndAdvance(spelling);
                if (this.currentChar == '/') {
                    this.appendAndAdvance(spelling);
                    return Token.Kind.COMMENT;
                }
            }
        }
        this.registerError(start, "Unterminated block comment");
        return Token.Kind.ERROR;
    }

    private Token.Kind handleEOF(StringBuilder spelling) {
        spelling.append("EOF");
        return Token.Kind.EOF;
    }

    private Token.Kind handleUnsupportedChar(StringBuilder spelling) {
        this.errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                sourceFile.getCurrentLineNumber(), "Unsupported character");
        spelling.append(Character.toString(this.currentChar));
        this.currentChar = this.sourceFile.getNextChar();
        return Token.Kind.ERROR;
    }

    private boolean isFollowedBy(char c, StringBuilder spelling) {
        this.appendAndAdvance(spelling);
        if (this.currentChar == c) {
            this.appendAndAdvance(spelling);
            return true;
        }
        return false;
    }

    private void goToNonWhitespaceChar() {
        // initialize current char if this is the start of the file
        if (this.currentChar == ' ') {
            this.currentChar = this.sourceFile.getNextChar();
        }

        // munch whitespace
        while (Character.isWhitespace(this.currentChar)) {
            this.currentChar = this.sourceFile.getNextChar();
        }
    }

    private void appendAndAdvance(StringBuilder spelling) {
        spelling.append(this.currentChar);
        this.currentChar = this.sourceFile.getNextChar();
    }

    private void registerError(int position, String message) {
        this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                position, message);
    }
}
