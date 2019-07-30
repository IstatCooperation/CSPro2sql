[CSPro2sql]

CsPro2Sql is a Java application to migrate questionnaires from CsPro 7.0 to a MySQL database.

The MySQL database will contain the microdata ie. a column per each variable (Item) defined in the CsPro dictionary files.

[Usage]

To start working with cspro2sql, you need to generate a new project. First of all execute the following command:

> cspro2sql -e generate -s survey

cspro2sql will generate a set of files and folders to support you in all the steps needed to transfer microdata from CSWeb and generate dashboard reports.

A complete overview of the engines provided by cspro2sql are available at:
https://github.com/IstatCooperation/CSPro2sql

[Acknowledgement]

The project has been implemented by Istat cooperation development team.

We would like to acknowledge the team responsible of Census and Survey Processing System (CSPro), for their wonderful support. 

The first release of cspro2sql has been developed in the framework of the Capacity building project in Ethiopia (fourth Population and Housing Census), funded by AICS.