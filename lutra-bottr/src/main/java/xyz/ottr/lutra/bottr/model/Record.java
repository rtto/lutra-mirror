package xyz.ottr.lutra.bottr.model;

import java.util.Arrays;

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


public class Record<V> {

    private final List<V> values;

    public Record(List<V> values) {
        this.values = values;
    }

    @SafeVarargs
    public Record(V... values) {
        this(Arrays.asList(values));
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.values == null) ? 0 : this.values.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Record)) {
            return false;
        }
        Record<?> other = (Record<?>) obj;
        if (this.values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!this.values.equals(other.values)) {
            return false;
        }
        return true;
    }

}
