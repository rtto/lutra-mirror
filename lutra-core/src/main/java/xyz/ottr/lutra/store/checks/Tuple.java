package xyz.ottr.lutra.store.checks;

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
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Tuple {

    private static int newId = 0;

    public static String freshVar() {
        newId++;
        return "_var" + newId;
    }

    private final Map<String, Object> map;

    public Tuple() {
        this.map = new HashMap<>(); 
    }

    private Tuple(Map<String, Object> map) {
        this.map = map; 
    }

    public Tuple bind(String name, Object obj) {
        Map<String, Object> nmap = new HashMap<>(this.map);
        nmap.put(name, obj);
        return new Tuple(nmap);
    }

    public Tuple unbind(String... vars) {
        Map<String, Object> nmap = new HashMap<>(this.map);
        for (String var : vars) {
            nmap.remove(var);
        }
        return new Tuple(nmap);
    }

    public Tuple copy() {
        return new Tuple(this.map);
    }

    public boolean hasBound(String name) {
        return this.map.containsKey(name);
    }

    public Object get(String name) {
        return this.map.get(name);
    }

    @Override
    public String toString() {
        return this.map.toString();
    }

    /**
     * Checks that the variable denoted by t is bound and of type clazz and casts it to clazz.
     */
    public <T> T getAs(Class<T> clazz, String name) {
        if (!this.hasBound(name)) {
            throw new VariableNotBoundException(name);
        }
        Object mt = this.get(name);
        if (!clazz.isInstance(mt)) {
            throw new VariableBoundToMultipleTypesException(name, mt.getClass(), clazz);
        }
        return clazz.cast(mt);
    }

    /**
     * Internally in the model, indices start at 0, but we want indices to start at 1
     * for end-users. This method simply gets the integer bound to the variable name
     * and increases it by 1, and turns it into a String. This method should only be used
     * when displaying the value of an index in a Message, and not internally.
     */
    public String getAsEndUserIndex(String name) {
        return Integer.toString(getAs(Integer.class, name) + 1);
    }
}
