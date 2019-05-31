package xyz.ottr.lutra.wottr.legacy.io;

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
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.AnnotationProperty;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.wottr.legacy.WOTTR;

@SuppressWarnings("CPD-START")
public abstract class AbstractWriter {

    private Map<String, Resource> blankNodes = new HashMap<String, Resource>(); // Maps label -> resource for reuse

    protected boolean isTriple(Instance node) {
        return WOTTR.triple.toString().equals(node.getIRI());
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
                Resource b = m.createResource();
                blankNodes.put(label, b);
                return b;
            }
        } else if (term instanceof NoneTerm) {
            return WOTTR.none;
        } else {
            return null; // TODO: Throw exception
        }
    }

    protected Statement getTriple(Model m, Instance i) {
        // TODO use functions in Terms?
        Resource s = toRDFNode(m, i.getArguments().get(0)).asResource();
        Property p = toRDFNode(m, i.getArguments().get(1)).as(Property.class);
        RDFNode o = toRDFNode(m, i.getArguments().get(2));
        return m.createStatement(s, p, o);
    }

    protected void addVariableStatement(Resource v, RDFNode n, Model m) {

        Property p;
        if (n.isLiteral()) {                                // Literal
            p = WOTTR.literalVariable;
        } else if (n.canAs(RDFList.class)) {                // List
            p = WOTTR.listVariable;
        } else if (n.canAs(OntClass.class)) {               // Class
            p = WOTTR.classVariable;
        } else if (n.canAs(Individual.class)) {             // Individual
            p = WOTTR.individualVariable;
        //} else if (n.canAs(Property.class)) {               // Property // Removed: Any reasource canAs(Property) TODO
        } else if (n.canAs(DatatypeProperty.class)) {          // DataProperty
            p = WOTTR.dataPropertyVariable;
        } else if (n.canAs(AnnotationProperty.class)) { // AnnotationProperty
            p = WOTTR.annotationPropertyVariable;
        } else if (n.canAs(ObjectProperty.class)) {     // ObjectProperty
            p = WOTTR.objectPropertyVariable;
        //} else {
        //    p = WOTTR.propertyVariable;
        //}
        // TODO: Find proper Datatype-class
        //} else if (n.canAs(Datatype.class)) {             // DataType
        //    p = WOTTR.datatypeVariable;
        } else {
            p = WOTTR.variable;
        }
        m.add(m.createStatement(v, p, n));
    }

    protected void addArguments(ArgumentList arguments, Resource iri, Model m) {

        if (arguments == null) {
            return; // TODO: Perhaps throw exception(?)
        }
        
        int index = 1; // Start index count on 1

        for (Term arg : arguments.asList()) {
            Resource b = m.createResource();    
            RDFNode n = toRDFNode(m, arg);

            m.add(m.createStatement(iri, WOTTR.hasArgument, b));
            if (arguments.hasListExpander(arg) && arguments.hasCrossExpander()) {
                m.add(m.createStatement(b, WOTTR.eachValue, n));
            } else {
                m.add(m.createStatement(b, WOTTR.value, n));
            }
            m.add(m.createStatement(b, WOTTR.index, m.createTypedLiteral(index, XSDDatatype.XSDint)));
            index++;
        }
    }

    protected void addParameters(ParameterList parameters, Resource iri, Model m) {

        if (parameters == null) {
            return; // TODO: Perhaps throw exception(?)
        }
        
        int index = 1; // Start index count on 1

        for (Term param : parameters.asList()) {
            Resource b = m.createResource();    
            RDFNode n = toRDFNode(m, param);

            m.add(m.createStatement(iri, WOTTR.hasParameter, b));
            addVariableStatement(b, n, m);
            m.add(m.createStatement(b, WOTTR.index, m.createTypedLiteral(index, XSDDatatype.XSDint)));

            if (parameters.isOptional(param)) {
                m.add(m.createStatement(b, WOTTR.optional, m.createTypedLiteral(true, XSDDatatype.XSDboolean)));
            }
            index++;
        }
    }
}
