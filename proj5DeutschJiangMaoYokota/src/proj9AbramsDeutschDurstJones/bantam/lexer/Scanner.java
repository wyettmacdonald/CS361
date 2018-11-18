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
public class Scanner {
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private char currentChar;
    private StringBuilder currentSpelling;

    private static Set<Character> punctuation = Stream.of('.', ';', ':', ',').collect(Collectors.toSet());
    private static Set<Character> escapeCharacters = Stream.of('t', 'b', 'n', 'r', 'f', '\'',
            '"', '\\').collect(Collectors.toSet());
    private static Set<Character> brackets = Stream.of('(', ')', '{', '}', '[', ']').collect(Collectors.toSet());
    private static Set<Character> operators = Stream.of('+', '-', '/', '=', '<', '>', '&', '|',
            '*', '%', '!', '^').collect(Collectors.toSet());

    /**
     * Constructor just taking the error handler
     *
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(ErrorHandler handler) {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = null;
    }

    /**
     * Constructor taking the file to tokenize and the error handler
     *
     * @param filename the path to the file to tokenize
     * @param handler  the ErrorHandler to register errors with
     */
    public Scanner(String filename, ErrorHandler handler) {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = new SourceFile(filename);
    }

    /**
     * Constructor taking a file reader and the error handler
     *
     * @param reader  the reader to initialize the SourceFile object with
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        this.errorHandler = handler;
        this.sourceFile = new SourceFile(reader);
    }

    /**
     * Looks for the next token. Will return the constructed token when any of the
     * conditions that end a token are met.
     *
     * @return returns a Token associated with what was built
     */
    public Token scan() {
        // go to next meaningful character
        this.goToNonWhitespaceChar();

        // initialize the spelling, kind and position of the token
        this.currentSpelling = new StringBuilder();
        Token.Kind kind;
        int position = this.sourceFile.getCurrentLineNumber();

        // identifier
        if (Character.isLetter(this.currentChar)) {
            kind = this.handleIdentifier();
        }
        // integer
        else if (Character.isDigit(this.currentChar)) {
            kind = this.handleInteger();
        }
        // string
        else if (this.currentChar == '"') {
            kind = this.handleString();
        }
        // punctuation
        else if (punctuation.contains(this.currentChar)) {
            kind = this.handlePunctuation();
        }
        // brackets
        else if (brackets.contains(this.currentChar)) {
            kind = this.handleBrace();
        }
        // operators
        else if (operators.contains(this.currentChar)) {
            kind = this.handleOperator();
        }
        // EOF
        else if (this.currentChar == SourceFile.eof) {
            this.currentSpelling.append("EOF");
            kind = Token.Kind.EOF;
        }
        // unsupported characters
        else {
            kind = this.handleUnsupportedChar();
        }

        // generate the token
        return new Token(kind, currentSpelling.toString(), position);
    }

    /**
     * Handles creating an identifier token.
     *
     * @return The token kind "IDENTIFIER"
     */
    private Token.Kind handleIdentifier() {
        while (Character.isLetterOrDigit(this.currentChar) || this.currentChar == '_') {
            this.currentSpelling.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
        }
        return Token.Kind.IDENTIFIER;
    }

