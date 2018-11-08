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

    public int getNumNontrivialLeftBraces(String fileName) {
    	String contents = "";
        try {
             File file = new File(fileName);
             Reader buffer = new BufferedReader(new FileReader(file));
             contents = this.removeTrivialStringSections(buffer);
        }
        catch (Exception e) {
        	System.out.println(e);
        }
        return this.countLeftBraces(contents);
    }

    private String removeTrivialStringSections(Reader reader)
            throws IOException {
        String contents = "";
        boolean startOfComment = false;
        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            
            if (startOfComment){
                startOfComment = false;
            	if (ch == '*') {
              		reader = removeMultiLineComment(reader);
            	} else if (ch == '/') {
              		reader = removeSingleLineComment(reader);
            	}
            } else if (ch == '\'') {
              reader = removeSingleQuotation(reader);
            } else if (ch == '\"') {
              reader = removeDoubleQuotation(reader);
            } else if (ch == '/') {
              startOfComment = true;
            } else {
              contents += ch;
            }
        }
        return contents;
   }

   private Reader removeSingleLineComment(Reader reader) throws IOException {
      int r;
      while(((r= reader.read()) != -1))
      {
      	if ((char) r == '\n') {
      		return reader;
      	}
      }
      return reader;
   }

   private Reader removeMultiLineComment(Reader reader) throws IOException {
      int r;
      boolean startEndComment = false;
      while((r = reader.read()) != -1)
      { 
        if (( (char) r == '/') && (startEndComment)) {
        	return reader;
        }
        startEndComment = (char)r == '*';
      }

      return reader;
   }
   
   private Reader removeSingleQuotation(Reader reader) throws IOException {
      int r;
      while(((r= reader.read()) != -1))
      {
      	 if ((char) r == '\'') {
      	 	return reader;
      	 }
      }

      return reader;
   }
   private Reader removeDoubleQuotation(Reader reader) throws IOException{
      int r;
      boolean ignoreNext = false;
      while(((r= reader.read()) != -1))
      {
      	if (ignoreNext) {
      		ignoreNext = false;
      		continue;
      	}
      	if ((char) r == '\\') {
      		ignoreNext = true;
      	}
      	 if((char) r == '\"') {
      	 	return reader;
      	 }
      }

      return reader;
   }
   
   private int countLeftBraces(String text) {
   	 int count = 0;
     for (int i = 0; i < text.length(); i++) {
     	if (text.charAt(i) == '{') {
     		count++;
     	}
     }
     return count;
    }
	 
	 public static void main(String[] args) {
	 	NontrivialBracesCounter nbc = new NontrivialBracesCounter();
	 	System.out.println(nbc.getNumNontrivialLeftBraces("Test1.java"));
	 }
}
