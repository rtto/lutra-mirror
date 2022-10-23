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
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public enum TypeRegistry {
    ;

    private static Map<String, BasicType> iris;
    private static Map<BasicType, Set<BasicType>> superTypes;
    
    static {
        init(OTTR.Files.StdTypes);
    }

    // TODO: Move these elsewhere?
    public static final BasicType TOP = asType(OTTR.TypeURI.Top);
    public static final BasicType BOT = asType(OTTR.TypeURI.Bot);
    public static final BasicType IRI = asType(OTTR.TypeURI.IRI);
    public static final BasicType LITERAL = asType(OTTR.TypeURI.Literal);
    public static final BasicType INTEGER = asType(OTTR.TypeURI.Integer);

    public static final ComplexType LUB_TOP = new LUBType(TOP);
    public static final ComplexType LUB_IRI = new LUBType(IRI);


    public static Result<Type> get(String uri) {
        var type = asType(uri);

        return type != null
            ? Result.of(type)
            : Result.error("Unrecognized type: " + RDFNodeWriter.toString(uri) + ". No such type registered.");
    }

    public static boolean isSubTypeOf(BasicType subType, BasicType superType) {
        return subType.equals(superType) || superTypes.get(subType).contains(superType);
    }

    private static void init(String modelFile) {
        InputStream filename = TypeRegistry.class.getClassLoader().getResourceAsStream(modelFile);
        Model types = ModelFactory.createDefaultModel();
        types.read(filename, null, FileUtils.guessLang(modelFile, "TTL"));
        Reasoner owlMicro = ReasonerRegistry.getOWLMicroReasoner();
        Model model = ModelFactory.createInfModel(owlMicro, types);

        initTypes(model);
        initSuperTypes(model);
    }

    private static void initTypes(Model model) {
        iris = model.listResourcesWithProperty(RDF.type, model.createResource(OTTR.TypeURI.Type))
            .mapWith(Resource::getURI)
            .mapWith(BasicType::new)
            .toSet()
            .stream()
            .collect(Collectors.toMap(BasicType::getIri, Function.identity()));
    }

    private static void initSuperTypes(Model model) {

        // prepare the map of types -> set of supertypes
        superTypes = iris.values().stream().collect(
                Collectors.toMap(Function.identity(), _x -> new HashSet<>()));
        
        Property subTypeOf = model.createProperty(OTTR.TypeURI.subTypeOf);
        model.listStatements(null, subTypeOf, (RDFNode) null)
            .forEachRemaining(stmt ->  {
                BasicType subType = asType(stmt.getSubject().asResource());
                BasicType superType = asType(stmt.getObject().asResource());
                superTypes.get(subType).add(superType);
            });
    }

    // TODO make these private or package-private, instead use get()

    public static BasicType asType(Resource resource)  {
        return asType(resource.getURI());
    }

    public static BasicType asType(String iri)  {
        return iris.get(iri);
    }

}
