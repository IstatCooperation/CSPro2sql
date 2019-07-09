package cspro2sql.bean;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
 * @author Paolo Giacomi <giacomi @ istat.it>
 * @version 0.9.15
 */
public final class Dictionary extends Taggable {

    public static final String DICT_HEADER = "[Dictionary]";
    public static final String DICT_LANGUAGES = "[Languages]";
    public static final String DICT_LEVEL = "[Level]";
    public static final String DICT_RECORD = "[Record]";
    public static final String DICT_IDITEMS = "[IdItems]";
    public static final String DICT_ITEM = "[Item]";
    public static final String DICT_VALUESET = "[ValueSet]";
    public static final String DICT_RELATION = "[Relation]";

    public static final String DICT_LABEL = "Label";
    public static final char DICT_LABEL_LANGUAGE_SEPARATOR = '|';
    public static final String DICT_NAME = "Name";
    public static final String DICT_NOTE = "Note";
    public static final String DICT_NOTENEWLINE = "\r\n";
    public static final String DICT_NEWLINE_REGEXP = "(?<!\r)\n";
    public static final String DICT_YES = "Yes";
    public static final String DICT_NO = "No";
    public static final String DICT_OCCLABEL = "OccurrenceLabel";

    public static final String HEADER_VERSION = "Version";
    public static final String HEADER_VERSION_CSPRO = "CSPro";
    public static final String HEADER_RECSTART = "RecordTypeStart";
    public static final String HEADER_RECLEN = "RecordTypeLen";
    public static final String HEADER_POSITIONS = "Positions";
    public static final String HEADER_ABSOLUTE = "Absolute";
    public static final String HEADER_RELATIVE = "Relative";
    public static final String HEADER_ZEROFILL = "ZeroFill";
    public static final String HEADER_DECCHAR = "DecimalChar";
    public static final String HEADER_VALUESETIMAGES = "ValueSetImages";

    public static final String RECORD_TYPE = "RecordTypeValue";
    public static final String RECORD_REQUIRED = "Required";
    public static final String RECORD_MAX = "MaxRecords";
    public static final String RECORD_LEN = "RecordLen";

    public static final String ITEM_START = "Start";
    public static final String ITEM_LEN = "Len";
    public static final String ITEM_DATATYPE = "DataType";
    public static final String ITEM_ITEMTYPE = "ItemType";
    public static final String ITEM_OCCS = "Occurrences";
    public static final String ITEM_DECIMAL = "Decimal";
    public static final String ITEM_DECCHAR = "DecimalChar";
    public static final String ITEM_ZEROFILL = "ZeroFill";
    public static final String ITEM_ALPHA = "Alpha";
    public static final String ITEM_SUBITEM = "SubItem";

    public static final String VALUE_VALUE = "Value";
    public static final String VALUE_LINK = "Link";
    public static final String VALUE_IMAGE = "Image";
    public static final String VALUE_SPECIAL = "Special";
    public static final String VALUE_MISSING = "MISSING";
    public static final String VALUE_NOTAPPL = "NOTAPPL";
    public static final String VALUE_DEFAULT = "DEFAULT";

    public static final String RELATION_PRIMARY = "Primary";
    public static final String RELATION_PRIMARYLINK = "PrimaryLink";
    public static final String RELATION_SECONDARY = "Secondary";
    public static final String RELATION_SECONDARYLINK = "SecondaryLink";

    public static final Tag TAG_MULTIPLE_RESPONSE = new Tag("#multipleResponse");
    public static final Tag TAG_IGNORE = new Tag("#ignore");
    public static final Tag TAG_INDIVIDUAL = new Tag("#individual");
    public static final Tag TAG_SEX = new Tag("#sex");
    public static final Tag TAG_RELIGION = new Tag("#religion");
    public static final Tag TAG_AGE = new Tag("#age");
    public static final Tag TAG_MALE = new Tag("#male");
    public static final Tag TAG_FEMALE = new Tag("#female");
    public static final Tag TAG_TONGUE = new Tag("#tongue");
    public static final Tag TAG_MARITAL = new Tag("#marital");
    public static final Tag TAG_GRADE = new Tag("#grade");
    public static final Tag TAG_RELATIONSHIP = new Tag("#relationship");
    public static final Tag TAG_FIRSTNAME = new Tag("#firstname");
    public static final Tag TAG_MIDDLENAME = new Tag("#middlename");
    public static final Tag TAG_LASTNAME = new Tag("#lastname");
    public static final Tag TAG_TERRITORY = new Tag("#territory");
    public static final Tag TAG_FIELDWORK = new Tag("#fieldwork");
    public static final Tag TAG_LISTING = new Tag("#listing");
    public static final Tag TAG_EXPECTED = new Tag("#expected");
    public static final Tag TAG_EXPECTED_QUESTIONNAIRES = new Tag("#expectedQuestionnaires");

    private final String schema, prefix;
    private final List<Record> records = new LinkedList<>();
    private final Map<String, Record> recordsByName = new LinkedHashMap<>();
    private final Map<String, ValueSet> valueSets = new HashMap<>();
    private final Map<Tag, List<Taggable>> tags = new HashMap<>();

