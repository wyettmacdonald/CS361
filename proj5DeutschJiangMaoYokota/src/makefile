# Robert Durst
# 10/23/18
# CS361 Project 10

# makefile begins
# Ref: http://profesores.elo.utfsm.cl/~agv/elo329/Java/javamakefile.html
# Ref: https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_makefiles.html#java
JFLAGS = -g # compiler flags
JC = javac # compiler variable
JVM= java   # specify JVM
CLASSPATH = "proj10AbramsDeutschDurstJones/resources/*": # maybe not most elegant, or best practices, but ok for now (we get all them jars)
SOURCEPATH = ./ # specify where the .class files should be found for file by file compilation

.SUFFIXES: .java .class

.java.class:
	$(JC) -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) $(JFLAGS) $*.java

# I think order matters here
# Realized that some files do not need to be included here inorder to compile
# TODO: delete extraneous classes here
CLASSES = \
					proj10AbramsDeutschDurstJones/StyledJavaCodeArea.java \
					proj10AbramsDeutschDurstJones/FileMenuController.java \
					proj10AbramsDeutschDurstJones/Controller.java \
					proj10AbramsDeutschDurstJones/CodeMenuController.java \
					proj10AbramsDeutschDurstJones/SettingMenuController.java \
					proj10AbramsDeutschDurstJones/StructureViewController.java \
					proj10AbramsDeutschDurstJones/JavaTab.java \
					proj10AbramsDeutschDurstJones/JavaTabPane.java \
					proj10AbramsDeutschDurstJones/DirectoryController.java \
					proj10AbramsDeutschDurstJones/EditMenuController.java \
					proj10AbramsDeutschDurstJones/ToolBarController.java \
					proj10AbramsDeutschDurstJones/Main.java \
					proj10AbramsDeutschDurstJones/bantam/lexer/Scanner.java \
					proj10AbramsDeutschDurstJones/bantam/lexer/SourceFile.java \
					proj10AbramsDeutschDurstJones/bantam/lexer/Token.java \
					proj10AbramsDeutschDurstJones/bantam/parser/Parser.java \
					proj10AbramsDeutschDurstJones/bantam/tests/TestScanner.java \
					proj10AbramsDeutschDurstJones/bantam/tests/TestParser.java \
					proj10AbramsDeutschDurstJones/bantam/tests/TestSuite.java \

# Name of our main class with its preceding path location
MAIN = proj10AbramsDeutschDurstJones.Main

# Name of our test class with its preceding path location
TEST = proj10AbramsDeutschDurstJones.bantam.tests.TestSuite

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
	$(RM) proj10AbramsDeutschDurstJones/*.class proj10AbramsDeutschDurstJones/bantam/lexer/*.class proj10AbramsDeutschDurstJones/bantam/util/*.class proj10AbramsDeutschDurstJones/bantam/tests/*.class

