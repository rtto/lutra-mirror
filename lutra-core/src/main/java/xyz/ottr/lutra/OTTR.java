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

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;
import xyz.ottr.lutra.model.types.TypeRegistry;

public enum OTTR  {
    ;

    private static final String ns = "http://ns.ottr.xyz/0.4/";
    
    public static final String prefix = "ottr";
    public static final String namespace = ns;
    
    public enum BaseURI {
        ;
        public static final String Triple = ns + "Triple";
        public static final String NullableTriple = ns + "NullableTriple";
    }

    public enum TypeURI {
        ;
        public static final String Type = ns + "Type";
        public static final String subTypeOf = ns + "subTypeOf";
        
        public static final String NEList = ns + "NEList";
        public static final String List = RDF.List.getURI();
        public static final String LUB = ns + "LUB";

        public static final String Top = RDFS.Resource.getURI();
        public static final String Literal = RDFS.Literal.getURI();
        public static final String IRI = ns + "IRI";
        public static final String Bot = ns + "Bot";
    }

    public enum BaseTemplate {
        ;
        public static final Signature Triple;
        public static final Signature NullableTriple;

        static {
            Term sub = new BlankNodeTerm("_:s");
            sub.setType(TypeRegistry.getType(OTTR.TypeURI.IRI));
            Term pred = new BlankNodeTerm("_:p");
            pred.setType(TypeRegistry.getType(OTTR.TypeURI.IRI));
            Term obj = new BlankNodeTerm("_:o");
            obj.setType(obj.getVariableType());

            Set<Term> nonBlanks = new HashSet<>();
            nonBlanks.add(pred);
            Triple = Template.createBaseTemplate(
                OTTR.BaseURI.Triple,
                new ParameterList(new TermList(sub, pred, obj), nonBlanks, null, null));

            Set<Term> optionals = new HashSet<>();
            optionals.add(sub);
            optionals.add(pred);
            optionals.add(obj);
            NullableTriple = Template.createBaseTemplate(
                OTTR.BaseURI.NullableTriple,
                new ParameterList(new TermList(sub, pred, obj), nonBlanks, optionals, null));
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
        return map;
    }
}
