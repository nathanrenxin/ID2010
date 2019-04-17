@Echo Off
REM -- ----------------------------------------------------------------
REM -- 2018-08-24/fki Lab 2 version 13
REM -- This file starts a Jini Lookup Server (reggie) on Windows.
REM -- ----------------------------------------------------------------
REM --

Set LABROOT=%~dp0..\..

Set PCY=%LABROOT%\lib\policy.all

Set LOG=%TEMP%\reggie_log

SET CFG=%~dp0.\HTTPD_CFG.BAT

IF NOT EXIST "%CFG%" ECHO File not found %CFG% & Goto :eof

CALL "%CFG%"
SET HTTP=%CODEBASE%

SET CBS=%HTTP%/reggie-dl.jar

SET JAR=%LABROOT%\lib\reggie.jar

Echo ******************************
Echo Starting a Jini Lookup Service
Echo PCY = %PCY%
Echo LOG = %LOG%
Echo CBS = %CBS%
Echo JAR = %JAR%

REM ********************************
REM Remove the old log
REM R[emove]D[irectory] /S[earch the tree] /Q[uietly]
REM ********************************

If Not Exist "%LOG%" Goto LogIsGone
Echo Removing %LOG%
RD /S /Q "%LOG%"
:LogIsGone

REM ********************************
REM Disable the classpath variable.
REM ********************************

Set CLASSPATH=

Echo ******************************

REM ************************
REM Invoke the program.
REM Since we are running reggie here, the 'public' at the end of
REM the arguments refers to the group which should be served.
REM 
REM The expected behaviour is that reggie registers with rmid as
REM an activatable service and then exits, ie the command prompt
REM reappears. The command window can be used again.
REM ************************

Set CBO=-Djava.rmi.server.useCodebaseOnly=false

java %CBO% -jar "%JAR%" "%CBS%" "%PCY%" "%LOG%" public
