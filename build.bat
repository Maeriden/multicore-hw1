@echo off

rem Pulisci/crea directory di build
IF EXIST "out" RMDIR "out"
MKDIR "out"

javac -O -d "out" -cp "lib\argparse4j-0.8.1.jar;lib\jopt-simple-5.0.4.jar" @win32.javac.files
if %ERRORLEVEL% != 0 GOTO exit

rem Pulisci/crea directory di output 
IF EXIST "bin" RMDIR "bin"
MKDIR "bin"

jar cf "bin\mchw1.jar" -C out .

:EXIT
