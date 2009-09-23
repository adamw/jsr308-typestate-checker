#!/bin/bash

###
### Fill this in
###

export JAVA_HOME=/Users/adamw/jsr308/soylatte16-i386-1.0.3
export CHECKERS_HOME=/Users/adamw/jsr308/checkers

###

export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=$CHECKERS_HOME/checkers.jar:typestate-checker.jar

### We want to use the typestate checker.
CHECKER=checkers.typestate.TypestateChecker

### Select which example you want to check. You can also specify your own sources here.

SOURCES=src/checkers/typestate/ioexample/Example1.java
#SOURCES=src/checkers/typestate/ioexample/Example2.java
#SOURCES=src/checkers/typestate/iteratorexample/Example3.java
#SOURCES=src/checkers/typestate/iteratorexample/Example4.java

SOURCEPATH=jdk:src:states

javac -processor $CHECKER -proc:only -sourcepath $SOURCEPATH -cp $CLASSPATH $SOURCES
