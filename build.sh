#!/bin/bash

[ -d out/ ] || mkdir out/
$(find out/ -type f -name '*.class' -delete)

classpath='lib/argparse4j-0.8.1.jar:lib/jopt-simple-5.0.4.jar:bin/mchw1.jar'
sources=$(find src/ -type f -name '*.java')

javac -O -d 'out/' -cp ${classpath} ${sources}


if [[ $? == 0 ]]; then
	[ -d bin/ ] || mkdir bin/
	
	jar cf bin/mchw1.jar -C out .
fi
