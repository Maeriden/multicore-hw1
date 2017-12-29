#!/bin/bash


[ -d "out/" ]          || mkdir "out/"
[ -d "bin/" ]          || mkdir "bin/"

[ -d "out/mchw1" ]     && rmdir "out/mchw1"
[ -f "bin/mchw1.jar" ] && rm "bin/mchw1.jar"

classpath='lib/argparse4j-0.8.1.jar'
sources=$(find src/ -type f -name '*.java')

javac -O -d 'out/' -cp ${classpath} ${sources}

if [[ $? == 0 ]]; then
	jar cf bin/mchw1.jar -C out .
fi
