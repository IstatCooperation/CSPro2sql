The structure of the territory file is strongly connected to the territory metadata specified in your dictionaries.
More specifically if your hierarchy is the following:

Region -> Province -> Commune -> EA

The territory.csv file should have the following columns:

Region; Region_NAME; Province; Province_NAME; Commune; Commune_NAME; EA; EA_NAME

Therefore at each level of the hierarchy correspond two columns (code, description).
The name of the columns shoud be the same that you specified in the territory notes.

