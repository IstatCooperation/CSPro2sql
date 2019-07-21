/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Tag;
import cspro2sql.reader.DictionaryReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mbruno
 */
public class ScanEngine {

    private static final Map<Tag, Boolean> reportTags = createMap();

    private static final Logger LOGGER = Logger.getLogger(ScanEngine.class.getName());

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream in = LoaderEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
            execute(prop);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read properties file", ex);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static boolean execute(Properties prop) {

        boolean isLocalFile;
        boolean dictionariesAvailable = true;
        String[] dicts = prop.getProperty("dictionary").split(",");
        System.out.println("Starting property file scan...");
        System.out.println("[Dictionaries]");
        for (int i = 0; i < dicts.length; i++) {
            isLocalFile = new File(dicts[i].trim()).exists();
            if (isLocalFile) {
                System.out.println("- File " + dicts[i].trim() + ": OK");
            } else {
                System.out.println("- File " + dicts[i].trim() + ": ERROR (file not available)");
                dictionariesAvailable = false;
            }
        }
        System.out.println("[Territory]");
        String territory = prop.getProperty("territory");
        if (territory != null && !territory.isEmpty()) {
            isLocalFile = new File(territory.trim()).exists();
            if (isLocalFile) {
                System.out.println("- File " + territory.trim() + ": OK");
            } else {
                System.out.println("- File " + territory.trim() + ": ERROR (file not available)");
            }
        } else {
            System.out.println("Territory file not specified!");
        }

        System.out.println("[Metadata]");
        if (dictionariesAvailable) {
            try {
                List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                        prop.getProperty("db.dest.schema"),
                        prop.getProperty("dictionary"),
                        prop.getProperty("dictionary.prefix"));

                for (Dictionary dictionary : dictionaries) {
                    checkTags(dictionary);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Cannot parse dictionary files", ex);
                return false;
            }
        } else {
            System.out.println("Could not access one or more dictionaries. Metadata scanning is disabled");
        }

        System.out.println("[Database]");
        TestConnectionEngine.execute(prop);

        System.out.println("...scanning completed!");
        return true;
    }

    private static Map<Tag, Boolean> createMap() {

        Map<Tag, Boolean> result = new LinkedHashMap<>();
        
        result.put(Dictionary.TAG_HOUSEHOLD, Boolean.FALSE);
        result.put(Dictionary.TAG_LISTING, Boolean.FALSE);
        result.put(Dictionary.TAG_EXPECTED, Boolean.FALSE);
        result.put(Dictionary.TAG_TERRITORY, Boolean.FALSE);
        result.put(Dictionary.TAG_AGE, Boolean.FALSE);
        result.put(Dictionary.TAG_SEX, Boolean.FALSE);
        result.put(Dictionary.TAG_RELIGION, Boolean.FALSE);

        return result;
    }

    private static void checkTags(Dictionary dictionary) {
        for (Map.Entry entry : reportTags.entrySet()) {
            Tag tag = (Tag)entry.getKey();
            if (dictionary.hasTag(tag) || dictionary.hasTagged(tag)) {
                entry.setValue(Boolean.TRUE);
                System.out.println("Tag " + tag.getName() + ": OK ( " + dictionary.getName() + ")");
            }
        }
    }

}
