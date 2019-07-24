package cspro2sql;

import cspro2sql.bean.ConnectionParams;
import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import cspro2sql.reader.DictionaryReader;
import cspro2sql.reader.TerritoryReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * @version 0.9.18.2
 */
public class TerritoryEngine {

    private static final int COMMIT_SIZE = 100;
    private static final Logger LOGGER = Logger.getLogger(TerritoryEngine.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream in = LoaderEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);

            List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                    prop.getProperty("db.dest.schema"),
                    prop.getProperty("dictionary"),
                    prop.getProperty("dictionary.prefix"));
            execute(dictionaries, prop);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read properties file", ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static boolean execute(List<Dictionary> dictionaries, Properties prop) {
        //Parse dictionary file
        for (Dictionary dictionary : dictionaries) {
            if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
                return execute(dictionary, prop);
            }
        }
        return false;
    }

    public static boolean execute(Dictionary dictionary, Properties prop) {
        List<Territory> territoryList;
        try {
            territoryList = TerritoryReader.parseTerritory(prop.getProperty("territory"), dictionary);
            try {
                Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                ConnectionParams destConnection = ConnectionParams.getDestParams(prop);
                try (Connection connSrc = DriverManager.getConnection(destConnection.getUri(), destConnection.getUsername(), destConnection.getPassword())) {
                    connSrc.setAutoCommit(false);
                    try (Statement stmt = connSrc.createStatement()) {
                        if (createTerritoryTable(dictionary, stmt, prop) && truncateTerritory(stmt, prop)) {
                            System.out.print("Loading territory table... ");
                            String insertQuery = "INSERT INTO " + prop.getProperty("db.dest.schema") + ".`territory` VALUES(";
                            String insertValues = "";
                            String territoryCode = "";
                            int rowCounter = 1;
                            for (Territory territory : territoryList) {
                                if (!isTerritoryStored(dictionary, territory, stmt, prop)) {
                                    int counter = 1;
                                    for (TerritoryItem territoryItem : territory.getItemsList()) {
                                        if (counter % 2 == 0) { //I assume that even columns contain description *_NAME
                                            insertValues += "\"" + territoryItem.getName() + "\",";
                                        } else {
                                            insertValues += Integer.parseInt(territoryItem.getName()) + ",";
                                            territoryCode += territoryItem.getName();
                                        }
                                        counter++;
                                    }
                                    stmt.executeUpdate(insertQuery + insertValues + "\"" + territoryCode + "\")");
                                    if (rowCounter % COMMIT_SIZE == 0) {
                                        System.out.print("+");
                                        connSrc.commit();
                                    }
                                    rowCounter++;
                                    insertValues = "";
                                    territoryCode = "";
                                }
                            }
                        }
                        connSrc.commit();
                        System.out.print("[OK]");
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
                System.out.println("Database exception (" + ex.getMessage() + ")");
                return false;
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error parsing territory file", ex);
            return false;
        }

        return true;
    }

    private static boolean truncateTerritory(Statement stmt, Properties prop) {
        try {
            stmt.executeQuery("SET foreign_key_checks=0");
            stmt.executeUpdate("TRUNCATE TABLE " + prop.getProperty("db.dest.schema") + ".`territory`");
            stmt.executeQuery("SET foreign_key_checks=1");
            stmt.getConnection().commit();
            return true;
        } catch (SQLException ex) {
            System.out.println("Database exception (Could not truncate territory table)!");
            return false;
        }
    }

    private static boolean createTerritoryTable(Dictionary dictionary, Statement stmt, Properties prop) {

        Territory territory = new Territory();
        if (dictionary.hasTagged(Dictionary.TAG_TERRITORY)) {
            Iterable<Item> territories = dictionary.getTaggedItems(Dictionary.TAG_TERRITORY);
            for (Item item : territories) {
                territory.addItem(item);
            }
        }

        if (!territory.isEmpty()) {
            try {
                stmt.executeQuery("SET foreign_key_checks=0");
                stmt.executeUpdate("DROP TABLE IF EXISTS " + prop.getProperty("db.dest.schema") + ".`territory`");
                String createQuery = "CREATE TABLE " + prop.getProperty("db.dest.schema") + ".`territory`(";
                String idx = "";
                for (int i = 0; i < territory.size(); i++) {
                    TerritoryItem territoryItem = territory.get(i);
                    String name = territoryItem.getItemName();
                    createQuery += "`" + name + "` int(11) DEFAULT NULL,";
                    createQuery += "`" + name + "_NAME` text COLLATE utf8mb4_unicode_ci,";
                    if (i > 0) {
                        idx += ",";
                    }
                    idx += "`" + name + "`";
                }
                createQuery += " `TERRITORY_CODE` text COLLATE utf8mb4_unicode_ci,";
                createQuery += " KEY `idx_territory` (" + idx + ")";
                createQuery += ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
                stmt.executeUpdate(createQuery);
                stmt.executeQuery("SET foreign_key_checks=1");
                stmt.getConnection().commit();
            } catch (SQLException ex) {
                System.out.println("Database exception (Could not create territory table)!");
                return false;
            }
        }
        return true;
    }

    private static boolean isTerritoryStored(Dictionary dictionary, Territory territoryCsv, Statement stmt, Properties prop) {

        String selectQuery = "SELECT COUNT(*) AS COUNT FROM " + prop.getProperty("db.dest.schema") + ".`territory` WHERE ";
        String filters = "";
        Territory territoryDictionary = new Territory();
        if (dictionary.hasTagged(Dictionary.TAG_TERRITORY)) {
            Iterable<Item> territories = dictionary.getTaggedItems(Dictionary.TAG_TERRITORY);
            for (Item item : territories) {
                territoryDictionary.addItem(item);
            }
        }
        List<TerritoryItem> csvRow = territoryCsv.getItemsList();
        List<TerritoryItem> tableFields = territoryDictionary.getItemsList();
        for (int i = 0; i < tableFields.size(); i++) {
            filters += tableFields.get(i).getItemName() + " = " + Integer.parseInt(csvRow.get(i * 2).getName()) + " AND ";
        }
        filters = filters.substring(0, filters.length() - 4); //REMOVE LAST AND 
        try (ResultSet result = stmt.executeQuery(selectQuery + filters)) {
            result.next();
            return result.getInt("COUNT") > 0;
        } catch (SQLException ex) {
            Logger.getLogger(TerritoryEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
