package xyz.ottr.lutra.wottr.writer;

/*-
 * #%L
 * lutra-wottr
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
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.wottr.WOTTR;

class WTermWriter {

    // TODO ok that this is static?
    private static final Map<BlankNodeTerm, Resource> createdBlankNodes = new HashMap<>(); // reuse blank nodes

    static RDFNode term(Model model, Term term) {

        if (term instanceof ListTerm) {
            return listTerm(model, (ListTerm) term);
        } else if (term instanceof IRITerm) {
            return iriTerm(model, (IRITerm) term);
        } else if (term instanceof LiteralTerm) {
            return literalTerm(model, (LiteralTerm) term);
        } else if (term instanceof BlankNodeTerm) {
            return blankNodeTerm(model, (BlankNodeTerm) term);
        } else if (term instanceof NoneTerm) {
            return none(model);
        } else {
            throw new IllegalArgumentException("Error converting term " + term + " to RDFNode. "
                + "Unexpected term class: " + term.getClass().getSimpleName());
        }
    }

    private static RDFList listTerm(Model model, ListTerm term) {
        Iterator<RDFNode> iterator = term.asList().stream()
            .map(t -> term(model, t))
            .iterator();
        return model.createList(iterator);
    }

    private static Resource iriTerm(Model model, IRITerm term) {
        return model.createResource(term.getIri());
    }

    private static Literal literalTerm(Model model, LiteralTerm term) {
        String val = term.getValue();
        // TODO: Check correctness of typing below
        if (term.getLanguageTag() != null) { // Literal with language tag
            String tag = term.getLanguageTag();
            return model.createLiteral(val, tag);
        } else if (term.getDatatype() != null) { // Typed literal
            String type = term.getDatatype();
            TypeMapper tm = TypeMapper.getInstance();
            return model.createTypedLiteral(val, tm.getSafeTypeByName(type));
        } else { // Untyped literal (just a string)
            return model.createLiteral(val);
        }
    }

    private static Resource blankNodeTerm(Model model, BlankNodeTerm term) {
        return createdBlankNodes.computeIfAbsent(term, l -> model.createResource());
    }

    private static Resource none(Model model) {
        return WOTTR.none.inModel(model);
    }
}
