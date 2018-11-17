# Robert Durst
# 10/23/18
# CS361 Project 6

# makefile begins
# Ref: http://profesores.elo.utfsm.cl/~agv/elo329/Java/javamakefile.html
# Ref: https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_makefiles.html#java
JFLAGS = -g # compiler flags
JC = javac # compiler variable
JVM= java   # specify JVM
CLASSPATH = "proj9AbramsDeutschDurstJones/resources/*": # maybe not most elegant, or best practices, but ok for now (we get all them jars)
SOURCEPATH = ./ # specify where the .class files should be found for file by file compilation

.SUFFIXES: .java .class

.java.class:
	$(JC) -classpath $(CLASSPATH) -sourcepath $(SOURCEPATH) $(JFLAGS) $*.java

# I think order matters here
CLASSES = \
					proj9AbramsDeutschDurstJones/StyledJavaCodeArea.java \
					proj9AbramsDeutschDurstJones/FileMenuController.java \
					proj9AbramsDeutschDurstJones/Controller.java \
					proj9AbramsDeutschDurstJones/CodeMenuController.java \
					proj9AbramsDeutschDurstJones/SettingMenuController.java \
					proj9AbramsDeutschDurstJones/StructureViewController.java \
					proj9AbramsDeutschDurstJones/JavaTab.java \
					proj9AbramsDeutschDurstJones/JavaTabPane.java \
					proj9AbramsDeutschDurstJones/DirectoryController.java \
					proj9AbramsDeutschDurstJones/EditMenuController.java \
					proj9AbramsDeutschDurstJones/ToolBarController.java \
					proj9AbramsDeutschDurstJones/Main.java

# Name of our main class with its preceding path location
MAIN = proj9AbramsDeutschDurstJones.Main

default: classes

classes: $(CLASSES:.java=.class)

# executes the code
# notice the colorn, this is necessary on unix to seperate between classpath and file to execute
run:
	$(JVM) -classpath $(CLASSPATH) $(MAIN)

# cleans up all the compiled class files
clean:
	$(RM) proj9AbramsDeutschDurstJones/*.class


