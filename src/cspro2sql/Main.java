package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.reader.DictionaryReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
 * @author Paolo Giacomi <giacomi @ istat.it>
 * @version 0.9.16
 */
public class Main {

    private static final String VERSION = "0.9.5";
    private static final Logger LOGGER = Logger.getLogger(LoaderEngine.class.getName());

    public static void main(String[] args) {
        CsPro2SqlOptions opts = getCommandLineOptions(args);
        boolean error = false;
        List<Dictionary> dictionaries;
        try {
            dictionaries = DictionaryReader.parseDictionaries(opts.schema, opts.dictionary, opts.tablePrefix);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not access dictionary files. Please check the paths specified in your properties file", e);
            opts.ps.close();
            opts.printError(e.getMessage());
            return;
        }

        if (opts.schemaEngine) {
            error = !SchemaEngine.execute(dictionaries, opts.prop, opts.foreignKeys, opts.ps);
        } else if (opts.loaderEngine) {
            if (opts.delay == null) {
                error = !LoaderEngine.execute(dictionaries, opts.prop, opts.allRecords, opts.checkConstraints, opts.checkOnly, opts.force, opts.recovery, opts.ps);
            } else {
                while (true) {
                    try {
                        LoaderEngine.execute(dictionaries, opts.prop, opts.allRecords, opts.checkConstraints, opts.checkOnly, opts.force, opts.recovery, opts.ps);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                    try {
                        Thread.sleep(opts.delay);
                    } catch (Exception ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        } else if (opts.monitorEngine) {
            error = !MonitorEngine.execute(dictionaries, opts.ps);
        } else if (opts.updateEngine) {
            error = !UpdateEngine.execute(opts.prop);
        } else if (opts.statusEngine) {
            error = !StatusEngine.execute(dictionaries, opts.prop);
        } else if (opts.linkageEngine) {
            //error = !LinkageEngine.execute(dictionary, pesDictionary, opts.prop, opts.ps);
        } else if (opts.testConnectionEngine) {
            error = !TestConnectionEngine.execute(opts.prop);
        } else if (opts.territoryEngine) {
            error = !TerritoryEngine.execute(dictionaries, opts.prop);
        } else if (opts.scanEngine) {
            error = !ScanEngine.execute(dictionaries, opts.prop);
        } else if (opts.loadAndUpdate) {
            while (true) {
                try {
                    LoaderEngine.execute(dictionaries, opts.prop, opts.allRecords, opts.checkConstraints, opts.checkOnly, opts.force, opts.recovery, opts.ps);
                    UpdateEngine.execute(opts.prop);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
                try {
                    Thread.sleep(opts.delay);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }

        if (opts.ps != null) {
            opts.ps.close();
        }

        if (error) {
            System.exit(1);
        }
    }

    private static CsPro2SqlOptions getCommandLineOptions(String[] args) {
        Options options = new Options();
        options.addOption("a", "all", false, "transfer all the questionnaires");
        options.addOption("cc", "check-constraints", false, "perform constraints check");
        options.addOption("co", "check-only", false, "perform only constraints check (no data transfer)");
        options.addOption("e", "engine", true, "select engine: [loader|schema|monitor|update|status|linkage]");
        options.addOption("f", "force", false, "skip check of loader multiple running instances");
        options.addOption("fk", "foreign-keys", false, "create foreign keys to value sets");
        options.addOption("h", "help", false, "display this help");
        options.addOption("o", "output", true, "name of the output file");
        options.addOption("p", "properties", true, "properties file");
        options.addOption("r", "recovery", false, "recover a broken session of the loader");
        options.addOption("v", "version", false, "print the version of the programm");
        options.addOption("d", "delay", true, "perform again after DELAY minutes");

        //Begin parsing command line
        CsPro2SqlOptions opts = new CsPro2SqlOptions(options);
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h") || args.length == 0) {
                opts.printHelp();
            }
            if (cmd.hasOption("v")) {
                opts.printVersion();
            }

            if (cmd.hasOption("e")) {

                //[TO DO] Check if engine type exists
                
                //Questo controllo sembra non funzionare
                if (!cmd.hasOption("p")) {
                    opts.printError("The properties file is mandatory!");
                }

                opts.propertiesFile = cmd.getOptionValue("p");
                String engine = cmd.getOptionValue("e");
                switch (engine) {
                    case "schema":
                        opts.schemaEngine = true;
                        opts.foreignKeys = cmd.hasOption("fk");
                        if (cmd.hasOption("o")) {
                            opts.ps = new PrintStream(cmd.getOptionValue("o"), "UTF-8");
                        } else {
                            opts.ps = System.out;
                        }
                        break;
                    case "loader":
                    case "LU":
                        opts.loaderEngine = "loader".equals(engine);
                        opts.loadAndUpdate = "LU".equals(engine);
                        opts.checkConstraints = cmd.hasOption("cc");
                        opts.checkOnly = cmd.hasOption("co");
                        opts.allRecords = cmd.hasOption("a");
                        opts.force = cmd.hasOption("f");
                        opts.recovery = cmd.hasOption("r");
                        if (cmd.hasOption("o")) {
                            opts.ps = new PrintStream(cmd.getOptionValue("o"), "UTF-8");
                        }
                        if (opts.loadAndUpdate && !cmd.hasOption("d")) {
                            opts.printError("The delay is mandatory!");
                        }
                        if (cmd.hasOption("d")) {
                            opts.delay = Integer.parseInt(cmd.getOptionValue("d")) * 60 * 1000;
                            if (opts.delay < 0) {
                                opts.printError("The delay cannot be negative!");
                            }
                        }
                        break;
                    case "monitor":
                        opts.monitorEngine = true;
                        if (cmd.hasOption("o")) {
                            opts.ps = new PrintStream(cmd.getOptionValue("o"), "UTF-8");
                        } else {
                            opts.ps = System.out;
                        }
                        break;
                    case "update":
                        opts.updateEngine = true;
                        break;
                    case "status":
                        opts.statusEngine = true;
                        break;
                    case "connection":
                        opts.testConnectionEngine = true;
                        break;
                    case "territory":
                        opts.territoryEngine = true;
                        break;
                    case "scan":
                        opts.scanEngine = true;
                        break;
                    case "linkage":
                        opts.linkageEngine = true;
                        break;
                    default:
                        opts.printError("Wrong engine type!");
                        break;
                }
            } else {
                opts.printHelp();
            }
        } catch (ParseException | FileNotFoundException | UnsupportedEncodingException e) {
            opts.printHelp();
        }
        //End parsing command line

        //Load property file
        Properties prop = new Properties();
        try (InputStream in = new FileInputStream(opts.propertiesFile)) {
            prop.load(in);
        } catch (IOException ex) {
            opts.printError("Cannot read properties file '" + opts.propertiesFile + "'");
            //opts.printHelp();
        }

        opts.prop = prop;
        opts.dictionary = prop.getProperty("dictionary");
        opts.schema = prop.getProperty("db.dest.schema");
        if (opts.schema == null || opts.schema.isEmpty()) {
            opts.printError("The database schema is mandatory!\nPlease set 'db.dest.schema' into the properties file");
        }
        opts.tablePrefix = prop.getProperty("dictionary.prefix", "");

        return opts;
    }

    public static class CsPro2SqlOptions {

        boolean schemaEngine;
        boolean loaderEngine;
        boolean monitorEngine;
        boolean updateEngine;
        boolean statusEngine;
        boolean linkageEngine;
        boolean loadAndUpdate;
        boolean testConnectionEngine;
        boolean territoryEngine;
        boolean scanEngine;
        boolean allRecords;
        boolean foreignKeys;
        boolean checkConstraints;
        boolean checkOnly;
        boolean force;
        boolean recovery;
        String dictionary;
        String schema;
        String tablePrefix;
        String propertiesFile;
        Integer delay;
        PrintStream ps = null;
        Properties prop;
        private final Options options;

        CsPro2SqlOptions(Options options) {
            this.options = options;
        }

        void printHelp() {
            HelpFormatter formatter = new HelpFormatter();

            //System.out.println("CsPro2Sql - version " + VERSION + "\n");
            formatter.printHelp("\n\n"
                    + "CsPro2Sql -e schema      -p PROPERTIES_FILE [-fk] [-o OUTPUT_FILE]\n"
                    + "CsPro2Sql -e loader      -p PROPERTIES_FILE [-a] [-cc] [-co] [-f|-r] [-o OUTPUT_FILE] [-d DELAY]\n"
                    + "CsPro2Sql -e monitor     -p PROPERTIES_FILE [-o OUTPUT_FILE]\n"
                    + "CsPro2Sql -e linkage     -p PROPERTIES_FILE [-o OUTPUT_FILE]\n"
                    + "CsPro2Sql -e update      -p PROPERTIES_FILE\n"
                    + "CsPro2Sql -e status      -p PROPERTIES_FILE\n"
                    + "CsPro2Sql -e connection  -p PROPERTIES_FILE\n"
                    + "CsPro2Sql -e territory   -p PROPERTIES_FILE\n"
                    + "CsPro2Sql -e LU          -p PROPERTIES_FILE -d DELAY [-a] [-cc] [-co] [-f|-r] [-o OUTPUT_FILE]\n"
                    + "CsPro2Sql -v\n"
                    + "\n"
                    + "Engines descriptions:\n"
                    + " - scan: check input data and metadata\n"
                    + " - schema: create the sql script for microdata\n"
                    + " - loader: load microdata into the sql database\n"
                    + " - monitor: create the sql script to setup the monitoring system\n"
                    + " - linkage: create the sql script to setup the PES system\n"
                    + " - update: update the reports of the monitoring system\n"
                    + " - status: print the loader status\n"
                    + " - connection: test source/destination database connection\n"
                    + " - territory: generate the territory table\n"
                    + " - LU: load and update\n"
                    + "\n", options);

            System.exit(0);
        }

        void printVersion() {
            System.out.println("CsPro2Sql - version " + VERSION);
            System.exit(0);
        }

        void printError(String errMessage) {
            
            if (errMessage != null) {
                System.err.println("\n[ERROR] " + errMessage);
                System.err.println("CsPro2Sql -h for usage \n");
            }

            System.exit(0);

        }
    }

}
