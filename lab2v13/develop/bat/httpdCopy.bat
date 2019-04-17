@REM ** ************************************************************
@REM ** 2018-08-27/fki Lab 2 version 13
@REM **
@REM ** Copies files so that they can be served by the HTTPd server.
@REM **
@REM ** ************************************************************
@ECHO OFF

SET TGT=%~dp0..\..\test\cbs

XCOPY /F /Z /Y "%1" "%TGT%\"
