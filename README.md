# CSPro2Sql ![New release](https://img.shields.io/badge/new-release%201.0-brightgreen?style=flat-square)

CsPro2Sql is a Java application to migrate questionnaires from [CsPro 7.0](http://www.csprousers.org/beta/) to a MySQL database.

The MySQL database will contain the microdata ie. a column per each variable (`Item`) defined in the CsPro-Dictionary.

## Requirements

Environment:

* Java 1.7+
* MySQL 5.7+

Libraries:

* Apache Commons CLI ([commons-cli-1.3.1.jar](https://commons.apache.org/proper/commons-cli/download_cli.cgi))
* MySQL Connector/J 5.1.40 ([mysql-connector-java-5.1.40-bin.jar](https://dev.mysql.com/downloads/connector/j/))

## Installation

CsPro2Sql is simple to install: all you need is to download and unzip the `CsPro2Sql.zip`. Depending on your system execute from the command line `CsPro2Sql.bat` or `CsPro2Sql.sh`.

## Database configuration ![New release](https://img.shields.io/badge/new-release%201.0-brightgreen?style=flat-square)

This new release of the software solves loader engine performance issues. To get the best performances from your InnoDB you should set the following properties in your my.ini (or my.cnf):

```
innodb_flush_log_at_trx_commit=2
innodb_log_buffer_size=128M
innodb_buffer_pool_size=512M
innodb_log_file_size=256M
```
The first setting will strongly impact the performance on the loaded engine!

In the [db folder](db/) there are two example files for [Mysql 5.7](db/mysql%205.7/my.ini) and [Mysql 8.0](db/mysql%208.0/my.ini)

## Usage

CsPro2Sql is composed of several engines (run `CsPro2Sql` to get usage info):
```
CsPro2Sql -e generate   -p SURVEY_NAME [-hh HOUSEHOLD_QUEST] [-l LISTING_QUEST] [-ea EA_QUEST]
CsPro2Sql -e scan       -p PROPERTIES_FILE
CsPro2Sql -e schema     -p PROPERTIES_FILE [-fk] [-o OUTPUT_FILE]
CsPro2Sql -e loader     -p PROPERTIES_FILE [-a] [-cc] [-co] [-f|-r] [-o OUTPUT_FILE]
CsPro2Sql -e monitor    -p PROPERTIES_FILE [-o OUTPUT_FILE]
CsPro2Sql -e update     -p PROPERTIES_FILE
CsPro2Sql -e status     -p PROPERTIES_FILE
CsPro2Sql -e territory  -p PROPERTIES_FILE
CsPro2Sql -e LU         -p PROPERTIES_FILE
CsPro2Sql -e connection -p PROPERTIES_FILE
```

Engines description:

* `generate`:  generates a cspro2sql project (files and folders needed to execute cspro2sql engines)
* `scan`:  check input data, metadata, territory structure and database connections
* `schema`:  to create the microdata MySQL script
* `loader`:  to transfer data from the CsPro 7.0 database to the microdata MySQL database
* `monitor`: to create the dashboard MySQL script
* `update`:  to update the dashboard report data
* `status`:  to check the loader engine status
* `territory`:  generates the territory table and uploads data from territory.csv file
* `LU`:  load & update (invoked the loader & update engines)
* `connection`:  tests source/destination database connection


Parameters:
```
 -a,--all                  transfer all the questionnaires
 -cc,--check-constraints   perform constraints check
 -co,--check-only          perform only constraints check (no data transfer)
 -e,--engine <arg>         select engine: [loader|schema|monitor|update|status]
 -ea,--enum area <arg>     name of enumeration area dictionary file
 -f,--force                skip check of loader multiple running instances
 -fk,--foreign-keys        create foreign keys to value sets
 -h,--help                 display this help
 -hh,--household <arg>     name of household dictionary file (dafault value is 'household')
 -l,--listing <arg>        name of listing dictionary file
 -o,--output <arg>         name of the output file
 -p,--properties <arg>     properties file
 -r,--recovery             recover a broken session of the loader
 -v,--version              print the version of the programm
```
## Execution steps

![new engine](https://img.shields.io/badge/new-engine-brightgreen) **Engine generate**

Suppose that you are using CSPro to manage data collection process in your `pilot` survey and that you have two dictionaries (household.dcf, listing.dcf). In order to setup a cspro2sql project to manage your `pilot` survey data, execute the following command:

```
> cspro2sql -e generate -s pilot -hh household -l listing
```

Cspro2sql will generate a set of files and folders to support the configuration activities. 
The output of the command will be:

```
Starting generation of project pilot
Created folder pilot
Created folder pilot/dictionary
Created folder pilot/territory
Created file pilot/pilot.properties
Created file pilot/README.txt
Created file pilot/dictionary/household_template.dcf
Created file pilot/dictionary/listing_template.dcf
Created file pilot/dictionary/README.txt
Created file pilot/territory/territory_template.csv
Created file pilot/territory/README.txt
Project pilot successfully created.
Now you are ready to start processing your data!

Please open the file pilot/README.txt
```

The `README.txt` file in the root folder of the project, provides a step by step guide. 

The files `Household_template.dcf` and `Listing_template.dcf`, in the `dictionary` folder, provide examples on cspro2sql metadata (a detailed description in provided in section Metadata).

The file `territory_template.dcf`, in the `territory` folder, provides examples on the territory data (a detailed description in provided in section Territory).


![new engine](https://img.shields.io/badge/new-engine-brightgreen) **Engine scan**

At the end of the `[PRELIMINARY STEPS]` described in the `README.txt`, execute the scan engine:

```
> cspro2sql -e scan -p test/test.properties
```
If you have set everything according to the step-by-step guide, you should get the following output:

```
Starting property file scan...
[DICTIONARIES]
- File popstandashboard/dictionary/household.dcf  OK
- File popstandashboard/dictionary/listing.dcf    OK
- File popstandashboard/dictionary/carto.dcf      OK

[METADATA]
Tag #household               OK (HOUSEHOLD)
Tag #listing                 OK (LISTING)
Tag #expected                OK (CARTOGRAPHY)
Tag #individual              OK (HOUSEHOLD)
Tag #age                     OK (HOUSEHOLD)
Tag #sex                     OK (HOUSEHOLD)
Tag #religion                MISSING
Tag #expectedQuestionnaires  OK (GEOCODES_DICT)
Tag #lat                     OK (LISTING)
Tag #lon                     OK (LISTING)
Tag #territory               OK (HOUSEHOLD, LISTING, CARTOGRAPHY)

[TERRITORY]
- File popstandashboard/territory/territory.csv:  OK
Parsing territory structure...
#Dictionary
PROVINCE[Province] -> DISTRICT[District] -> EA[EA]
#Territory file
Province -> Province_NAME -> District -> District_NAME -> EA -> EA_NAME
Territory file matches metadata. It is possible to generate the territory table!
[Database]
Connecting to jdbc:mysql://localhost:3307/csweb
Connection successful!
Connecting to jdbc:mysql://localhost:3307/dashboard
Connection successful!
...scanning completed!
```

**Engine schema & loader**

Now you are ready to generate the microdata database and store CSPro data.

```
> cspro2sql -e schema -p Household.properties –o microdata.sql
> mysql -u dstUsername -p < microdata.sql
> cspro2sql -e loader -p Household.properties –cc
```

**Engine territory**

Generate and populate the territory table.

```
> cspro2sql -e territory -p test/test.properties
```

**Engine monitor & update**

Generate report tables and calculate reports.

```
> cspro2sql -e monitor -p test/test.properties -o test/dashboard_report.sql
> mysql -u dstUsername -p < test/dashboard_report.sql
> cspro2sql -e update -p test/test.properties
```


To monitor the loader activity run:
```
> CsPro2Sql -e status -p Household.properties
```

## Metadata

In order to generate dashboard reports it is necessary to add metadata to CSPro dictionaries. Metadata are classified in:

* `dictionary`:  these metadata are used to mark dictionaries (household, listing, ea) and to mark `individual` record
* `variable`:  these metadata are used to mark variables (i.e. sex, age, latitude, longitude, etc.)
* `territory`:  these metadata are used to mark the territory structure

The list of metadata is provided below:

**Dictionary metadata**

* `household`:  use this tag to mark the household dictionary [MANDATORY]
* `individual`:  use this tag to mark the individual table [MANDATORY]
* `listing`:  use this tag to mark the listing dictionary 
* `expected`:  use this tag to mark the EA code dictionary


**Variable metadata**

* `age`:  use this tag to mark the age variable. It is also necessary to specify the range of variable [MANDATORY]
* `sex`:  use this tag to mark the sex variable. It is also necessary to mark in the valueset the Male/Female values [MANDATORY]
* `religion`:  use this tag to mark the religion variable
* `expectedQuestionnaires`:  use this tag to mark the expected households from cartograhpy
* `lat`:  use this tag to mark the latitude of the household
* `lon`:  use this tag to mark the longitude of the household

**Territory metadata**

The territory metadata allow to specify the territorial hierarchy. Suppose that your hierarchy is the following:

Region -> Province -> Commune -> EA

Further let us suppose that in your Household dictionary the variables related to your territory structure are:

```
ID101 -> Region
ID102 -> Province
ID103 -> Commune
ID104 -> EA
```
In order to bind variables and territorial hiedarchy it is necessary to add the following notes (check the Household_template.dcf file):

```
[Item]
Label=101 Region
Name=ID101
Note=#territory[Region]

[Item]
Label=102 Province
Name=ID102
Note=#territory[Province, ID101]

[Item]
Label=103 Commune
Name=ID103
Note=#territory[Commune, ID102]

[Item]
Label=104 EA
Name=ID104
Note=#territory[EA, ID103]
```

The territory.csv file should have the following columns:
```
Region; Region_NAME; Province; Province_NAME; Commune; Commune_NAME; EA; EA_NAME
```

## Configuration

In order to run CsPro2Sql engines it is necessary to configure a properties file. Such file must contain the following properties:

```
#[CSPro] List of CSPro dictionaries (household, freshlist, EA)
dictionary=test/dictionary/household.dcf

#[Dashboard] Table prefixes in Dashboard database (household, freshlist, EA)
dictionary.prefix=h

#[Territory] File containing territory data (codes, values)
territory=test/territory/territory.csv

#[CSPro] Specify CSWEB database connection parameters
db.source.server=localhost
db.source.port=3307
db.source.schema=bdcivcensus2019
db.source.username=root
db.source.password=root

#[Dashboard] Specify Dashboard database connection parameters
db.dest.server=localhost
db.dest.port=3307
db.dest.schema=test2
db.dest.username=root
db.dest.password=root
```

Optional properties are:

* `multiple.response`: list of items to be considered as a multiple answer (comma separated)
* `ignore.items`: list of items to be ignored (comma separated)

*Note: the source CsPro 7.0 database and the microdata MySQL could be the same*

Example of properties file (eg. `household.properties`):
```
#[CSPro] List of CSPro dictionaries (household, freshlist, EA)
dictionary=survey/dictionary/household.dcf, survey/dictionary/listing.dcf

#[Dashboard] Table prefixes in Dashboard database (household, freshlist, EA)
dictionary.prefix=h,l

#[Territory] File containing territory data (codes, values)
territory=test/territory/territory.csv

#[CSPro] Specify CSWEB database connection parameters
db.source.server=localhost
db.source.port=3307
db.source.schema=csweb
db.source.username=root
db.source.password=root

#[Dashboard] Specify Dashboard database connection parameters
db.dest.server=localhost
db.dest.port=3307
db.dest.schema=dashboard
db.dest.username=root
db.dest.password=root
```

## Warnings

* The CsPro tag `[Relation]` is ignored
* A `ValueSet` with more than 1000 elements is ignored (the threshold will be parameterized in future realesed)


## Acknowledgement
The team responsible of [Census and Survey Processing System (CSPro)](https://www.census.gov/population/international/software/cspro/).

The first release of cspro2sql has been developed in the framework of the Capacity building project in Ethiopia (fourth Population and Housing Census), funded by AICS

## License
CSPro2Sql is EUPL-licensed
