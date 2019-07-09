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
 * @version 0.9.15
 */
public class Answer {

    private final Item item;
    private final String value;

    public String error;

    public Answer(Item item, String value) {
        //System.out.println("Item " + item.getName() + ": " + value);
        this.item = item;
        this.value = (value == null) ? null : value.trim();
    }

    public Item getItem() {
        return item;
    }

    public String getValue() {
        return value;
    }

    public boolean validate() {
        if (value == null) {
            return true;
        }
        try {
            String v = value;
            boolean decimal = Dictionary.ITEM_DECIMAL.equals(item.getDataType());
            if (decimal) {
                if (!v.trim().matches("^[-+]?[0-9]*[.]?[0-9]*$")) {
                    throw new Exception("Not valid number: " + v);
                }
            }
            if (item.hasValueSets()) {
                boolean found = false;
                for (ValueSet vs : item.getValueSets()) {
                    if (vs.containsKey(v)) {
                        found = true;
                        break;
                    }
                    if (decimal) {
                        String vv = v.replaceFirst("^0*", "");
                        if (vv.isEmpty()) {
                            vv = "0";
                        } else if (vv.startsWith(".")) {
                            vv = "0" + vv;
                        }
                        if (vs.containsKey(vv)) {
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    error = "[Col: " + item.getName() + " Val: " + value + "]";
                    return false;
                }
            }
        } catch (Exception ex) {
            error = "[Col: " + item.getName() + " Val: " + value + " (" + ex.getMessage() + ")]";
            return false;
        }
        return true;
    }

    public String getError() {
        return error;
    }

}
