#!/bin/bash
export PATH=/Users/adamw/jsr308/soylatte16-i386-1.0.3/bin:$PATH
export CLASSPATH=/Users/adamw/jsr308/checkers/checkers.jar:/Users/adamw/jsr308/jsr308-typestate-checker/build

CHECKER=checkers.typestate.TypestateChecker

SOURCES=example/src/checkers/typestate/ioexample/Example2.java

SOURCEPATH=example/jdk/src:example/src

javac -processor $CHECKER -proc:only -sourcepath $SOURCEPATH -cp $CLASSPATH -d build $SOURCES
