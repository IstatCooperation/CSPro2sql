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
CsPro2Sql -e generate   -p SURVEY_NAME [-hh HOUSEHOLD_QUEST] [-l LISTING_QUEST] [-ea EA_QUEST] ![new engine](https://img.shields.io/badge/new-engine-brightgreen)
CsPro2Sql -e scan       -p PROPERTIES_FILE ![new engine](https://img.shields.io/badge/new-engine-brightgreen)
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
* `scan`:  check input data, metadata, terrotory structure and database connections
* `schema`:  to create the microdata MySQL script
* `loader`:  to transfer data from the CsPro 7.0 database to the microdata MySQL database
* `monitor`: to create the dashboard MySQL script
* `update`:  to update the dashboard report data
* `status`:  to check the loader engine status

Parameters:
```
 -a,--all                  transfer all the questionnaires
 -cc,--check-constraints   perform constraints check
 -co,--check-only          perform only constraints check (no data transfer)
 -e,--engine <arg>         select engine: [loader|schema|monitor|update|status]
 -f,--force                skip check of loader multiple running instances
 -fk,--foreign-keys        create foreign keys to value sets
 -h,--help                 display this help
 -o,--output <arg>         name of the output file
 -p,--properties <arg>     properties file
 -r,--recovery             recover a broken session of the loader
 -v,--version              print the version of the programm
```

## Configuration

In order to run CsPro2Sql engines it is necessary to configure a properties file. Such file must contain the following properties:

* `db.source.uri`: CsPro 7.0 database connection string
* `db.source.schema`: CsPro 7.0 database schema
* `db.source.username`: CsPro 7.0 database username
* `db.source.password`: CsPro 7.0 database password
* `db.source.data.table`: CsPro 7.0 table containing questionnaires plain data
* `db.dest.uri`: microdata MySQL connection string
* `db.dest.schema`: microdata MySQL schema
* `db.dest.username`: microdata MySQL username
* `db.dest.password`: microdata MySQL password
* `db.dest.table.prefix`: microdata MySQL table prefix

Within this configuration CsPro2Sql reads the CsPro-Dictionary from CsPro 7.0 database. It is also possible to specify a CsPro-Dictionary file:

* `dictionary.filename`: the path to the CsPro-Dictionary file

Optional properties are:

* `multiple.response`: list of items to be considered as a multiple answer (comma separated)
* `ignore.items`: list of items to be ignored (comma separated)

*Note: the source CsPro 7.0 database and the microdata MySQL could be the same*

## Example

Example of properties file (eg. `Household.properties`):
```
# Source CsPro database
db.source.uri=jdbc:mysql://localhost:3306
db.source.schema=cspro
db.source.username=srcUsername
db.source.password=srcPassword
db.source.data.table=household_dict

# Destination microdata MySQL
db.dest.uri=jdbc:mysql://localhost:3306
db.dest.schema=cspro_microdata
db.dest.username=dstUsername
db.dest.password=dstPassword
db.dest.table.prefix=h
```

Execution steps:
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
