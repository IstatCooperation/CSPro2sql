package cspro2sql.bean;

import java.util.HashMap;
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
 * @version 0.9.12
 */
public class Concepts {

    public static final String HOUSEHOLD = "household";
    public static final String LISTING = "listing";
    public static final String EXPECTED = "expected";
    public static final String TERRITORY = "territory";
    public static final String INDIVIDUAL = "individual";
    public static final String AGE = "age";
    public static final String SEX = "sex";
    public static final String RELIGION = "religion";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String EXPECTED_QUEST = "expectedQuest";

    public static final Integer HOUSEHOLD_ID = 1;
    public static final Integer LISTING_ID = 2;
    public static final Integer EXPECTED_ID = 3;
    public static final Integer TERRITORY_ID = 4;
    public static final Integer INDIVIDUAL_ID = 5;
    public static final Integer AGE_ID = 6;
    public static final Integer SEX_ID = 7;
    public static final Integer RELIGION_ID = 8;
    public static final Integer LATITUDE_ID = 9;
    public static final Integer LONGITUDE_ID = 10;
    public static final Integer EXPECTED_QUEST_ID = 11;

    private static final Map<Integer, String> conceptMap = getConcepts();

    public static Map<Integer, String> getConcepts() {
        Map<Integer, String> tmpMap = new HashMap<>();

        tmpMap.put(HOUSEHOLD_ID, HOUSEHOLD);
        tmpMap.put(LISTING_ID, LISTING);
        tmpMap.put(EXPECTED_ID, EXPECTED);
        tmpMap.put(TERRITORY_ID, TERRITORY);
        tmpMap.put(INDIVIDUAL_ID, INDIVIDUAL);
        tmpMap.put(AGE_ID, AGE);
        tmpMap.put(SEX_ID, SEX);
        tmpMap.put(RELIGION_ID, RELIGION);
        tmpMap.put(LATITUDE_ID, LATITUDE);
        tmpMap.put(LONGITUDE_ID, LONGITUDE);
        tmpMap.put(EXPECTED_QUEST_ID, EXPECTED_QUEST);

        return tmpMap;
    }

    public static Integer getId(String description) {
        for (Map.Entry<Integer, String> element : conceptMap.entrySet()) {
            if (element.getValue().equals(description.replace("#", ""))) {
                return element.getKey();
            }
        }
        return null;
    }

    public static Integer getId(Dictionary dictionary) {
        if (dictionary.hasTag(Dictionary.TAG_HOUSEHOLD)) {
            return HOUSEHOLD_ID;
        } else if (dictionary.hasTag(Dictionary.TAG_LISTING)) {
            return LISTING_ID;
        } else if (dictionary.hasTag(Dictionary.TAG_EXPECTED)) {
            return EXPECTED_ID;
        }
        return null;
    }

    public static Integer getId(Record record) {
        if (record.hasTag(Dictionary.TAG_INDIVIDUAL)) {
            return INDIVIDUAL_ID;
        }
        return null;
    }

    public static Integer getId(Item item) {
        if (item.hasTag(Dictionary.TAG_TERRITORY)) {
            return TERRITORY_ID;
        } else if (item.hasTag(Dictionary.TAG_AGE)) {
            return AGE_ID;
        } else if (item.hasTag(Dictionary.TAG_SEX)) {
            return SEX_ID;
        } else if (item.hasTag(Dictionary.TAG_RELIGION)) {
            return RELIGION_ID;
        } else if (item.hasTag(Dictionary.TAG_LATITUDE)) {
            return LATITUDE_ID;
        } else if (item.hasTag(Dictionary.TAG_LONGITUDE)) {
            return LONGITUDE_ID;
        } else if (item.hasTag(Dictionary.TAG_EXPECTED_QUESTIONNAIRES)) {
            return EXPECTED_QUEST_ID;
        }

        return null;
    }
}
