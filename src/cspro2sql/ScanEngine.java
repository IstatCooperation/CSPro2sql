/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Tag;
import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import cspro2sql.reader.DictionaryReader;
import cspro2sql.reader.TerritoryReader;
import static cspro2sql.reader.TerritoryReader.parseTerritoryStructure;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        Territory territoryStructure = null;
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
        System.out.println("[Metadata]");
        if (dictionariesAvailable) {
            try {
                List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                        prop.getProperty("db.dest.schema"),
                        prop.getProperty("dictionary"),
                        prop.getProperty("dictionary.prefix"));

                for (Dictionary dictionary : dictionaries) {
                    checkTags(dictionary);
                    if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
                        System.out.println("Territory structure variable[label]");
                        territoryStructure = parseTerritoryStructure(dictionary);
                        boolean isFirst = true;
                        for (TerritoryItem terrItem : territoryStructure.getItemsList()) {
                            if (isFirst) {
                                System.out.print(terrItem.getItemName() + "[" + terrItem.getName() + "]");
                                isFirst = false;
                            } else {
                                System.out.print(" -> " + terrItem.getItemName() + "[" + terrItem.getName() + "]");
                            }
                        }
                        System.out.println("");
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Cannot parse dictionary files", ex);
                return false;
            }
        } else {
            System.out.println("Could not access one or more dictionaries. Metadata scanning is disabled");
        }

        System.out.println("[Territory]");
        String territory = prop.getProperty("territory");
        if (territory != null && !territory.isEmpty()) {
            isLocalFile = new File(territory.trim()).exists();
            if (isLocalFile) {
                System.out.println("- File " + territory.trim() + ": OK");
                String[] header = TerritoryReader.getHeader(territory);
                for (int i = 0; i < header.length; i++) {
                    if (i == 0) {
                        System.out.print(header[i]);
                    } else {
                        System.out.print(" -> " + header[i]);
                    }
                }
                System.out.println("");
                checkTerritoryStructure(territoryStructure, header);
            } else {
                System.out.println("- File " + territory.trim() + ": ERROR (file not available)");
            }
        } else {
            System.out.println("Territory file not specified!");
        }

        System.out.println("[Database]");
        TestConnectionEngine.execute(prop);

        System.out.println("...scanning completed!");
        return true;
    }

    private static Map<Tag, Boolean> createMap() {

        Map<Tag, Boolean> result = new LinkedHashMap<>();

        result.put(Dictionary.TAG_HOUSEHOLD, Boolean.TRUE);
        result.put(Dictionary.TAG_LISTING, Boolean.FALSE);
        result.put(Dictionary.TAG_EXPECTED, Boolean.FALSE);
        result.put(Dictionary.TAG_INDIVIDUAL, Boolean.TRUE);
        result.put(Dictionary.TAG_AGE, Boolean.TRUE);
        result.put(Dictionary.TAG_SEX, Boolean.TRUE);
        result.put(Dictionary.TAG_RELIGION, Boolean.FALSE);
        result.put(Dictionary.TAG_TERRITORY, Boolean.TRUE);

        return result;
    }

    private static void checkTags(Dictionary dictionary) {

        List<String> foundTags = new ArrayList<>();

        for (Map.Entry entry : reportTags.entrySet()) {
            Tag tag = (Tag) entry.getKey();
            if (dictionary.hasTag(tag) || dictionary.hasTagged(tag)) {
                foundTags.add(tag.getName());
            }
        }

        //Cicle on tags
        for (Map.Entry entry : reportTags.entrySet()) {
            Tag tag = (Tag) entry.getKey();
            if (!foundTags.contains(tag.getName())) {
                System.out.print("Tag " + tag.getName() + ": MISSING");
                if ((Boolean) entry.getValue()) { //mandatory tag
                    System.out.print(" (this tag is mandatory)");
                }
                System.out.println("");
            } else {
                System.out.println("Tag " + tag.getName() + ": OK (" + dictionary.getName() + ")");
            }
        }
    }

    private static void checkTerritoryStructure(Territory territoryStructure, String[] header) {
        boolean matching = true;
        if (territoryStructure != null && !territoryStructure.isEmpty()) {
            for (int i = 0; i < header.length; i++) {
                if (!checkTerritoryFileColumn(header[i], territoryStructure)){
                    System.out.println("Column " + header[i] + " does not match territory structure");
                    matching = false;
                }
            }
        }
        if(matching){
            System.out.println("Territory file matches metadata. It is possible to generate the territory table!");
        }
    }

    private static boolean checkTerritoryFileColumn(String columnName, Territory territoryStructure) {
        for (TerritoryItem item : territoryStructure.getItemsList()) {
            if (item.getName().equals(columnName.replace("_NAME", "")) || item.getItemName().equals(columnName.replace("_NAME", ""))) {
                return true;
            }
        }
        return false;
    }
}
