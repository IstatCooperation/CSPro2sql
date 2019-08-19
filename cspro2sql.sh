#!/bin/bash

java -Xmx1048m -Xms512m -cp 'lib/cspro2sql.jar:lib/commons-cli-1.3.1.jar:lib/mysql-connector-java-8.0.16.jar' cspro2sql.Main $@

