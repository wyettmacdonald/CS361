/*
 * File: proj8AbramsDeutschDurstJones.NontrivialBracesCounter.java
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Class: CS 361
 * Project 8
 * Date: November 9, 2018
 */

//package proj8AbramsDeutschDurstJones;

import java.io.*;

/**
 * This class contains a method for counting non-trivial left braces
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class NontrivialBracesCounter {

    /**
     * This method returns the number of left braces in the given Java file,
     * or -1 if reading the file throws an IOException
     * @param fileName the Java file to analyze
     * @return the number of left braces in the file or -1 if IOException thrown
     */
    public int getNumNontrivialLeftBraces(String fileName) {
        try {
            File file = new File(fileName);
            Reader buffer = new BufferedReader(new FileReader(file));
            return this.countNontrivialLeftBraces(buffer);
        }
        catch (FileNotFoundException e) {
        	System.out.println("File " + fileName + " not found");
        }
        catch (IOException e) {
        	System.out.println("Error reading file " + fileName);
        }
        return -1;
    }

    /**
     * This method counts the number of nontrivial left braces in the file being
     * read by the given Reader
     * @param reader the Reader object reading the Java file
     * @return the number of left braces in the file being read
     * @throws IOException
     */
    private int countNontrivialLeftBraces(Reader reader) throws IOException {
        int braceCounter = 0;
        int r;

        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            switch (ch) {
                case '/':
                    ch = (char) reader.read();
                    if (ch == '*') {
                        ignoreMultiLineComment(reader);
                    } else if (ch == '/') {
                        ignoreSingleLineComment(reader);
                    }
                    break;
                case '\'':
                    ignoreQuotation(reader, '\'');
                    break;
                case '\"':
                    ignoreQuotation(reader, '\"');
                    break;
                case '{':
                    braceCounter++;
                    break;
            }
        }
        return braceCounter;
    }

    /**
     * This method cycles the given Reader until it reaches the end of the current line
     * @param reader the Reader object reading the Java file
     * @throws IOException
     */
    private void ignoreSingleLineComment(Reader reader) throws IOException {
        int r;
        while (((r = reader.read()) != -1)) {
            if ((char) r == '\n') {
                return;
            }
        }
    }

    /**
     * This method cycles the given Reader until it reaches the end of
     * the multi-line comment
     * @param reader the Reader object reading the Java file
     * @throws IOException
     */
    private void ignoreMultiLineComment(Reader reader) throws IOException {
        int r;
        boolean startEndComment = false;
        while ((r = reader.read()) != -1) {
            if (((char) r == '/') && (startEndComment)) {
                return;
            }
            startEndComment = (char) r == '*';
        }
    }

    /**
     * This method cycles the given Reader until it reaches the end of the quotation
     * @param reader the Reader object reading the Java file
     * @param endQuote the type of quote (single or double) marking
     *                 the end of the quotation
     * @throws IOException
     */
    private void ignoreQuotation(Reader reader, char endQuote) throws IOException {
        int r;
        boolean ignoreNext = false;
        while (((r = reader.read()) != -1)) {
            if (ignoreNext) {
                ignoreNext = false;
                continue;
            }
            if ((char) r == '\\') {
                ignoreNext = true;
            }
            if ((char) r == endQuote) {
                return;
            }
        }
    }

    /**
     * The main method runs getNumNontrivialLeftBraces on a test file
     */
    public static void main(String[] args) {
        NontrivialBracesCounter nbc = new NontrivialBracesCounter();
        System.out.println(nbc.getNumNontrivialLeftBraces("Test.java"));
    }
}
