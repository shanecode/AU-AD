@echo off
title Aion-Unique GameServer Console

REM Start...
:start
echo Starting Aion-Unique Game Server.
echo.

REM SET PATH="Type here your path to java jdk/jre (including bin folder)."
REM NOTE: Remove tag REM from previous line.

REM -------------------------------------
REM Default parameters for a basic server.
<<<<<<< HEAD:AE-go_GameServer/dist/StartGS.bat
java -Xms512m -Xmx1024m -ea -Xbootclasspath/p:./libs/jsr166.jar -javaagent:libs/ae_commons.jar -cp ./libs/*;ae_gameserver.jar com.aionemu.gameserver.GameServer
=======
java -Xms512m -Xmx1536m -ea -Xbootclasspath/p:./libs/jsr166.jar -javaagent:libs/ae_commons.jar -cp ./libs/*;ae_gameserver.jar com.aionemu.gameserver.GameServer
>>>>>>> trunk:AE-go_GameServer/dist/StartGS.bat
REM -------------------------------------

SET CLASSPATH=%OLDCLASSPATH%

if ERRORLEVEL 2 goto restart
if ERRORLEVEL 1 goto error
if ERRORLEVEL 0 goto end

REM Restart...
:restart
echo.
echo Administrator Restart ...
echo.
goto start

REM Error...
:error
echo.
echo Server is terminated abnormaly ...
echo.
goto end

REM End...
:end
echo.
echo Server is terminated ...
echo.
pause
