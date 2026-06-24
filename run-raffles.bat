@echo off
echo Starting Raffles Hotel Management System...

REM Set JAVA_HOME if not already set
if "%JAVA_HOME%" == "" (
    set JAVA_HOME=C:\Program Files\Java\jdk-22
    echo Setting JAVA_HOME to %JAVA_HOME%
)

REM Create path to JavaFX modules in Maven repository
set M2_REPO=%USERPROFILE%\.m2\repository
set JAVAFX_VERSION=17.0.10
set JAVAFX_BASE=%M2_REPO%\org\openjfx

REM Build module path
set MODULE_PATH=%JAVAFX_BASE%\javafx-base\%JAVAFX_VERSION%\javafx-base-%JAVAFX_VERSION%-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_BASE%\javafx-controls\%JAVAFX_VERSION%\javafx-controls-%JAVAFX_VERSION%-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_BASE%\javafx-fxml\%JAVAFX_VERSION%\javafx-fxml-%JAVAFX_VERSION%-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_BASE%\javafx-graphics\%JAVAFX_VERSION%\javafx-graphics-%JAVAFX_VERSION%-win.jar
set MODULE_PATH=%MODULE_PATH%;%JAVAFX_BASE%\javafx-media\%JAVAFX_VERSION%\javafx-media-%JAVAFX_VERSION%-win.jar

REM Compile the application
echo Compiling the application...
call mvnw.cmd clean compile

REM Download HSQLDB if it doesn't exist
if not exist %M2_REPO%\org\hsqldb\hsqldb\2.7.2\hsqldb-2.7.2.jar (
    echo Downloading HSQLDB...
    call mvnw.cmd dependency:get -Dartifact=org.hsqldb:hsqldb:2.7.2 -DremoteRepositories=https://repo1.maven.org/maven2
)

REM Create full classpath including database driver
set CLASSPATH=target\classes
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\hsqldb\hsqldb\2.7.2\hsqldb-2.7.2.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\com\h2database\h2\2.2.224\h2-2.2.224.jar
set CLASSPATH=%CLASSPATH%;%M2_REPO%\org\hashids\hashids\1.0.3\hashids-1.0.3.jar

REM Run the application with JavaFX modules and full classpath
echo Running the application directly with JavaFX modules...
"%JAVA_HOME%\bin\java" --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.graphics -cp "%CLASSPATH%" com.example.demo10.raffles.hotelmgmt.MainApp

echo Application closed.
exit /b 0 