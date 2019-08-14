This file contains the steps to generate and populate the Dashboard database.

[PRELIMINARY STEPS]

[Step 1] Copy your CSPro dictionary files in the folder ethio_pilot2/dictionary

[Step 2] Add cspro2sql metadata to each dictionary. Metadata guidelines are provided in ethio_pilot2/dictionary/README.txt

[Step 3] Set CSPro and Dashboard databases connection parameters in ethio_pilot2/ethio_pilot2.properties

[Step 4] Copy the territory file in the folder ethio_pilot2/territory. Territory guidelines are provided in ethio_pilot2/territory/README.txt

[Step 5] In order to test your environment, execute the following command in a terminal:

cd C:\Users\mbruno\Desktop\CsPro2Sql

cspro2sql -e scan -p ethio_pilot2/ethio_pilot2.properties


[RUNTIME STEPS]

[Step 1] Generate the Dashboard database script ethio_pilot2/dashboard_micro.sql

cd C:\Users\mbruno\Desktop\CsPro2Sql

cspro2sql -e schema -p ethio_pilot2/ethio_pilot2.properties -o ethio_pilot2/dashboard_micro.sql

[Step 2] Create the Dashboard database. Use your favourite Mysql client or execute the following command (replace #root_user with the root username):

mysql -u #root_user -p < ethio_pilot2/dashboard_micro.sql

[Step 3] Load CSPro data in Dashboard database (step to be repeated during fieldwork)

cspro2sql -e loader -p ethio_pilot2/ethio_pilot2.properties

[Step 4] Create and populate the territory table

cspro2sql -e territory -p ethio_pilot2/ethio_pilot2.properties

[Step 5] Generate the Dashboard report tables script ethio_pilot2/dashboard_report.sql

cspro2sql -e monitor -p ethio_pilot2/ethio_pilot2.properties -o ethio_pilot2/dashboard_report.sql

[Step 6] Create the Dashboard report tables. Use your favourite Mysql client or execute the following command(replace #root_user with the root username):

mysql -u #root_user -p < ethio_pilot2/dashboard_report.sql

[Step 7] Update reports (step to be repeated during fieldwork)

cspro2sql -e loader -p ethio_pilot2/ethio_pilot2.properties

cspro2sql -e update -p ethio_pilot2/ethio_pilot2.properties

