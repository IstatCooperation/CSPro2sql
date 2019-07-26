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

## Execution steps

![new engine](https://img.shields.io/badge/new-engine-brightgreen) ** -Engine generate**

Suppose you want to store data collected in a pilot survey (the dictionaries are household and listing)

```
> cspro2sql -e generate -s pilot -hh household -l listing
```

Cspro2sql will generate a set of files and folders to support the configuration activities. The output of the command will be:

```
Starting generation of project pilot
Created folder pilot
Created folder pilot/dictionary
Created folder pilot/territory
Created file pilot/pilot.properties
Created file pilot/README.txt
Created file pilot/dictionary/Household_template.dcf
Created file pilot/dictionary/Listing_template.dcf
Created file pilot/dictionary/README.txt
Created file pilot/territory/territory_template.csv
Created file pilot/territory/README.txt
Project pilot successfully created.
Now you are ready to start processing your data!

Please open the file pilot/README.txt
```

The `README.txt` file in the root folder of the project, provides a step by step guide. 

![new engine](https://img.shields.io/badge/new-engine-brightgreen) **-Engine scan**

At the end of the `[PRELIMINARY STEPS]` execute the scan engine:

```
cspro2sql -e scan -p test/test.properties
```
If you set everithing according to the step by step guide, you should get the following output:

```
Starting property file scan...
[Dictionaries]
- File pilot/dictionary/household.dcf: OK
[Metadata]
Tag #household: OK (DENOMBREMENT_DICT)
Tag #listing: MISSING
Tag #expected: MISSING
Tag #individual: OK (DENOMBREMENT_DICT)
Tag #age: OK (DENOMBREMENT_DICT)
Tag #sex: OK (DENOMBREMENT_DICT)
Tag #religion: MISSING
Tag #territory: OK (DENOMBREMENT_DICT)
Territory structure variable[label]
REGION[REGION] -> DEPART[DEPARTMENT] -> SOUSPREFID[SOUSPREFECTURE] -> P04[COMMUNE] -> P05[ZONE] -> P08[MILIEU]
[Territory]
- File pilot/territory/territory.csv: OK
REGION -> REGION_NAME -> DEPARTMENT -> DEPARTMENT_NAME -> SOUSPREFECTURE -> SOUSPREFECTURE_NAME -> COMMUNE -> COMMUNE_NAME -> ZONE -> ZONE_NAME -> MILIEU -> MILIEU_NAME
Territory file matches metadata. It is possible to generate the territory table!
[Database]
Connecting to jdbc:mysql://localhost:3307/csweb
Connection successful!
Connecting to jdbc:mysql://localhost:3307/dashboard
Connection successful!
...scanning completed!
```

```
> CsPro2Sql -e schema -p Household.properties –o microdata.sql
> mysql -u dstUsername -p < microdata.sql
> CsPro2Sql -e loader -p Household.properties –cc
```

To monitor the loader activity run:
```
> CsPro2Sql -e status -p Household.properties
```

## Warnings

* The CsPro tag `[Relation]` is ignored
* A `ValueSet` with more than 1000 elements is ignored (the threshold will be parameterized in future realesed)

## Acknowledgement
The team responsible of [Census and Survey Processing System (CSPro)](https://www.census.gov/population/international/software/cspro/) 

## License
CSPro2Sql is EUPL-licensed
