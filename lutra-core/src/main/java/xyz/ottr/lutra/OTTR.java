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

import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.model.types.TypeFactory;

public class OTTR  {

    private static final String ns = "http://ns.ottr.xyz/0.4/";
    
    public static final String prefix = "ottr";
    public static final String namespace = ns;
    
    public static class BaseURI {
        public static final String Triple = ns + "Triple";
        public static final String NullableTriple = ns + "NullableTriple";
    }

    public static class TypeURI {
        public static final String Type = ns + "Type";
        public static final String subTypeOf = ns + "subTypeOf";
        
        public static final String NEList = ns + "NEList";
        public static final String LUB = ns + "LUB";
        

        public static final String IRI = ns + "IRI";
        public static final String Bot = ns + "Bot";
    }

    public static class BaseTemplate {
        public static final TemplateSignature Triple;
        public static final TemplateSignature NullableTriple;

        static {
            Term sub = new BlankNodeTerm("_:s");
            sub.setType(TypeFactory.getType(OTTR.TypeURI.IRI));
            Term pred = new BlankNodeTerm("_:p");
            pred.setType(TypeFactory.getType(OTTR.TypeURI.IRI));
            Term obj = new BlankNodeTerm("_:o");
            obj.setType(TypeFactory.getVariableType(obj));

            Set<Term> nonBlanks = new HashSet<>();
            nonBlanks.add(pred);
            Triple = new TemplateSignature(
                OTTR.BaseURI.Triple,
                new ParameterList(new TermList(sub, pred, obj), nonBlanks, null, null),
                true);

            Set<Term> optionals = new HashSet<>();
            optionals.add(sub);
            optionals.add(pred);
            optionals.add(obj);
            NullableTriple = new TemplateSignature(
                OTTR.BaseURI.NullableTriple,
                new ParameterList(new TermList(sub, pred, obj), nonBlanks, optionals, null),
                true);
        }
    }
    
    public static class Files {
        public static final String StdTypes = "types.owl.ttl";
    }
}
