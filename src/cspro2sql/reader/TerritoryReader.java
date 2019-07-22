package cspro2sql.reader;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.server.ExportException;
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
public class TerritoryReader {

    public static List<Territory> parseTerritory(String territoryFile, Dictionary dictionary) throws Exception {
        List<Territory> territoryList = new ArrayList<>();
        Territory territoryStructure = parseTerritoryStructure(dictionary);

        if (territoryFile != null && !territoryFile.isEmpty()) {
            try {
                territoryList = read(territoryFile, territoryStructure);
            } catch (IOException ex) {
                throw new Exception("Impossible to read territory file " + territoryFile + " (" + ex.getMessage() + ")", ex);
            }
        }

        return territoryList;
    }

    public static String[] getHeader(String territoryFile) {
        String[] header = null;
        if (territoryFile != null && !territoryFile.isEmpty()) {
            try {
                header = readHeader(territoryFile);
            } catch (IOException ex) {
                System.out.println("Impossible to read territory file " + territoryFile + " (" + ex.getMessage() + ")");
            }
        }
        return header;
    }

    public static Territory parseTerritoryStructure(Dictionary dictionary) {
        Territory territory = new Territory();
        if (dictionary.hasTagged(Dictionary.TAG_TERRITORY)) {
            Iterable<Item> items = dictionary.getTaggedItems(Dictionary.TAG_TERRITORY);
            for (Item terrItem : items) {
                territory.addItem(terrItem);
            }
        }
        return territory;
    }

    private static List<Territory> read(String fileName, Territory territoryStructure) throws IOException {
        List<Territory> territoryList = new ArrayList<>();
        boolean isLocalFile = new File(fileName).exists();
        try (InputStream in
                = (isLocalFile
                        ? new FileInputStream(fileName)
                        : DictionaryReader.class.getResourceAsStream("/" + fileName))) {
            try (InputStreamReader fr = new InputStreamReader(in, "UTF-8")) {
                try (BufferedReader br = new BufferedReader(fr)) {
                    read(territoryList, territoryStructure, br);
                }
            }
        }
        return territoryList;
    }

    private static String[] readHeader(String fileName) throws IOException {
        boolean isLocalFile = new File(fileName).exists();
        try (InputStream in
                = (isLocalFile
                        ? new FileInputStream(fileName)
                        : DictionaryReader.class.getResourceAsStream("/" + fileName))) {
            try (InputStreamReader fr = new InputStreamReader(in, "UTF-8")) {
                try (BufferedReader br = new BufferedReader(fr)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] columns = line.split(";");
                        return columns;
                    }
                }
            }
        }
        return null;
    }

    private static void read(List<Territory> territoryList, Territory territoryStructure, BufferedReader br) throws IOException {
        String line;
        Boolean isFirstLine = true;
        while ((line = br.readLine()) != null) {
            String[] columns = line.split(";");
            if (isFirstLine) {
                if (!checkFileStructure(columns, territoryStructure)) {
                    throw new ExportException("Territory file doesn't match territory structure");
                }
                isFirstLine = false;
            } else {
                territoryList.add(parseLine(columns));
            }
        }
    }

    private static boolean checkFileStructure(String[] fileCols, Territory territoryStructure) {
        List<TerritoryItem> items = territoryStructure.getItemsList();
        for (String col : fileCols) {
            if (!checkColumnName(col, items)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkColumnName(String colName, List<TerritoryItem> items) {
        for (TerritoryItem terrItem : items) {
            if (terrItem.getName().equals(colName) || terrItem.getName().equals(colName.replace("_NAME", ""))) {
                return true;
            }
        }
        return false;
    }

    private static Territory parseLine(String[] fileCols) {
        Territory terr = new Territory();
        for (String col : fileCols) {
            terr.addItem(col);
        }
        return terr;
    }

}
