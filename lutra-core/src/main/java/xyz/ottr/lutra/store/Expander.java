package xyz.ottr.lutra.store;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public interface Expander {

    /**
     * Expands the argument template instance according to the definitions in this
     * store, but fetches misisng templates, and returns empty Result-instances if the instance is using
     * a template wrongly (e.g.~wrong number of arguments or wrong types, optionals).
     *
     * @param instance
     *     the template instance to expand
     *
     * @return
     *     a ResultStream of expanded template instances
     */
    ResultStream<Instance> expandInstanceFetch(Instance instance);

    /**
     * Expands all nodes without losing information, that is, it does not expand
     * nodes with non-optional possible null valued parameters, and does not alter
     * this TemplateStore.
     *
     * @return
     *          a new TemplateStore containing the expansion of this graph
     */
    Result<? extends TemplateStoreNew> expandAll();

    /**
     * Expands the argument template instance according to the definitions in this
     * store, and returns empty Result-instances if the instance is using
     * a template wrongly (e.g.~wrong number of arguments or wrong types, optionals).
     */
    ResultStream<Instance> expandInstance(Instance instance);

    /**
     * Expands the argument template according to the definitions in this
     * store, and returns empty Result-instances if the template is using
     * a template wrongly (e.g.~wrong number of arguments or wrong types, optionals).
     */
    Result<Template> expandTemplate(Template template);

}
