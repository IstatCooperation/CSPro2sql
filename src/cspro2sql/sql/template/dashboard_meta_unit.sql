DROP TABLE IF EXISTS @SCHEMA.`dashboard_meta_unit`;
CREATE TABLE @SCHEMA.DASHBOARD_META_UNIT (
  `ID` int(11) NOT NULL,
  `NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TABLE_NAME` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `NOTE` text,
  `PARENT_ID` int(11) DEFAULT NULL,
  `CONCEPT_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_unit_concept_idx` (`concept_id`),
  CONSTRAINT `fk_unit_concept_idx` FOREIGN KEY (`concept_id`) REFERENCES `dashboard_meta_concept` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
