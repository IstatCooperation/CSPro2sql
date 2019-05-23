package cspro2sql;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.DictionaryInfo;
import cspro2sql.reader.DictionaryReader;
import cspro2sql.sql.DictionaryQuery;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
public class StatusEngine {

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        try (InputStream in = StatusEngine.class.getResourceAsStream("/database.properties")) {
            prop.load(in);
        } catch (IOException ex) {
            return;
        }
        try {
            List<Dictionary> dictionaries = DictionaryReader.parseDictionaries(
                    prop.getProperty("db.dest.schema"),
                    prop.getProperty("dictionary"),
                    prop.getProperty("dictionary.prefix"));
            execute(dictionaries, prop);
        } catch (Exception ex) {
            System.exit(1);
        }
    }

    static boolean execute(List<Dictionary> dictionaries, Properties prop) {
        boolean notRunning = true;
        for (Dictionary dictionary : dictionaries) {
            String srcDataTable = dictionary.getName();

            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();

                //Connect to the destination database
                try (Connection connDst = DriverManager.getConnection(
                        prop.getProperty("db.dest.uri") + "/" + dictionary.getSchema() + "?autoReconnect=true&useSSL=false",
                        prop.getProperty("db.dest.username"),
                        prop.getProperty("db.dest.password"))) {
                    connDst.setReadOnly(true);

                    DictionaryQuery dictionaryQuery = new DictionaryQuery(connDst);
                    DictionaryInfo dictionaryInfo = dictionaryQuery.getDictionaryInfo(srcDataTable);
                    dictionaryInfo.print(System.out);
                    notRunning = !dictionaryInfo.isRunning();
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
                System.out.println("Impossible to get LOADER status!");
                return false;
            }
        }
        return notRunning;
    }

}
