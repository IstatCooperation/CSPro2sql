DROP TABLE IF EXISTS @SCHEMA.`dashboard_meta_variable`;
CREATE TABLE @SCHEMA.DASHBOARD_META_VARIABLE (
  `ID` int(11) NOT NULL,
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `NOTE` text,
  `TYPE` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ORDER` int(11) DEFAULT NULL,
  `UNIT_ID` int(11) DEFAULT NULL,
  `CONCEPT_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_variable_concpet_idx` (`concept_id`),
  KEY `fk_variable_unit_idx` (`unit_id`),
  CONSTRAINT `fk_variable_concept` FOREIGN KEY (`concept_id`) REFERENCES `dashboard_meta_concept` (`id`),
  CONSTRAINT `fk_variable_unit` FOREIGN KEY (`unit_id`) REFERENCES `dashboard_meta_unit` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
