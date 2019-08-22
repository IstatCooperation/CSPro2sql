CREATE TABLE IF NOT EXISTS @SCHEMA.DASHBOARD_META_VARIABLE (
  `ID` int(11) NOT NULL,
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `NOTE` text,
  `TYPE` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `VAR_ORDER` int(11) DEFAULT NULL,
  `UNIT_ID` int(11) DEFAULT NULL,
  `CONCEPT_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `fk_variable_concpet_idx` (`CONCEPT_ID`),
  KEY `fk_variable_unit_idx` (`UNIT_ID`),
  CONSTRAINT `fk_variable_concept` FOREIGN KEY (`CONCEPT_ID`) REFERENCES DASHBOARD_META_CONCEPT (`ID`),
  CONSTRAINT `fk_variable_unit` FOREIGN KEY (`UNIT_ID`) REFERENCES DASHBOARD_META_UNIT (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