    private String name;
    private Integer recordTypeLen;
    private Record lastRecord;
    private List<Item> lastItems;
    private List<Item> lastItemsNotSubItem;
    
    public Dictionary(String schema, String prefix) {
        this.schema = schema;
        this.prefix = prefix;
    }

    public String getSchema() {
        return schema;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getRecordTypeLen() {
        return recordTypeLen;
    }

    public void setRecordTypeLen(Integer recordTypeLen) {
        this.recordTypeLen = recordTypeLen;
    }
    
    public void addRecord(Record record) {
        if (record.hasTag(TAG_IGNORE)) {
            return;
        }
        if (this.records.isEmpty()) {
            // idItems record
            record.setIsMainRecord(true);
        } else {
            record.setMainRecord(getMainRecord());
        }
        this.lastRecord = record;
        this.records.add(this.lastRecord);
        this.recordsByName.put(record.getRecordTypeValue(), record);
    }

    public boolean addItem(Item item) {
        if (item.hasTag(TAG_IGNORE)) {
            return false;
        }
        if (item.getOccurrences() > 1) {
            List<Item> its = new LinkedList<>();
            for (int i = 0; i < item.getOccurrences(); i++) {
                Item it = item.clone();
                it.setOccurenceNumber(i);
                it.setName(it.getName() + "_" + i);
                it.setValueSetName(item.getName());
                it.setStart(it.getStart() + i * it.getLength());
                its.add(it);
            }
            if (item.isSubItem()) {
                addSubItems(its);
            } else {
                addLastItemsNotSubItem(its);
                this.lastRecord.addItems(its);
            }
            addLastItems(its);
        } else {
            if (item.isSubItem()) {
                addSubItem(item);
            } else {
                addLastItemNotSubItem(item);
                this.lastRecord.addItem(item);
            }
            addLastItem(item);
        }
        return true;
    }

    public void addValueSet(ValueSet valueSet) {
        if (valueSet != null) {
            if (valueSet.getLink() != null && !valueSet.getLink().isEmpty()
                    && valueSets.containsKey(valueSet.getLink())) {
                valueSet = valueSets.get(valueSet.getLink()).clone();
            } else {
                if (valueSet.isEmpty()) {
                    return;
                }
                if (valueSet.getLink() != null && !valueSet.getLink().isEmpty()) {
                    this.valueSets.put(valueSet.getLink(), valueSet);
                }
            }
            addValueSetToLastItems(valueSet);
        }
    }

    private void addLastItem(Item item) {
        this.lastItems = new LinkedList<>();
        this.lastItems.add(item);
    }

    private void addLastItems(List<Item> items) {
        this.lastItems = items;
    }

    private void addValueSetToLastItems(ValueSet valueSet) {
        for (Item item : this.lastItems) {
            if (ITEM_ALPHA.equals(item.getDataType()) && item.hasTag(TAG_MULTIPLE_RESPONSE)) {
                List<Item> splits = item.splitIntoColumns(valueSet);
                this.lastRecord.replaceItemWithSplit(item, splits);
            } else {
                if (!ITEM_ALPHA.equals(item.getDataType())) {
                    valueSet.removeEmptyValues();
                }
                item.addValueSet(valueSet);
            }
        }
    }

    private void addLastItemNotSubItem(Item item) {
        this.lastItemsNotSubItem = new LinkedList<>();
        this.lastItemsNotSubItem.add(item);
    }

    private void addLastItemsNotSubItem(List<Item> items) {
        this.lastItemsNotSubItem = items;
    }

    private void addSubItem(Item subItem) {
        for (Item item : this.lastItemsNotSubItem) {
            item.addSubItem(subItem);
        }
    }

    private void addSubItems(List<Item> subItems) {
        for (Item item : this.lastItemsNotSubItem) {
            for (Item subItem : subItems) {
                item.addSubItem(subItem);
            }
        }
    }

    public Record getMainRecord() {
        return this.records.get(0);
    }

    public List<Record> getRecords() {
        return records;
    }

    public Record getRecord(char name) {
        return recordsByName.get("" + name);
    }

    public Record getRecord(String name) {
        return recordsByName.get(name);
    }

    public void addTagged(Tag tag, Taggable o) {
        if (!tags.containsKey(tag)) {
            tags.put(tag, new LinkedList<Taggable>());
        }
        tags.get(tag).add(o);
    }

    public boolean hasTagged(Tag tag) {
        return tags.containsKey(tag);
    }

    public Record getTaggedRecord(Tag tag) {
        return (Record) tags.get(tag).get(0);
    }

    public Item getTaggedItem(Tag tag) {
        if (tags.containsKey(tag)) {
            return (Item) tags.get(tag).get(0);
        }
        return null;
    }

    public Iterable<Item> getTaggedItems(Tag tag) {
        return (Iterable<Item>) (Object) tags.get(tag);
    }

    public ValueSet getTaggedValueSet(Tag tag) {
        return (ValueSet) tags.get(tag).get(0);
    }

    public ValueSetValue getTaggedValueSetValue(Tag tag) {
        return (ValueSetValue) tags.get(tag).get(0);
    }

    public Tag getTag(Tag tag) {
        for (Tag t : tags.keySet()) {
            if (t.equals(tag)) {
                return t;
            }
        }
        return null;
    }

}
