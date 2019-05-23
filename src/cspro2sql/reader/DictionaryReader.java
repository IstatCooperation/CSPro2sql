package cspro2sql.reader;

import cspro2sql.bean.BeanFactory;
import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Record;
import cspro2sql.bean.Tag;
import cspro2sql.bean.ValueSet;
import cspro2sql.bean.ValueSetValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
public class DictionaryReader {

    public static List<Dictionary> parseDictionaries(String schema, String dictionaryFiles, String prefixes) throws Exception {
        String[] dicts = dictionaryFiles.split(",");
        String[] prefs = prefixes.split(",");
        List<Dictionary> dictionaries = new ArrayList<>(dicts.length);
        for (int i=0;i<dicts.length;i++) {
            dictionaries.add(parseDictionary(schema, dicts[i], prefs[i]));
        }
        return dictionaries;
    }

    private static Dictionary parseDictionary(String schema, String dictFile, String tablePrefix) throws Exception {
        Dictionary dictionary = null;
        if (dictFile != null && !dictFile.isEmpty()) {
            try {
                dictionary = read(schema, dictFile, tablePrefix);
            } catch (IOException ex) {
                throw new Exception("Impossible to read dictionary " + dictFile + " (" + ex.getMessage() + ")", ex);
            }
        } else {
            /*
            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                String srcSchema = opts.prop.getProperty("db.source." + prefix + "schema");
                String srcDataTable = opts.prop.getProperty("db.source." + prefix + "data.table");
                try (Connection connSrc = DriverManager.getConnection(
                        opts.prop.getProperty("db.source." + prefix + "uri") + "/" + srcSchema + "?autoReconnect=true&useSSL=false",
                        opts.prop.getProperty("db.source." + prefix + "username"),
                        opts.prop.getProperty("db.source." + prefix + "password"))) {
                    connSrc.setReadOnly(true);
                    try (Statement stmt = connSrc.createStatement()) {
                        try (ResultSet r = stmt.executeQuery("select dictionary_full_content from " + srcSchema + ".cspro_dictionaries where dictionary_name = '" + srcDataTable + "'")) {
                            r.next();
                            dictionary = DictionaryReader.readFromString(r.getString(1), opts.tablePrefix);
                        }
                    }
                }
            } catch (ClassNotFoundException | SQLException | IOException | InstantiationException | IllegalAccessException ex) {
                opts.ps.close();
                System.err.println("Impossible to read dictionary from database (" + ex.getMessage() + ")");
            }
             */
        }
        return dictionary;
    }

    private static Dictionary read(String schema, String fileName, String tablePrefix) throws IOException {
        Dictionary dictionary = new Dictionary(schema, tablePrefix);
        boolean isLocalFile = new File(fileName).exists();
        try (InputStream in
                = (isLocalFile
                        ? new FileInputStream(fileName)
                        : DictionaryReader.class.getResourceAsStream("/" + fileName))) {
            try (InputStreamReader fr = new InputStreamReader(in, "UTF-8")) {
                try (BufferedReader br = new BufferedReader(fr)) {
                    read(dictionary, tablePrefix, br);
                }
            }
        }
        createTagsCatalog(dictionary);
        return dictionary;
    }

    private static Dictionary readFromString(String schema, String dictionaryString, String tablePrefix) throws IOException {
        Dictionary dictionary = new Dictionary(schema, tablePrefix);
        try (Reader reader = new StringReader(dictionaryString)) {
            try (BufferedReader br = new BufferedReader(reader)) {
                read(dictionary, tablePrefix, br);
            }
        }
        createTagsCatalog(dictionary);
        return dictionary;
    }

    private static void read(Dictionary dictionary, String tablePrefix, BufferedReader br) throws IOException {
        String line;
        boolean skipValueSet = false;
        BeanFactory.parseDictionary(br, dictionary);
        while ((line = br.readLine()) != null) {
            switch (line) {
                case Dictionary.DICT_LEVEL:
                case Dictionary.DICT_RECORD:
                    dictionary.addRecord(BeanFactory.createRecord(br, tablePrefix, dictionary));
                    skipValueSet = false;
                    break;
                case Dictionary.DICT_ITEM:
                    Item item = BeanFactory.createItem(br);
                    if (dictionary.addItem(item)) {
                        skipValueSet = false;
                    } else {
                        skipValueSet = true;
                    }
                    break;
                case Dictionary.DICT_VALUESET:
                    ValueSet vs = BeanFactory.createValueSet(br);
                    if (!skipValueSet) {
                        dictionary.addValueSet(vs);
                    }
                    break;
                default:
            }
        }
    }

    private static void createTagsCatalog(Dictionary dictionary) {
        for (Record record : dictionary.getRecords()) {
            createTagsCatalog(dictionary, record);
        }
    }

    private static void createTagsCatalog(Dictionary dictionary, Record record) {
        for (Tag tag : record.getTags()) {
            dictionary.addTagged(tag, record);
        }
        for (Item item : record.getItems()) {
            createTagsCatalog(dictionary, item);
        }
    }

    private static void createTagsCatalog(Dictionary dictionary, Item item) {
        for (Tag tag : item.getTags()) {
            dictionary.addTagged(tag, item);
        }
        for (ValueSet vs : item.getValueSets()) {
            createTagsCatalog(dictionary, vs);
        }
        for (Item subItem : item.getSubItems()) {
            createTagsCatalog(dictionary, subItem);
        }
    }

    private static void createTagsCatalog(Dictionary dictionary, ValueSet vs) {
        for (Tag tag : vs.getTags()) {
            dictionary.addTagged(tag, vs);
        }
        for (ValueSetValue value : vs.getValues().values()) {
            for (Tag tag : value.getTags()) {
                dictionary.addTagged(tag, value);
            }
        }
    }

}
