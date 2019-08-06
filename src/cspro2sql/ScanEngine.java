package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Tag;
import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import cspro2sql.reader.DictionaryReader;
import cspro2sql.reader.TerritoryReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class ScanEngine {

    private static final Map<Tag, Boolean> reportTags = createMap();
    private static final Map<String, String> foundTags = new HashMap<>();

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
        System.out.println();
        System.out.println("[DICTIONARIES]");

        int fileNameLength = getFileNameLength(dicts);

        for (int i = 0; i < dicts.length; i++) {
            isLocalFile = new File(dicts[i].trim()).exists();
            if (!isLocalFile) {
                dictionariesAvailable = false;
            }
            printFile(dicts[i].trim(), isLocalFile, fileNameLength);
        }
        System.out.println();
        System.out.println("[METADATA]");
        if (dictionariesAvailable) {
            try {
                List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                        prop.getProperty("db.dest.schema"),
                        prop.getProperty("dictionary"),
                        prop.getProperty("dictionary.prefix"));

                for (Dictionary dictionary : dictionaries) {
                    checkTags(dictionary);
                }
                printTags();

                System.out.println();
                System.out.println("[TERRITORY]");

                String territory = prop.getProperty("territory");
                if (territory != null && !territory.isEmpty()) { //Parse territory file
                    isLocalFile = new File(territory.trim()).exists();
                    if (isLocalFile) {
                        System.out.println("- File " + territory.trim() + ":  OK");
                        System.out.println("Parsing territory structure...");

                        for (Dictionary dictionary : dictionaries) { //Print territory structure (parsing HOUSEHOLD DICT)
                            if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
                                System.out.println("#Dictionary");
                                territoryStructure = TerritoryReader.parseTerritoryStructure(dictionary);
                                boolean isFirst = true;
                                for (TerritoryItem terrItem : territoryStructure.getItemsList()) {
                                    if (isFirst) {
                                        System.out.print(terrItem.getItemName() + "[" + terrItem.getName() + "]");
                                        isFirst = false;
                                    } else {
                                        System.out.print(" -> " + terrItem.getItemName() + "[" + terrItem.getName() + "]");
                                    }
                                }
                                System.out.println();
                            }
                        }
                        System.out.println("#Territory file");
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

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Cannot parse dictionary files", ex);
                return false;
            }
        } else {
            System.out.println("Could not access one or more dictionaries. Metadata scanning is disabled");
        }

        System.out.println();
        System.out.println("[DATABASE]");
        TestConnectionEngine.execute(prop);
        System.out.println();
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
        result.put(Dictionary.TAG_EXPECTED_QUESTIONNAIRES, Boolean.FALSE);
        result.put(Dictionary.TAG_LATITUDE, Boolean.FALSE);
        result.put(Dictionary.TAG_LONGITUDE, Boolean.FALSE);
        result.put(Dictionary.TAG_TERRITORY, Boolean.TRUE);

        return result;
    }

    private static void checkTags(Dictionary dictionary) {
        String value;
        for (Map.Entry entry : reportTags.entrySet()) {
            Tag tag = (Tag) entry.getKey();
            if (dictionary.hasTag(tag) || dictionary.hasTagged(tag)) {
                if (foundTags.containsKey(tag.getName()) && tag.equals(Dictionary.TAG_TERRITORY)) {
                    value = foundTags.get(tag.getName()) + ", " + dictionary.getName();
                } else {
                    value = dictionary.getName();
                }
                foundTags.put(tag.getName(), value);
            }
        }
    }

    private static void printTags() {
        for (Map.Entry entry : reportTags.entrySet()) {
            Tag tag = (Tag) entry.getKey();
            if (!foundTags.containsKey(tag.getName())) {
                printTag(tag.getName(), true, (Boolean) entry.getValue());
            } else {
                printTag(tag.getName(), false, false);
            }
        }
    }

    private static void checkTerritoryStructure(Territory territoryStructure, String[] header) {
        boolean matching = true;
        if (territoryStructure != null && !territoryStructure.isEmpty()) {
            for (int i = 0; i < header.length; i++) {
                if (!checkTerritoryFileColumn(header[i], territoryStructure)) {
                    System.out.println("Column " + header[i] + " does not match territory structure");
                    matching = false;
                }
            }
        }
        if (matching) {
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

    private static void printTag(String name, boolean isMissing, boolean isMandatory) {
        System.out.print("Tag " + name);
        for (int i = 0; i < getTagsLength() - name.length(); i++) {
            System.out.print(" ");
        }
        if (isMissing) {
            System.out.print("MISSING");
            if (isMandatory) {
                System.out.print(" (this tag is mandatory)");
            }
        } else {
            System.out.print("OK (" + foundTags.get(name) + ")");
        }
        System.out.println();
    }

    private static int getTagsLength() {
        int length = 0;
        for (Map.Entry entry : reportTags.entrySet()) {
            Tag tag = (Tag) entry.getKey();
            if (tag.getName().length() > length) {
                length = tag.getName().length();
            }
        }
        return length + 2;
    }

    private static int getFileNameLength(String[] dicts) {
        int length = 0;
        for (int i = 0; i < dicts.length; i++) {
            if (dicts[i].trim().length() > length) {
                length = dicts[i].trim().length();
            }
        }
        return length;
    }

    private static void printFile(String fileName, boolean fileExists, int outputLength) {
        System.out.print("- File " + fileName + "  ");
        for (int i = 0; i < outputLength - fileName.length(); i++) {
            System.out.print(" ");
        }
        if (fileExists) {
            System.out.print("OK");
        } else {
            System.out.print("ERROR (file not available)");
        }
        System.out.println();
    }
}
