# Robert Durst
# 11/09/18
# CS361 Project 8
 
# makefile begins
# Ref: http://profesores.elo.utfsm.cl/~agv/elo329/Java/javamakefile.html
# Ref: https://www.cs.swarthmore.edu/~newhall/unixhelp/howto_makefiles.html#java
FLAGS = -g # compiler flags 
JC = javac # compiler variable
JVM= java   # specify JVM
SOURCEPATH = ./ # specify where the .class files should be found for file by file compilation

.SUFFIXES: .java .class
  
.java.class:
  $(JC) -sourcepath $(SOURCEPATH) $(JFLAGS) $*.java
  
# I think order matters here
CLASSES = \
          proj8AbramsDeutschDurstJones/NontrivialBracesCounter.java
  
# Name of our main class with its preceding path location
MAIN = proj8AbramsDeutschDurstJones.NontrivialBracesCounter
  
default: classes
  
classes: $(CLASSES:.java=.class)
  
# executes the code
# notice the colon, this is necessary on unix to seperate between classpath and file to execute
run:
  $(JVM) -classpath $(CLASSPATH): $(MAIN)
  
# cleans up all the compiled class files
clean:
  $(RM) proj8AbramsDeutschDurstJones/*.class
  
