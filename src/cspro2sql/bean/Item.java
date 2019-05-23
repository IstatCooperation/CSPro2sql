package cspro2sql.bean;

import java.util.ArrayList;
import java.util.List;
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
 * @version 0.9.15
 */
public final class Item extends Taggable {

    private Record record;
    private String name;
    private String valueSetName;
    private String dataType = Dictionary.ITEM_DECIMAL;
    private int start;
    private int length;
    private int occurrences;
    private int decimal;
    private int occurenceNumber;
    private boolean subItem;
    private boolean zeroFill;
    private boolean decimalChar;
    private Item parent;
    private final List<Item> subItems = new ArrayList<>();
    private final List<ValueSet> valueSets = new ArrayList<>();

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public Item getParent() {
        return parent;
    }

    private void setParent(Item parent) {
        this.parent = parent;
    }

    public void setValueSetName(String valueSetName) {
        this.valueSetName = valueSetName;
    }

    public String getValueSetName() {
        if (valueSetName != null) {
            return record.getValueSetPrefix() + valueSetName;
        }
        return record.getValueSetPrefix() + name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public int getOccurenceNumber() {
        return occurenceNumber;
    }

    public void setOccurenceNumber(int occurenceNumber) {
        this.occurenceNumber = occurenceNumber;
    }

    public boolean isSubItem() {
        return subItem;
    }

    public void setSubItem(boolean subItem) {
        this.subItem = subItem;
    }

    public boolean isZeroFill() {
        return zeroFill;
    }

    public void setZeroFill(boolean zeroFill) {
        this.zeroFill = zeroFill;
    }

    public boolean hasDecimalChar() {
        return decimalChar;
    }

    public void setDecimalChar(boolean decimalChar) {
        this.decimalChar = decimalChar;
    }

    public List<Item> getSubItems() {
        return subItems;
    }

    public void addSubItem(Item subItem) {
        if (this.getOccurrences() > 1) {
            subItem = subItem.clone();
            subItem.setName(subItem.getName() + "_" + this.getOccurenceNumber());
            subItem.setValueSetName(subItem.getName());
            subItem.setStart(subItem.getStart() + this.getLength() * this.getOccurenceNumber());
        }
        this.subItems.add(subItem);
        subItem.setRecord(record);
        subItem.setParent(this);
    }

    public List<ValueSet> getValueSets() {
        return valueSets;
    }

    public void addValueSet(ValueSet valueSet) {
        this.valueSets.add(valueSet);
    }

    public boolean hasValueSets() {
        return !this.valueSets.isEmpty();
    }

    public int getValueSetsValueLength() {
        int len = 0;
        for (ValueSet vs : this.valueSets) {
            len = Math.max(len, vs.getValueLength());
        }
        return len;
    }

    public List<Item> splitIntoColumns(ValueSet values) {
        List<Item> items = new ArrayList<>(getLength());
        int columns = getLength() / values.getKeyLength();
        for (int i = 0; i < columns; i++) {
            Item c = clone();
            c.addValueSet(values);
            c.setName(c.getName() + "_" + i);
            c.setStart(c.getStart() + i * values.getKeyLength());
            c.valueSetName = this.name;
            c.setLength(values.getKeyLength());
            items.add(c);
        }
        return items;
    }

    public String parseValue(String v) {
        if (Dictionary.ITEM_DECIMAL.equals(getDataType()) && getDecimal() > 0 && !hasDecimalChar()) {
            String head = v.substring(0, v.length() - getDecimal()).trim();
            String tail = v.substring(v.length() - getDecimal()).trim();
            if ((head + tail).matches("^[*]*$")) {
                return null;
            }
            if (head.isEmpty()) {
                head = "0";
            }
            if (tail.isEmpty()) {
                tail = "0";
            }
            return head + "." + tail;
        }
        if (Dictionary.ITEM_DECIMAL.equals(getDataType()) && v.matches("^[*.]+$")) {
            return null;
        }
        return v;
    }

    public String getColunmFullName() {
        return getRecord().getTableName() + "." + getName();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 43 * hash + Objects.hashCode(this.name);
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
        final Item other = (Item) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public Item clone() {
        Item clone = new Item();
        clone.record = this.record;
        clone.parent = this.parent;
        clone.dataType = this.dataType;
        clone.decimal = this.decimal;
        clone.decimalChar = this.decimalChar;
        clone.length = this.length;
        clone.name = this.name;
        clone.occurrences = this.occurrences;
        clone.start = this.start;
        clone.subItem = this.subItem;
        clone.zeroFill = this.zeroFill;
        return clone;
    }

}
