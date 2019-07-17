package cspro2sql.bean;

import java.util.HashMap;
import java.util.Map;
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
 * @version 0.9.12
 */
public class Report {

    private static final Pattern HOUSEHOLD_BY_PATTERN = Pattern.compile("^r_household_expected_by_(.*)$");

    private static final String REPORT_HOUSEHOLD = "Household";
    private static final String REPORT_POPULATION = "Population";
    private static final String REPORT_SEX_DISTRIBUTION = "Sex Distribution";
    private static final String REPORT_RELIGION = "Religion";
    private static final String REPORT_AUXILIARY = "Aux";

    public static final int REPORT_TYPE_PROGRESS = 1;
    public static final int REPORT_TYPE_ANALYSIS = 2;
    public static final int REPORT_TYPE_GIS = 3;
    public static final int REPORT_TYPE_AUXILIARY = 4;

    private static final Map<String, String> tableMap = new HashMap();

    public static Map<String, String> getMap() {
        
        tableMap.put("r_questionnaire_info", REPORT_HOUSEHOLD);
        tableMap.put("r_individual_info", REPORT_POPULATION);
        tableMap.put("r_sex_by_age", REPORT_SEX_DISTRIBUTION);
        tableMap.put("r_religion", REPORT_RELIGION);
        
        return tableMap;
    }

    public static String getReportName(String tableName) {

        String reportName = "";

        switch (getReportType(tableName)) {
            case REPORT_TYPE_PROGRESS:
                reportName = "Households by " + tableName.toUpperCase();
                break;
            case REPORT_TYPE_ANALYSIS:
                reportName = getAnalysisReportName(tableName);
                break;
            case REPORT_TYPE_AUXILIARY:
                reportName = REPORT_AUXILIARY;
            default:
                break;
        }
        return reportName;
    }

    public static int getReportType(String tableName) {
        if (isProgressReport(tableName)) {
            return REPORT_TYPE_PROGRESS;
        } else if (isAnalysisReport(tableName)) {
            return REPORT_TYPE_ANALYSIS;
        } else {
            return REPORT_TYPE_AUXILIARY;
        }
    }

    private static boolean isProgressReport(String tableName) {
        Matcher m = HOUSEHOLD_BY_PATTERN.matcher(tableName);
        return m.find();
    }

    private static boolean isAnalysisReport(String tableName) {
        return getMap().entrySet().stream().anyMatch((entry) -> (entry.getKey().equals(tableName)));
    }

    private static String getAnalysisReportName(String tableName) {
        for (Map.Entry<String, String> entry : getMap().entrySet()) {
            if (entry.getKey().equals(tableName)) {
                return entry.getValue();
            }
        }
        return "";
    }
}
