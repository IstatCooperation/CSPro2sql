package cspro2sql.reader;

import cspro2sql.bean.Answer;
import cspro2sql.bean.Dictionary;
import cspro2sql.bean.Item;
import cspro2sql.bean.Questionnaire;
import cspro2sql.bean.Record;
import java.util.LinkedList;
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
 * @version 0.9.4
 */
public class QuestionnaireReader {

    //Parse a questionnaire (CSPro plain text file) according to its dictionary 
    public static Questionnaire parse(Dictionary dictionary, String questionnaire, String schema, byte[] guid, boolean deleted) {
        Questionnaire result = new Questionnaire(questionnaire, schema, guid, deleted);
        String[] rows = questionnaire.split(Dictionary.DICT_NEWLINE_REGEXP);
        Record record = dictionary.getMainRecord();
        //System.out.println("Record: " + record.getName());
        List<List<Answer>> valuesList = new LinkedList<>();
        result.setRecordValues(record, valuesList);
        List<Answer> values = new LinkedList<>();
        valuesList.add(values);
        for (Item item : record.getItems()) {
            parseItem(item, rows[0], values);
        }
        for (String row : rows) {
            record = dictionary.getRecord(getRecordTypeValue(row, dictionary.getRecordTypeLen()));
            valuesList = result.getRecordValues(record);
            if (valuesList == null) {
                valuesList = new LinkedList<>();
                result.setRecordValues(record, valuesList);
            }
            values = new LinkedList<>();
            valuesList.add(values);
            for (Item item : record.getItems()) {
                parseItem(item, row, values);
            }
        }
        return result;
    }

    private static void parseItem(Item item, String row, List<Answer> values) {
        //System.out.println("Item " + item.getName());
        if (row.length() < item.getStart()) {
            values.add(new Answer(item, null));
        } else {
            String v;
            if (row.length() < item.getStart() - 1 + item.getLength()) {
                v = row.substring(item.getStart() - 1);
            } else {
                v = row.substring(item.getStart() - 1, item.getStart() - 1 + item.getLength());
            }
            if (v.trim().isEmpty()) {
                values.add(new Answer(item, null));
            } else {
                values.add(new Answer(item, item.parseValue(v)));
            }
        }
        for (Item subItem : item.getSubItems()) {
            parseItem(subItem, row, values);
        }
    }

    private static String getRecordTypeValue(String row, Integer recordTypeLength) {
        String recordTypeValue = "";
        for (int i = 0; i < recordTypeLength; i++) {
            recordTypeValue += row.charAt(i);
        }
        //System.out.println("RecordTypeValue: " + recordTypeValue);        
        return recordTypeValue;
    }

}
