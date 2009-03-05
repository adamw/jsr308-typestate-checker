#!/bin/bash
export PATH=/Users/adamwarski/jsr308/soylatte16-i386-1.0.3/bin:$PATH
export CLASSPATH=/Users/adamwarski/jsr308/checkers/checkers.jar:/Users/adamwarski/jsr308/typestatechecker/build
export DEBUGOPTS="-J-Xms48m -J-Xdebug -J-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
CHECKER=checkers.typestate.TypestateChecker
javac $DEBUGOPTS -processor $CHECKER -sourcepath src -cp $CLASSPATH -d build src/checkers/typestate/test/Test1.java src/checkers/typestate/test/Test2.java
