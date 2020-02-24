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
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.wottr.WOTTR;

class RDFFactory {

    // TODO should this be static?
    private final Map<String, Resource> createdBlankNodes = new HashMap<>(); // reuse blank nodes

    static boolean isTriple(Instance instance) {
        String templateIRI = instance.getIri();
        return OTTR.BaseURI.Triple.equals(templateIRI)
            || OTTR.BaseURI.NullableTriple.equals(templateIRI);
    }

    Statement createTriple(Model model, Instance instance) {

        var arguments = instance.getArguments();

        RDFNode s = createRDFNode(model, arguments.get(0).getTerm());
        RDFNode p = createRDFNode(model, arguments.get(1).getTerm());
        RDFNode o = createRDFNode(model, arguments.get(2).getTerm());

        // TODO: these checks should be superfluous once instance type checking is in place.
        checkSubject(s, instance);
        checkPredicate(p, instance);

        return model.createStatement(s.asResource(), p.as(Property.class), o);
    }

    private void checkSubject(RDFNode subject, Instance instance) throws IllegalArgumentException {
        if (!subject.isResource()) {
            throw new IllegalArgumentException("Error creating triple of instance " + instance
                + ". Expected a resource on subject position, but found "
                + (subject.isLiteral() ? "a literal: " + subject.asLiteral() : subject));
        }
    }

    private void checkPredicate(RDFNode predicate, Instance instance) throws IllegalArgumentException {
        String error;
        if (predicate.isLiteral()) {
            error = "a literal: " + predicate.asLiteral();
        } else if (predicate.isAnon()) {
            error = "a blank node: " + predicate;
        } else {
            error = predicate.toString();
        }

        if (!predicate.canAs(Property.class)) {
            throw new IllegalArgumentException("Error creating triple of instance " + instance
                + ". Expected a property on predicate position, but found " + error);
        }
    }


    RDFNode createRDFNode(Model model, Term term) {

        if (term instanceof ListTerm) {
            return createRDFList(model, (ListTerm) term);
        } else if (term instanceof IRITerm) {
            return createURIResource(model, (IRITerm) term);
        } else if (term instanceof LiteralTerm) {
            return createLiteral(model, (LiteralTerm) term);
        } else if (term instanceof BlankNodeTerm) {
            return createBlankNode(model, (BlankNodeTerm) term);
        } else if (term instanceof NoneTerm) {
            return createNone(model, (NoneTerm) term);
        } else {
            throw new IllegalArgumentException("Error converting term " + term + " to RDFNode. "
                + "Unexpected term class: " + term.getClass().getSimpleName());
        }
    }

    private RDFList createRDFList(Model model, ListTerm term) {
        Iterator<RDFNode> iterator = term.asList().stream()
            .map(t -> createRDFNode(model, t))
            .iterator();
        return model.createList(iterator);
    }

    private Resource createURIResource(Model model, IRITerm term) {
        return model.createResource(term.getIri());
    }

    private Literal createLiteral(Model model, LiteralTerm term) {
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

    private Resource createBlankNode(Model model, BlankNodeTerm term) {
        return this.createdBlankNodes.computeIfAbsent(term.getLabel(), l -> model.createResource(new AnonId(l)));
    }

    private Resource createNone(Model model, NoneTerm term) {
        // Note: the resource is recreated *in/by the model* to allow the none-resource
        // to be cast to Property (by as(Property.class)). If we return the resource without
        // no "hosting" model, then the cast throws a UnsupportedPolymorphismException.
        return model.createResource(WOTTR.none);
    }
}
