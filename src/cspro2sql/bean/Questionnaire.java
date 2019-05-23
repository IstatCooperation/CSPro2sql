package cspro2sql.bean;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
 * @version 0.9
 */
public class Questionnaire {

    private final String plainText;
    private final String schema;
    private final byte[] guid;
    private final boolean deleted;
    private final Map<Record, List<List<Answer>>> microdata = new LinkedHashMap<>();
    private final List<String> checkErrors = new LinkedList<>();

    public Questionnaire(String plainText, String schema, byte[] guid, boolean deleted) {
        this.plainText = plainText;
        this.schema = schema;
        this.guid = (guid == null ? null : Arrays.copyOf(guid, guid.length));
        this.deleted = deleted;
    }

    public void setRecordValues(Record record, List<List<Answer>> valuesList) {
        microdata.put(record, valuesList);
    }

    public List<List<Answer>> getRecordValues(Record record) {
        return microdata.get(record);
    }

    public Set<Map.Entry<Record, List<List<Answer>>>> getMicrodataSet() {
        return microdata.entrySet();
    }

    public String getPlainText() {
        return plainText;
    }

    public byte[] getGuid() {
        return (guid == null ? null : Arrays.copyOf(guid, guid.length));
    }

    public String getSchema() {
        return schema;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public boolean checkValueSets() {
        boolean error = false;
        for (Map.Entry<Record, List<List<Answer>>> e : microdata.entrySet()) {
            for (List<Answer> answers : e.getValue()) {
                for (Answer a : answers) {
                    if (!a.validate()) {
                        checkErrors.add(a.getError());
                        error = true;
                    }
                }
            }
        }
        return !error;
    }

    public String getCheckErrors() {
        StringBuilder result = new StringBuilder();
        for (String s : checkErrors) {
            result.append(s).append('\n');
        }
        return result.toString();
    }

}
