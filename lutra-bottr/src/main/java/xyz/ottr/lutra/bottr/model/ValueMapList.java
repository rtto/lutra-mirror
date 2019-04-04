package xyz.ottr.lutra.bottr.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*-
 * #%L
 * lutra-bottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


/**
 * ValueMapList currently only holds just the RDF types that the input data values
 * are to be cast to. I expect ValueMaps to grow to be more complex, possibly computing
 * terms based on multiple input values.
 * 
 * @author martige
 */
public class ValueMapList {

    private List<ValueMap> maps;

    public ValueMapList() {
        this.maps = new ArrayList<>();
    }

    public void addValueMap(String type) {
        this.maps.add(new ValueMap(type));
    }

    public List<String> getTypes() {
        return maps.stream().map(m -> m.getType()).collect(Collectors.toList());
    }

    /*
    // TODO. The idea is that this method will transform and type the row values into OTTR Terms.
    public List<Term> getTerms(Source.Row row) {
        return null; // TODO
    }
     */

    private static class ValueMap {

        private String type;

        public ValueMap(String type) {
            this.type = type;
        }

        public String getType() {
            return this.type;
        }

    }

}
