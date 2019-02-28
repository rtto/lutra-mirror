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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.SetUtils 

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.ROTTR;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;


public class TypeFactory {
    
    private static Map<String, BasicType> iris;
    private static Map<String, BasicType> names;
    private static Map<BasicType, Set<BasicType>> superTypes;
    private static Map<BasicType, Set<BasicType>> subTypes;
    private static BasicType top;
    private static BasicType bot;
    
    // TODO move - perhaps OTTR.java, or Settings?
    private static final String IRI = "types.owl.ttl";

    static {
        init();
    }
    
    private static void init() {
        InputStream filename = TypeFactory.class.getClassLoader().getResourceAsStream(IRI);
        Model types = ModelFactory.createDefaultModel();
        types.read(filename, null, "TTL");
        Reasoner owlMicro = ReasonerRegistry.getOWLMicroReasoner();
        Model model = ModelFactory.createInfModel(owlMicro, types);

        initNames(model);
        initSuperSubTypes(model);

    }
    
    private static void initNames(Model model) {
        iris = new HashMap<>();
        names = new HashMap<>();
        superTypes = new HashMap<>();
        subTypes = new HashMap<>();
        
        for (BasicType term : getBasicTypes(model)) {                
            String uri = term.getIRI();
            iris.put(uri, term);
            
            String name = term.getName();
            if (names.containsKey(name)) {
                Message msg = Message.error("Error: duplicate name: " + name + ". Conflicts with " + names.get(name));
                MessageHandler.printMessage(msg);
                // TODO log error
            }
            names.put(name, term);
            superTypes.put(term, new HashSet<>());
            subTypes.put(term, new HashSet<>());
        }
        top = getByName("Resource");
        bot = getByName("Bot");
    }

    private static void initSuperSubTypes(Model model) {

        model.listStatements((Resource) null, model.createProperty(ROTTR.subTypeOf), (RDFNode) null)
            .forEachRemaining(stmt -> {
                BasicType subType = iris.get(stmt.getSubject().asResource().getURI());
                BasicType superType = iris.get(stmt.getObject().asResource().getURI());

                superTypes.get(subType).add(superType);
                subTypes.get(superType).add(subType);
            });
    }

    private static Set<BasicType> getBasicTypes(Model model) {
        return model.listResourcesWithProperty(RDF.type, model.createResource(ROTTR.termType))
            .toSet().stream()
            .map(RDFNode::asResource)
            .map(BasicType::new)
            .collect(Collectors.toSet());
    }

    /**
     * Returns the TermType that the argument Term
     * has as a constant, and is only based on the Term itself,
     * and therefore not usage. 
     */
    public static TermType getConstantType(Term term) {

        if (term instanceof BlankNodeTerm) {
            return new LUBType(top);
        } else if (term instanceof IRITerm) {
            return new LUBType(TypeFactory.getByName("IRI"));
        } else if (term instanceof LiteralTerm) {

            String datatypeStr = ((LiteralTerm) term).getDatatype();
            TermType datatype = datatypeStr != null
                ? TypeFactory.getByIRI(datatypeStr)
                : null;
            return datatype == null ? TypeFactory.getByName("Literal") : datatype;

        } else if (term instanceof TermList) {

            List<Term> terms = ((TermList) term).asList();
            if (terms.isEmpty()) {
                return new ListType(bot);
            } else {
                Set<TermType> types = terms.stream()
                    .map(trm -> trm.getType())
                    .collect(Collectors.toSet());
                return new NEListType(getSubtypeLeastCompatible(types));
            }

        } else {
            return new LUBType(top);
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

    /**
     * Returns the subtype-least type S for which every
     * type T in the argument set is compatible with (T compatible S)
     */
    private static TermType getSubtypeLeastCompatible(Set<TermType> types) {

        TermType slc = bot; // slc = [S]ubtype[L]east[C]ompatible
        for (TermType type : types) {
            if (slc.equals(bot)) {
                slc = type;
            } else if (!(type.isCompatibleWith(slc))) {
                Set<TermType> ubs = getCompatibleTypes(slc);
                ubs.retainAll(getCompatibleTypes(type));
                slc = findLeast(ubs);
            } // otherwise we can leave slc untouched as type is compatible with slc
        }
        return slc;
    }

    private static Set<TermType> getSuperTypes(TermType type) {
        Set<TermType> sups;
        if (type instanceof BasicType) {
            sups = superTypes.get((BasicType) type)
                .stream()
                .map(tp -> (TermType) tp)
                .collect(Collectors.toSet());
        } else if (type instanceof NEListType) {
            sups = getSuperTypes(((NEListType) type).getInner())
                .stream()
                .flatMap(tp -> Stream.of(new NEListType(tp), new ListType(tp)))
                .collect(Collectors.toSet());
        } else if (type instanceof ListType) {
            sups = getSuperTypes(((ListType) type).getInner())
                .stream()
                .map(tp -> new ListType(tp))
                .collect(Collectors.toSet());
        } else if (type instanceof LUBType) {
            sups = getSuperTypes(((LUBType) type).getInner());
        } else { // Should never happen
            sups = Collections.singleton(bot);
        }
        sups.add(type);
        return sups;
    }

    private static Set<TermType> getSubTypes(TermType type) {
        return null; // TODO
    }

    private static Set<TermType> getCompatibleTypes(TermType type) {
        if (type instanceof LUBType) {
            TermType inner = ((LUBType) type).getInner();
            return SetUtils.union(getSubTypes(inner), getSuperTypes(inner);
        } else { // Should never happen
            return getSuperTypes(type);
        }
    }

    /**
     * Returns the least type (wrt. subtyping) for the input set of types.
     */
    private static TermType findLeast(Set<TermType> types) {
        TermType least = top;
        for (TermType type : types) {
            if (type.isSubTypeOf(least)) {
                least = type;
            } 
        }
        return least;
    }
    
    protected static String normaliseName(String name) {
        return name.toLowerCase(Locale.ENGLISH); 
    }
    
    public static boolean isSubTypeOf(BasicType subType, BasicType superType) {
        return subType.equals(superType) || superTypes.get(subType).contains(superType);
    }
    
    /**
     * Get a term type by its localname, ignoring casing.
     * @param name the localname of the term type to get
     * @return the mathcing termtype, or null if no such termtype
     */
    public static BasicType getByName(String name) {
        return names.get(normaliseName(name));
    }
    
    /**
     * Get a term type by its IRI.
     * @param iri the iri of the term type to get
     * @return the matching termtype, or null if no such termtype
     */
    public static BasicType getByIRI(String iri)  {
        return iris.get(iri);
    }

    /**
     * Returns the Top type, the type which is the super type
     * of all other types.
     */
    public static BasicType getTopType() {
        return top;
    }

    /**
     * Returns the Bot type, the type which is the subtype
     * of all other types.
     */
    public static BasicType getBotType() {
        return bot;
    }
}
