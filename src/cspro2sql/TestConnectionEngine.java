/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cspro2sql;

import static cspro2sql.UpdateEngine.execute;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author UTENTE
 */
public class TestConnectionEngine {

    private static final Logger LOGGER = Logger.getLogger(LoaderEngine.class.getName());

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
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            //Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Connecting to " + prop.getProperty("db.source.uri").trim() + "/" + prop.getProperty("db.source.schema").trim());
            try (Connection connSrc = DriverManager.getConnection(
                    prop.getProperty("db.source.uri").trim() + "/" + prop.getProperty("db.source.schema").trim() + "?autoReconnect=true&useSSL=false",
                    prop.getProperty("db.source.username").trim(),
                    prop.getProperty("db.source.password").trim())) {
                connSrc.setReadOnly(true);
                System.out.println("Connection successful!");

                //Connect to the destination database
                System.out.println("Connecting to " + prop.getProperty("db.dest.uri").trim() + "/" + prop.getProperty("db.dest.schema").trim());
                String destConnString;
                if ("sqlserver".equals(prop.getProperty("db.dest.type"))) {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
                    destConnString = prop.getProperty("db.dest.uri").trim() + ";databasename=" + prop.getProperty("db.dest.schema").trim();
                } else {
                    destConnString = prop.getProperty("db.dest.uri").trim() + "/" + prop.getProperty("db.dest.schema").trim() + "?autoReconnect=true&useSSL=false";
                }
       
                try (Connection connDst = DriverManager.getConnection(
                        destConnString,
                        prop.getProperty("db.dest.username").trim(),
                        prop.getProperty("db.dest.password").trim())) {
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
