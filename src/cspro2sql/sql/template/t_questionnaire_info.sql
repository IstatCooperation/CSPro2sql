DROP TABLE IF EXISTS @SCHEMA.`tr_questionnaire_info`;
CREATE TABLE IF NOT EXISTS @SCHEMA.`tr_questionnaire_info` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `TOTAL` int(11) DEFAULT NULL,
  `UPDATE_TIME` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;