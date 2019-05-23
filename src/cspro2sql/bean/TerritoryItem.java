package cspro2sql.bean;

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
public class TerritoryItem {

    private final Item item;
    private final TerritoryItem parent;
    private final String name;

    public TerritoryItem(Item item, TerritoryItem parent, String name) {
        this.item = item;
        this.parent = parent;
        this.name = name;
    }

    public Item getItem() {
        return item;
    }

    public String getItemName() {
        return item.getName();
    }

    public String getName() {
        return name;
    }

    public String selectDescription() {
        String select;
        select = "(SELECT " + item.getName() + "_name FROM territory WHERE ";
        select += parse();
        select += " limit 1)";
        return select;
    }

    private String parse() {
        String select = "";
        if (this.parent != null) {
            select = this.parent.parse() + " AND ";
        }
        return select + item.getName() + " = h." + item.getName();
    }

}
