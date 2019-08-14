@ECHO OFF
echo  %date%-%time%
ECHO Starting batch_load execution at %date% – %time% >> C:\Users\UTENTE\Desktop\CsPro2Sql\ethio_pilot2\batch\log\batch_load.log
java -cp C:\Users\UTENTE\Desktop\CSPro2sql\lib\cspro2sql.jar;C:\Users\UTENTE\Desktop\CSPro2sql\lib\commons-cli-1.3.1.jar;C:\Users\UTENTE\Desktop\CSPro2sql\lib\mysql-connector-java-8.0.16.jar cspro2sql.Main %* -e loader -p C:\Users\UTENTE\Desktop\CsPro2Sql\ethio_pilot2\ethio_pilot2.properties -f >> C:\Users\UTENTE\Desktop\CsPro2Sql\ethio_pilot2\batch\log\batch_load.log 2>>&1
