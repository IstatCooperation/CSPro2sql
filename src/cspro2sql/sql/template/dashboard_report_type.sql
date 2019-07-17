DROP TABLE IF EXISTS @SCHEMA.`dashboard_report_type`;
CREATE TABLE IF NOT EXISTS @SCHEMA.`dashboard_report_type` (
  `ID` int(11) NOT NULL,
  `NAME` varchar(256),
  `DESCRIPTION` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO @SCHEMA.`dashboard_report_type` (`ID`, `NAME`, `DESCRIPTION`) 
    VALUES (1, "progress" ,"Progress reports");
INSERT INTO @SCHEMA.`dashboard_report_type` (`ID`, `NAME`, `DESCRIPTION`) 
    VALUES (2, "analysis" ,"Analysis reports");
INSERT INTO @SCHEMA.`dashboard_report_type` (`ID`, `NAME`, `DESCRIPTION`) 
    VALUES (3, "gis" ,"GIS reports");
INSERT INTO @SCHEMA.`dashboard_report_type` (`ID`, `NAME`, `DESCRIPTION`) 
    VALUES (4, "aux" ,"Internal reports needed by cspro2slq");
