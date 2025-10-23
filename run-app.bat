@echo off
echo Building and Running POS System...

REM Set Java paths
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%

REM Set JavaFX path
set JAVAFX_PATH=C:\Program Files\Java\javafx-sdk-22\lib

echo JavaFX path: %JAVAFX_PATH%

REM Create output directory
if not exist "out" mkdir out

REM Download SQLite JDBC driver if not exists
if not exist "sqlite-jdbc.jar" (
    echo Downloading SQLite JDBC driver...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar' -OutFile 'sqlite-jdbc.jar'"
)

REM Compile Java files
echo Compiling Java files...
javac --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -d out -cp "out;sqlite-jdbc.jar" src/main/java/module-info.java src/main/java/com/pos/*.java src/main/java/com/pos/controllers/*.java src/main/java/com/pos/models/*.java src/main/java/com/pos/repository/*.java src/main/java/com/pos/database/*.java src/main/java/com/pos/utils/*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Copy resources
echo Copying resources...
xcopy /E /I /Y src\main\resources out\resources

REM Run the application
echo Starting POS System...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml -cp "out;out/resources;sqlite-jdbc.jar" com.pos.POSApplication

pause
