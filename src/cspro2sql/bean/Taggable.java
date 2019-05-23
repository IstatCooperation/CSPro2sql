package cspro2sql.bean;

import java.util.HashSet;
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
 * @version 0.9.7
 * @since 0.9.7
 */
abstract class Taggable {

    private final Set<Tag> tags = new HashSet<>();

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public Tag getTag(Tag tag) {
        for (Tag t : tags) {
            if (t.equals(tag)) {
                return t;
            }
        }
        return null;
    }

    public boolean hasTag(Tag tag) {
        return this.tags.contains(tag);
    }

    public Iterable<Tag> getTags() {
        return this.tags;
    }

}
