package cspro2sql.writer;

import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import java.io.PrintStream;
import java.util.List;

/**
 * Copyright 2017 ISTAT
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 *
 * @author Guido Drovandi <drovandi @ istat.it>
 * @author Mauro Bruno <mbruno @ istat.it>
 * @version 0.9.18
 * @since 0.9.18
 */
public class TerritoryWriter {

    public static void write(List<Territory> territoryList, Territory territoryStructure, String schema, PrintStream ps) {
        String key = "";
        ps.println("CREATE TABLE IF NOT EXISTS " + schema + ".`territory`(");
        for (TerritoryItem territoryItem : territoryStructure.getItemsList()) {
            if (territoryItem.getName().contains("_NAME")) {
                ps.println("`" + territoryItem.getName() + "` text,");
            } else {
                ps.println("`" + territoryItem.getName() + "` int(11) DEFAULT NULL,");
                key += "`" + territoryItem.getName() + "`,";
            }
        }
        ps.println("KEY `idx_territory`(" + removeLastChar(key) + ")");
        ps.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        ps.println();

        String insert = "";
        for (Territory territory : territoryList) {
            ps.println("INSERT INTO " + schema + ".`territory` VALUES(");
            int counter = 1;
            for (TerritoryItem territoryItem : territory.getItemsList()) {
                if (counter % 2 == 0) {
                    insert += "'" + territoryItem.getName() + "',";
                } else {
                    insert += Integer.parseInt(territoryItem.getName()) + ",";
                }
                counter++;

            }
            ps.println(removeLastChar(insert) + ");");
            insert = "";
        }

    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
}
