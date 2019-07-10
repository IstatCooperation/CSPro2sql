/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cspro2sql;

import static cspro2sql.TerritoryEngine.execute;
import static cspro2sql.TestConnectionEngine.execute;
import cspro2sql.bean.Dictionary;
import cspro2sql.reader.DictionaryReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mbruno
 */
public class ScanEngine {

    private static final Logger LOGGER = Logger.getLogger(ScanEngine.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream in = LoaderEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
            boolean isLocalFile = false;
            String[] dicts = prop.getProperty("dictionary").split(",");
            System.out.println("Starting property file scan...");
            System.out.println("[Dictionaries]");
            for (int i = 0; i < dicts.length; i++) {
                isLocalFile = new File(dicts[i].trim()).exists();
                if (isLocalFile) {
                    System.out.println("  File " + dicts[i].trim() + ": OK");
                } else {
                    System.out.println("  File " + dicts[i].trim() + ": ERROR (file not available)");
                }
            }
            System.out.println("[Territory]");
            String territory = prop.getProperty("territory");
            if (territory != null && !territory.isEmpty()) {
                isLocalFile = new File(territory.trim()).exists();
                if (isLocalFile) {
                    System.out.println("  File " + territory.trim() + ": OK");
                } else {
                    System.out.println("  File " + territory.trim() + ": ERROR (file not available)");
                }
            } else{
                 System.out.println("Territory file not specified!");
            }
            
            System.out.println("[Database]");
            TestConnectionEngine.execute(prop);
            
            System.out.println("...scanning completed!");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read properties file", ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static boolean execute(List<Dictionary> dictionaries, Properties prop) {

        return true;
    }

}
