package cspro2sql;

import cspro2sql.bean.ConnectionParams;
import cspro2sql.bean.Territory;
import cspro2sql.reader.TerritoryReader;
import cspro2sql.writer.TerritoryWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
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

    private static final Logger LOGGER = Logger.getLogger(TerritoryEngine.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream in = LoaderEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read properties file", ex);
            return;
        }
        try {
            execute(prop, System.out);
        } catch (Exception ex) {
            System.exit(1);
        }

    }

    public static boolean execute(Properties prop, PrintStream out) {
        List<Territory> territoryList;
        Territory territoryStructure;
        if (prop.getProperty("territory.structure") != null && !prop.getProperty("territory.structure").isEmpty()) {
            try {
                territoryStructure = TerritoryReader.parseTerritoryStructure(prop.getProperty("territory.structure"));
                territoryList = TerritoryReader.parseTerritory(prop.getProperty("territory"), prop.getProperty("territory.structure"));
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
                    System.out.println("Connecting to " + prop.getProperty("db.dest.uri") + "/" + prop.getProperty("db.dest.schema"));
                    ConnectionParams destConnection = ConnectionParams.getDestParams(prop);
                    try (Connection connSrc = DriverManager.getConnection(destConnection.getUri(), destConnection.getUsername(), destConnection.getPassword())) {
                        System.out.println("Connection successful!");
                        connSrc.setAutoCommit(false);
                        TerritoryWriter.write(territoryList, territoryStructure, prop.getProperty("db.dest.schema"), System.out, connSrc);
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
                    System.out.println("Database exception (" + ex.getMessage() + ")");
                    return false;
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error parsing territory file", ex);
                return false;
            }
        } else {
            System.out.println("Territory properties are missing in properties file!");
            return false;
        }

        return true;
    }
}
