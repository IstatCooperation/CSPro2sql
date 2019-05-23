CREATE OR REPLACE VIEW @SCHEMA.`r_individual_info` AS
    SELECT 
        ANY_VALUE(`total`.`total`) AS `total`,
        ANY_VALUE(`total`.`age_avg`) AS `age_avg`,
        ANY_VALUE(`total`.`age_max`) AS `age_max`,
        ANY_VALUE(`male`.`total_male`) AS `total_male`,
        ANY_VALUE(`male`.`age_male_avg`) AS `age_avg_male`,
        ANY_VALUE(`male`.`age_male_max`) AS `age_max_male`,
        ANY_VALUE(`female`.`total_female`) AS `total_female`,
        ANY_VALUE(`female`.`age_female_avg`) AS `age_avg_female`,
        ANY_VALUE(`female`.`age_female_max`) AS `age_max_female`
    FROM
        ((((SELECT 
            COUNT(0) AS `total`,
                AVG(@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) AS `age_avg`,
                MAX(@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) AS `age_max`
        FROM
            @SCHEMA.@INDIVIDUAL_TABLE)) `total`
        JOIN (SELECT 
            COUNT(0) AS `total_male`,
                AVG(@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) AS `age_male_avg`,
                MAX(@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) AS `age_male_max`
        FROM
            @SCHEMA.@INDIVIDUAL_TABLE
        WHERE
            (@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_SEX = @INDIVIDUAL_VALUE_SEX_MALE)) `male`)
        JOIN (SELECT 
            COUNT(0) AS `total_female`,
                AVG(@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) AS `age_female_avg`,
                MAX(@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) AS `age_female_max`
        FROM
            @SCHEMA.@INDIVIDUAL_TABLE
        WHERE
            (@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_SEX = @INDIVIDUAL_VALUE_SEX_FEMALE)) `female`);

