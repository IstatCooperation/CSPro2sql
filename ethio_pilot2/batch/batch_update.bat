@ECHO OFF
echo  %date%-%time%
ECHO Starting batch_update execution at %date% – %time% >> D:\git\CSPro2sql\batch\log\batch_update.log
java -cp D:\git\CSPro2sql\lib\cspro2sql.jar;D:\git\CSPro2sql\lib\commons-cli-1.3.1.jar;D:\git\CSPro2sql\lib\mysql-connector-java-8.0.16.jar cspro2sql.Main %* -e update -p D:\git\CSPro2sql\pilot\pilot.properties >> D:\git\CSPro2sql\pilot\batch\log\batch_update.log 2>>&1
