package cspro2sql.writer;

import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Record;
import cspro2sql.bean.Report;
import cspro2sql.bean.Territory;
import cspro2sql.bean.TerritoryItem;
import cspro2sql.sql.TemplateManager;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @version 0.9.18.1
 */
public class MonitorWriter {

    

    private static final String[] TEMPLATES = new String[]{
        "r_questionnaire_info",
        "r_individual_info",
        "r_religion",
        "r_sex_by_age"
    };

    private static int reportCount = 0;

    public static boolean write(TemplateManager tm, TemplateManager tmListing, TemplateManager tmExpected, PrintStream out) {
        String schema = tm.getDictionary().getSchema();

        tm.addParam("@LISTING", tmListing == null ? "0" : "1");
        tm.addParam("@EXPECTED", tmExpected == null ? "0" : "1");

        out.println("USE " + schema + ";");
        out.println();

        Territory territory = tm.getTerritory();
        int[] ageRange = tm.getAgeRange();

        try {
            tm.printTemplate("dashboard_info", out);
        } catch (IOException ex) {
            return false;
        }

        try {
            for (String template : TEMPLATES) {
                if (tm.printTemplate(template, out)) {
                    printMaterialized(schema, template, out);
                }
            }
        } catch (IOException ex) {
            return false;
        }

//        if (!territory.isEmpty()) {
//            out.println("CREATE TABLE IF NOT EXISTS `territory` (");
//            String idx = "";
//            for (int i = 0; i < territory.size(); i++) {
//                TerritoryItem territoryItem = territory.get(i);
//                String name = territoryItem.getItemName();
//                out.println("    `" + name + "_NAME` text COLLATE utf8mb4_unicode_ci,");
//                out.println("    `" + name + "` int(11) DEFAULT NULL,");
//                if (i > 0) {
//                    idx += ",";
//                }
//                idx += "`" + name + "`";
//            }
//            out.println("    `TERRITORY_CODE` text COLLATE utf8mb4_unicode_ci,");
//            out.println("    KEY `idx_territory` (" + idx + ")");
//            out.println(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
//            out.println();
//        }
        if (!territory.isEmpty()) {
            TerritoryItem territoryItem = territory.getFirst();
            out.println("CREATE OR REPLACE VIEW " + schema + ".`r_regional_area` AS");
            String groupBy = territoryItem.getItemName();
            String name = territoryItem.getName();
            out.print("  SELECT '" + name + "' name, COUNT(0) value FROM (SELECT COUNT(0) FROM " + schema + "." + tm.getParam("@QUESTIONNAIRE_TABLE") + " GROUP BY " + groupBy + ") a0");
            for (int i = 1; i < territory.size(); i++) {
                territoryItem = territory.get(i);
                name = territoryItem.getName();
                groupBy += "," + territoryItem.getItemName();
                out.println(" UNION");
                out.print("  SELECT '" + name + "', COUNT(0) FROM (SELECT COUNT(0) FROM " + schema + "." + tm.getParam("@QUESTIONNAIRE_TABLE") + " GROUP BY " + groupBy + ") a" + i);
            }
            out.println();
            out.println(";");
            printMaterialized(schema, "r_regional_area", out);
        }

        if (ageRange != null && tm.hasParam("@INDIVIDUAL_TABLE") && tm.hasParam("@INDIVIDUAL_COLUMN_SEX")
                && tm.hasParam("@INDIVIDUAL_VALUE_SEX_MALE") && tm.hasParam("@INDIVIDUAL_VALUE_SEX_FEMALE")
                && tm.hasParam("@INDIVIDUAL_COLUMN_AGE")) {
            out.println("CREATE OR REPLACE VIEW " + schema + ".`r_sex_by_age_group` AS");
            out.print("  SELECT '" + ageRange[0] + " to " + (ageRange[1] - 1) + "' as 'range', a.male, b.female FROM "
                    + "(SELECT COUNT(0) male FROM " + schema + "." + tm.getParam("@INDIVIDUAL_TABLE") + " WHERE " + tm.getParam("@INDIVIDUAL_COLUMN_SEX") + " = " + tm.getParam("@INDIVIDUAL_VALUE_SEX_MALE") + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " >= " + ageRange[0] + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " < " + ageRange[1] + ") a,"
                    + "(SELECT COUNT(0) female FROM " + schema + "." + tm.getParam("@INDIVIDUAL_TABLE") + " WHERE " + tm.getParam("@INDIVIDUAL_COLUMN_SEX") + " = " + tm.getParam("@INDIVIDUAL_VALUE_SEX_FEMALE") + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " >= " + ageRange[0] + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " < " + ageRange[1] + ") b");
            for (int i = 1; i < ageRange.length - 1; i++) {
                out.println(" UNION");
                out.print("  SELECT '" + ageRange[i] + " to " + (ageRange[i + 1] - 1) + "' as 'range', a.male, b.female FROM "
                        + "(SELECT COUNT(0) male FROM " + schema + "." + tm.getParam("@INDIVIDUAL_TABLE") + " WHERE " + tm.getParam("@INDIVIDUAL_COLUMN_SEX") + " = " + tm.getParam("@INDIVIDUAL_VALUE_SEX_MALE") + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " >= " + ageRange[i] + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " < " + ageRange[i + 1] + ") a,"
                        + "(SELECT COUNT(0) female FROM " + schema + "." + tm.getParam("@INDIVIDUAL_TABLE") + " WHERE " + tm.getParam("@INDIVIDUAL_COLUMN_SEX") + " = " + tm.getParam("@INDIVIDUAL_VALUE_SEX_FEMALE") + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " >= " + ageRange[i] + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " < " + ageRange[i + 1] + ") b");
            }
            out.println(" UNION");
            out.print("  SELECT '" + ageRange[ageRange.length - 1] + "+' as 'range', a.male, b.female FROM "
                    + "(SELECT COUNT(0) male FROM " + schema + "." + tm.getParam("@INDIVIDUAL_TABLE") + " WHERE " + tm.getParam("@INDIVIDUAL_COLUMN_SEX") + " = " + tm.getParam("@INDIVIDUAL_VALUE_SEX_MALE") + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " >= " + ageRange[ageRange.length - 1] + ") a,"
                    + "(SELECT COUNT(0) female FROM " + schema + "." + tm.getParam("@INDIVIDUAL_TABLE") + " WHERE " + tm.getParam("@INDIVIDUAL_COLUMN_SEX") + " = " + tm.getParam("@INDIVIDUAL_VALUE_SEX_FEMALE") + " AND " + tm.getParam("@INDIVIDUAL_COLUMN_AGE") + " >= " + ageRange[ageRange.length - 1] + ") b");
            out.println();
            out.println(";");
            printMaterialized(schema, "r_sex_by_age_group", out);
        }

        if (!territory.isEmpty()) {
            TerritoryItem territoryItem = territory.getFirst();
            out.println("CREATE OR REPLACE VIEW " + schema + ".`r_household_by_ea` AS");
            out.print("  SELECT concat(");
            out.print("'" + territoryItem.getName() + "'");
            for (int i = 1; i < territory.size(); i++) {
                territoryItem = territory.get(i);
                out.print(",'#','" + territoryItem.getName() + "'");
            }
            out.println(") as name, null as household");
            out.println("  UNION");
            out.print("  SELECT concat(");
            territoryItem = territory.getFirst();
            out.print(territoryItem.selectDescription());
            for (int i = 1; i < territory.size(); i++) {
                territoryItem = territory.get(i);
                out.print(",'#'," + territoryItem.selectDescription());
            }
            out.println(") as name, COUNT(0) AS `household`");
            out.println("  FROM " + schema + "." + tm.getParam("@QUESTIONNAIRE_TABLE") + " `h`");
            out.print("  GROUP BY ");
            territoryItem = territory.getFirst();
            out.print("`h`." + territoryItem.getItemName());
            for (int i = 1; i < territory.size(); i++) {
                territoryItem = territory.get(i);
                out.print(", `h`." + territoryItem.getItemName());
            }
            out.println(";");
            printMaterialized(schema, "r_household_by_ea", out);
        }

        if (!territory.isEmpty()) {
            printAuxTable(tm, tm, "aux_household_returned", "returned", out);
            printAuxTable(tm, (tmListing != null) ? tmListing : tm, "aux_listing_returned", "returned", out);
            printAuxTable(tm, (tmExpected != null) ? tmExpected : tm, "aux_household_expected", "expected", out);

            int upTo = 1;
            for (int i = 0; i < territory.size(); i++) {
                TerritoryItem territoryItem = territory.get(i);
                printExpectedReport(tm, "r_household_expected_by_" + territoryItem.getName().toLowerCase(), upTo++, out);
                printMaterialized(schema, "r_household_expected_by_" + territoryItem.getName().toLowerCase(), out);
            }

            printTotalReport(tm, (tmListing != null) ? tmListing : tm, out);
            printMaterialized(schema, "r_total", out);
        }

        return true;
    }

