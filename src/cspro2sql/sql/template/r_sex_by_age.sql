CREATE OR REPLACE VIEW @SCHEMA.`r_sex_by_age` AS
    SELECT 
        `a`.`p308` AS `age`,
        `a`.`total` AS `total`,
        `b`.`total_male` AS `total_male`,
        `c`.`total_female` AS `total_female`
    FROM
        (((SELECT 
            @SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE AS `p308`,
                COUNT(0) AS `total`
        FROM
            @SCHEMA.@INDIVIDUAL_TABLE
        GROUP BY @SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) `a`
        JOIN (SELECT 
            @SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE AS `p308`,
                COUNT(0) AS `total_male`
        FROM
            @SCHEMA.@INDIVIDUAL_TABLE
        WHERE
            (@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_SEX = @INDIVIDUAL_VALUE_SEX_MALE)
        GROUP BY @SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) `b`)
        JOIN (SELECT 
            @SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE AS `p308`,
                COUNT(0) AS `total_female`
        FROM
            @SCHEMA.@INDIVIDUAL_TABLE
        WHERE
            (@SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_SEX = @INDIVIDUAL_VALUE_SEX_FEMALE)
        GROUP BY @SCHEMA.@INDIVIDUAL_TABLE.@INDIVIDUAL_COLUMN_AGE) `c`)
    WHERE
        ((`a`.`p308` = `b`.`p308`)
            AND (`b`.`p308` = `c`.`p308`));

