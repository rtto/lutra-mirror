package xyz.ottr.lutra.io;

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

import java.util.Set;
import java.util.function.Consumer;

import xyz.ottr.lutra.model.Instance;

public interface InstanceWriter extends Consumer<Instance> {

    /**
     * Adds a set of instances to this writer.
     *
     * @param instances
     *          a set of template instances to add to this Writer 
     */
    default void addInstances(Set<Instance> instances) {
        instances.stream().forEach(this);
    }

    String write();
}
