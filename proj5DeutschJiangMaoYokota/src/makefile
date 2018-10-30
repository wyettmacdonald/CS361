# Robert Durst
# 10/23/18
# CS361 Project 6

# makefile begins
# Ref: http://profesores.elo.utfsm.cl/~agv/elo329/Java/javamakefile.html
# Ref: https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_makefiles.html#java
JFLAGS = -g # compiler flags
JC = javac # compiler variable
JVM= java   # specify JVM
CLASSPATH = proj7AbramsDeutschDurstJones/richtextfx-fat-0.9.1.jar# our only dependency right now -- with more this will be more complex
SOURCEPATH = ./ # specify where the .class files should be found for file by file compilation

.SUFFIXES: .java .class

.java.class:
	$(JC) -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) $(JFLAGS) $*.java

# I think order matters here
CLASSES = \
					proj7AbramsDeutschDurstJones/StyledJavaCodeArea.java \
					proj7AbramsDeutschDurstJones/FileMenuController.java \
					proj7AbramsDeutschDurstJones/Controller.java \
					proj7AbramsDeutschDurstJones/DirectoryController.java \
					proj7AbramsDeutschDurstJones/EditMenuController.java \
					proj7AbramsDeutschDurstJones/ToolBarController.java \
					proj7AbramsDeutschDurstJones/Main.java

# Name of our main class with its preceding path location
MAIN = proj7AbramsDeutschDurstJones.Main

default: classes

classes: $(CLASSES:.java=.class)

# executes the code
# notice the colorn, this is necessary on unix to seperate between classpath and file to execute
run:
	$(JVM) -classpath $(CLASSPATH): $(MAIN)

# cleans up all the compiled class files
clean:
	$(RM) proj7AbramsDeutschDurstJones/*.class


