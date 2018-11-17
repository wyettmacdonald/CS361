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

public class Scanner
{
    private SourceFile sourceFile;
    private ErrorHandler errorHandler;
    private char currentChar;
    private String currentSpelling; // maybe put in scan and pass to handler methods?????


//    public ScannerCode(ErrorHandler handler) {
//        errorHandler = handler;
//        currentChar = ' ';
//        sourceFile = null;
//    }

    public Scanner(String filename, ErrorHandler handler) {
        errorHandler = handler;
        currentChar = ' ';
        sourceFile = new SourceFile(filename);
    }

//    public ScannerCode(Reader reader, ErrorHandler handler) {
//        errorHandler = handler;
//        sourceFile = new SourceFile(reader);
//    }

    public Token scan()
    {
        // initialize the spelling
        currentSpelling = "";

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
            return new Token(handleIdentifier(), currentSpelling, sourceFile.getCurrentLineNumber());

        }
        // integer
        else if (Character.isDigit(currentChar)) {
            return new Token(handleInteger(), currentSpelling, sourceFile.getCurrentLineNumber());
        }
        // string
        else if (currentChar == '"') {
            return new Token(handleString(), currentSpelling, sourceFile.getCurrentLineNumber());
        }
        // punctuation
        else if (punctuation.contains(currentChar)) {
            return new Token(handlePunctuation(), currentSpelling, sourceFile.getCurrentLineNumber());
        }
        // brackets
        else if (brackets.contains(currentChar)) {
            return new Token(handleBracket(), currentSpelling, sourceFile.getCurrentLineNumber());
        }
        else if (operators.contains(currentChar)) {
            return new Token(handleOperator(), currentSpelling, sourceFile.getCurrentLineNumber());
        }
        // EOF
        else if (currentChar == SourceFile.eof) {
            return new Token(Token.Kind.EOF, "EOF", sourceFile.getCurrentLineNumber());
        }
        // unsupported characters
        else {
            errorHandler.register(Error.Kind.LEX_ERROR, sourceFile.getFilename(), sourceFile.getCurrentLineNumber(),
                    "Unsupported character");
            return new Token(Token.Kind.ERROR, Character.toString(currentChar), sourceFile.getCurrentLineNumber());
        }
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
                currentSpelling += currentChar;
            }
            // if eof or eol, string is invalid
            if (currentChar == SourceFile.eof || currentChar == SourceFile.eol) {
                return Token.Kind.ERROR;
            }
            currentChar = sourceFile.getNextChar();
        }
        return Token.Kind.STRCONST;
    }

    private Token.Kind handlePunctuation() {
        currentSpelling += currentChar;
        switch (currentChar) {
            case '.': return Token.Kind.DOT;
            case ',': return Token.Kind.COMMA;
            case ';': return Token.Kind.SEMICOLON;
            default: return Token.Kind.COLON;
        }
    }

    private Token.Kind handleBracket() {
        currentSpelling += currentChar;
        switch (currentChar) {
            case '(': return Token.Kind.RPAREN;
            case ')': return Token.Kind.LPAREN;
            case '{': return Token.Kind.RCURLY;
            case '}': return Token.Kind.LCURLY;
            case '[': return Token.Kind.LBRACKET;
            default: return Token.Kind.RBRACKET;
        }
    }

    private Token.Kind handleOperator() {
        currentSpelling += currentChar;
        switch (currentChar) {
            case '+':
                if (isDoubleSymbol()) {
                    return Token.Kind.UNARYINCR;
                }
                else {
                    return Token.Kind.PLUSMINUS;
                }
            case '-':
                if (isDoubleSymbol()) {
                    return Token.Kind.UNARYDECR;
                }
                else {
                    return Token.Kind.PLUSMINUS;
                }
            case '*': return Token.Kind.MULDIV;
            default: return null;
        }
    }

    private boolean isDoubleSymbol() {
        char checkChar = currentChar;
        currentChar = sourceFile.getNextChar();
        if (checkChar == currentChar) {
            currentSpelling += currentChar;
            currentChar = sourceFile.getNextChar();
            return true;
        }
        return false;
    }

    private Set<Character> punctuation = Stream.of('.', ';', ':', ',').collect(Collectors.toSet());
    private Set<Character> brackets = Stream.of('(', ')', '{', '}', '[', ']').collect(Collectors.toSet());
    private Set<Character> operators = Stream.of('+', '-','/', '=', '<', '>', '&', '|',
            '*', '%', '!', '^').collect(Collectors.toSet());
}