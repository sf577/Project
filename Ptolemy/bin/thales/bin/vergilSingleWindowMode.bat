@echo off
rem MSDOS batch script to start ptolemy

rem @author Edward A. Lee
rem @version $Id: vergilSingleWindowMode.bat,v 1.3 2005/02/28 20:52:00 cxh Exp $
rem @copyright: Copyright (c) 1997-2005
rem The Regents of the University of California.

java -Dptolemy.ptII.dir=%PTII% -classpath %PTII%;%PTII%\lib\diva.jar;%PTII%\lib\singleWindow.jar;%PTII%\lib\xml-api.jar;%PTII%\lib\xercesImpl.jar ptolemy.vergil.SingleWindowApplication %1 %2 %3 %4 %5 %6 %7 %8 %9

