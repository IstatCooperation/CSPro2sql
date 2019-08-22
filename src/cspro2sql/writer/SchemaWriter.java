package cspro2sql.writer;

import cspro2sql.bean.Concepts;
import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Record;
import cspro2sql.bean.ValueSet;
import cspro2sql.bean.ValueSetValue;
import cspro2sql.sql.TemplateManager;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * @version 0.9.17
 */
public class SchemaWriter {

    private static final Map<String, Integer> unitMap = new HashMap<>();
    private static int unitId = 0;
    private static int variableId = 0;

    public static void write(Dictionary dictionary, boolean foreignKeys, PrintStream ps) {
        try {

            TemplateManager tm = new TemplateManager(dictionary);
            String schema = dictionary.getSchema();

            if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
                ps.println("CREATE SCHEMA IF NOT EXISTS " + schema + " CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;");
                ps.println();
                ps.println("USE " + schema + ";");
                ps.println();
                tm.printTemplate("cspro2sql_dictionary", ps);
                tm.printTemplate("cspro2sql_error", ps);
                tm.printTemplate("dashboard_user", ps);
                tm.printTemplate("dashboard_status", ps);
                tm.printTemplate("dashboard_report_type", ps);
                tm.printTemplate("dashboard_report", ps);
                tm.printTemplate("dashboard_meta_concept", ps);
                tm.printTemplate("dashboard_meta_unit", ps);
                tm.printTemplate("dashboard_meta_variable", ps);
            }

            ps.println("INSERT INTO " + schema + ".CSPRO2SQL_DICTIONARY (NAME) values ('" + dictionary.getName() + "');");
            ps.println();

            printUnits(dictionary, ps);
            printVariables(dictionary, ps);

            for (Record record : dictionary.getRecords()) {
                for (Item item : record.getItems()) {
                    printValueSet(schema, item, ps);
                }
            }

            for (Record record : dictionary.getRecords()) {
                ps.println("CREATE TABLE " + record.getFullTableName() + " (");
                //ps.println("    ID INT(9) UNSIGNED AUTO_INCREMENT,");
                //NEW ENGINE removed AUTO_INCREMENT
                ps.println("    ID INT(9) UNSIGNED,");
                if (!record.isMainRecord()) {
                    ps.println("    " + record.getMainRecord().getName() + " INT(9) UNSIGNED NOT NULL,");
                    ps.println("    COUNTER INT(9) UNSIGNED NOT NULL,");
                }
                for (Item item : record.getItems()) {
                    printItem(schema, foreignKeys, item, ps);
                }
                if (!record.isMainRecord()) {
                    ps.println("    INDEX (" + record.getMainRecord().getName() + "),");
                    ps.println("    FOREIGN KEY (" + record.getMainRecord().getName() + ") REFERENCES " + record.getMainRecord().getFullTableName() + "(id),");
                } else{
                    ps.println("    INDEX (" + printItems(record.getItems()) + "),");
                    
                }
                ps.println("    PRIMARY KEY (ID)");
                ps.println(") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
                ps.println();
            }
        } catch (IOException ex) {
            Logger.getLogger(SchemaWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void writesqlserver(Dictionary dictionary, boolean foreignKeys, PrintStream ps) {
        TemplateManager tm = new TemplateManager(dictionary);
        String schema = dictionary.getSchema();

        if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
            ps.println("CREATE DATABASE " + schema + ";");
            ps.println();
            ps.println();

            try {
                tm.printTemplate("sqlserver/cspro2sql_dictionary", ps);
                tm.printTemplate("sqlserver/cspro2sql_error", ps);
            } catch (IOException ex) {
                return;
            }
        }

        ps.println("INSERT INTO dbo.CSPRO2SQL_DICTIONARY (NAME) values ('" + dictionary.getName() + "');");

        for (Record record : dictionary.getRecords()) {
            for (Item item : record.getItems()) {
                printValueSetSqlserver(schema, item, ps);
            }
        }

        for (Record record : dictionary.getRecords()) {
            ps.println("CREATE TABLE dbo." + record.getTableName() + " (");
            ps.println("    ID INT IDENTITY(1,1),");
            if (!record.isMainRecord()) {
                ps.println("    " + record.getMainRecord().getName() + " INT NOT NULL,");
                ps.println("    COUNTER INT NOT NULL,");
            }
            for (Item item : record.getItems()) {
                printItemSqlserver(schema, foreignKeys, item, ps);
            }
            if (!record.isMainRecord()) {
                ps.println("    PRIMARY KEY (ID),");
                ps.println("    CONSTRAINT fk_" + record.getTableName() + "_" + record.getMainRecord().getName() + " FOREIGN KEY (" + record.getMainRecord().getName() + ") REFERENCES dbo." + record.getMainRecord().getTableName() + "(id)");
                ps.println("); CREATE INDEX idx_" + record.getTableName() + "_" + record.getMainRecord().getName() + " ON dbo." + record.getTableName() + "(" + record.getMainRecord().getName() + ");");
            } else {
                ps.println("    PRIMARY KEY (ID)");
                ps.println(");");
            }
            ps.println();
        }
    }

    private static void printItem(String schema, boolean foreignKeys, Item item, PrintStream ps) {
        String name = item.getName();
        int length = item.getLength();
        switch (item.getDataType()) {
            case Dictionary.ITEM_ALPHA:
                ps.println("    " + name + " CHAR(" + length + "),");
                break;
            case Dictionary.ITEM_DECIMAL:
                if (item.getDecimal() > 0) {
                    ps.println("    " + name + " DECIMAL(" + length + ", " + item.getDecimal() + "),");
                } else {
                    ps.println("    " + name + " DECIMAL(" + length + ", 0),");
                }
                break;
            default:
        }
        if (foreignKeys && item.hasValueSets()) {
            ps.println("    FOREIGN KEY (" + name + ") REFERENCES " + schema + "." + item.getValueSetName() + "(ID),");
        }
        for (Item subItem : item.getSubItems()) {
            printItem(schema, foreignKeys, subItem, ps);
        }
    }

    private static void printItemSqlserver(String schema, boolean foreignKeys, Item item, PrintStream ps) {
        String name = item.getName();
        int length = item.getLength();
        switch (item.getDataType()) {
            case Dictionary.ITEM_ALPHA:
                ps.println("    " + name + " CHAR(" + length + "),");
                break;
            case Dictionary.ITEM_DECIMAL:
                if (item.getDecimal() > 0) {
                    ps.println("    " + name + " DECIMAL(" + length + ", " + item.getDecimal() + "),");
                } else {
                    ps.println("    " + name + " DECIMAL(" + length + ", 0),");
                }
                break;
            default:
        }
        if (foreignKeys && item.hasValueSets()) {
            ps.println("    FOREIGN KEY (" + name + ") REFERENCES " + schema + "." + item.getValueSetName() + "(ID),");
        }
        for (Item subItem : item.getSubItems()) {
            printItemSqlserver(schema, foreignKeys, subItem, ps);
        }
    }

    private static void printValueSet(String schema, Item item, PrintStream ps) {
        if (item.hasValueSets() && item.getValueSets().get(0).isNotCreated()) {
            ps.println("CREATE TABLE " + schema + "." + item.getValueSetName() + " (");
            switch (item.getDataType()) {
                case Dictionary.ITEM_ALPHA:
                    ps.println("    ID CHAR(" + item.getLength() + "),");
                    break;
                case Dictionary.ITEM_DECIMAL:
                    ps.println("    ID INT(" + item.getLength() + "),");
                    break;
                default:
            }
            ps.println("    VALUE TEXT,");
            ps.println("    PRIMARY KEY (ID)");
            ps.println(") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            ps.println();
            boolean first = true;
            Set<String> keys = new HashSet<>();
            ps.println("INSERT INTO " + schema + "." + item.getValueSetName() + "(ID,VALUE) VALUES ");
            for (int i = 0; i < item.getValueSets().size(); i++) {
                ValueSet valueSet = item.getValueSets().get(i);
                valueSet.setCreated();
                for (ValueSetValue e : valueSet.getValues().values()) {
                    if (keys.contains(e.getKey())) {
                        continue;
                    }
                    if (!first) {
                        ps.println(",");
                    }
                    ps.print("    (\"" + e.getKey() + "\",\"" + e.getValue().replace("\"", "\\\"") + "\")");
                    keys.add(e.getKey());
                    first = false;
                }
            }
            ps.println(";");
            ps.println();
        }
        for (Item subItem : item.getSubItems()) {
            printValueSet(schema, subItem, ps);
        }
    }

    private static void printValueSetSqlserver(String schema, Item item, PrintStream ps) {
        if (item.hasValueSets() && item.getValueSets().get(0).isNotCreated()) {
            ps.println("CREATE TABLE dbo." + item.getValueSetName() + " (");
            switch (item.getDataType()) {
                case Dictionary.ITEM_ALPHA:
                    ps.println("    ID CHAR(" + item.getLength() + "),");
                    break;
                case Dictionary.ITEM_DECIMAL:
                    if (item.getLength() > 1) {
                        ps.println("    ID INT,");
                    } else {
                        ps.println("    ID SMALLINT,");
                    }

                    break;
                default:
            }
            ps.println("    VALUE NTEXT,");
            ps.println("    PRIMARY KEY (ID)");
            ps.println(");");
            ps.println();
            boolean first = true;
            Set<String> keys = new HashSet<>();
            ps.println("INSERT INTO dbo." + item.getValueSetName() + "(ID,VALUE) VALUES ");
            for (int i = 0; i < item.getValueSets().size(); i++) {
                ValueSet valueSet = item.getValueSets().get(i);
                valueSet.setCreated();
                for (ValueSetValue e : valueSet.getValues().values()) {
                    if (keys.contains(e.getKey())) {
                        continue;
                    }
                    if (!first) {
                        ps.println(",");
                    }
                    ps.print("    (\'" + e.getKey() + "\',N\'" + e.getValue().replace("\'", "\'\'") + "\')");
                    keys.add(e.getKey());
                    first = false;
                }
            }
            ps.println(";");
            ps.println();
        }
        for (Item subItem : item.getSubItems()) {
            printValueSetSqlserver(schema, subItem, ps);
        }
    }

    private static void printUnits(Dictionary dictionary, PrintStream ps) {

        Integer mainRecordId = null;
        Integer conceptId;

        for (Record record : dictionary.getRecords()) {

            if (record.isMainRecord()) {
                mainRecordId = unitId;
                conceptId = Concepts.getId(dictionary);
            } else {
                conceptId = Concepts.getId(record);
            }

            ps.print("INSERT INTO " + dictionary.getSchema() + ".DASHBOARD_META_UNIT (`ID`, `NAME`, `TABLE_NAME`, `PARENT_ID`, `CONCEPT_ID`) "
                    + "values (" + unitId + ", \'" + record.getName() + "\', \'" + record.getTableName() + "\', " + printmainRecordId(mainRecordId, record) + ", " + printId(conceptId) + ");");
            ps.println();

            unitMap.put(record.getName(), unitId);
            unitId++;
        }
        ps.println();

    }

    private static void printVariables(Dictionary dictionary, PrintStream ps) {

        int order = 0;
        Integer unitId = null;
        Integer conceptId;

        for (Record record : dictionary.getRecords()) {
            for (Item item : record.getItems()) {
                conceptId = Concepts.getId(item);
                if (item.hasTag(Dictionary.TAG_TERRITORY)) {
                    order++;
                }
                if (unitMap.containsKey(item.getRecord().getName())) {
                    unitId = unitMap.get(item.getRecord().getName());
                }

                ps.print("INSERT INTO " + dictionary.getSchema() + ".DASHBOARD_META_VARIABLE (`ID`, `NAME`, `TYPE`, `VAR_ORDER`, `UNIT_ID`, `CONCEPT_ID`) "
                        + "values (" + variableId + ", \'" + item.getName() + "\', \'" + item.getDataType() + "\', " + printOrder(order, item) + ", " + printId(unitId) + ", " + printId(conceptId) + ");");
                ps.println();
                variableId++;
            }
        }
        ps.println();

    }

    private static String printId(Integer recordId) {

        if (recordId == null) {
            return "null";
        } else {
            return "" + recordId;
        }

    }

    private static String printOrder(int order, Item item) {
        if (item.hasTag(Dictionary.TAG_TERRITORY)) {
            return "" + order;
        } else {
            return "0";
        }
    }

    private static String printmainRecordId(Integer mainRecordId, Record record) {
        if (record.isMainRecord()) {
            return "null";
        } else{
            return "" + mainRecordId;
        }
    }

    private static String printItems(List<Item> items){
        String out = "";
        boolean isFirst = true;
        for(Item item : items){
            if(isFirst){
                out += "`" + item.getName()+ "`";
                isFirst = false;
            }
            else{
                out += ", `" + item.getName() + "`";
            }
        }
        return out;
    }
}
