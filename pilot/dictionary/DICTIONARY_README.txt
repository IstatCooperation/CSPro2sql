This file contains the list of metadata that you should add to your dictionaries.

[METADATA @dictionary level]

#household: use this tag to mark the household dictionary (check the 'Note=#fieldwork' in the Household_template.dcf file)

#individual: use this tag to mark the individual table (check the 'Note=#individual' in the Household_template.dcf file)

#listing: use this tag to mark the listing dictionary (check the 'Note=#listing' in the Listing_template.dcf file)

#expected: use this tag to mark the EA code dictionary (check the 'Note=#expected' in the Eacode_template.dcf file)

#household and #individual tags are MANDATORY

[METADATA @variable level]

#age: use this tag to mark the age variable. It is also necessary to specify the range of variable (check the 'Note=#age' in the Household_template.dcf file)

#sex: use this tag to mark the sex variable. It is also necessary to mark in the valueset the Male/Female values (check the 'Note=#sex' in the Household_template.dcf file)

#religion: use this tag to mark the religion variable (check the Note in the Household_template.dcf file)

[METADATA @territory level]

The territory metadata allow to specify the territorial hierarchy. Suppose that your hierarchy is the following:

Region -> Province -> Commune -> EA

Further let us suppose that in your Household dictionary the variables related to your territory structure are:

ID101 -> Region

ID102 -> Province

ID103 -> Commune

ID104 -> EA

In order to bind variables and territorial hiedarchy it is necessary to add the following notes (check the Household_template.dcf file):

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

The territory metadata are MANDATORY

