package cspro2sql;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class GenerateEngine {

    public static final String FOLDER_DICTIONARY = "dictionary";
    public static final String FOLDER_TERRITORY = "territory";
    public static final String FOLDER_BATCH = "batch";
    public static final String FOLDER_LOG = "log";
    public static final String FILE_README = "README.txt";
    public static final String FILE_README_DICTIONARY = "README_DICTIONARY.txt";
    public static final String FILE_README_TERRITORY = "README_TERRITORY.txt";
    public static final String FILE_README_BATCH = "README_BATCH.txt";
    public static final String FILE_TERRITORY = "territory.csv";
    public static final String FILE_SQL_MICRO = "dashboard_micro.sql";
    public static final String FILE_SQL_REPORT = "dashboard_report.sql";
    public static final String FILE_HOUSEHOLD_TEMPLATE = "household_template.dcf";
    public static final String FILE_LISTING_TEMPLATE = "listing_template.dcf";
    public static final String FILE_CARTOGRAPHY_TEMPLATE = "cartography_template.dcf";
    public static final String FILE_TERRITORY_TEMPLATE = "territory_template.csv";
    public static final String FILE_BATCH_UPDATE = "batch_update";
    public static final String FILE_BATCH_LOAD = "batch_load";
    private static String OS = System.getProperty("os.name").toLowerCase();
    private static final String FILE_SEPARATOR = getFileSeparator();

    public static void main(String[] args) {
        try {
            execute("pilot", "household", "listing", "eacode");
        } catch (Exception ex) {
            System.exit(1);
        }

    }

    public static boolean execute(String surveyFolder, String householdName, String listingName, String eaName) {
        File dir = new File(surveyFolder);
        File dirDictionary = new File(surveyFolder + "/" + FOLDER_DICTIONARY);
        File dirTerritory = new File(surveyFolder + "/" + FOLDER_TERRITORY);
        File dirBatch = new File(surveyFolder + "/" + FOLDER_BATCH);
        File dirBatchLog = new File(surveyFolder + "/" + FOLDER_BATCH + "/" + FOLDER_LOG);
        System.out.println("Starting generation of project " + surveyFolder);
        if (!dir.exists()) {
            dir.mkdir();
            dirDictionary.mkdir();
            dirTerritory.mkdir();
            dirBatch.mkdir();
            dirBatchLog.mkdir();
            System.out.println("Created folder " + surveyFolder);
            System.out.println("Created folder " + surveyFolder + "/" + FOLDER_DICTIONARY);
            System.out.println("Created folder " + surveyFolder + "/" + FOLDER_TERRITORY);
            System.out.println("Created folder " + surveyFolder + "/" + FOLDER_BATCH);
            System.out.println("Created folder " + surveyFolder + "/" + FOLDER_BATCH + "/" + FOLDER_LOG);

            createPropertiesFile(dir, surveyFolder, householdName, listingName, eaName);
            createReadmeFile(dir, surveyFolder);
            createHouseholdFile(dirDictionary, surveyFolder);
            if (!listingName.equals("")) {
                createListingFile(dirDictionary, surveyFolder);
            }
            if (!eaName.equals("")) {
                createCartographyFile(dirDictionary, surveyFolder);
            }
            createMetadataReadmeFile(dirDictionary, surveyFolder);
            createTerritoryFile(dirTerritory, surveyFolder);
            createTerritoryReadmeFile(dirTerritory, surveyFolder);
            //Batch
            createBatchScripts(dirBatch, dir);
            createBatchReadmeFile(dirBatch, dir);

            System.out.println("Project " + surveyFolder + " successfully created.");
            System.out.println("Now you are ready to start processing your data!");
            System.out.println("");
            System.out.println("Please open the file " + surveyFolder + "/" + FILE_README);

        } else {
            System.err.println("[ERROR] Could not generate project files. Folder " + surveyFolder + " already exists!");
            return false;
        }

        return true;
    }

    private static void createPropertiesFile(File dir, String surveyFolder, String householdName, String listingName, String eaName) {
        File properties = new File(dir, surveyFolder + ".properties");
        try {
            if (properties.createNewFile()) {
                FileWriter fw = new FileWriter(properties.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    String dictionaries = "";
                    String prefixes = "";
                    if (!householdName.equals("")) {
                        dictionaries += surveyFolder + "/" + FOLDER_DICTIONARY + "/" + householdName + ".dcf";
                        prefixes += "h";
                    }
                    if (!listingName.equals("")) {
                        dictionaries += "," + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + listingName + ".dcf";
                        prefixes += ",l";
                    }
                    if (!eaName.equals("")) {
                        dictionaries += "," + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + eaName + ".dcf";
                        prefixes += ",ea";
                    }
                    bw.write("#[CSPro] List of CSPro dictionaries (household, freshlist, EA)");
                    bw.newLine();
                    bw.write("dictionary=" + dictionaries);
                    bw.newLine();
                    bw.newLine();
                    bw.write("#[Dashboard] Table prefixes in Dashboard database (household, freshlist, EA)");
                    bw.newLine();
                    bw.write("dictionary.prefix=" + prefixes);
                    bw.newLine();
                    bw.newLine();
                    bw.write("#[Territory] File containing territory data (codes, names)");
                    bw.newLine();
                    bw.write("territory=" + surveyFolder + "/" + FOLDER_TERRITORY + "/" + FILE_TERRITORY);
                    bw.newLine();
                    bw.newLine();
                    bw.write("#[CSPro] Specify CSWEB database connection parameters");
                    bw.newLine();
                    bw.write("db.source.server=server-host");
                    bw.newLine();
                    bw.write("db.source.port=server-port");
                    bw.newLine();
                    bw.write("db.source.schema=csweb-schema");
                    bw.newLine();
                    bw.write("db.source.username=root");
                    bw.newLine();
                    bw.write("db.source.password=root");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#[Dashboard] Specify Dashboard database connection parameters");
                    bw.newLine();
                    bw.write("db.dest.server=server-host");
                    bw.newLine();
                    bw.write("db.dest.port=server-port");
                    bw.newLine();
                    bw.write("db.dest.schema=dashboard-schema");
                    bw.newLine();
                    bw.write("db.dest.username=root");
                    bw.newLine();
                    bw.write("db.dest.password=root");
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + surveyFolder + ".properties");
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createReadmeFile(File dir, String surveyFolder) {
        File readme = new File(dir, FILE_README);
        try {
            if (readme.createNewFile()) {
                FileWriter fw = new FileWriter(readme.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("This file contains the steps to generate and populate the Dashboard database.");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[PRELIMINARY STEPS]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 1] Copy your CSPro dictionary files in the folder " + surveyFolder + "/" + FOLDER_DICTIONARY);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 2] Add cspro2sql metadata to each dictionary. Metadata guidelines are provided in " + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + FILE_README);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 3] Set CSPro and Dashboard databases connection parameters in " + surveyFolder + "/" + surveyFolder + ".properties");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 4] Copy the territory file in the folder " + surveyFolder + "/" + FOLDER_TERRITORY + ". ");
                    bw.write("Territory guidelines are provided in " + surveyFolder + "/" + FOLDER_TERRITORY + "/" + FILE_README);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 5] In order to test your environment, execute the following command in a terminal:");
                    bw.newLine();
                    bw.newLine();
                    bw.write("cd " + System.getProperty("user.dir"));
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e scan -p " + surveyFolder + "/" + surveyFolder + ".properties");
                    bw.newLine();
                    bw.newLine();
                    bw.newLine();
                    bw.write("[RUNTIME STEPS]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 1] Generate the Dashboard database script " + surveyFolder + "/" + FILE_SQL_MICRO);
                    bw.newLine();
                    bw.newLine();
                    bw.write("cd " + System.getProperty("user.dir"));
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e schema -p " + surveyFolder + "/" + surveyFolder + ".properties -o " + surveyFolder + "/" + FILE_SQL_MICRO);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 2] Create the Dashboard database. Use your favourite Mysql client or execute the following command ");
                    bw.write("(replace #root_user with the root username):");
                    bw.newLine();
                    bw.newLine();
                    bw.write("mysql -u #root_user -p < " + surveyFolder + "/" + FILE_SQL_MICRO);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 3] Load CSPro data in Dashboard database (step to be repeated during fieldwork)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e loader -p " + surveyFolder + "/" + surveyFolder + ".properties");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 4] Create and populate the territory table");
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e territory -p " + surveyFolder + "/" + surveyFolder + ".properties");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 5] Generate the Dashboard report tables script " + surveyFolder + "/" + FILE_SQL_REPORT);
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e monitor -p " + surveyFolder + "/" + surveyFolder + ".properties -o " + surveyFolder + "/" + FILE_SQL_REPORT);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 6] Create the Dashboard report tables. Use your favourite Mysql client or execute the following command");
                    bw.write("(replace #root_user with the root username):");
                    bw.newLine();
                    bw.newLine();
                    bw.write("mysql -u #root_user -p < " + surveyFolder + "/" + FILE_SQL_REPORT);
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Step 7] Update reports (step to be repeated during fieldwork)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e loader -p " + surveyFolder + "/" + surveyFolder + ".properties");
                    bw.newLine();
                    bw.newLine();
                    bw.write("cspro2sql -e update -p " + surveyFolder + "/" + surveyFolder + ".properties");
                    bw.newLine();
                    bw.newLine();
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FILE_README);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createHouseholdFile(File dirDictionary, String surveyFolder) {
        File properties = new File(dirDictionary, FILE_HOUSEHOLD_TEMPLATE);
        try {
            if (properties.createNewFile()) {
                FileWriter fw = new FileWriter(properties.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("[Dictionary]");
                    bw.newLine();
                    bw.write("Version=CSPro 7.0");
                    bw.newLine();
                    bw.write("Label=Household");
                    bw.newLine();
                    bw.write("Name=HOUSEHOLD_DICT");
                    bw.newLine();
                    bw.write("RecordTypeStart=1");
                    bw.newLine();
                    bw.write("RecordTypeLen=1");
                    bw.newLine();
                    bw.write("Positions=Relative");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("DecimalChar=Yes");
                    bw.newLine();
                    bw.write("Note=#household");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Level]");
                    bw.newLine();
                    bw.write("Label=Household questionnaire");
                    bw.newLine();
                    bw.write("Name=HOUSEHOLD_QUEST");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[IdItems]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=101 Region");
                    bw.newLine();
                    bw.write("Name=ID101");
                    bw.newLine();
                    bw.write("Start=2");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Region]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=102 Province");
                    bw.newLine();
                    bw.write("Name=ID102");
                    bw.newLine();
                    bw.write("Start=4");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Province, ID101]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=103 Commune");
                    bw.newLine();
                    bw.write("Name=ID103");
                    bw.newLine();
                    bw.write("Start=6");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Commune, ID102]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=104 Enumeration Area");
                    bw.newLine();
                    bw.write("Name=ID104");
                    bw.newLine();
                    bw.write("Start=8");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[EA, ID103]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Record]");
                    bw.newLine();
                    bw.write("Label=Individual");
                    bw.newLine();
                    bw.write("Name=INDIVIDUAL");
                    bw.newLine();
                    bw.write("RecordTypeValue='I'");
                    bw.newLine();
                    bw.write("Required=No");
                    bw.newLine();
                    bw.write("MaxRecords=99");
                    bw.newLine();
                    bw.write("RecordLen=210");
                    bw.newLine();
                    bw.write("Note=#individual");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=205 Sex");
                    bw.newLine();
                    bw.write("Name=P205");
                    bw.newLine();
                    bw.write("Start=12");
                    bw.newLine();
                    bw.write("Len=1");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#sex");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[ValueSet]");
                    bw.newLine();
                    bw.write("Label=205 Sex");
                    bw.newLine();
                    bw.write("Name=P205_VS1");
                    bw.newLine();
                    bw.write("Value=1;Male");
                    bw.newLine();
                    bw.write("Note=#male");
                    bw.newLine();
                    bw.write("Value=1;Male");
                    bw.newLine();
                    bw.write("Note=#female");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=206 Age");
                    bw.newLine();
                    bw.write("Name=P206");
                    bw.newLine();
                    bw.write("Start=13");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#age[0,5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,97]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=207 Religion");
                    bw.newLine();
                    bw.write("Name=P207");
                    bw.newLine();
                    bw.write("Start=15");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#religion");
                    bw.newLine();
                    bw.newLine();
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + FILE_HOUSEHOLD_TEMPLATE);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createListingFile(File dirDictionary, String surveyFolder) {
        File properties = new File(dirDictionary, FILE_LISTING_TEMPLATE);
        try {
            if (properties.createNewFile()) {
                FileWriter fw = new FileWriter(properties.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("[Dictionary]");
                    bw.newLine();
                    bw.write("Version=CSPro 7.0");
                    bw.newLine();
                    bw.write("Label=Listing");
                    bw.newLine();
                    bw.write("Name=LISTING_DICT");
                    bw.newLine();
                    bw.write("RecordTypeStart=1");
                    bw.newLine();
                    bw.write("RecordTypeLen=1");
                    bw.newLine();
                    bw.write("Positions=Relative");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("DecimalChar=Yes");
                    bw.newLine();
                    bw.write("Note=#listing");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Level]");
                    bw.newLine();
                    bw.write("Label=Listing questionnaire");
                    bw.newLine();
                    bw.write("Name=LISTING_QUEST");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[IdItems]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=101 Region");
                    bw.newLine();
                    bw.write("Name=L_ID101");
                    bw.newLine();
                    bw.write("Start=2");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Region]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=102 Province");
                    bw.newLine();
                    bw.write("Name=L_ID102");
                    bw.newLine();
                    bw.write("Start=4");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Province, L_ID101]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=103 Commune");
                    bw.newLine();
                    bw.write("Name=L_ID103");
                    bw.newLine();
                    bw.write("Start=6");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Commune, L_ID102]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=104 Enumeration Area");
                    bw.newLine();
                    bw.write("Name=L_ID104");
                    bw.newLine();
                    bw.write("Start=8");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[EA, L_ID103]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Record]");
                    bw.newLine();
                    bw.write("Label=GPS");
                    bw.newLine();
                    bw.write("Name=GPA");
                    bw.newLine();
                    bw.write("RecordTypeValue='2'");
                    bw.newLine();
                    bw.write("RecordLen=82");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=A1_Latitude");
                    bw.newLine();
                    bw.write("Name=A1");
                    bw.newLine();
                    bw.write("Start=36");
                    bw.newLine();
                    bw.write("Len=13");
                    bw.newLine();
                    bw.write("Decimal=9");
                    bw.newLine();
                    bw.write("Note=#lat");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=A2_Longitude");
                    bw.newLine();
                    bw.write("Name=A2");
                    bw.newLine();
                    bw.write("Start=49");
                    bw.newLine();
                    bw.write("Len=13");
                    bw.newLine();
                    bw.write("Decimal=9");
                    bw.newLine();
                    bw.write("Note=#lon");
                    bw.newLine();
                    bw.newLine();

                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + FILE_LISTING_TEMPLATE);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createCartographyFile(File dirDictionary, String surveyFolder) {
        File properties = new File(dirDictionary, FILE_CARTOGRAPHY_TEMPLATE);
        try {
            if (properties.createNewFile()) {
                FileWriter fw = new FileWriter(properties.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("[Dictionary]");
                    bw.newLine();
                    bw.write("Version=CSPro 7.0");
                    bw.newLine();
                    bw.write("Label=EA code");
                    bw.newLine();
                    bw.write("Name=EA_CODE_DICT");
                    bw.newLine();
                    bw.write("RecordTypeStart=1");
                    bw.newLine();
                    bw.write("RecordTypeLen=1");
                    bw.newLine();
                    bw.write("Positions=Relative");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("DecimalChar=Yes");
                    bw.newLine();
                    bw.write("Note=#expected");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Level]");
                    bw.newLine();
                    bw.write("Label=EA code questionnaire");
                    bw.newLine();
                    bw.write("Name=EA_CODE_QUEST");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[IdItems]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=101 Region");
                    bw.newLine();
                    bw.write("Name=EA_ID101");
                    bw.newLine();
                    bw.write("Start=2");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Region]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=102 Province");
                    bw.newLine();
                    bw.write("Name=EA_ID102");
                    bw.newLine();
                    bw.write("Start=4");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Province, EA_ID101]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=103 Commune");
                    bw.newLine();
                    bw.write("Name=EA_ID103");
                    bw.newLine();
                    bw.write("Start=6");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[Commune, EA_ID102]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=104 Enumeration Area");
                    bw.newLine();
                    bw.write("Name=EA_ID104");
                    bw.newLine();
                    bw.write("Start=8");
                    bw.newLine();
                    bw.write("Len=2");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#territory[EA, EA_ID103]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Record]");
                    bw.newLine();
                    bw.write("Label=EA code record");
                    bw.newLine();
                    bw.write("Name=EA_CODE_REC");
                    bw.newLine();
                    bw.write("RecordTypeValue='I'");
                    bw.newLine();
                    bw.write("MaxRecords=10");
                    bw.newLine();
                    bw.write("RecordLen=21");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=Expected Households from Cartograhpy");
                    bw.newLine();
                    bw.write("Name=EA_HHS");
                    bw.newLine();
                    bw.write("Start=12");
                    bw.newLine();
                    bw.write("Len=5");
                    bw.newLine();
                    bw.write("ZeroFill=Yes");
                    bw.newLine();
                    bw.write("Note=#expectedQuestionnaires");
                    bw.newLine();
                    bw.newLine();
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + FILE_CARTOGRAPHY_TEMPLATE);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createMetadataReadmeFile(File dirDictionary, String surveyFolder) {
        File readme = new File(dirDictionary, FILE_README_DICTIONARY);
        try {
            if (readme.createNewFile()) {
                FileWriter fw = new FileWriter(readme.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("This file contains the list of metadata that you should add to your dictionaries.");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[METADATA @dictionary level]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#household: use this tag to mark the household dictionary (check the 'Note=#fieldwork' in the Household_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#individual: use this tag to mark the individual table (check the 'Note=#individual' in the Household_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#listing: use this tag to mark the listing dictionary (check the 'Note=#listing' in the Listing_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#expected: use this tag to mark the EA code dictionary (check the 'Note=#expected' in the Eacode_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#household and #individual tags are MANDATORY");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[METADATA @variable level]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#age: use this tag to mark the age variable. It is also necessary to specify the range of variable (check the 'Note=#age' in the Household_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#sex: use this tag to mark the sex variable. It is also necessary to mark in the valueset the Male/Female values (check the 'Note=#sex' in the Household_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#religion: use this tag to mark the religion variable (check the Note in the Household_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#expectedQuestionnaires: use this tag to mark the expected households from cartograhpy (check the Note in the EA_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#lat: use this tag to mark the latitude of the household (check the Note in the Listing_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("#lon: use this tag to mark the longitude of the household (check the Note in the Listing_template.dcf file)");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[METADATA @territory level]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("The territory metadata allow to specify the territorial hierarchy. Suppose that your hierarchy is the following:");
                    bw.newLine();
                    bw.newLine();
                    bw.write("Region -> Province -> Commune -> EA");
                    bw.newLine();
                    bw.newLine();
                    bw.write("Further let us suppose that in your Household dictionary the variables related to your territory structure are:");
                    bw.newLine();
                    bw.newLine();
                    bw.write("ID101 -> Region");
                    bw.newLine();
                    bw.newLine();
                    bw.write("ID102 -> Province");
                    bw.newLine();
                    bw.newLine();
                    bw.write("ID103 -> Commune");
                    bw.newLine();
                    bw.newLine();
                    bw.write("ID104 -> EA");
                    bw.newLine();
                    bw.newLine();
                    bw.write("In order to bind variables and territorial hiedarchy it is necessary to add the following notes (check the Household_template.dcf file):");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=101 Region");
                    bw.newLine();
                    bw.write("Name=ID101");
                    bw.newLine();
                    bw.write("Note=#territory[Region]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=102 Province");
                    bw.newLine();
                    bw.write("Name=ID102");
                    bw.newLine();
                    bw.write("Note=#territory[Province, ID101]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=103 Commune");
                    bw.newLine();
                    bw.write("Name=ID103");
                    bw.newLine();
                    bw.write("Note=#territory[Commune, ID102]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("[Item]");
                    bw.newLine();
                    bw.write("Label=104 EA");
                    bw.newLine();
                    bw.write("Name=ID104");
                    bw.newLine();
                    bw.write("Note=#territory[EA, ID103]");
                    bw.newLine();
                    bw.newLine();
                    bw.write("The territory metadata are MANDATORY");
                    bw.newLine();
                    bw.newLine();
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_DICTIONARY + "/" + FILE_README_DICTIONARY);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createTerritoryFile(File dirTerritory, String surveyFolder) {
        File properties = new File(dirTerritory, FILE_TERRITORY_TEMPLATE);
        try {
            if (properties.createNewFile()) {
                FileWriter fw = new FileWriter(properties.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("Region;Region_NAME;Province;Province_NAME;Commune;Commune_NAME;EA EA_NAME");
                    bw.newLine();
                    bw.write("1;Lazio;1;Frosinone;1;Alvito;1;001");
                    bw.newLine();
                    bw.write("1;Lazio;1;Frosinone;1;Alvito;2;002");
                    bw.newLine();
                    bw.write("1;Lazio;1;Frosinone;1;Alvito;3;003");
                    bw.newLine();
                    bw.write("1;Lazio;1;Frosinone;2;Cervaro;1;001");
                    bw.newLine();
                    bw.write("1;Lazio;1;Frosinone;2;Cervaro;2;002");
                    bw.newLine();
                    bw.write("1;Lazio;1;Frosinone;2;Cervaro;4;004");
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_TERRITORY + "/" + FILE_TERRITORY_TEMPLATE);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createTerritoryReadmeFile(File dirTerritory, String surveyFolder) {
        File readme = new File(dirTerritory, FILE_README_TERRITORY);
        try {
            if (readme.createNewFile()) {
                FileWriter fw = new FileWriter(readme.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("The structure of the territory file is strongly connected to the territory metadata specified in your dictionaries.");
                    bw.newLine();
                    bw.write("More specifically if your hierarchy is the following:");
                    bw.newLine();
                    bw.newLine();
                    bw.write("Region -> Province -> Commune -> EA");
                    bw.newLine();
                    bw.newLine();
                    bw.write("The territory.csv file should have the following columns:");
                    bw.newLine();
                    bw.newLine();
                    bw.write("Region; Region_NAME; Province; Province_NAME; Commune; Commune_NAME; EA; EA_NAME");
                    bw.newLine();
                    bw.newLine();
                    bw.write("Therefore at each level of the hierarchy correspond two columns (code, description).");
                    bw.newLine();
                    bw.write("The name of the columns shoud be the same that you specified in the territory notes.");
                    bw.newLine();
                    bw.newLine();
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_TERRITORY + "/" + FILE_README_TERRITORY);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createBatchReadmeFile(File dirBatch, File surveyFolder) {
        File readme = new File(dirBatch, FILE_README_BATCH);
        try {
            if (readme.createNewFile()) {
                FileWriter fw = new FileWriter(readme.getAbsoluteFile());
                try (BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write("This folder contains batch files, that can be scheduled during the survey.");
                    bw.newLine();
                    bw.write(FILE_BATCH_LOAD + getExtension() + ":   executes cspro2sql 'loader' engine.");
                    bw.newLine();
                    bw.write(FILE_BATCH_UPDATE + getExtension() + ": executes cspro2sql 'update' engine.");
                    bw.newLine();
                    bw.newLine();
                    bw.write("The output of the batch processes is stored in the log folder.");
                    bw.newLine();
                    bw.newLine();
                    bw.write("USAGE:");
                    bw.newLine();
                    bw.write("copy " + FILE_BATCH_LOAD + getExtension() + " " + getRootFolder(surveyFolder));
                    bw.newLine();
                    bw.write("copy " + FILE_BATCH_UPDATE + getExtension() + " " + getRootFolder(surveyFolder));
                    bw.newLine();
                    bw.write("cd " + getRootFolder(surveyFolder));
                    bw.newLine();
                    bw.write(FILE_BATCH_LOAD + getExtension());
                    bw.newLine();
                    bw.write(FILE_BATCH_UPDATE + getExtension());
                    bw.newLine();
                    bw.close();
                    System.out.println("Created file " + surveyFolder + "/" + FOLDER_BATCH + "/" + FILE_README_BATCH);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void createBatchScripts(File dirBatch, File surveyFolder) {
        File loadBatch;
        File updateBatch;
        if (isWindows()) {
            loadBatch = new File(dirBatch, FILE_BATCH_LOAD + ".bat");
            try {
                if (loadBatch.createNewFile()) {
                    FileWriter fw = new FileWriter(loadBatch.getAbsoluteFile());
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write("@ECHO OFF");
                        bw.newLine();
                        bw.write("echo  %date%-%time%");
                        bw.newLine();
                        bw.write("ECHO Starting " + FILE_BATCH_LOAD + " execution at %date% – %time% >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_LOAD + ".log");
                        bw.newLine();
                        bw.write("java -cp ");
                        bw.write("lib" + FILE_SEPARATOR + "cspro2sql.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "commons-cli-1.3.1.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "mysql-connector-java-8.0.16.jar ");
                        bw.write("cspro2sql.Main %* -e loader -p ");
                        bw.write(surveyFolder + FILE_SEPARATOR + surveyFolder + ".properties" + " -f >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_LOAD + ".log 2>>&1");
                        bw.newLine();
                        bw.close();
                        System.out.println("Created file " + surveyFolder + "/" + FOLDER_BATCH + "/" + FILE_BATCH_LOAD + ".bat");
                    }
                }
                updateBatch = new File(dirBatch, FILE_BATCH_UPDATE + ".bat");
                if (updateBatch.createNewFile()) {
                    FileWriter fw = new FileWriter(updateBatch.getAbsoluteFile());
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write("@ECHO OFF");
                        bw.newLine();
                        bw.write("echo  %date%-%time%");
                        bw.newLine();
                        bw.write("ECHO Starting " + FILE_BATCH_UPDATE + " execution at %date% – %time% >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_UPDATE + ".log");
                        bw.newLine();
                        bw.write("java -cp ");
                        bw.write("lib" + FILE_SEPARATOR + "cspro2sql.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "commons-cli-1.3.1.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "mysql-connector-java-8.0.16.jar ");
                        bw.write("cspro2sql.Main %* -e update -p ");
                        bw.write(surveyFolder + FILE_SEPARATOR + surveyFolder + ".properties" + " >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_UPDATE + ".log 2>>&1");
                        bw.newLine();
                        bw.close();
                        System.out.println("Created file " + surveyFolder + "/" + FOLDER_BATCH + "/" + FILE_BATCH_UPDATE + ".bat");
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (isUnix()) { //TO BE TESTED
            loadBatch = new File(dirBatch, FILE_BATCH_LOAD + ".sh");
            try {
                if (loadBatch.createNewFile()) {
                    FileWriter fw = new FileWriter(loadBatch.getAbsoluteFile());
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write("#!/bin/bash");
                        bw.newLine();
                        bw.write("NOW=$(date +\"%D%T\")");
                        bw.newLine();
                        bw.write("ECHO Starting " + FILE_BATCH_LOAD + " execution at $NOW >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_LOAD + ".log");
                        bw.newLine();
                        bw.write("java -cp '");
                        bw.write("lib" + FILE_SEPARATOR + "cspro2sql.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "commons-cli-1.3.1.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "mysql-connector-java-8.0.16.jar' ");
                        bw.write("cspro2sql.Main $@ -e loader -p ");
                        bw.write(surveyFolder + FILE_SEPARATOR + surveyFolder + ".properties" + " -f >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_LOAD + ".log 2>>&1");
                        bw.newLine();
                        bw.close();
                        System.out.println("Created file " + surveyFolder + "/" + FOLDER_BATCH + "/" + FILE_BATCH_LOAD + ".sh");
                    }
                }
                updateBatch = new File(dirBatch, FILE_BATCH_UPDATE + ".sh");
                if (updateBatch.createNewFile()) {
                    FileWriter fw = new FileWriter(updateBatch.getAbsoluteFile());
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        bw.write("#!/bin/bash");
                        bw.newLine();
                        bw.write("NOW=$(date +\"%D%T\")");
                        bw.newLine();
                        bw.write("ECHO Starting " + FILE_BATCH_UPDATE + " execution at $NOW >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_UPDATE + ".log");
                        bw.newLine();
                        bw.write("java -cp '");
                        bw.write("lib" + FILE_SEPARATOR + "cspro2sql.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "commons-cli-1.3.1.jar;");
                        bw.write("lib" + FILE_SEPARATOR + "mysql-connector-java-8.0.16.jar' ");
                        bw.write("cspro2sql.Main $@ -e update -p ");
                        bw.write(surveyFolder + FILE_SEPARATOR + surveyFolder + ".properties" + " >> ");
                        bw.write(surveyFolder + FILE_SEPARATOR + FOLDER_BATCH + FILE_SEPARATOR + FOLDER_LOG + FILE_SEPARATOR + FILE_BATCH_UPDATE + ".log 2>>&1");
                        bw.newLine();
                        bw.close();
                        System.out.println("Created file " + surveyFolder + "/" + FOLDER_BATCH + "/" + FILE_BATCH_UPDATE + ".sh");
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(GenerateEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.err.println("Unsupported operating system!");
        }

    }

    private static String getRootFolder(File surveyFolder) {
        return surveyFolder.getAbsoluteFile().getParentFile().getAbsolutePath();
    }

    private static String getProjectFolder(File surveyFolder) {
        return surveyFolder.getAbsoluteFile().getAbsolutePath();
    }

    private static boolean isWindows() {
        return (OS.contains("win"));
    }

    private static boolean isUnix() {
        return (OS.contains("nux"));
    }

    private static String getFileSeparator() {
        if (isWindows()) {
            return "\\";
        } else {
            return "/";
        }
    }

    private static String getExtension() {
        if (isWindows()) {
            return ".bat";
        } else if (isUnix()) {
            return ".sh";
        } else {
            return ".txt";
        }
    }
}
