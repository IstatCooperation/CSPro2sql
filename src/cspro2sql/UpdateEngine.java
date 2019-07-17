package cspro2sql;

import cspro2sql.bean.ConnectionParams;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

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
 * @version 0.9.17
 */
public class UpdateEngine {

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (InputStream in = UpdateEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
        } catch (IOException ex) {
            return;
        }
        try {
            execute(prop);
        } catch (Exception ex) {
            System.exit(1);
        }
    }

    static boolean execute(Properties prop) {
        String schema = prop.getProperty("db.dest.schema").trim();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            //Connect to the destination database
            ConnectionParams sourceConnection = ConnectionParams.getSourceParams(prop);
            try (Connection connDst = DriverManager.getConnection(sourceConnection.getUri(), sourceConnection.getUsername(), sourceConnection.getPassword())) {
                connDst.setAutoCommit(false);

                try (Statement readDst = connDst.createStatement()) {
                    try (Statement writeDst = connDst.createStatement()) {
                        try (ResultSet rs = readDst.executeQuery("SELECT * FROM " + schema + ".dashboard_report where REPORT_TYPE IN (1,2,4) AND IS_VISIBLE = 1")) {
                            while (rs.next()) {
                                String template = rs.getString(4);
                                System.out.print("Updating " + template + "... ");
                                writeDst.executeUpdate("DROP TABLE IF EXISTS " + schema + ".m" + template);
                                writeDst.executeQuery("SELECT 0 INTO @ID");
                                writeDst.executeUpdate("CREATE TABLE " + schema + ".m" + template + " (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, " + template + ".* FROM " + schema + "." + template);
                                connDst.commit();
                                System.out.println("done");
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            System.out.println("Database exception (" + ex.getMessage() + ")");
            return false;
        }
        return true;
    }

}
