USE dashboard_pilot_2;

CREATE TABLE IF NOT EXISTS dashboard_pilot_2.DASHBOARD_INFO (
    `ID` int(1) NOT NULL DEFAULT 0,
    `LISTING` int(1) NOT NULL DEFAULT 0,
    `EXPECTED` int(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`ID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
INSERT INTO dashboard_pilot_2.DASHBOARD_INFO values (0, 1, 1);

CREATE OR REPLACE VIEW dashboard_pilot_2.`r_questionnaire_info` AS
    SELECT 
        COUNT(0) AS `total`,
        ANY_VALUE(`avg_individual`.`avg_individual`) AS `avg_individual`,
        ANY_VALUE(`avg_individual_male`.`avg_individual_male`) AS `avg_individual_male`,
        ANY_VALUE(`avg_individual_female`.`avg_individual_female`) AS `avg_individual_female`
    FROM
        (((dashboard_pilot_2.H_HOUSEHOLD_QUEST
        JOIN (SELECT 
            AVG(`a`.`num`) AS `avg_individual`
        FROM
            (SELECT 
            COUNT(0) AS `num`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        GROUP BY dashboard_pilot_2.H_INDIVIDUAL.HOUSEHOLD_QUEST) `a`) `avg_individual`)
        JOIN (SELECT 
            AVG(`a`.`num`) AS `avg_individual_male`
        FROM
            (SELECT 
            COUNT(0) AS `num`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        WHERE
            (dashboard_pilot_2.H_INDIVIDUAL.P307 = 1)
        GROUP BY dashboard_pilot_2.H_INDIVIDUAL.HOUSEHOLD_QUEST) `a`) `avg_individual_male`)
        JOIN (SELECT 
            AVG(`a`.`num`) AS `avg_individual_female`
        FROM
            (SELECT 
            COUNT(0) AS `num`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        WHERE
            (dashboard_pilot_2.H_INDIVIDUAL.P307 = 2)
        GROUP BY dashboard_pilot_2.H_INDIVIDUAL.HOUSEHOLD_QUEST) `a`) `avg_individual_female`);

DROP TABLE IF EXISTS dashboard_pilot_2.mr_questionnaire_info;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_questionnaire_info (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_questionnaire_info.* FROM dashboard_pilot_2.r_questionnaire_info;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Household','r_questionnaire_info', 0, 1, 2);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_individual_info` AS
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
                AVG(dashboard_pilot_2.H_INDIVIDUAL.P308) AS `age_avg`,
                MAX(dashboard_pilot_2.H_INDIVIDUAL.P308) AS `age_max`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL)) `total`
        JOIN (SELECT 
            COUNT(0) AS `total_male`,
                AVG(dashboard_pilot_2.H_INDIVIDUAL.P308) AS `age_male_avg`,
                MAX(dashboard_pilot_2.H_INDIVIDUAL.P308) AS `age_male_max`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        WHERE
            (dashboard_pilot_2.H_INDIVIDUAL.P307 = 1)) `male`)
        JOIN (SELECT 
            COUNT(0) AS `total_female`,
                AVG(dashboard_pilot_2.H_INDIVIDUAL.P308) AS `age_female_avg`,
                MAX(dashboard_pilot_2.H_INDIVIDUAL.P308) AS `age_female_max`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        WHERE
            (dashboard_pilot_2.H_INDIVIDUAL.P307 = 2)) `female`);

