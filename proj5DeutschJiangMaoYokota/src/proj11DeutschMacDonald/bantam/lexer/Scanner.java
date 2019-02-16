/*
 * File: bantam.lexer.Scanner.java
 * CS361 Project 9
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Date: 11/20/2018
 * This file contains the Scanner, which tokenizes the source file
 */

package proj11DeutschMacDonald.bantam.lexer;

import proj11DeutschMacDonald.bantam.util.CompilationException;
import proj11DeutschMacDonald.bantam.util.Error;
import proj11DeutschMacDonald.bantam.util.ErrorHandler;
import java.io.Reader;
import java.math.BigInteger;
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

    private final static Set<Character> punctuation = Stream.of('.', ';',
            ':', ',').collect(Collectors.toSet());
    private final static Set<Character> escapeCharacters = Stream.of('t', 'n',
            'f', '"', '\\').collect(Collectors.toSet());
    private final static Set<Character> brackets = Stream.of('(', ')', '{',
            '}', '[', ']').collect(Collectors.toSet());
    private final static Set<Character> operators = Stream.of('+', '-','/', '=', '<', '>', '&', '|',
            '*', '%', '!').collect(Collectors.toSet());

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
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(String filename, ErrorHandler handler) {
        this.errorHandler = handler;
        this.currentChar = ' ';
        this.sourceFile = new SourceFile(filename);
    }

    /**
     * Constructor taking a file reader and the error handler
     *
     * @param reader the reader to initialize the SourceFile object with
     * @param handler the ErrorHandler to register errors with
     */
    public Scanner(Reader reader, ErrorHandler handler) {
        this.errorHandler = handler;
        this.sourceFile = new SourceFile(reader);
    }

    /**
     * Sets the sourceFile to scan
     * @param sourceFile the SourceFile object to scan from
     */
    public void setSourceFile(SourceFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    /**
     * Looks for the next token. Will return the constructed token when any of the
     * conditions that end a token are met. Returns a Token of type eof if the
     * end of the file has been reached.
     *
     * @return returns a Token associated with what was built
     */
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

    /**
     * Handles creating an identifier token.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "IDENTIFIER"
     */
    private Token.Kind handleIdentifier(StringBuilder spelling) {
        while (Character.isLetterOrDigit(this.currentChar) || this.currentChar == '_') {
            this.appendAndAdvance(spelling);
        }
        return Token.Kind.IDENTIFIER;
    }

    /**
     * Handles creating an integer token.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "INTCONST" assuming the integer is not too long. If the
     * integer is longer than 2^31-1, then the token kind will be "ERROR"
     */
    private Token.Kind handleInteger(StringBuilder spelling) {
        int start = this.sourceFile.getCurrentLineNumber();
        while (Character.isDigit(this.currentChar)) {
            this.appendAndAdvance(spelling);
        }
        // use BigInteger to compare arbitrarily large number from program to max int value
        BigInteger bigInt = new BigInteger(spelling.toString());
        if(bigInt.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            this.registerError(start, "Integer constant too large");
            return Token.Kind.ERROR;
        }
        return Token.Kind.INTCONST;
    }

    /**
     * Handles creating an String token.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "STRCONST." If the string is more than 5000 characters,
     * or if the eor or eol token is found, or if the escape character is not supported
     * then the token kind will be "ERROR"
     */
    private Token.Kind handleString(StringBuilder spelling) {
        int start = this.sourceFile.getCurrentLineNumber();
        boolean hitEOL = false;
        Token.Kind kind = Token.Kind.STRCONST;

        this.appendAndAdvance(spelling);

        while (this.currentChar != '"') {
            // if escape character, add the next character to the string and continue
            if (this.currentChar == '\\') {
                this.appendAndAdvance(spelling);
                // check if escape character is supported
                if (!escapeCharacters.contains(this.currentChar)) {
                    this.registerError(start, "Unsupported escape character");
                    kind = Token.Kind.ERROR;
                }
            }
            this.appendAndAdvance(spelling);

            // if eol, string is invalid
            if (this.currentChar == SourceFile.eol && !hitEOL) {
                hitEOL = true;
                this.registerError(start, "String spanning multiple lines");
                kind = Token.Kind.ERROR;
            }

            // if eof, string is invalid and stop scanning
            if (this.currentChar == SourceFile.eof) {
                this.registerError(start, "Unterminated string");
                return Token.Kind.ERROR;
            }
        }

        // error if string greater than 5000 characters (not including start and end quotes)
        if (spelling.length() > 5002) {
            this.registerError(start, "String constant too long");
            kind = Token.Kind.ERROR;
        }
        this.appendAndAdvance(spelling);
        return kind;
    }

    /**
     * Handles creating a punctuation token.
     * Includes '.', ',', ';', and ':' tokens.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind corresponding to the type of punctuation found.
     */
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

    /**
     * Handles creating a brace token.
     * Includes '(', ')', '{', '}', '[' and ']' tokens
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind corresponding to the type of brace found.
     */
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

    /**
     * Handles creating an operator token.
     * Includes '*', '%', '+', '++', '-', '--', '=', '==', '&&', '||', '!',
     * '<', and '>' tokens.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind corresponding to the type of operation found. If the
     * operation character is followed by an unsupported character, the token kind will
     * be "ERROR"
     */
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
            case '+':
                if (this.getNextCharAndCompare('+', spelling)) {
                    kind = Token.Kind.UNARYINCR;
                }
                else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '-':
                if (this.getNextCharAndCompare('-', spelling)) {
                    kind = Token.Kind.UNARYDECR;
                }
                else {
                    kind = Token.Kind.PLUSMINUS;
                }
                break;
            case '=':
                if (this.getNextCharAndCompare('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.ASSIGN;
                }
                break;
            case '&':
                if (this.getNextCharAndCompare('&', spelling)) {
                    kind = Token.Kind.BINARYLOGIC;
                }
                else {
                    this.registerError(start, "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '|':
                if (this.getNextCharAndCompare('|', spelling)) {
                    kind = Token.Kind.BINARYLOGIC;
                }
                else {
                    this.registerError(start, "Unsupported character");
                    kind = Token.Kind.ERROR;
                }
                break;
            case '!':
                if (getNextCharAndCompare('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind= Token.Kind.UNARYNOT;
                }
                break;
            case '<':
                if (getNextCharAndCompare('=', spelling)) {
                    kind = Token.Kind.COMPARE;
                }
                else {
                    kind = Token.Kind.COMPARE;
                }
                break;
            case '>':
                if (getNextCharAndCompare('=', spelling)) {
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

    /**
     * Handles creating a token including a forward slash.
     * Includes '/' and comment tokens.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "MULDIV" if single forward slash or "COMMENT" if a
     * comment.
     */
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

    /**
     * Handles creating a token for a single-line comment.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "COMMENT"
     */
    private Token.Kind handleSingleLineComment(StringBuilder spelling) {
        while (this.currentChar != SourceFile.eol && this.currentChar != SourceFile.eof) {
            this.appendAndAdvance(spelling);
        }
        return Token.Kind.COMMENT;
    }

    /**
     * Handles creating a token for a multi-line comment.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "COMMENT" or "ERROR if the comment block is unterminated.
     */
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

    /**
     * Handles creating a token for EOF
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind EOF
     */
    private Token.Kind handleEOF(StringBuilder spelling) {
        spelling.append("EOF");
        return Token.Kind.EOF;
    }

    /**
     * Handles checking for an unsupported character.
     *
     * @param spelling the StringBuilder representing the current spelling
     * @return The token kind "ERROR"
     */
    private Token.Kind handleUnsupportedChar(StringBuilder spelling) {
        this.registerError(sourceFile.getCurrentLineNumber(), "Unsupported character");
        this.appendAndAdvance(spelling);
        return Token.Kind.ERROR;
    }

    /**
     * Gets the next character and checks to see if it matches the given character.
     *
     * @param compareChar The character to be checked against the current character.
     * @param spelling The StringBuilder representing the current spelling
     * @return true if the current character is followed by the character
     * passed in by the parameter compareChar. Otherwise, will return false.
     */
    private boolean getNextCharAndCompare(char compareChar, StringBuilder spelling) {
        this.appendAndAdvance(spelling);
        if (this.currentChar == compareChar) {
            this.appendAndAdvance(spelling);
            return true;
        }
        return false;
    }

    /**
     * Will munch whitespace until a non-whitespace character is found.
     */
    private void goToNonWhitespaceChar() {
        while (Character.isWhitespace(this.currentChar)) {
            this.currentChar = this.sourceFile.getNextChar();
        }
    }

    /**
     * Append the current character and go to the next character from the source file
     * @param spelling the StringBuilder representing the current spelling
     */
    private void appendAndAdvance(StringBuilder spelling) {
        spelling.append(this.currentChar);
        this.currentChar = this.sourceFile.getNextChar();
    }

    /**
     * Register an error with the given line number and error message
     * @param position the line number of the error
     * @param message the description of the error
     */
    private void registerError(int position, String message) {
        this.errorHandler.register(Error.Kind.LEX_ERROR, this.sourceFile.getFilename(),
                position, message);
    }

    /**
     * Main method scans the given files and prints out their tokens and
     * the number of errors
     * @param args a list of file names
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter files to scan");
        }

        ErrorHandler errorHandler = new ErrorHandler();
        for (int i = 0; i < args.length; i++) {
            try {
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
                } else {
                    System.out.println("Scanning successful");
                }
                errorHandler.clear();
            }
            catch (CompilationException e) {
                System.out.println("Unable to read file " + args[i]);
            }
        }
    }
}
