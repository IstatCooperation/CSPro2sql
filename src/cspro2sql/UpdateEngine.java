package cspro2sql;

import cspro2sql.bean.Concepts;
import cspro2sql.bean.ConnectionParams;
import cspro2sql.bean.Report;
import cspro2sql.utils.Utility;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

    private static final int DASHBOARD_STATUS_ID = 1;

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
        int territoryLevel = -1;
        Long start, stop;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();

            //Connect to the destination database
            ConnectionParams destConnection = ConnectionParams.getDestParams(prop);
            try (Connection connDst = DriverManager.getConnection(destConnection.getUri(), destConnection.getUsername(), destConnection.getPassword())) {
                connDst.setAutoCommit(false);

                try (Statement readDst = connDst.createStatement()) {
                    try (Statement writeDst = connDst.createStatement()) {
                        try (ResultSet rs = readDst.executeQuery("SELECT * FROM " + schema + ".dashboard_report where REPORT_TYPE != " + Report.REPORT_TYPE_GIS + " AND IS_VISIBLE = 1")) {
                            while (rs.next()) {
                                String template = rs.getString(4); //GET REPORT NAME
                                start = System.currentTimeMillis();
                                System.out.print("Updating " + template + "... ");
                                writeDst.executeUpdate("DROP TABLE IF EXISTS " + schema + ".m" + template);
                                writeDst.executeQuery("SELECT 0 INTO @ID");
                                writeDst.executeUpdate("CREATE TABLE " + schema + ".m" + template + " (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, " + template + ".* FROM " + schema + "." + template);
                                stop = System.currentTimeMillis();
                                System.out.print("done");
                                System.out.println(" [" + Utility.convertMillis(stop - start) + "]");
                                territoryLevel = getReportTerritoryLevel(connDst, schema, template);
                                if (territoryLevel > 0) {
                                    System.out.print("Updating t" + template + "... ");
                                    updateProgressReport(connDst, schema, template, territoryLevel);
                                    System.out.println("done");
                                }
                                connDst.commit();                              
                            }
                        }
                        updateDashboardStatus(connDst, schema);
                    }
                }
                connDst.close();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            System.out.println("Database exception (" + ex.getMessage() + ")");
            return false;
        }
        return true;
    }

    private static void updateDashboardStatus(Connection conn, String schema) {
        Statement insertStmt;
        Integer count;
        System.out.print("Updating dashboard_status... ");
        try (Statement countStmt = conn.createStatement()) {
            try (ResultSet rs = countStmt.executeQuery("SELECT COUNT(*) FROM " + schema + ".DASHBOARD_STATUS")) {
                while (rs.next()) {
                    count = rs.getInt(1);
                    if (count == 0) { //empty table
                        insertStmt = conn.createStatement();
                        insertStmt.executeUpdate("INSERT INTO " + schema + ".DASHBOARD_STATUS values (" + DASHBOARD_STATUS_ID + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1)");
                    } else {
                        insertStmt = conn.createStatement();
                        insertStmt.executeUpdate("UPDATE " + schema + ".dashboard_status set LAST_UPDATE = CURRENT_TIMESTAMP where id = " + DASHBOARD_STATUS_ID);

                    }
                    insertStmt.getConnection().commit();
                }
            }
            System.out.println("done");
        } catch (SQLException ex) {
            System.out.println("Database exception (" + ex.getMessage() + ")");
        }
    }

    private static void updateProgressReport(Connection connDst, String schema, String template, int territoryLevel) {
        List<String> territoryFields = getTerritoryFields(connDst, schema, territoryLevel);
        String query = "";
        String fields = "";
        for (String field : territoryFields) {
            fields += "`" + field + "`,";
        }
        fields += "`field`, `freshlist`, `expected`, `field_freshlist`, `field_expected`, `freshlist_expected`";
        query = "INSERT INTO " + schema + ".t" + template + "(" + fields + ") SELECT " + fields + " FROM " + schema + ".m" + template + " WHERE `" + territoryFields.get(0) + "` IS NOT NULL";
        try (Statement countStmt = connDst.createStatement()) {
            countStmt.executeUpdate(query);
        } catch (SQLException ex) {
            System.out.println("Database exception (" + ex.getMessage() + ")");
        }

    }

    private static List<String> getTerritoryFields(Connection connDst, String schema, int territoryLevel) {

        List<String> territoryFields = new ArrayList();

        String query = "SELECT var.NAME FROM " + schema + ".DASHBOARD_META_VARIABLE var JOIN " + schema + ".DASHBOARD_META_UNIT as unit on var.unit_id = unit.id "
                + "where var.concept_id = " + Concepts.TERRITORY_ID + " and unit.concept_id = " + Concepts.HOUSEHOLD_ID + " and var.var_order <= " + territoryLevel;
        if (territoryLevel > 0) {
            try (Statement countStmt = connDst.createStatement()) {
                try (ResultSet rs = countStmt.executeQuery(query)) {
                    while (rs.next()) {
                        territoryFields.add(rs.getString(1));
                    }
                }
            } catch (SQLException ex) {
                System.out.println("Database exception (" + ex.getMessage() + ")");
            }
        }
        return territoryFields;
    }

    private static int getReportTerritoryLevel(Connection connDst, String schema, String template) {
        int level = -1;
        String query = "SELECT TERRITORY_LEVEL FROM " + schema + ".DASHBOARD_REPORT WHERE REPORT_VIEW = \'" + template + "\'";
        try (Statement countStmt = connDst.createStatement()) {
            try (ResultSet rs = countStmt.executeQuery(query)) {
                if (rs.next()) {
                    level = rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Database exception (" + ex.getMessage() + ")");
        }
        return level;
    }
}
