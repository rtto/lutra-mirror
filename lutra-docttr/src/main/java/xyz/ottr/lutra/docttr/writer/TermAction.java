package xyz.ottr.lutra.docttr.writer;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;

interface TermAction<O> {

    default O perform(Term term) {
        if (term instanceof BlankNodeTerm) {
            return perform((BlankNodeTerm)term);
        } else if (term instanceof IRITerm) {
            return perform((IRITerm)term);
        } else if (term instanceof ListTerm) {
            return perform((ListTerm)term);
        } else if (term instanceof LiteralTerm) {
            return perform((LiteralTerm)term);
        } else if (term instanceof NoneTerm) {
            return perform((NoneTerm)term);
        } else {
            throw new IllegalArgumentException("Unexpected term class " + term.getClass().getName()
                + ". Not supported by " + this.getClass().getName());
        }
    }

    O perform(BlankNodeTerm term);

    O perform(IRITerm term);

    O perform(ListTerm term);

    O perform(LiteralTerm term);

    O perform(NoneTerm term);
}