    private static void printMaterialized(String schema, String name, PrintStream out) {
        out.println("DROP TABLE IF EXISTS " + schema + ".m" + name + ";");
        out.println("SELECT 0 INTO @ID;");
        out.println("CREATE TABLE " + schema + ".m" + name + " (PRIMARY KEY (ID)) AS SELECT @ID := @ID + 1 ID, " + name + ".* FROM " + schema + "." + name + ";");
        
        out.println("INSERT INTO " + schema + ".`dashboard_report` (`NAME`, `REPORT_VIEW`, `LIST_ORDER`, `IS_VISIBLE`, `REPORT_TYPE`) "
                + "VALUES ('" + Report.getReportName(name) + "','" + name + "', " + (reportCount++) + ", 1, " + Report.getReportType(name) + ");");
    }

    private static void printAuxTable(TemplateManager mainTm, TemplateManager tm, String auxName, String columnName, PrintStream out) {
        Set<Record> records = new LinkedHashSet<>();
        Territory territory = tm.getTerritory();
        Territory mainTerritory = mainTm.getTerritory();
        out.println("CREATE OR REPLACE VIEW " + tm.getDictionary().getSchema() + "." + auxName + " AS");
        out.println("    SELECT ");
        for (int i = 0; i < territory.size(); i++) {
            TerritoryItem territoryItem = territory.get(i);
            TerritoryItem mainTerritoryItem = mainTerritory.get(i);
            Item item = territoryItem.getItem();
            out.println("        " + item.getColunmFullName() + " AS " + mainTerritoryItem.getItemName() + ",");
            records.add(item.getRecord());
        }
        Record[] recArray = records.toArray(new Record[0]);
        Item expected = tm.getDictionary().getTaggedItem(Dictionary.TAG_EXPECTED_QUESTIONNAIRES);
        if (expected == null) {
            out.println("        COUNT(0) AS `" + columnName + "`");
        } else {
            out.println("        SUM(" + expected.getColunmFullName() + ") AS `" + columnName + "`");
        }
        out.println("    FROM");
        out.println("        " + recArray[0].getMainRecord().getFullTableName());
        for (Record record : recArray) {
            if (!record.isMainRecord()) {
                out.println("            JOIN " + record.getFullTableName() + " ON " + record.getMainRecord().getTableName() + ".ID = " + record.getTableName() + "." + record.getMainRecord().getName());
            }
        }
        out.println("    GROUP BY");
        for (int i = 0; i < territory.size() - 1; i++) {
            Item item = territory.get(i).getItem();
            out.println("        " + item.getColunmFullName() + ",");
        }
        Item item = territory.get(territory.size() - 1).getItem();
        out.println("        " + item.getColunmFullName() + ";");
        out.println();
    }

