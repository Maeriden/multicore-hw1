#!/bin/sh

classpath='lib/argparse4j-0.8.1.jar:bin/mchw1.jar'
mainclass='mchw1.Main'
java -cp ${classpath} -XX:MaxHeapSize=2G ${mainclass} "$@"
