package xyz.ottr.lutra.model.types;

/*-
 * #%L
 * lutra-core
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

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;

public enum TypeFactory {
    ;

    private static Map<String, BasicType> iris;
    private static Map<BasicType, Set<BasicType>> superTypes;
    
    static {
        init();
    }
    
    public static final BasicType TOP = getType(RDFS.Resource);
    public static final BasicType BOT = getType(OTTR.TypeURI.Bot);
    public static final BasicType IRI = getType(OTTR.TypeURI.IRI);
    public static final BasicType LITERAL = getType(RDFS.Literal);

    private static void init() {
        InputStream filename = TypeFactory.class.getClassLoader().getResourceAsStream(OTTR.Files.StdTypes);
        Model types = ModelFactory.createDefaultModel();
        types.read(filename, null, "TTL");
        Reasoner owlMicro = ReasonerRegistry.getOWLMicroReasoner();
        Model model = ModelFactory.createInfModel(owlMicro, types);

        initTypes(model);
        initSuperTypes(model);
    }

    private static void initTypes(Model model) {
        iris = model.listResourcesWithProperty(RDF.type, model.createResource(OTTR.TypeURI.Type))
                .toSet().stream()
                .map(RDFNode::asResource)
                .map(BasicType::new)
                .collect(Collectors.toMap(BasicType::getIRI, Function.identity()));
    }

    private static void initSuperTypes(Model model) {

        // prepare the map of types -> set of supertypes
        superTypes = iris.values().stream().collect(
                Collectors.toMap(Function.identity(), _x -> new HashSet<>()));
        
        Property subTypeOf = model.createProperty(OTTR.TypeURI.subTypeOf);
        model.listStatements((Resource) null, subTypeOf, (RDFNode) null)
            .forEachRemaining(stmt ->  {
                BasicType subType = getType(stmt.getSubject().asResource());
                BasicType superType = getType(stmt.getObject().asResource());
                superTypes.get(subType).add(superType);
            });
    }

    /**
     * Returns the TermType that the argument Term
     * has as a constant, and is only based on the Term itself,
     * and therefore not usage. 
     */
    public static TermType getConstantType(Term term) {

        if (term instanceof BlankNodeTerm) {
            return new LUBType(TOP);
        } else if (term instanceof IRITerm) {
            return new LUBType(IRI);
        } else if (term instanceof LiteralTerm) {

            String datatypeStr = ((LiteralTerm) term).getDatatype();
            TermType datatype = datatypeStr != null ? TypeFactory.getType(datatypeStr) : null;
            return datatype == null ? LITERAL : datatype;

        } else if (term instanceof TermList) {

            List<Term> terms = ((TermList) term).asList();
            if (terms.isEmpty()) {
                return new ListType(BOT);
            } else {
                return new NEListType(new LUBType(TOP));
            }

        } else {
            return new LUBType(TOP);
        }
    }

    /**
     * Returns the TermType that the variable Term
     * has as default if no type is given, and is only based on the Term itself,
     * and therefore not usage.
     */
    public static TermType getVariableType(Term term) {
        // The default type of a variable is the same as
        // for a constant term, except that we remove
        // any surrounding LUB. E.g. an IRI variable
        // has default type IRI.
        return removeLUB(getConstantType(term));
    }

    private static TermType removeLUB(TermType constantType) {
        if (constantType instanceof LUBType) {
            return ((LUBType) constantType).getInner();
        } else if (constantType instanceof BasicType) {
            return constantType;
        } else if (constantType instanceof NEListType) {
            return new NEListType(
                    removeLUB(((NEListType) constantType).getInner()));
        } else {
            return new ListType(
                    removeLUB(((ListType) constantType).getInner()));
        } 
    }

    public static boolean isSubTypeOf(BasicType subType, BasicType superType) {
        return subType.equals(superType) || superTypes.get(subType).contains(superType);
    }

    /**
     * Get a term type by its Resource.
     * @param resource the Resource of the term type to get
     * @return the matching termtype, or null if no such termtype
     */
    // TODO: rename to asType since getType could be taken to mean getting the type *of* the resource.
    public static BasicType getType(Resource resource)  {
        return getType(resource.getURI());
    }

    /**
     * Get a term type by its IRI.
     * @param iri the iri of the term type to get
     * @return the matching termtype, or null if no such termtype
     */
    public static BasicType getType(String iri)  {
        return iris.get(iri);
    }

    /**
     * Returns the Top type, the type which is the super type
     * of all other types.
     */
    public static BasicType getTopType() {
        return TOP;
    }

    /**
     * Returns the Bot type, the type which is the subtype
     * of all other types.
     */
    public static BasicType getBotType() {
        return BOT;
    }

}
