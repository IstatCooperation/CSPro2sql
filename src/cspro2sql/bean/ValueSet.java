package cspro2sql.bean;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * @version 0.9.10
 */
public final class ValueSet extends Taggable {

    private String label;
    private String name;
    private String link;
    private int keyLength;
    private int valueLength;
    private boolean notCreated = true;
    private Map<String, ValueSetValue> values = new LinkedHashMap<>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getValueLength() {
        return valueLength;
    }

    public void setValueLength(int valueLength) {
        this.valueLength = valueLength;
    }

    public Map<String, ValueSetValue> getValues() {
        return values;
    }

    public boolean containsKey(String key) {
        return this.values.containsKey(key);
    }

    public ValueSetValue getValue(String key) {
        return this.values.get(key);
    }

    public void addValue(String key, ValueSetValue value) {
        this.values.put(key, value);
        keyLength = Math.max(keyLength, key.trim().length());
    }

    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    public void setCreated() {
        this.notCreated = false;
    }

    public boolean isNotCreated() {
        return this.notCreated;
    }

    public int size() {
        return this.values.size();
    }

    public int getKeyLength() {
        return keyLength;
    }

    public void removeEmptyValues() {
        Iterator<Map.Entry<String, ValueSetValue>> it = this.values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ValueSetValue> entry = it.next();
            if (entry.getKey().trim().isEmpty()) {
                it.remove();
            }
        }
    }

    @Override
    public ValueSet clone() {
        ValueSet vs = new ValueSet();
        vs.label = this.label;
        vs.name = this.name;
        vs.link = this.link;
        vs.keyLength = this.keyLength;
        vs.valueLength = this.valueLength;
        vs.notCreated = this.notCreated;
        vs.values = this.values;
        return vs;
    }

}
