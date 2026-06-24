@echo off
echo Starting Raffles Hotel Management System...

REM First, compile with Maven if needed
call mvnw.cmd clean compile

REM Use Maven's JavaFX plugin to run the application (preferred method)
call mvnw.cmd javafx:run

REM If you want to run manually, uncomment these lines and adjust paths
REM SET JAVAFX_PATH="C:\path\to\javafx-sdk\lib"
REM java --module-path %JAVAFX_PATH% --add-modules javafx.controls,javafx.fxml,javafx.media,javafx.graphics -cp target\classes com.example.demo10.raffles.hotelmgmt.MainApp

echo Application closed. 