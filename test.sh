#!/bin/bash
export PATH=/Users/adamw/jsr308/soylatte16-i386-1.0.3/bin:$PATH
export CLASSPATH=/Users/adamw/jsr308/checkers/checkers.jar:/Users/adamw/jsr308/jsr308-typestate-checker/build

#CHECKER=checkers.nullness.NullnessChecker
#CHECKER=checkers.javari.JavariChecker
CHECKER=checkers.typestate.TypestateChecker

SOURCES=tests/work/work/Test3.java
#SOURCES=src/checkers/typestate/Any.java
#SOURCES=tests/typestate/SimpleReceiverTransitionState.java

SOURCEPATH=src:../checkers/jdk/nullness/src

javac -processor $CHECKER -proc:only -sourcepath $SOURCEPATH -cp $CLASSPATH -d build $SOURCES
