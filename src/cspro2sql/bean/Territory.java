package cspro2sql.bean;

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
 * @version 0.9.18
 * @since 0.9.18
 */
public class Territory {

    private final Map<String, TerritoryItem> items;
    private final List<TerritoryItem> itemsList;

    public Territory() {
        this.items = new LinkedHashMap<>();
        this.itemsList = new LinkedList<>();
    }

    public void addItem(Item item) {
        String name = null;
        TerritoryItem parent = null;
        Tag tag = item.getTag(Dictionary.TAG_TERRITORY);
        if (tag.getValue() != null) {
            String[] tagValues = tag.getValue().split(",");
            name = tagValues[0];
            if (tagValues.length > 1 && !tagValues[1].isEmpty()) {
                parent = items.get(tagValues[1]);
            }
        }
        TerritoryItem territoryItem = new TerritoryItem(item, parent, name);
        items.put(item.getName(), territoryItem);
        itemsList.add(territoryItem);
    }

    public void addItem(String name){
        itemsList.add(new TerritoryItem(null, null, name));
    }
    
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public List<TerritoryItem> getItemsList() {
        return itemsList;
    }
    
    public TerritoryItem getFirst() {
        return this.itemsList.get(0);
    }

    public TerritoryItem get(int i) {
        return this.itemsList.get(i);
    }

    public int size() {
        return this.itemsList.size();
    }

}
