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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import org.apache.jena.rdf.model.Statement;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.wottr.vocabulary.WOTTRVocabulary;

public class RDFFactory {

    // TODO should this be static?
    private final Map<String, Resource> createdBlankNodes = new HashMap<String, Resource>(); // reuse blank nodes

    private WOTTRVocabulary vocaulary;

    public RDFFactory(WOTTRVocabulary vocaulary) {
        this.vocaulary = vocaulary;
    }

    public RDFNode createRDFNode(Model model, Term term) {

        if (term instanceof TermList) {
            Iterator<RDFNode> iterator = ((TermList) term).asList().stream()
                .map(t -> createRDFNode(model, t))
                .iterator();
            return model.createList(iterator);
        } else if (term instanceof IRITerm) {
            String uri = ((IRITerm) term).getIRI();
            return model.createResource(uri);
        } else if (term instanceof LiteralTerm) {
            LiteralTerm lit = (LiteralTerm) term;
            String val = lit.getPureValue();
            // TODO: Check correctness of typing below
            if (lit.getDatatype() != null) { // Typed literal
                String type = lit.getDatatype();
                TypeMapper tm = TypeMapper.getInstance();
                return model.createTypedLiteral(val, tm.getSafeTypeByName(type));
            } else if (lit.getLangTag() != null) { // Literal with language tag
                String tag = lit.getLangTag();
                return model.createLiteral(val, tag);
            } else { // Untyped literal (just a string)
                return model.createLiteral(val);
            }
        } else if (term instanceof BlankNodeTerm) {
            String label = ((BlankNodeTerm) term).getLabel();
            return createdBlankNodes.computeIfAbsent(label,
                blankLabel -> model.createResource(new AnonId(blankLabel)));
        } else if (term instanceof NoneTerm) {
            // Note: the resource is recreated *in/by the model* to allow the none-resource
            // to be cast to Property (by as(Property.class)). If we return the resource without
            // no "hosting" model, then the cast throws a UnsupportedPolymorphismException.
            return model.createResource(this.vocaulary.getNoneResource().getURI());
        } else {
            return null; // TODO: Throw exception
        }
    }

    public static boolean isTriple(Instance instance) {
        String templateIRI = instance.getIRI();
        return OTTR.BaseURI.Triple.equals(templateIRI)
            || OTTR.BaseURI.NullableTriple.equals(templateIRI);
    }

    public Statement createTriple(Model model, Instance instance) {
        // TODO use functions in Terms?
        Resource s = createRDFNode(model, instance.getArguments().get(0)).asResource();
        Property p = createRDFNode(model, instance.getArguments().get(1)).as(Property.class);
        RDFNode o = createRDFNode(model, instance.getArguments().get(2));
        return model.createStatement(s, p, o);
    }
}
