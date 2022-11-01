package xyz.ottr.lutra.stottr.parser;

/*-
 * #%L
 * lutra-stottr
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

import java.util.Map;
import xyz.ottr.lutra.model.terms.Term;

public class SPatternInstanceParser extends SInstanceParser {

    SPatternInstanceParser(Map<String, String> prefixes, Map<String, Term> variables) {
        this.prefixes = prefixes;
        this.termParser = new STermParser(this.prefixes, variables);
        this.argumentParser = new SArgumentParser(this.termParser);
    }

    @Override
    protected void initSubParsers() {
        // no-op: don't overwrite variables set by constructor.
    }

}
