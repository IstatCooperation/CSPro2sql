DROP TABLE IF EXISTS @SCHEMA.`dashboard_report`;
CREATE TABLE IF NOT EXISTS @SCHEMA.`dashboard_report` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `NAME` varchar(256) NOT NULL,
  `DESCRIPTION` text,
  `LIST_ORDER` int(11) NOT NULL,
  `IS_VISIBLE` tinyint DEFAULT 1,
  `REPORT_TYPE` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_report_tyep_idx` (`REPORT_TYPE`),
  CONSTRAINT `fk_report_tyep_idx` FOREIGN KEY (`REPORT_TYPE`) REFERENCES `dashboard_report_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO @SCHEMA.`dashboard_report` (`NAME`, `DESCRIPTION`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) 
    VALUES ("Population", "Population report", 1, 1, 2);
INSERT INTO @SCHEMA.`dashboard_report` (`NAME`, `DESCRIPTION`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) 
    VALUES ("Households", "Household report", 2, 1, 2);
INSERT INTO @SCHEMA.`dashboard_report` (`NAME`, `DESCRIPTION`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) 
    VALUES ("Sex distribution", "Sex distribution at country level", 3, 1, 2);
INSERT INTO @SCHEMA.`dashboard_report` (`NAME`, `DESCRIPTION`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) 
    VALUES ("Territory", "Territorial hierarchy", 1, 1, 3);
INSERT INTO @SCHEMA.`dashboard_report` (`NAME`, `DESCRIPTION`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) 
    VALUES ("Maps", "GIS maps", 2, 0, 3);