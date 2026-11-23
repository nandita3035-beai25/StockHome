@echo off
title Pushing StockHome to GitHub...
cd /d "%~dp0"
echo ===================================================
echo   Pushing StockHome to GitHub
echo   Repository: https://github.com/nandita3035-beai25/StockHome.git
echo ===================================================
echo.
git push -u origin main --force
echo.
if %ERRORLEVEL% EQU 0 (
    echo ===================================================
    echo   [SUCCESS] Pushed to GitHub successfully!
    echo   View your repository at:
    echo   https://github.com/nandita3035-beai25/StockHome
    echo ===================================================
) else (
    echo ===================================================
    echo   [NOTICE] If GitHub asked you to sign in, please
    echo   complete the sign-in in your browser and run
    echo   this file again.
    echo ===================================================
)
echo.
pause
