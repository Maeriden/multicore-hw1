@echo off
setlocal

if not exist "out" mkdir "out"
if not exist "bin" mkdir "bin"

if exist "out\mchw1" rmdir /S /Q "out\mchw1"
if exist "bin\mchw1.jar" del "bin\mchw1.jar"

set classpath="lib\argparse4j-0.8.1.jar"
set sources=@win32.files.javac

javac -O -d "out" -cp %classpath% %sources%
if not %ERRORLEVEL% == 0 goto EXIT

jar cf "bin\mchw1.jar" -C out .

:EXIT
