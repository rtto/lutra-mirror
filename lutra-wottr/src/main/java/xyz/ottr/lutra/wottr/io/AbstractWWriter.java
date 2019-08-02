package xyz.ottr.lutra.wottr.io;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.wottr.WOTTR;

public abstract class AbstractWWriter {

    private Map<String, Resource> blankNodes = new HashMap<String, Resource>(); // Maps label -> resource for reuse

    protected boolean isTriple(Instance node) {

        String nodeIRI = node.getIRI();
        return OTTR.BaseURI.Triple.equals(nodeIRI) || OTTR.BaseURI.NullableTriple.equals(nodeIRI);
    }

    protected RDFNode toRDFNode(Model m, Term term) {

        if (term instanceof TermList) {
            List<RDFNode> lst = new ArrayList<RDFNode>();
            ((TermList) term).asList().stream().forEach(t -> lst.add(toRDFNode(m, t)));
            Iterator<RDFNode> iter = lst.iterator();
            return m.createList(iter);
        } else if (term instanceof IRITerm) {
            String uri = ((IRITerm) term).getIRI();
            return m.createResource(uri);
        } else if (term instanceof LiteralTerm) {
            LiteralTerm lit = (LiteralTerm) term;
            String val = lit.getPureValue();
            // TODO: Check correctness of typing below
            if (lit.getDatatype() != null) { // Typed literal
                String type = lit.getDatatype();
                TypeMapper tm = TypeMapper.getInstance();
                return m.createTypedLiteral(val, tm.getSafeTypeByName(type));
            } else if (lit.getLangTag() != null) { // Literal with language tag
                String tag = lit.getLangTag();
                return m.createLiteral(val, tag);
            } else { // Untyped literal (just a string)
                return m.createLiteral(val);
            }
        } else if (term instanceof BlankNodeTerm) {
            String label = ((BlankNodeTerm) term).getLabel();
            if (blankNodes.containsKey(label)) {
                return blankNodes.get(label);
            } else {
                Resource b = m.createResource(new AnonId(label));
                blankNodes.put(label, b);
                return b;
            }
        } else if (term instanceof NoneTerm) {
            // Note: the resource is recreated *in/by the model* to allow the none-resource
            // to be cast to Property (by as(Property.class)). If we return the resource without
            // no "hosting" model, then the cast throws a UnsupportedPolymorphismException.
            return m.createResource(WOTTR.none.getURI());
        } else {
            return null; // TODO: Throw exception
        }
    }

    protected Resource toRDFType(Model model, TermType type) {

        if (type instanceof BasicType) {
            return model.createResource(((BasicType) type).getIRI());
        } else {
            return toComplexRDFType(model, type);
        }
    }

    private RDFList toComplexRDFType(Model model, TermType type) {

        if (type instanceof ListType) {
            RDFList rest = toComplexRDFType(model, ((ListType) type).getInner());
            return rest.cons(RDF.List);
        } else if (type instanceof NEListType) {
            RDFList rest = toComplexRDFType(model, ((NEListType) type).getInner());
            return rest.cons(model.createResource(OTTR.TypeURI.NEList));
        } else if (type instanceof LUBType) {
            RDFList rest = toComplexRDFType(model, ((LUBType) type).getInner());
            return rest.cons(model.createResource(OTTR.TypeURI.LUB));
        } else {
            RDFList nil = model.createList();
            Resource rdfType = model.createResource(((BasicType) type).getIRI());
            return nil.cons(rdfType);
        }
    }

    protected Statement getTriple(Model m, Instance i) {
        // TODO use functions in Terms?
        Resource s = toRDFNode(m, i.getArguments().get(0)).asResource();
        Property p = toRDFNode(m, i.getArguments().get(1)).as(Property.class);
        RDFNode o = toRDFNode(m, i.getArguments().get(2));
        return m.createStatement(s, p, o);
    }

    protected void addArguments(ArgumentList arguments, Resource iri, Model model) {

        if (arguments == null) {
            return; // TODO: Perhaps throw exception(?)
        }
        
        RDFList argsLst = model.createList();

        for (Term arg : arguments.asList()) {
            RDFNode val = toRDFNode(model, arg);

            Resource argNode = model.createResource();
            model.add(model.createStatement(argNode, WOTTR.value, val));

            if (arguments.hasListExpander(arg)) {
                model.add(model.createStatement(argNode, WOTTR.modifier, WOTTR.listExpand));
            }
            argsLst = argsLst.with(argNode);
        }
        model.add(model.createStatement(iri, WOTTR.arguments, argsLst));
    }

    protected void addParameters(ParameterList parameters, Resource iri, Model model) {

        if (parameters == null) {
            return; // TODO: Perhaps throw exception(?)
        }
        
        RDFList paramLst = model.createList();

        for (Term param : parameters.asList()) {
            RDFNode var = toRDFNode(model, param);
            Resource type = toRDFType(model, param.getType());

            Resource paramNode = model.createResource();
            model.add(model.createStatement(paramNode, WOTTR.variable, var));
            model.add(model.createStatement(paramNode, WOTTR.type, type));

            if (parameters.isOptional(param)) {
                model.add(model.createStatement(paramNode, WOTTR.modifier, WOTTR.optional));
            }
            if (parameters.isNonBlank(param)) {
                model.add(model.createStatement(paramNode, WOTTR.modifier, WOTTR.nonBlank));
            }
            if (parameters.hasDefaultValue(param)) {
                RDFNode def = toRDFNode(model, parameters.getDefaultValue(param));
                model.add(model.createStatement(paramNode, WOTTR.defaultVal, def));
            }
            paramLst = paramLst.with(paramNode);
        }
        model.add(model.createStatement(iri, WOTTR.parameters, paramLst));
    }
}
