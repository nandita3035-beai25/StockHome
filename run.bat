@echo off
title Mall of Tomorrow - Smart Inventory Demand Predictor
echo ============================================================
echo   MALL OF TOMORROW - SMART INVENTORY DEMAND PREDICTOR
echo ============================================================
echo Compiling Java project (Console + Swing GUI)...

if not exist bin mkdir bin
javac -d bin src\com\mall\*.java src\com\mall\exceptions\*.java src\com\mall\model\*.java src\com\mall\service\*.java src\com\mall\ui\*.java src\com\mall\ui\gui\*.java src\com\mall\util\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Ensure JDK 8+ is installed and configured in PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful!
echo.

if "%1"=="--console" (
    echo Starting Console Interface...
    java -cp bin com.mall.Main %*
) else (
    echo Starting Graphical Interface [Swing/AWT]...
    echo [Tip: Run 'run.bat --console' anytime to launch the terminal console version]
    java -cp bin com.mall.ui.gui.GuiMain %*
)