DROP TABLE IF EXISTS dashboard_pilot_2.mr_individual_info;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_individual_info (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_individual_info.* FROM dashboard_pilot_2.r_individual_info;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Population','r_individual_info', 1, 1, 2);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_religion` AS
    SELECT 
        `vs`.`VALUE` AS `RELIGION`, COUNT(0) AS `INDIVIDUALS`
    FROM
        (dashboard_pilot_2.H_INDIVIDUAL `i`
        JOIN dashboard_pilot_2.VSH_P309 `vs` ON ((`i`.P309 = `vs`.`ID`)))
    GROUP BY `vs`.`VALUE`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_religion;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_religion (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_religion.* FROM dashboard_pilot_2.r_religion;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Religion','r_religion', 2, 1, 2);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_sex_by_age` AS
    SELECT 
        `a`.`p308` AS `age`,
        `a`.`total` AS `total`,
        `b`.`total_male` AS `total_male`,
        `c`.`total_female` AS `total_female`
    FROM
        (((SELECT 
            dashboard_pilot_2.H_INDIVIDUAL.P308 AS `p308`,
                COUNT(0) AS `total`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        GROUP BY dashboard_pilot_2.H_INDIVIDUAL.P308) `a`
        JOIN (SELECT 
            dashboard_pilot_2.H_INDIVIDUAL.P308 AS `p308`,
                COUNT(0) AS `total_male`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        WHERE
            (dashboard_pilot_2.H_INDIVIDUAL.P307 = 1)
        GROUP BY dashboard_pilot_2.H_INDIVIDUAL.P308) `b`)
        JOIN (SELECT 
            dashboard_pilot_2.H_INDIVIDUAL.P308 AS `p308`,
                COUNT(0) AS `total_female`
        FROM
            dashboard_pilot_2.H_INDIVIDUAL
        WHERE
            (dashboard_pilot_2.H_INDIVIDUAL.P307 = 2)
        GROUP BY dashboard_pilot_2.H_INDIVIDUAL.P308) `c`)
    WHERE
        ((`a`.`p308` = `b`.`p308`)
            AND (`b`.`p308` = `c`.`p308`));

DROP TABLE IF EXISTS dashboard_pilot_2.mr_sex_by_age;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_sex_by_age (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_sex_by_age.* FROM dashboard_pilot_2.r_sex_by_age;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Sex Distribution','r_sex_by_age', 3, 1, 2);
DROP TABLE IF EXISTS dashboard_pilot_2.`tr_questionnaire_info`;
CREATE TABLE IF NOT EXISTS dashboard_pilot_2.`tr_questionnaire_info` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TOTAL` int(11) DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
DROP TABLE IF EXISTS dashboard_pilot_2.`tr_individual_info`;
CREATE TABLE IF NOT EXISTS dashboard_pilot_2.`tr_individual_info` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TOTAL` int(11) DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_regional_area` AS
  SELECT 'Region' name, COUNT(0) value FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101) a0 UNION
  SELECT 'Zone', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102) a1 UNION
  SELECT 'Woreda', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103) a2 UNION
  SELECT 'Town', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103,ID104) a3 UNION
  SELECT 'Subcity', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103,ID104,ID105) a4 UNION
  SELECT 'Psa', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103,ID104,ID105,ID106) a5 UNION
  SELECT 'SA', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103,ID104,ID105,ID106,ID107) a6 UNION
  SELECT 'Kebele', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103,ID104,ID105,ID106,ID107,ID108) a7 UNION
  SELECT 'EA', COUNT(0) FROM (SELECT COUNT(0) FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST GROUP BY ID101,ID102,ID103,ID104,ID105,ID106,ID107,ID108,ID109) a8
;
DROP TABLE IF EXISTS dashboard_pilot_2.mr_regional_area;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_regional_area (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_regional_area.* FROM dashboard_pilot_2.r_regional_area;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Aux','r_regional_area', 4, 1, 4);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_sex_by_age_group` AS
  SELECT '0 to 4' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 0 AND P308 < 5) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 0 AND P308 < 5) b UNION
  SELECT '5 to 9' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 5 AND P308 < 10) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 5 AND P308 < 10) b UNION
  SELECT '10 to 14' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 10 AND P308 < 15) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 10 AND P308 < 15) b UNION
  SELECT '15 to 19' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 15 AND P308 < 20) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 15 AND P308 < 20) b UNION
  SELECT '20 to 24' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 20 AND P308 < 25) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 20 AND P308 < 25) b UNION
  SELECT '25 to 29' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 25 AND P308 < 30) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 25 AND P308 < 30) b UNION
  SELECT '30 to 34' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 30 AND P308 < 35) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 30 AND P308 < 35) b UNION
  SELECT '35 to 39' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 35 AND P308 < 40) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 35 AND P308 < 40) b UNION
  SELECT '40 to 44' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 40 AND P308 < 45) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 40 AND P308 < 45) b UNION
  SELECT '45 to 49' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 45 AND P308 < 50) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 45 AND P308 < 50) b UNION
  SELECT '50 to 54' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 50 AND P308 < 55) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 50 AND P308 < 55) b UNION
  SELECT '55 to 59' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 55 AND P308 < 60) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 55 AND P308 < 60) b UNION
  SELECT '60 to 64' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 60 AND P308 < 65) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 60 AND P308 < 65) b UNION
  SELECT '65 to 69' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 65 AND P308 < 70) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 65 AND P308 < 70) b UNION
  SELECT '70 to 74' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 70 AND P308 < 75) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 70 AND P308 < 75) b UNION
  SELECT '75 to 79' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 75 AND P308 < 80) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 75 AND P308 < 80) b UNION
  SELECT '80 to 84' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 80 AND P308 < 85) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 80 AND P308 < 85) b UNION
  SELECT '85 to 89' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 85 AND P308 < 90) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 85 AND P308 < 90) b UNION
  SELECT '90 to 94' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 90 AND P308 < 95) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 90 AND P308 < 95) b UNION
  SELECT '95 to 96' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 95 AND P308 < 97) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 95 AND P308 < 97) b UNION
  SELECT '97+' as 'range', a.male, b.female FROM (SELECT COUNT(0) male FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 1 AND P308 >= 97) a,(SELECT COUNT(0) female FROM dashboard_pilot_2.H_INDIVIDUAL WHERE P307 = 2 AND P308 >= 97) b
