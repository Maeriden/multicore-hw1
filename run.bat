@echo off

SETLOCAL classpath=lib\argparse4j-0.8.1.jar;lib\jopt-simple-5.0.4.jar;bin\mchw1.jar
SETLOCAL mainclass=mchw1.Main
java -cp %classpath% -XX:MaxHeapSize=2G %mainclass% %*