    private static void printExpectedReport(TemplateManager tm, String reportName, int upTo, PrintStream out) {
        String schema = tm.getDictionary().getSchema();
        Territory territory = tm.getTerritory();

        out.println("CREATE OR REPLACE VIEW " + schema + ".`" + reportName + "` AS");
        out.println("    SELECT ");
        out.print("        _utf8mb4 '" + territory.getFirst().getName());
        for (int i = 1; i < upTo; i++) {
            out.print("#" + territory.get(i).getName());
        }
        out.println("' COLLATE utf8mb4_unicode_ci AS `name`,");
        for (int i = 0; i < upTo; i++) {
            out.println("        NULL AS `" + territory.get(i).getItemName() + "`,");
        }
        out.println("        NULL AS `field`,");
        out.println("        NULL AS `freshlist`,");
        out.println("        NULL AS `expected`,");
        out.println("        NULL AS `field_freshlist`,");
        out.println("        NULL AS `field_expected`,");
        out.println("        NULL AS `freshlist_expected`");
        out.println("    ");
        out.println("    UNION SELECT ");
        out.print("        CONCAT(");
        TerritoryItem territoryItem = territory.getFirst();
        out.print(territoryItem.selectDescription());
        for (int i = 1; i < upTo; i++) {
            territoryItem = territory.get(i);
            out.print(",'#'," + territoryItem.selectDescription());
        }
        out.println(") AS `name`,");
        for (int i = 0; i < upTo; i++) {
            out.println("        `h`." + territory.get(i).getItemName() + " AS `" + territory.get(i).getItemName() + "`,");
        }
        out.println("        SUM(`h`.`returned`) AS `returned`,");
        out.println("        SUM(`l`.`returned`) AS `returned`,");
        out.println("        SUM(`e`.`expected`) AS `expected`,");
        out.println("        ((SUM(`h`.`returned`) / SUM(`l`.`returned`)) * 100) AS `field_freshlist`,");
        out.println("        ((SUM(`h`.`returned`) / SUM(`e`.`expected`)) * 100) AS `field_expected`,");
        out.println("        ((SUM(`l`.`returned`) / SUM(`e`.`expected`)) * 100) AS `freshlist_expected`");
        out.println("    FROM");
        printSubTable(tm, "aux_household_returned", "returned", upTo, out);
        out.println("        `h`");
        out.println("        JOIN ");
        printSubTable(tm, "aux_listing_returned", "returned", upTo, out);
        out.println("            `l` ON");
        territoryItem = territory.getFirst();
        out.println("            (`h`.`" + territoryItem.getItemName() + "` = `l`.`" + territoryItem.getItemName() + "`)");
        for (int i = 1; i < upTo; i++) {
            territoryItem = territory.get(i);
            out.println("            AND (`h`.`" + territoryItem.getItemName() + "` = `l`.`" + territoryItem.getItemName() + "`)");
        }
        out.println("        JOIN");
        printSubTable(tm, "aux_household_expected", "expected", upTo, out);
        out.println("        `e` ON");
        territoryItem = territory.getFirst();
        out.println("            (`h`.`" + territoryItem.getItemName() + "` = `e`.`" + territoryItem.getItemName() + "`)");
        for (int i = 1; i < upTo; i++) {
            territoryItem = territory.get(i);
            out.println("            AND (`h`.`" + territoryItem.getItemName() + "` = `e`.`" + territoryItem.getItemName() + "`)");
        }
        out.print("    GROUP BY `name`");
        for (int i = 0; i < upTo; i++) {
            out.print(", `h`.`" + territory.get(i).getItemName() + "`");
        }
        out.println(";");
        out.println();
    }

