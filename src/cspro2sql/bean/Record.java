package cspro2sql.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
public final class Record extends Taggable {

    private final Dictionary dictionary;
    private final String tablePrefix;
    private final String valueSetPrefix;
    private String name;
    private String recordTypeValue;
    private boolean required;
    private int max;
    private int length;

    private Record mainRecord;
    private boolean isMainRecord = false;
    private final List<Item> items = new LinkedList<>();

    public Record(Dictionary dictionary, String tablePrefix) {
        this.dictionary = dictionary;
        if (tablePrefix == null || tablePrefix.isEmpty()) {
            this.tablePrefix = "";
            this.valueSetPrefix = "VS_";
        } else {
            this.tablePrefix = tablePrefix.toUpperCase(Locale.getDefault()) + "_";
            this.valueSetPrefix = "VS" + this.tablePrefix;
        }
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public String getValueSetPrefix() {
        return valueSetPrefix;
    }

    public String getName() {
        return name;
    }

    public String getTableName() {
        return (tablePrefix + name).toUpperCase();
    }

    public String getFullTableName() {
        return dictionary.getSchema() + "." + (tablePrefix + name).toUpperCase();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecordTypeValue() {
        return recordTypeValue;
    }

    public void setRecordTypeValue(String recordTypeValue) {
        this.recordTypeValue = recordTypeValue;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void addItem(Item it) {
        items.add(it);
        it.setRecord(this);
    }

    public void addItems(List<Item> its) {
        items.addAll(its);
        for (Item it : items) {
            it.setRecord(this);
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public void replaceItemWithSplit(Item item, List<Item> split) {
        if (item.isSubItem()) {
            List<Item> subItems = item.getParent().getSubItems();
            int i = subItems.indexOf(item);
            for (Item it : split) {
                subItems.add(i++, it);
                it.setRecord(this);
            }
            subItems.remove(i);
        } else {
            int i = items.indexOf(item);
            for (Item it : split) {
                items.add(i++, it);
                it.setRecord(this);
            }
            items.remove(i);
        }
    }

    public Record getMainRecord() {
        if (isMainRecord()) {
            return this;
        }
        return mainRecord;
    }

    public void setMainRecord(Record mainRecord) {
        this.mainRecord = mainRecord;
    }

    public boolean isMainRecord() {
        return isMainRecord;
    }

    public void setIsMainRecord(boolean isMainRecord) {
        this.isMainRecord = isMainRecord;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Record other = (Record) obj;
        return Objects.equals(this.name, other.name);
    }

}
