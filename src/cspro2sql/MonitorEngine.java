package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.reader.DictionaryReader;
import cspro2sql.sql.TemplateManager;
import cspro2sql.writer.MonitorWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
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
 * @version 0.9.12
 */
public class MonitorEngine {

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (InputStream in = MonitorEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
        } catch (IOException ex) {
            return;
        }
        try {
            List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                    prop.getProperty("db.dest.schema"),
                    prop.getProperty("dictionary"),
                    prop.getProperty("dictionary.prefix"));
            execute(dictionaries, System.out);
        } catch (Exception ex) {
            System.exit(1);
        }
    }

    static boolean execute(List<Dictionary> dictionaries, PrintStream out) {
        TemplateManager tmHousehold = null, tmListing = null, tmExpected = null;
        System.out.print("Generating dashboard report tables " + GenerateEngine.FILE_SQL_REPORT + "...");
        for (Dictionary dictionary : dictionaries) {
            if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
                tmHousehold = new TemplateManager(dictionary);
            } else if (dictionary.hasTag(Dictionary.TAG_LISTING)) {
                tmListing = new TemplateManager(dictionary);
            } else if (dictionary.hasTag(Dictionary.TAG_EXPECTED)) {
                tmExpected = new TemplateManager(dictionary);
            }
            System.out.print("[OK]");
        }
        return MonitorWriter.write(tmHousehold, tmListing, tmExpected, out);
    }

}