;
DROP TABLE IF EXISTS dashboard_pilot_2.mr_sex_by_age_group;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_sex_by_age_group (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_sex_by_age_group.* FROM dashboard_pilot_2.r_sex_by_age_group;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Aux','r_sex_by_age_group', 5, 1, 4);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_by_ea` AS
  SELECT concat('Region','#','Zone','#','Woreda','#','Town','#','Subcity','#','Psa','#','SA','#','Kebele','#','EA') as name, null as household
  UNION
  SELECT concat((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1),'#',(SELECT ID105_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 limit 1),'#',(SELECT ID106_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 limit 1),'#',(SELECT ID107_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 limit 1),'#',(SELECT ID108_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 AND ID108 = h.ID108 limit 1),'#',(SELECT ID109_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 AND ID108 = h.ID108 AND ID109 = h.ID109 limit 1)) as name, COUNT(0) AS `household`
  FROM dashboard_pilot_2.H_HOUSEHOLD_QUEST `h`
  GROUP BY `h`.ID101, `h`.ID102, `h`.ID103, `h`.ID104, `h`.ID105, `h`.ID106, `h`.ID107, `h`.ID108, `h`.ID109;
DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_by_ea;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_by_ea (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_by_ea.* FROM dashboard_pilot_2.r_household_by_ea;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Aux','r_household_by_ea', 6, 1, 4);
CREATE OR REPLACE VIEW dashboard_pilot_2.aux_household_returned AS
    SELECT 
        H_HOUSEHOLD_QUEST.ID101 AS ID101,
        H_HOUSEHOLD_QUEST.ID102 AS ID102,
        H_HOUSEHOLD_QUEST.ID103 AS ID103,
        H_HOUSEHOLD_QUEST.ID104 AS ID104,
        H_HOUSEHOLD_QUEST.ID105 AS ID105,
        H_HOUSEHOLD_QUEST.ID106 AS ID106,
        H_HOUSEHOLD_QUEST.ID107 AS ID107,
        H_HOUSEHOLD_QUEST.ID108 AS ID108,
        H_HOUSEHOLD_QUEST.ID109 AS ID109,
        COUNT(0) AS `returned`
    FROM
        dashboard_pilot_2.H_HOUSEHOLD_QUEST
    GROUP BY
        H_HOUSEHOLD_QUEST.ID101,
        H_HOUSEHOLD_QUEST.ID102,
        H_HOUSEHOLD_QUEST.ID103,
        H_HOUSEHOLD_QUEST.ID104,
        H_HOUSEHOLD_QUEST.ID105,
        H_HOUSEHOLD_QUEST.ID106,
        H_HOUSEHOLD_QUEST.ID107,
        H_HOUSEHOLD_QUEST.ID108,
        H_HOUSEHOLD_QUEST.ID109;

CREATE OR REPLACE VIEW dashboard_pilot_2.aux_listing_returned AS
    SELECT 
        L_LISTING_QUEST.L_ID101 AS ID101,
        L_LISTING_QUEST.L_ID102 AS ID102,
        L_LISTING_QUEST.L_ID103 AS ID103,
        L_LISTING_QUEST.L_ID104 AS ID104,
        L_LISTING_QUEST.L_ID105 AS ID105,
        L_LISTING_QUEST.L_ID106 AS ID106,
        L_LISTING_QUEST.L_ID107 AS ID107,
        L_LISTING_QUEST.L_ID108 AS ID108,
        L_LISTING_QUEST.L_ID109 AS ID109,
        COUNT(0) AS `returned`
    FROM
        dashboard_pilot_2.L_LISTING_QUEST
    GROUP BY
        L_LISTING_QUEST.L_ID101,
        L_LISTING_QUEST.L_ID102,
        L_LISTING_QUEST.L_ID103,
        L_LISTING_QUEST.L_ID104,
        L_LISTING_QUEST.L_ID105,
        L_LISTING_QUEST.L_ID106,
        L_LISTING_QUEST.L_ID107,
        L_LISTING_QUEST.L_ID108,
        L_LISTING_QUEST.L_ID109;

CREATE OR REPLACE VIEW dashboard_pilot_2.aux_household_expected AS
    SELECT 
        EA_EA_CODE_QUEST.EA_ID101 AS ID101,
        EA_EA_CODE_QUEST.EA_ID102 AS ID102,
        EA_EA_CODE_QUEST.EA_ID103 AS ID103,
        EA_EA_CODE_QUEST.EA_ID104 AS ID104,
        EA_EA_CODE_QUEST.EA_ID105 AS ID105,
        EA_EA_CODE_QUEST.EA_ID106 AS ID106,
        EA_EA_CODE_QUEST.EA_ID107 AS ID107,
        EA_EA_CODE_REC.EA_ID108 AS ID108,
        EA_EA_CODE_REC.EA_ID109 AS ID109,
        SUM(EA_EA_CODE_REC.EA_HHS) AS `expected`
    FROM
        dashboard_pilot_2.EA_EA_CODE_QUEST
            JOIN dashboard_pilot_2.EA_EA_CODE_REC ON EA_EA_CODE_QUEST.ID = EA_EA_CODE_REC.EA_CODE_QUEST
    GROUP BY
        EA_EA_CODE_QUEST.EA_ID101,
        EA_EA_CODE_QUEST.EA_ID102,
        EA_EA_CODE_QUEST.EA_ID103,
        EA_EA_CODE_QUEST.EA_ID104,
        EA_EA_CODE_QUEST.EA_ID105,
        EA_EA_CODE_QUEST.EA_ID106,
        EA_EA_CODE_QUEST.EA_ID107,
        EA_EA_CODE_REC.EA_ID108,
        EA_EA_CODE_REC.EA_ID109;

CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_region` AS
    SELECT 
        _utf8mb4 'Region' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
    GROUP BY `name`, `h`.`ID101`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_region;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_region (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_region.* FROM dashboard_pilot_2.r_household_expected_by_region;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_REGION','r_household_expected_by_region', 7, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_zone` AS
    SELECT 
        _utf8mb4 'Region#Zone' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_zone;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_zone (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_zone.* FROM dashboard_pilot_2.r_household_expected_by_zone;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_ZONE','r_household_expected_by_zone', 8, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_woreda` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_woreda;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_woreda (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_woreda.* FROM dashboard_pilot_2.r_household_expected_by_woreda;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_WOREDA','r_household_expected_by_woreda', 9, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_town` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda#Town' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `ID104`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        `h`.ID104 AS `ID104`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
            AND (`h`.`ID104` = `l`.`ID104`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
            AND (`h`.`ID104` = `e`.`ID104`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`, `h`.`ID104`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_town;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_town (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_town.* FROM dashboard_pilot_2.r_household_expected_by_town;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_TOWN','r_household_expected_by_town', 10, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_subcity` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda#Town#Subcity' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `ID104`,
        NULL AS `ID105`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1),'#',(SELECT ID105_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        `h`.ID104 AS `ID104`,
        `h`.ID105 AS `ID105`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
                `aux_household_returned`.ID105 AS ID105,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104,
                `aux_household_returned`.ID105
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
                `aux_listing_returned`.ID105 AS ID105,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104,
                `aux_listing_returned`.ID105
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
            AND (`h`.`ID104` = `l`.`ID104`)
            AND (`h`.`ID105` = `l`.`ID105`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
            AND (`h`.`ID104` = `e`.`ID104`)
            AND (`h`.`ID105` = `e`.`ID105`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`, `h`.`ID104`, `h`.`ID105`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_subcity;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_subcity (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_subcity.* FROM dashboard_pilot_2.r_household_expected_by_subcity;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_SUBCITY','r_household_expected_by_subcity', 11, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_psa` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda#Town#Subcity#Psa' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `ID104`,
        NULL AS `ID105`,
        NULL AS `ID106`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1),'#',(SELECT ID105_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 limit 1),'#',(SELECT ID106_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        `h`.ID104 AS `ID104`,
        `h`.ID105 AS `ID105`,
        `h`.ID106 AS `ID106`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
                `aux_household_returned`.ID105 AS ID105,
                `aux_household_returned`.ID106 AS ID106,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104,
                `aux_household_returned`.ID105,
                `aux_household_returned`.ID106
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
                `aux_listing_returned`.ID105 AS ID105,
                `aux_listing_returned`.ID106 AS ID106,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104,
                `aux_listing_returned`.ID105,
                `aux_listing_returned`.ID106
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
            AND (`h`.`ID104` = `l`.`ID104`)
            AND (`h`.`ID105` = `l`.`ID105`)
            AND (`h`.`ID106` = `l`.`ID106`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
                `aux_household_expected`.ID106 AS ID106,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105,
                `aux_household_expected`.ID106
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
            AND (`h`.`ID104` = `e`.`ID104`)
            AND (`h`.`ID105` = `e`.`ID105`)
            AND (`h`.`ID106` = `e`.`ID106`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`, `h`.`ID104`, `h`.`ID105`, `h`.`ID106`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_psa;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_psa (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_psa.* FROM dashboard_pilot_2.r_household_expected_by_psa;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_PSA','r_household_expected_by_psa', 12, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_sa` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda#Town#Subcity#Psa#SA' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `ID104`,
        NULL AS `ID105`,
        NULL AS `ID106`,
        NULL AS `ID107`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1),'#',(SELECT ID105_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 limit 1),'#',(SELECT ID106_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 limit 1),'#',(SELECT ID107_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        `h`.ID104 AS `ID104`,
        `h`.ID105 AS `ID105`,
        `h`.ID106 AS `ID106`,
        `h`.ID107 AS `ID107`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
                `aux_household_returned`.ID105 AS ID105,
                `aux_household_returned`.ID106 AS ID106,
                `aux_household_returned`.ID107 AS ID107,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104,
                `aux_household_returned`.ID105,
                `aux_household_returned`.ID106,
                `aux_household_returned`.ID107
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
                `aux_listing_returned`.ID105 AS ID105,
                `aux_listing_returned`.ID106 AS ID106,
                `aux_listing_returned`.ID107 AS ID107,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104,
                `aux_listing_returned`.ID105,
                `aux_listing_returned`.ID106,
                `aux_listing_returned`.ID107
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
            AND (`h`.`ID104` = `l`.`ID104`)
            AND (`h`.`ID105` = `l`.`ID105`)
            AND (`h`.`ID106` = `l`.`ID106`)
            AND (`h`.`ID107` = `l`.`ID107`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
                `aux_household_expected`.ID106 AS ID106,
                `aux_household_expected`.ID107 AS ID107,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105,
                `aux_household_expected`.ID106,
                `aux_household_expected`.ID107
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
            AND (`h`.`ID104` = `e`.`ID104`)
            AND (`h`.`ID105` = `e`.`ID105`)
            AND (`h`.`ID106` = `e`.`ID106`)
            AND (`h`.`ID107` = `e`.`ID107`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`, `h`.`ID104`, `h`.`ID105`, `h`.`ID106`, `h`.`ID107`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_sa;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_sa (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_sa.* FROM dashboard_pilot_2.r_household_expected_by_sa;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_SA','r_household_expected_by_sa', 13, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_kebele` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda#Town#Subcity#Psa#SA#Kebele' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `ID104`,
        NULL AS `ID105`,
        NULL AS `ID106`,
        NULL AS `ID107`,
        NULL AS `ID108`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1),'#',(SELECT ID105_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 limit 1),'#',(SELECT ID106_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 limit 1),'#',(SELECT ID107_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 limit 1),'#',(SELECT ID108_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 AND ID108 = h.ID108 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        `h`.ID104 AS `ID104`,
        `h`.ID105 AS `ID105`,
        `h`.ID106 AS `ID106`,
        `h`.ID107 AS `ID107`,
        `h`.ID108 AS `ID108`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
                `aux_household_returned`.ID105 AS ID105,
                `aux_household_returned`.ID106 AS ID106,
                `aux_household_returned`.ID107 AS ID107,
                `aux_household_returned`.ID108 AS ID108,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104,
                `aux_household_returned`.ID105,
                `aux_household_returned`.ID106,
                `aux_household_returned`.ID107,
                `aux_household_returned`.ID108
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
                `aux_listing_returned`.ID105 AS ID105,
                `aux_listing_returned`.ID106 AS ID106,
                `aux_listing_returned`.ID107 AS ID107,
                `aux_listing_returned`.ID108 AS ID108,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104,
                `aux_listing_returned`.ID105,
                `aux_listing_returned`.ID106,
                `aux_listing_returned`.ID107,
                `aux_listing_returned`.ID108
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
            AND (`h`.`ID104` = `l`.`ID104`)
            AND (`h`.`ID105` = `l`.`ID105`)
            AND (`h`.`ID106` = `l`.`ID106`)
            AND (`h`.`ID107` = `l`.`ID107`)
            AND (`h`.`ID108` = `l`.`ID108`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
                `aux_household_expected`.ID106 AS ID106,
                `aux_household_expected`.ID107 AS ID107,
                `aux_household_expected`.ID108 AS ID108,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105,
                `aux_household_expected`.ID106,
                `aux_household_expected`.ID107,
                `aux_household_expected`.ID108
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
            AND (`h`.`ID104` = `e`.`ID104`)
            AND (`h`.`ID105` = `e`.`ID105`)
            AND (`h`.`ID106` = `e`.`ID106`)
            AND (`h`.`ID107` = `e`.`ID107`)
            AND (`h`.`ID108` = `e`.`ID108`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`, `h`.`ID104`, `h`.`ID105`, `h`.`ID106`, `h`.`ID107`, `h`.`ID108`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_kebele;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_kebele (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_kebele.* FROM dashboard_pilot_2.r_household_expected_by_kebele;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_KEBELE','r_household_expected_by_kebele', 14, 1, 1);
CREATE OR REPLACE VIEW dashboard_pilot_2.`r_household_expected_by_ea` AS
    SELECT 
        _utf8mb4 'Region#Zone#Woreda#Town#Subcity#Psa#SA#Kebele#EA' COLLATE utf8mb4_unicode_ci AS `name`,
        NULL AS `ID101`,
        NULL AS `ID102`,
        NULL AS `ID103`,
        NULL AS `ID104`,
        NULL AS `ID105`,
        NULL AS `ID106`,
        NULL AS `ID107`,
        NULL AS `ID108`,
        NULL AS `ID109`,
        NULL AS `field`,
        NULL AS `freshlist`,
        NULL AS `expected`,
        NULL AS `field_freshlist`,
        NULL AS `field_expected`,
        NULL AS `freshlist_expected`
    
    UNION SELECT 
        CONCAT((SELECT ID101_name FROM territory WHERE ID101 = h.ID101 limit 1),'#',(SELECT ID102_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 limit 1),'#',(SELECT ID103_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 limit 1),'#',(SELECT ID104_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 limit 1),'#',(SELECT ID105_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 limit 1),'#',(SELECT ID106_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 limit 1),'#',(SELECT ID107_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 limit 1),'#',(SELECT ID108_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 AND ID108 = h.ID108 limit 1),'#',(SELECT ID109_name FROM territory WHERE ID101 = h.ID101 AND ID102 = h.ID102 AND ID103 = h.ID103 AND ID104 = h.ID104 AND ID105 = h.ID105 AND ID106 = h.ID106 AND ID107 = h.ID107 AND ID108 = h.ID108 AND ID109 = h.ID109 limit 1)) AS `name`,
        `h`.ID101 AS `ID101`,
        `h`.ID102 AS `ID102`,
        `h`.ID103 AS `ID103`,
        `h`.ID104 AS `ID104`,
        `h`.ID105 AS `ID105`,
        `h`.ID106 AS `ID106`,
        `h`.ID107 AS `ID107`,
        `h`.ID108 AS `ID108`,
        `h`.ID109 AS `ID109`,
        SUM(`h`.`returned`) AS `returned`,
        SUM(`l`.`returned`) AS `returned`,
        SUM(`e`.`expected`) AS `expected`,
        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,
        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,
        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`
    FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
                `aux_household_returned`.ID105 AS ID105,
                `aux_household_returned`.ID106 AS ID106,
                `aux_household_returned`.ID107 AS ID107,
                `aux_household_returned`.ID108 AS ID108,
                `aux_household_returned`.ID109 AS ID109,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104,
                `aux_household_returned`.ID105,
                `aux_household_returned`.ID106,
                `aux_household_returned`.ID107,
                `aux_household_returned`.ID108,
                `aux_household_returned`.ID109
            )
        `h`
        JOIN 
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
                `aux_listing_returned`.ID105 AS ID105,
                `aux_listing_returned`.ID106 AS ID106,
                `aux_listing_returned`.ID107 AS ID107,
                `aux_listing_returned`.ID108 AS ID108,
                `aux_listing_returned`.ID109 AS ID109,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104,
                `aux_listing_returned`.ID105,
                `aux_listing_returned`.ID106,
                `aux_listing_returned`.ID107,
                `aux_listing_returned`.ID108,
                `aux_listing_returned`.ID109
            )
            `l` ON
            (`h`.`ID101` = `l`.`ID101`)
            AND (`h`.`ID102` = `l`.`ID102`)
            AND (`h`.`ID103` = `l`.`ID103`)
            AND (`h`.`ID104` = `l`.`ID104`)
            AND (`h`.`ID105` = `l`.`ID105`)
            AND (`h`.`ID106` = `l`.`ID106`)
            AND (`h`.`ID107` = `l`.`ID107`)
            AND (`h`.`ID108` = `l`.`ID108`)
            AND (`h`.`ID109` = `l`.`ID109`)
        JOIN
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
                `aux_household_expected`.ID106 AS ID106,
                `aux_household_expected`.ID107 AS ID107,
                `aux_household_expected`.ID108 AS ID108,
                `aux_household_expected`.ID109 AS ID109,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105,
                `aux_household_expected`.ID106,
                `aux_household_expected`.ID107,
                `aux_household_expected`.ID108,
                `aux_household_expected`.ID109
            )
        `e` ON
            (`h`.`ID101` = `e`.`ID101`)
            AND (`h`.`ID102` = `e`.`ID102`)
            AND (`h`.`ID103` = `e`.`ID103`)
            AND (`h`.`ID104` = `e`.`ID104`)
            AND (`h`.`ID105` = `e`.`ID105`)
            AND (`h`.`ID106` = `e`.`ID106`)
            AND (`h`.`ID107` = `e`.`ID107`)
            AND (`h`.`ID108` = `e`.`ID108`)
            AND (`h`.`ID109` = `e`.`ID109`)
    GROUP BY `name`, `h`.`ID101`, `h`.`ID102`, `h`.`ID103`, `h`.`ID104`, `h`.`ID105`, `h`.`ID106`, `h`.`ID107`, `h`.`ID108`, `h`.`ID109`;

DROP TABLE IF EXISTS dashboard_pilot_2.mr_household_expected_by_ea;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_household_expected_by_ea (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_household_expected_by_ea.* FROM dashboard_pilot_2.r_household_expected_by_ea;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Households by R_HOUSEHOLD_EXPECTED_BY_EA','r_household_expected_by_ea', 15, 1, 1);
CREATE OR REPLACE VIEW `dashboard_pilot_2`.`r_total` AS
    SELECT 
        (SELECT 
                COUNT(0)
            FROM
        (SELECT
                `aux_household_returned`.ID101 AS ID101,
                `aux_household_returned`.ID102 AS ID102,
                `aux_household_returned`.ID103 AS ID103,
                `aux_household_returned`.ID104 AS ID104,
                `aux_household_returned`.ID105 AS ID105,
                `aux_household_returned`.ID106 AS ID106,
                `aux_household_returned`.ID107 AS ID107,
                `aux_household_returned`.ID108 AS ID108,
                `aux_household_returned`.ID109 AS ID109,
            SUM(`aux_household_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_household_returned`
            GROUP BY
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID101,
                `aux_household_returned`.ID102,
                `aux_household_returned`.ID103,
                `aux_household_returned`.ID104,
                `aux_household_returned`.ID105,
                `aux_household_returned`.ID106,
                `aux_household_returned`.ID107,
                `aux_household_returned`.ID108,
                `aux_household_returned`.ID109
            )
            `a`) AS `ea_fieldwork`,
        (SELECT 
                COUNT(0)
            FROM
        (SELECT
                `aux_listing_returned`.ID101 AS ID101,
                `aux_listing_returned`.ID102 AS ID102,
                `aux_listing_returned`.ID103 AS ID103,
                `aux_listing_returned`.ID104 AS ID104,
                `aux_listing_returned`.ID105 AS ID105,
                `aux_listing_returned`.ID106 AS ID106,
                `aux_listing_returned`.ID107 AS ID107,
                `aux_listing_returned`.ID108 AS ID108,
                `aux_listing_returned`.ID109 AS ID109,
            SUM(`aux_listing_returned`.`returned`) AS `returned`
            FROM `dashboard_pilot_2`.`aux_listing_returned`
            GROUP BY
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID101,
                `aux_listing_returned`.ID102,
                `aux_listing_returned`.ID103,
                `aux_listing_returned`.ID104,
                `aux_listing_returned`.ID105,
                `aux_listing_returned`.ID106,
                `aux_listing_returned`.ID107,
                `aux_listing_returned`.ID108,
                `aux_listing_returned`.ID109
            )
            `a`) AS `ea_freshlist`,
        (SELECT 
                COUNT(0)
            FROM
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
                `aux_household_expected`.ID106 AS ID106,
                `aux_household_expected`.ID107 AS ID107,
                `aux_household_expected`.ID108 AS ID108,
                `aux_household_expected`.ID109 AS ID109,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105,
                `aux_household_expected`.ID106,
                `aux_household_expected`.ID107,
                `aux_household_expected`.ID108,
                `aux_household_expected`.ID109
            )
            `a`) AS `ea_expected`,
        (SELECT 
                COUNT(0)
            FROM
                dashboard_pilot_2.H_HOUSEHOLD_QUEST) AS `household_fieldwork`,
        (SELECT 
                COUNT(0)
            FROM
                dashboard_pilot_2.L_LISTING_QUEST) AS `household_freshlist`,
        (SELECT 
                SUM(`a`.`expected`)
            FROM
        (SELECT
                `aux_household_expected`.ID101 AS ID101,
                `aux_household_expected`.ID102 AS ID102,
                `aux_household_expected`.ID103 AS ID103,
                `aux_household_expected`.ID104 AS ID104,
                `aux_household_expected`.ID105 AS ID105,
                `aux_household_expected`.ID106 AS ID106,
                `aux_household_expected`.ID107 AS ID107,
                `aux_household_expected`.ID108 AS ID108,
                `aux_household_expected`.ID109 AS ID109,
            SUM(`aux_household_expected`.`expected`) AS `expected`
            FROM `dashboard_pilot_2`.`aux_household_expected`
            GROUP BY
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID101,
                `aux_household_expected`.ID102,
                `aux_household_expected`.ID103,
                `aux_household_expected`.ID104,
                `aux_household_expected`.ID105,
                `aux_household_expected`.ID106,
                `aux_household_expected`.ID107,
                `aux_household_expected`.ID108,
                `aux_household_expected`.ID109
            )
                `a`) AS `household_expected`;
DROP TABLE IF EXISTS dashboard_pilot_2.mr_total;
SELECT 0 INTO @ID;
CREATE TABLE dashboard_pilot_2.mr_total (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, r_total.* FROM dashboard_pilot_2.r_total;
INSERT INTO dashboard_pilot_2.`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) VALUES ('Aux','r_total', 16, 1, 4);
