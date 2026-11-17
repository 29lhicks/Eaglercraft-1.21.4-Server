@echo off
title Build EaglerSkillTree
cd /d "%~dp0"
echo Compiling EaglerSkillTree-1.0.0.jar...
call mvn clean package
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed. Make sure Java 21 and Apache Maven are installed.
    pause
    exit /b %errorlevel%
)
copy /Y "target\EaglerSkillTree-1.0.0.jar" "..\..\server\plugins\EaglerSkillTree-1.0.0.jar" >nul
if %errorlevel% neq 0 (
    echo [ERROR] Could not install the plugin into ..\..\server\plugins.
    pause
    exit /b %errorlevel%
)
echo SUCCESS! EaglerSkillTree-1.0.0.jar is installed in ..\..\server\plugins.
pause
