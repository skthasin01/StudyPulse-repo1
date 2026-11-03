@echo off
rem Compiles and starts StudyPulse without NetBeans. Needs JDK 8 or newer.
rem Uses JAVA_HOME when it is set, otherwise javac / java from PATH.
cd /d "%~dp0"

set "JAVAC=javac"
set "JAVA=java"
if defined JAVA_HOME (
    set "JAVAC=%JAVA_HOME%\bin\javac"
    set "JAVA=%JAVA_HOME%\bin\java"
)

if not exist build\classes mkdir build\classes
"%JAVAC%" -nowarn -encoding UTF-8 -source 1.8 -target 1.8 -d build\classes src\studypulse\*.java src\studypulse\forms\*.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
"%JAVA%" -cp build\classes studypulse.Main
