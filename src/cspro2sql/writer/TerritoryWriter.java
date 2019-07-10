package cspro2sql.writer;

import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

    private static final int COMMIT_SIZE = 100;

    public static void write(List<Territory> territoryList, Territory territoryStructure, String schema, PrintStream ps, Connection conn) throws SQLException {

        try (Statement stmt = conn.createStatement()) {
            
            System.out.println("Creating territory table...");
            
            String createQuery = "";
            String key = "";
            createQuery += "CREATE TABLE IF NOT EXISTS " + schema + ".`territory`(\n";
            for (TerritoryItem territoryItem : territoryStructure.getItemsList()) {
                if (territoryItem.getName().contains("_NAME")) {
                    createQuery += "`" + territoryItem.getName() + "` text,\n";
                } else {
                    createQuery += "`" + territoryItem.getName() + "` int(11) DEFAULT NULL,\n";
                    key += "`" + territoryItem.getName() + "`,";
                }
            }
            createQuery += "KEY `idx_territory`(" + removeLastChar(key) + ")\n";
            createQuery += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
            stmt.executeUpdate(createQuery);
            
            System.out.println("Territory table successfully created");
            System.out.println("Loading territory table [" + territoryList.size() + "]");

            String insertQuery = "INSERT INTO " + schema + ".`territory` VALUES(";
            String insertValues = "";
            int rowCounter = 1;
            for (Territory territory : territoryList) {
                int counter = 1;
                for (TerritoryItem territoryItem : territory.getItemsList()) {
                    if (counter % 2 == 0) {
                        insertValues += "\"" + territoryItem.getName() + "\",";
                    } else {
                        insertValues += Integer.parseInt(territoryItem.getName()) + ",";
                    }
                    counter++;
                }
                stmt.executeUpdate(insertQuery + removeLastChar(insertValues) + ")");
                if (rowCounter % COMMIT_SIZE == 0) {
                    System.out.print("+");
                    conn.commit();
                }
                rowCounter++;
                insertValues = "";
            }
            conn.commit();
            System.out.println("");
            System.out.println("Territory table successfully loaded!");
        }

    }

    private static String removeLastChar(String str) {
        return str.substring(0, str.length() - 1);
    }
}
