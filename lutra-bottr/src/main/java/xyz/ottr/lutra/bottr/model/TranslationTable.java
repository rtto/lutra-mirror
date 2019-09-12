package xyz.ottr.lutra.bottr.model;

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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import xyz.ottr.lutra.bottr.util.TermFactory;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;

public class TranslationTable implements Function<Term, Result<Term>> {

    private final Map<Term, Term> table;

    public TranslationTable(Map<Term, Term> table) {
        this.table = table;
    }

    public TranslationTable() {
        this(new HashMap<>());
    }

    public boolean containsKey(Term value) {
        return this.table.containsKey(value);
    }

    public Result<Term> apply(Term value) {
        Term translation = this.table.getOrDefault(value, value);
        return translation.isBlank() ? TermFactory.createBlankNode().map(t -> (Term) t) : Result.of(translation);
    }

}
