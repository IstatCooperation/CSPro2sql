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

* `dictionary`: List of CsPro 7 dictionary files (household, freshlist, EA)
* `dictionary.prefix`: Table prefixes in Dashboard database (h, f, ea)

* `territory`: File containing territory data (codes, names)

* `db.source.server`: CsPro 7 database server name or ip address
* `db.source.port`: CsPro 7 database server port
* `db.source.schema`: CsPro 7 database schema
* `db.source.username`: CsPro 7 database username
* `db.source.password`: CsPro 7 database password

* `db.dest.server`: Dashboard database server name or ip address
* `db.dest.port`: Dashboard database server port
* `db.dest.schema`: Dashboard database schema
* `db.dest.username`: Dashboard database server username
* `db.dest.password`: Dashboard database server password
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