    private static void printSubTable(TemplateManager tm, String tableName, String columnName, int upTo, PrintStream out) {
        String schema = tm.getDictionary().getSchema();
        Territory territory = tm.getTerritory();

        out.println("        (SELECT");
        for (int i = 0; i < upTo && i < territory.size(); i++) {
            TerritoryItem territoryItem = territory.get(i);
            Item item = territoryItem.getItem();
            out.println("                `" + tableName + "`." + item.getName() + " AS " + item.getName() + ",");
        }
        out.println("            SUM(`" + tableName + "`.`" + columnName + "`) AS `" + columnName + "`");
        out.println("            FROM `" + schema + "`.`" + tableName + "`");
        out.println("            GROUP BY");
        out.print("                `" + tableName + "`." + territory.getFirst().getItemName());
        for (int i = 0; i < upTo && i < territory.size(); i++) {
            TerritoryItem territoryItem = territory.get(i);
            out.print(",\n                `" + tableName + "`." + territoryItem.getItemName());
        }
        out.println();
        out.println("            )");
    }

    private static void printTotalReport(TemplateManager tm, TemplateManager tmListing, PrintStream out) {
        String schema = tm.getDictionary().getSchema();

        out.println("CREATE OR REPLACE VIEW `" + schema + "`.`r_total` AS");
        out.println("    SELECT ");
        out.println("        (SELECT ");
        out.println("                COUNT(0)");
        out.println("            FROM");
        printSubTable(tm, "aux_household_returned", "returned", 1000, out);
        out.println("            `a`) AS `ea_fieldwork`,");
        out.println("        (SELECT ");
        out.println("                COUNT(0)");
        out.println("            FROM");
        printSubTable(tm, "aux_listing_returned", "returned", 1000, out);
        out.println("            `a`) AS `ea_freshlist`,");
        out.println("        (SELECT ");
        out.println("                COUNT(0)");
        out.println("            FROM");
        printSubTable(tm, "aux_household_expected", "expected", 1000, out);
        out.println("            `a`) AS `ea_expected`,");
        out.println("        (SELECT ");
        out.println("                COUNT(0)");
        out.println("            FROM");
        out.println("                " + tm.getDictionary().getMainRecord().getFullTableName() + ") AS `household_fieldwork`,");
        out.println("        (SELECT ");
        out.println("                COUNT(0)");
        out.println("            FROM");
        out.println("                " + tmListing.getDictionary().getMainRecord().getFullTableName() + ") AS `household_freshlist`,");
        out.println("        (SELECT ");
        out.println("                SUM(`a`.`expected`)");
        out.println("            FROM");
        printSubTable(tm, "aux_household_expected", "expected", 1000, out);
        out.println("                `a`) AS `household_expected`;");
    }

    

   

}
