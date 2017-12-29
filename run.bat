@echo off
setlocal

set classpath=lib\argparse4j-0.8.1.jar;bin\mchw1.jar
set mainclass=mchw1.Main
java -cp %classpath% -XX:MaxHeapSize=1G %mainclass% %*
