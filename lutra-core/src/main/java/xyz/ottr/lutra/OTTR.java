package xyz.ottr.lutra;

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

import java.util.Set;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.TypeRegistry;

public enum OTTR  {
    ;

    private static final String ns = "http://ns.ottr.xyz/0.4/";

    public static final String prefix = "ottr";
    public static final String namespace = ns;

    public static final String ns_example_arg = "http://example.ottr.xyz/argument/";

    public static final String ns_library = "http://tpl.ottr.xyz";
    public static final String ns_library_package_prefix = "p/";
    public static final String ns_library_package = ns_library + "/" + ns_library_package_prefix;

    public static final String none = ns + "none";
    
    public enum BaseURI {
        ;
        public static final String Triple = ns + "Triple";
        public static final String NullableTriple = ns + "NullableTriple";

        public static final Set<String> ALL = Set.of(Triple, NullableTriple);
    }

    public enum TypeURI {
        ;
        public static final String Type = ns + "Type";
        public static final String subTypeOf = ns + "subTypeOf";
        
        public static final String NEList = ns + "NEList";
        public static final String List = RDF.uri + "List";
        public static final String LUB = ns + "LUB";

        public static final String Top = RDFS.Resource.getURI();
        public static final String Literal = RDFS.Literal.getURI();
        public static final String IRI = ns + "IRI";
        public static final String Bot = ns + "Bot";

    }

    @SuppressWarnings("PMD.UnnecessaryFullyQualifiedName")
    public enum BaseTemplate {
        ;
        public static final Signature Triple;
        public static final Signature NullableTriple;

        public static final Set<Signature> ALL;

        static {
            Term sub = new BlankNodeTerm("_:s");
            sub.setType(TypeRegistry.IRI);
            Term pred = new BlankNodeTerm("_:p");
            pred.setType(TypeRegistry.IRI);
            Term obj = new BlankNodeTerm("_:o");
            obj.setType(TypeRegistry.TOP);

            Triple = xyz.ottr.lutra.model.BaseTemplate.builder()
                .iri(OTTR.BaseURI.Triple)
                .parameter(Parameter.builder().term(sub.shallowClone()).build())
                .parameter(Parameter.builder().term(pred.shallowClone()).nonBlank(true).build())
                .parameter(Parameter.builder().term(obj.shallowClone()).build())
                .build();

            NullableTriple = xyz.ottr.lutra.model.BaseTemplate.builder()
                .iri(OTTR.BaseURI.NullableTriple)
                .parameter(Parameter.builder().term(sub.shallowClone()).optional(true).build())
                .parameter(Parameter.builder().term(pred.shallowClone()).optional(true).nonBlank(true).build())
                .parameter(Parameter.builder().term(obj.shallowClone()).optional(true).build())
                .build();

            ALL = Set.of(Triple, NullableTriple);
        }
    }
    
    public enum Files {
        ;
        public static final String StdTypes = "types.owl.ttl";
    }

    public static PrefixMapping getDefaultPrefixes() {
        PrefixMapping map = PrefixMapping.Factory.create();
        map.setNsPrefix("xsd", XSD.getURI());
        map.setNsPrefix("rdf", RDF.getURI());
        map.setNsPrefix("rdfs", RDFS.getURI());
        map.setNsPrefix("owl", OWL.getURI());
        map.setNsPrefix(OTTR.prefix, OTTR.ns);
        // TODO consider: map.lock();
        return map;
    }

    public static PrefixMapping getStandardLibraryPrefixes() {
        PrefixMapping map = PrefixMapping.Factory.create();
        map.withDefaultMappings(getDefaultPrefixes());
        map.setNsPrefix("o-rdf", "http://tpl.ottr.xyz/rdf/0.1/");
        map.setNsPrefix("o-rdfs", "http://tpl.ottr.xyz/rdfs/0.1/");
        map.setNsPrefix("o-owl-ax", "http://tpl.ottr.xyz/owl/axiom/0.1/");
        map.setNsPrefix("o-owl-re", "http://tpl.ottr.xyz/owl/restriction/0.1/");
        map.setNsPrefix("o-owl-ut", "http://tpl.ottr.xyz/owl/util/0.1/");
        map.setNsPrefix("o-pizza", "http://tpl.ottr.xyz/pizza/0.1/");
        map.lock();
        return map;
    }
}
