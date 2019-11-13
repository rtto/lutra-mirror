package xyz.ottr.lutra.writer;

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

import xyz.ottr.lutra.model.Signature;

public interface TemplateWriter extends Consumer<Signature> {

    /**
     * Returns the set of IRIs which is added to this writer.
     */
    Set<String> getIRIs();

    /**
     * Adds a template definition to this writer.
     *
     * @param template
     *          a template to add to this Writer
     */
    void accept(Signature template);

    /**
     * Adds a set of definitions to this writer.
     *
     * @param templates
     *          a set of templates to add to this Writer
     */
    default void addTemplates(Set<Signature> templates) {
        templates.forEach(this);
    }

    String write(String iri);
    
}
