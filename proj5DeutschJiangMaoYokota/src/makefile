# Robert Durst
# 10/23/18
# CS361 Project 10

# makefile begins
# Ref: http://profesores.elo.utfsm.cl/~agv/elo329/Java/javamakefile.html
# Ref: https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_makefiles.html#java
JFLAGS = -g # compiler flags
JC = javac # compiler variable
JVM= java   # specify JVM
CLASSPATH = "proj11DeutschMacDonald/resources/*": # maybe not most elegant, or best practices, but ok for now (we get all them jars)
SOURCEPATH = ./ # specify where the .class files should be found for file by file compilation

.SUFFIXES: .java .class

.java.class:
	$(JC) -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) $(JFLAGS) $*.java

# I think order matters here
# Realized that some files do not need to be included here inorder to compile
# TODO: delete extraneous classes here
CLASSES = \
					proj11DeutschMacDonald/StyledJavaCodeArea.java \
					proj11DeutschMacDonald/FileMenuController.java \
					proj11DeutschMacDonald/Controller.java \
					proj11DeutschMacDonald/CodeMenuController.java \
					proj11DeutschMacDonald/SettingMenuController.java \
					proj11DeutschMacDonald/StructureViewController.java \
					proj11DeutschMacDonald/JavaTab.java \
					proj11DeutschMacDonald/JavaTabPane.java \
					proj11DeutschMacDonald/DirectoryController.java \
					proj11DeutschMacDonald/EditMenuController.java \
					proj11DeutschMacDonald/ToolBarController.java \
					proj11DeutschMacDonald/Main.java \
					proj11DeutschMacDonald/bantam/lexer/Scanner.java \
					proj11DeutschMacDonald/bantam/lexer/SourceFile.java \
					proj11DeutschMacDonald/bantam/lexer/Token.java \
					proj11DeutschMacDonald/bantam/parser/Parser.java \
					proj11DeutschMacDonald/bantam/tests/TestScanner.java \
					proj11DeutschMacDonald/bantam/tests/TestParser.java \
					proj11DeutschMacDonald/bantam/tests/TestSuite.java \

# Name of our main class with its preceding path location
MAIN = proj11DeutschMacDonald.Main

# Name of our test class with its preceding path location
TEST = proj11DeutschMacDonald.bantam.tests.TestSuite

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
	$(RM) proj11DeutschMacDonald/*.class proj11DeutschMacDonald/bantam/lexer/*.class proj11DeutschMacDonald/bantam/util/*.class proj11DeutschMacDonald/bantam/tests/*.class

