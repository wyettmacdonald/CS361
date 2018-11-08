/*
* File: proj8AbramsDeutschDurstJones.NontrivialBracesCounter.java
* Names: Douglas Abrams, Martin Deutsch, Robert Durst, Matt Jones
* Class: CS 361
* Project 8
* Date: November 9, 2018
*/

package proj8AbramsDeutschDurstJones;

import java.nio.charset.Charset;
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

	   //Java 7 source level
    public static void main(String[] args) throws IOException {
        // replace this with a known encoding if possible
        Charset encoding = Charset.defaultCharset();
        for (String filename : args) {
            File file = new File(filename);
            handleFile(file, encoding);
        }
    }

    private static void handleFile(File file, Charset encoding)
            throws IOException {
        try (InputStream in = new FileInputStream(file);
             Reader reader = new InputStreamReader(in, encoding);
             // buffer for efficiency
             Reader buffer = new BufferedReader(reader)) {
            handleCharacters(buffer);
        }
    }

    private static void handleCharacters(Reader reader)
            throws IOException {
       
        boolean startOfComment = false;
        int r;
        while ((r = reader.read()) != -1) {
            char ch = (char) r;
            // first check to see if inside one of the ignore comment
            
            if (startOfComment && ch == '*') {
              reader = handleMultiLineComment(reader);
            } else if (startOfComment && ch == '/') {
              reader = handleSingleLineComment(reader);
            } else if (startOfComment) {
              startOfComment = false;
            } else if (ch == '\'') {
              reader = handleSingleQuotation(reader);
            } else if (ch == '\"') {
              reader = handleDoubleQuotation(reader);
            } else if (ch == '/') {
              startOfComment = true;
            } else {
              System.out.printf("%c", ch);
            }
        }
   }

   private static Reader handleSingleLineComment(Reader reader) throws IOException {
      int r;
      while(((r= reader.read()) != -1) && ((char) r != '\n'))
      {}

      return reader;
   }

   private static Reader handleMultiLineComment(Reader reader) throws IOException {
      int r;
      boolean startEndComment = false;
      while(((r= reader.read()) != -1) && !(((char)r == '/') && (startEndComment)))
      {
        startEndComment = (char)r == '*';  
      }

      return reader;
   }
   private static Reader handleSingleQuotation(Reader reader) throws IOException {
      int r;
      while(((r= reader.read()) != -1) && ((char) r != '\''))
      {}

      return reader;
   }
   private static Reader handleDoubleQuotation(Reader reader) throws IOException{
      int r;
      while(((r= reader.read()) != -1) && ((char) r != '\"'))
      {}

      return reader;
   }
}
