package xyz.ottr.lutra.store.graph;

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

import xyz.ottr.lutra.system.MessageHandler;

public interface Checker {

    /**
     * Performs all checks on all templates in this library, and returns
     * errors or warnings if checks fail. The following is checked:
     * - Type correctness, non-blank flags, and consistent use of resources
     * - Correct calling of templates in instances
     * - Cycles in template definitions
     * - Unused variables, reused variables in different parameters
     * - Use of lists and expansion modifiers
     * - Missing template
     */
    MessageHandler checkTemplates();

    /**
     * Performs the same checks as #checkTemplates(), except "Missing templates".
     * This method should be used if one either wants to check single templates
     * (without having its dependencies loaded in the store) or to check templates
     * in an unfinished library where not all templates are (yet) defined.
     */
    MessageHandler checkTemplatesForErrorsOnly();

}
