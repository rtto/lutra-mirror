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
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;
import xyz.ottr.lutra.wottr.vocabulary.WOTTRVocabulary;

public class RDFFactory {

    // TODO should this be static?
    private final Map<String, Resource> createdBlankNodes = new HashMap<>(); // reuse blank nodes

    private final WOTTRVocabulary vocabulary;

    public RDFFactory(WOTTRVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public static boolean isTriple(Instance instance) {
        String templateIRI = instance.getIri();
        return OTTR.BaseURI.Triple.equals(templateIRI)
            || OTTR.BaseURI.NullableTriple.equals(templateIRI);
    }

    public Statement createTriple(Model model, Instance instance) {
        RDFNode s = createRDFNode(model, instance.getArguments().get(0));
        RDFNode p = createRDFNode(model, instance.getArguments().get(1));
        RDFNode o = createRDFNode(model, instance.getArguments().get(2));

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


    public RDFNode createRDFNode(Model model, Term term) {

        if (term instanceof TermList) {
            return createRDFList(model, (TermList) term);
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

    public RDFList createRDFList(Model model, TermList term) {
        Iterator<RDFNode> iterator = term.asList().stream()
            .map(t -> createRDFNode(model, t))
            .iterator();
        return model.createList(iterator);
    }

    public Resource createURIResource(Model model, IRITerm term) {
        return model.createResource(term.getIri());
    }

    public Literal createLiteral(Model model, LiteralTerm term) {
        String val = term.getPureValue();
        // TODO: Check correctness of typing below
        if (term.getDatatype() != null) { // Typed literal
            String type = term.getDatatype();
            TypeMapper tm = TypeMapper.getInstance();
            return model.createTypedLiteral(val, tm.getSafeTypeByName(type));
        } else if (term.getLangTag() != null) { // Literal with language tag
            String tag = term.getLangTag();
            return model.createLiteral(val, tag);
        } else { // Untyped literal (just a string)
            return model.createLiteral(val);
        }
    }

    public Resource createBlankNode(Model model, BlankNodeTerm term) {
        return this.createdBlankNodes.computeIfAbsent(term.getLabel(), l -> model.createResource(new AnonId(l)));
    }

    public Resource createNone(Model model, NoneTerm term) {
        // Note: the resource is recreated *in/by the model* to allow the none-resource
        // to be cast to Property (by as(Property.class)). If we return the resource without
        // no "hosting" model, then the cast throws a UnsupportedPolymorphismException.
        return model.createResource(this.vocabulary.getNoneResource().getURI());
    }
}
