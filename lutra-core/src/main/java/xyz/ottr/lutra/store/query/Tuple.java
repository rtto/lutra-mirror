package xyz.ottr.lutra.store.query;

/*-
 * #%L
 * lutra-core
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

import java.util.HashMap;
import java.util.Map;

public class Tuple {

    private final Map<String, Object> map;

    public Tuple() {
        this.map = new HashMap<>(); 
    }

    private Tuple(Map<String, Object> map) {
        this.map = map; 
    }

    public Tuple bind(String s, Object o) {
        Map<String, Object> nmap = new HashMap<>(this.map);
        nmap.put(s, o);
        return new Tuple(nmap);
    }

    public Tuple unbind(String... vars) {
        Map<String, Object> nmap = new HashMap<>(this.map);
        for (int i = 0; i < vars.length; i++) {
            nmap.remove(vars[i]);
        }
        return new Tuple(nmap);
    }

    public Tuple copy() {
        return new Tuple(this.map);
    }

    public boolean hasBound(String s) {
        return this.map.containsKey(s);
    }

    public Object get(String s) {
        return this.map.get(s);
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || o instanceof Tuple && this.map.equals(((Tuple) o).map);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

    /**
     * Checks that the variable denoted by t is bound and of type clazz and casts it to clazz.
     */
    public <T> T getAs(Class<T> clazz, String t) {
        if (!this.hasBound(t)) {
            throw new VariableNotBoundException(t);
        }
        Object mt = this.get(t);
        if (!(clazz.isInstance(mt))) {
            throw new VariableBoundToMultipleTypesException(t, mt.getClass(), clazz);
        }
        return clazz.cast(mt);
    }

}