    /**
     * Handles creating an integer token.
     *
     * @return The token kind "INTCONST" assuming the integer is not too long. If the
     * integer is longer than 2^31-1, then the token kind will be "ERROR"
     */
    private Token.Kind handleInteger() {
        int position = this.sourceFile.getCurrentLineNumber();

        while (Character.isDigit(this.currentChar)) {
            this.currentSpelling.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
        }
        if (Integer.parseInt(this.currentSpelling.toString()) > (Math.pow(2, 31) - 1)) {
            this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                    position, "Integer constant too large");
            return Token.Kind.ERROR;
        }
        return Token.Kind.INTCONST;
    }

    /**
     * Handles creating a string token.
     *
     * @return The token kind "STRCONST." If the string is more than 5000 characters,
     * or if the eor or eol token is found, or if the escape character is not supported
     * then the token kind will be "ERROR"
     */
    private Token.Kind handleString() {
        int position = this.sourceFile.getCurrentLineNumber();
        this.currentSpelling.append(this.currentChar);
        this.currentChar = this.sourceFile.getNextChar();

        while (this.currentChar != '"') {
            this.currentSpelling.append(this.currentChar);
            // if escape character, add the next character to the string and continue
            if (this.currentChar == '\\') {
                this.currentChar = this.sourceFile.getNextChar();
                // check if escape character is supported
                if (!escapeCharacters.contains(this.currentChar)) {
                    this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                            position, "Unsupported escape character");
                    return Token.Kind.ERROR;
                }
                this.currentSpelling.append(this.currentChar);
            }
            // if eof or eol, string is invalid
            if (this.currentChar == SourceFile.eof || this.currentChar == SourceFile.eol) {
                this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                        position, "Unterminated String");
                return Token.Kind.ERROR;
            }
            this.currentChar = this.sourceFile.getNextChar();
        }

        // error if string greater than 5000 characters (not including start and end quotes)
        if (this.currentSpelling.length() > 5002) {
            this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(), position,
                    "String constant too long");
            return Token.Kind.ERROR;
        }
        this.currentSpelling.append(currentChar);
        this.currentChar = this.sourceFile.getNextChar();
        return Token.Kind.STRCONST;
    }

    /**
     * Handles creating a punctuation token.
     *
     * @return The token kind corresponding to the type of punctuation found.
     */
    private Token.Kind handlePunctuation() {
        this.currentSpelling.append(this.currentChar);
        Token.Kind kind;
        switch (this.currentChar) {
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
        this.currentChar = this.sourceFile.getNextChar();
        return kind;
    }

    /**
     * Handles creating a brace token.
     *
     * @return The token kind corresponding to the type of brace found.
     */
    private Token.Kind handleBrace() {
        this.currentSpelling.append(this.currentChar);
        Token.Kind kind;
        switch (this.currentChar) {
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
        this.currentChar = this.sourceFile.getNextChar();
        return kind;
    }

    /**
     * Handles creating an operator token.
     *
     * @return The token kind corresponding to the type of operation found. If the
     * operation character is followed by an unsupported character, the token kind will
     * be "ERROR"
     */
    private Token.Kind handleOperator() {
        int position = this.sourceFile.getCurrentLineNumber();
        this.currentSpelling.append(this.currentChar);
        Token.Kind kind;

        switch (this.currentChar) {
            case '*':
                kind = Token.Kind.MULDIV;
                this.currentChar = this.sourceFile.getNextChar();
                break;
            case '%':
                kind = Token.Kind.MULDIV;
                this.currentChar = this.sourceFile.getNextChar();
                break;
            case '^':
                kind = Token.Kind.MULDIV;
                this.currentChar = this.sourceFile.getNextChar();
                break;
            case '+':
                if (this.isFollowedBy('+')) {
                    kind = Token.Kind.UNARYINCR;
                } else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '-':
                if (this.isFollowedBy('-')) {
                    kind = Token.Kind.UNARYDECR;
                } else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '=':
                if (this.isFollowedBy('=')) {
                    kind = Token.Kind.COMPARE;
                } else {
                    kind = Token.Kind.ASSIGN;
                }
                break;
            case '&':
                if (this.isFollowedBy('&')) {
                    kind = Token.Kind.BINARYLOGIC;
                } else {
                    this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                            position, "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '|':
                if (this.isFollowedBy('|')) {
                    kind = Token.Kind.BINARYLOGIC;
                } else {
                    this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                            position, "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '!':
                if (isFollowedBy('=')) {
                    kind = Token.Kind.COMPARE;
                } else {
                    kind = Token.Kind.UNARYNOT;
                }
                break;
            case '<':
                if (isFollowedBy('=')) {
                    kind = Token.Kind.COMPARE;
                } else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            case '>':
                if (isFollowedBy('=')) {
                    kind = Token.Kind.COMPARE;
                } else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            default:
                kind = handleForwardSlash();
        }
        return kind;
    }

    /**
     * Handles creating a token including a forward slash.
     *
     * @return The token kind "MULDIV" if single forward slash or "COMMENT" if a
     * comment.
     */
    private Token.Kind handleForwardSlash() {
        this.currentChar = this.sourceFile.getNextChar();
        if (this.currentChar == '/') {
            return handleSingleLineComment();
        } else if (this.currentChar == '*') {
            return handleMultiLineComment();
        } else {
            return Token.Kind.MULDIV;
        }
    }

    /**
     * Handles creating a token for a single-line comment.
     *
     * @return The token kind "COMMENT"
     */
    private Token.Kind handleSingleLineComment() {
        while (this.currentChar != SourceFile.eol && this.currentChar != SourceFile.eof) {
            this.currentSpelling.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
        }
        return Token.Kind.COMMENT;
    }

    /**
     * Handles creating a token for a multi-line comment.
     *
     * @return The token kind "COMMENT" or "ERROR if the comment block is unterminated.
     */
    private Token.Kind handleMultiLineComment() {
        int position = this.sourceFile.getCurrentLineNumber();

        while (this.currentChar != SourceFile.eof) {
            this.currentSpelling.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
            if (this.currentChar == '*') {
                this.currentSpelling.append(this.currentChar);
                this.currentChar = this.sourceFile.getNextChar();
                if (this.currentChar == '/') {
                    this.currentSpelling.append(this.currentChar);
                    this.currentChar = this.sourceFile.getNextChar();
                    return Token.Kind.COMMENT;
                }
            }
        }
        this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                position, "Unterminated block comment");
        return Token.Kind.ERROR;
    }

    /**
     * Handles checking for an unsupported character.
     *
     * @return The token kind "ERROR"
     */
    private Token.Kind handleUnsupportedChar() {
        this.errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(),
                sourceFile.getCurrentLineNumber(), "Unsupported character");
        this.currentSpelling.append(Character.toString(this.currentChar));
        this.currentChar = this.sourceFile.getNextChar();
        return Token.Kind.ERROR;
    }

    /**
     * Checks to see if the current character is followed by the character passed in by
     * the parameter c.
     *
     * @param c The character is checked against the current character.
     * @return true boolean if the current character is followed by the character
     * passed in by the parameter c. Otherwise, will return false.
     */
    private boolean isFollowedBy(char c) {
        this.currentChar = this.sourceFile.getNextChar();
        if (this.currentChar == c) {
            this.currentSpelling.append(this.currentChar);
            this.currentChar = this.sourceFile.getNextChar();
            return true;
        }
        return false;
    }

    /**
     * Will munch whitespace until a non-whitespace character is found.
     */
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
}
