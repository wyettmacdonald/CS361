/*
 * File: proj8AbramsDeutschDurstJones.Test.java
 * Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
 * Class: CS 361
 * Project 8
 * Date: November 9, 2018
 */

package proj8AbramsDeutschDurstJones;

/**
 * This is a test class for the NontrivialBracesCounter
 *
 * @author Douglas Abrams
 * @author Martin Deutsch
 * @author Robert Durst
 * @author Matt Jones
 */
public class Test {

	/**
	 * This method contains single and multi-line comments
	 */
	private void commentTest() {
		int i = 0; // {{{{
		if (i < 1) /* sneaky comment {{{ */ {i++;}
	}

	/**
	 * This method contains chars and strings
	 */
    private void charAndStringTest() {
        char c1 = '\'';
        char c2 = '\"';
        char c3 = '{';
        String str1 = "{{{ \" {{{";
        String str2 = "Line one" +
        	"line 2 {{{";
        String str3 = "// this is not a comment"; if (c1 == '\'') {c1='a';}
    }

	/**
	 * Main method for printing how many non-trivial left braces there are
	 */
    public static void main(String[] args) {
        System.out.println("This file contains 6 non-trivial left braces");
    }
}
