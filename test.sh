#!/bin/bash
export PATH=/Users/adamwarski/jsr308/soylatte16-i386-1.0.3/bin:$PATH
export CLASSPATH=/Users/adamwarski/jsr308/checkers/checkers.jar:/Users/adamwarski/jsr308/typestatechecker/build

#CHECKER=checkers.nullness.NullnessChecker
#CHECKER=checkers.javari.JavariChecker
CHECKER=checkers.typestate.TypestateChecker

SOURCES=tests/work/work/Test1.java
#SOURCES=tests/typestate/SimpleReceiverTransitionState.java

javac -processor $CHECKER -sourcepath src -cp $CLASSPATH -d build $SOURCES
