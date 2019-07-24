package cspro2sql;

import cspro2sql.bean.ConnectionParams;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
public class TestConnectionEngine {

    private static final Logger LOGGER = Logger.getLogger(TestConnectionEngine.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream in = LoaderEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read properties file", ex);
            return;
        }
        try {
            execute(prop);
        } catch (Exception ex) {
            System.exit(1);
        }

    }

    public static boolean execute(Properties prop) {
        try {
            //Test source database connetcion
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Connecting to " + ConnectionParams.MYSQL_JDBC + prop.getProperty("db.source.server") + ":" + prop.getProperty("db.source.port") 
                    +  "/" + prop.getProperty("db.source.schema"));
            ConnectionParams sourceConnection = ConnectionParams.getSourceParams(prop);
            try (Connection connSrc = DriverManager.getConnection(sourceConnection.getUri(), sourceConnection.getUsername(), sourceConnection.getPassword())) {
                connSrc.setReadOnly(true);
                System.out.println("Connection successful!");

                //Connect to the destination database
                System.out.println("Connecting to " + ConnectionParams.MYSQL_JDBC + prop.getProperty("db.dest.server") + ":" + prop.getProperty("db.dest.port") 
                        + "/" + prop.getProperty("db.dest.schema"));
                if ("sqlserver".equals(prop.getProperty("db.dest.type"))) {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
                } 
                ConnectionParams destConnParams = ConnectionParams.getDestParams(prop);
                try (Connection connDst = DriverManager.getConnection(destConnParams.getUri(), destConnParams.getUsername(), destConnParams.getPassword())) {
                    connDst.setAutoCommit(false);
                    System.out.println("Connection successful!");
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            System.out.println("Database exception (" + ex.getMessage() + ")");
            return false;
        }
        return true;
    }
}
