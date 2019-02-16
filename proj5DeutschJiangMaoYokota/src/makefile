# Robert Durst
# 10/23/18
# CS361 Project 10

# makefile begins
# Ref: http://profesores.elo.utfsm.cl/~agv/elo329/Java/javamakefile.html
# Ref: https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_makefiles.html#java
JFLAGS = -g # compiler flags
JC = javac # compiler variable
JVM= java   # specify JVM
CLASSPATH = "proj12MacDonaldDouglas/resources/*": # maybe not most elegant, or best practices, but ok for now (we get all them jars)
SOURCEPATH = ./ # specify where the .class files should be found for file by file compilation

.SUFFIXES: .java .class

.java.class:
	$(JC) -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) $(JFLAGS) $*.java

# I think order matters here
# Realized that some files do not need to be included here inorder to compile
# TODO: delete extraneous classes here
CLASSES = \
					proj12MacDonaldDouglas/StyledJavaCodeArea.java \
					proj12MacDonaldDouglas/FileMenuController.java \
					proj12MacDonaldDouglas/Controller.java \
					proj12MacDonaldDouglas/CodeMenuController.java \
					proj12MacDonaldDouglas/SettingMenuController.java \
					proj12MacDonaldDouglas/StructureViewController.java \
					proj12MacDonaldDouglas/JavaTab.java \
					proj12MacDonaldDouglas/JavaTabPane.java \
					proj12MacDonaldDouglas/DirectoryController.java \
					proj12MacDonaldDouglas/EditMenuController.java \
					proj12MacDonaldDouglas/ToolBarController.java \
					proj12MacDonaldDouglas/Main.java \
					proj12MacDonaldDouglas/bantam/lexer/Scanner.java \
					proj12MacDonaldDouglas/bantam/lexer/SourceFile.java \
					proj12MacDonaldDouglas/bantam/lexer/Token.java \
					proj12MacDonaldDouglas/bantam/parser/Parser.java \
					proj12MacDonaldDouglas/bantam/tests/TestScanner.java \
					proj12MacDonaldDouglas/bantam/tests/TestParser.java \
					proj12MacDonaldDouglas/bantam/tests/TestSuite.java \

# Name of our main class with its preceding path location
MAIN = proj12MacDonaldDouglas.Main

# Name of our test class with its preceding path location
TEST = proj12MacDonaldDouglas.bantam.tests.TestSuite

default: classes

classes: $(CLASSES:.java=.class)

# executes the code
# notice the colon, this is necessary on unix to seperate between classpath and file to execute
run:
	$(JVM) -classpath $(CLASSPATH) $(MAIN)

# executes test suite for compiler
test:
	$(JVM) -classpath $(CLASSPATH) $(TEST)

# cleans up all the compiled class files
clean:
	$(RM) proj12MacDonaldDouglas/*.class proj12MacDonaldDouglas/bantam/lexer/*.class proj12MacDonaldDouglas/bantam/util/*.class proj12MacDonaldDouglas/bantam/tests/*.class

