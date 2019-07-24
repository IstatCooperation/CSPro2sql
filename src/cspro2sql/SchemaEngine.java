package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.reader.DictionaryReader;
import cspro2sql.writer.SchemaWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
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
 * @version 0.9.12
 */
public class SchemaEngine {

    private static final Logger LOGGER = Logger.getLogger(SchemaEngine.class.getName());

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();

        //Load property file
        try (InputStream in = SchemaEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Cannot read properties file", ex);
            return;
        }

        //Parse dictionary file
        try {
            List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                    prop.getProperty("db.dest.schema"),
                    prop.getProperty("dictionary"),
                    prop.getProperty("dictionary.prefix"));
            execute(dictionaries, prop, false, System.out);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Impossible to create the database schema", ex);
            System.exit(1);
        }

    }

    static boolean execute(List<Dictionary> dictionaries, Properties prop, boolean foreignKeys, PrintStream out) {

        for (Dictionary dictionary : dictionaries) {
            if ("sqlserver".equals(prop.getProperty("db.dest.type"))) {
                System.out.print("Generating dashboard microdata database " + GenerateEngine.FILE_SQL_MICRO + "...");
                SchemaWriter.writesqlserver(dictionary, foreignKeys, out);
                System.out.print("[OK]");
            } else {
                SchemaWriter.write(dictionary, foreignKeys, out);
            }
        }
        return true;
    }

}
