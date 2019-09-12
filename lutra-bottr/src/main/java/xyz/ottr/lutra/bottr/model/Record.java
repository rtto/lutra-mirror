package xyz.ottr.lutra.bottr.model;

import java.util.ArrayList;

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

import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Record<V> {

    private final List<V> values;

    public Record(V value) {
        this.values = new ArrayList<>();
        this.values.add(value);
    }

    public Record(List<V> values) {
        this.values = values;
    }

    public V getValue(int index) {
        return this.values.get(index);
    }

    public List<V> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public String toString() {
        return this.values.toString();
    }

}
