#!/bin/sh


[ -d 'out/' ] || mkdir 'out/'
[ -d 'bin/' ] || mkdir 'bin/'

[ -d 'out/mchw1']      && rm -r 'out/mchw1'
[ -f 'bin/mchw1.jar' ] && rm    'bin/mchw1.jar'

classpath='lib/argparse4j-0.8.1.jar'
sources=$(find src/ -type f -name '*.java')

javac -O -d 'out/' -cp ${classpath} ${sources}
[ $? -eq 0 ] || exit 1

jar cf bin/mchw1.jar -C out .
[ $? -eq 0 ] || exit 2

[ -f 'bin/argparse4j-0.8.1.jar' ] || cp 'lib/argparse4j-0.8.1.jar' 'bin/'
