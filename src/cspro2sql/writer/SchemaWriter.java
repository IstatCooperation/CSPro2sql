package cspro2sql.writer;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Record;
import cspro2sql.bean.ValueSet;
import cspro2sql.bean.ValueSetValue;
import cspro2sql.sql.TemplateManager;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

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

    public static void write(Dictionary dictionary, boolean foreignKeys, PrintStream ps) {
        TemplateManager tm = new TemplateManager(dictionary);
        String schema = dictionary.getSchema();

        ps.println("CREATE SCHEMA IF NOT EXISTS " + schema + " CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        ps.println();
        ps.println("USE " + schema + ";");
        ps.println();

        try {
            tm.printTemplate("cspro2sql_dictionary", ps);
            tm.printTemplate("cspro2sql_error", ps);
            tm.printTemplate("dashboard_user", ps);
            tm.printTemplate("dashboard_report_type", ps);
            tm.printTemplate("dashboard_report", ps);
            tm.printTemplate("dashboard_meta_concept", ps);
            tm.printTemplate("dashboard_meta_unit", ps);
            tm.printTemplate("dashboard_meta_variable", ps);

        } catch (IOException ex) {
            return;
        }

        for (Record record : dictionary.getRecords()) {
            for (Item item : record.getItems()) {
                printValueSet(schema, item, ps);
            }
        }

        for (Record record : dictionary.getRecords()) {
            ps.println("CREATE TABLE " + record.getFullTableName() + " (");
            ps.println("    ID INT(9) UNSIGNED AUTO_INCREMENT,");
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
            }
            ps.println("    PRIMARY KEY (ID)");
            ps.println(") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
            ps.println();
        }
    }

    public static void writesqlserver(Dictionary dictionary, boolean foreignKeys, PrintStream ps) {
        TemplateManager tm = new TemplateManager(dictionary);
        String schema = dictionary.getSchema();

        ps.println("CREATE DATABASE " + schema + ";");
        ps.println();
        ps.println();

        try {
            tm.printTemplate("sqlserver/cspro2sql_dictionary", ps);
            tm.printTemplate("sqlserver/cspro2sql_error", ps);
        } catch (IOException ex) {
            return;
        }

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
}
