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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.Term;

public class TypeRegistryTest {

    private LiteralTerm typedLiteral(String val, Resource type) {
        return LiteralTerm.createTypedLiteral(val, TypeRegistry.asType(type).getIri());
    }

    @Test
    public void simpleTypeSetting1() {
        assertEquals(
                new IRITerm("example.com/v").getType(),
                new LUBType(TypeRegistry.asType(OTTR.TypeURI.IRI)));
    }

    @Test
    public void simpleTypeSetting2() {
        assertEquals(
                new BlankNodeTerm().getType(),
                new LUBType(TypeRegistry.TOP));
    }

    @Test
    public void simpleTypeSetting3() {
        assertEquals(
                LiteralTerm.createPlainLiteral("test").getType(),
                TypeRegistry.asType(XSD.xstring));
    }

    @Test
    public void simpleTypeSetting4() {
        assertEquals(
                typedLiteral("1", XSD.integer).getType(),
                TypeRegistry.asType(XSD.integer));
    }

    @Test
    public void simpleTypeSetting5() {
        assertEquals(
                LiteralTerm.createTypedLiteral("val", "example.com/mytype").getType(),
                TypeRegistry.asType(RDFS.Literal));
    }

    @Test
    public void listTypeSetting1() {
        assertEquals(
            new ListTerm(new IRITerm("example.com/v1"), new IRITerm("example.com/v2")).getType(),
            new NEListType(new LUBType(TypeRegistry.TOP)));
    }
    
    @Test
    public void listTypeSetting2() {
        assertEquals(
                new ListTerm(new IRITerm("example.com/v1"), new BlankNodeTerm()).getType(),
                new NEListType(new LUBType(TypeRegistry.TOP)));
    }
    
    @Test
    public void listTypeSetting3() {
        assertEquals(
                new ListTerm(new IRITerm("example.com/v1"), LiteralTerm.createPlainLiteral("v2")).getType(),
                new NEListType(new LUBType(TypeRegistry.TOP)));
    }

    @Test
    public void templateVariableTypes() {

        Term var1 = new IRITerm("example.org/var1");
        var1.setType(TypeRegistry.asType(OWL.Class));
        Term var2 = typedLiteral("1", XSD.integer);
        Term var3 = new BlankNodeTerm("_:b");
        var3.setType(new ListType(TypeRegistry.asType(XSD.xstring)));

        Term var1b1 = new IRITerm("example.org/var1");
        Term var1b2 = new IRITerm("example.org/var1");
        Term var2b = typedLiteral("1", XSD.integer);
        Term var3b = new BlankNodeTerm("_:b");

        Instance i1 = Instance.builder()
            .iri("triple")
            .argument(Argument.builder().term(var1b1).build())
            .argument(Argument.builder().term(new IRITerm("example.org/hasValue")).build())
            .argument(Argument.builder().term(var2b).build())
            .build();

        Instance i2 = Instance.builder()
            .iri("triple")
            .argument(Argument.builder().term(var1b2).build())
            .argument(Argument.builder().term(new IRITerm("example.org/hasCommentList")).build())
            .argument(Argument.builder().term(var3b).build())
            .build();

        Template tpl = Template.builder()
            .iri("t1")
            .parameter(Parameter.builder().term(var1).build())
            .parameter(Parameter.builder().term(var2).build())
            .parameter(Parameter.builder().term(var3).build())
            .instance(i1)
            .instance(i2)
            .build();

        assertEquals(var1.getType(), var1b1.getType());
        assertEquals(var1.getType(), var1b2.getType());
        assertEquals(var2.getType(), var2b.getType());
        assertEquals(var3.getType(), var3b.getType());
    }

    @Test
    public void templateNestedListVariableTypes() {

        // Checks that nested variables inside lists in
        // instances in template bodies are properly set

        Term var1 = new IRITerm("example.org/var1");
        var1.setType(TypeRegistry.asType(OWL.Class));
        Term var21 = new IRITerm("example.org/var21");
        var21.setType(TypeRegistry.asType(OWL.Class));
        Term var22 = new IRITerm("example.org/var22");
        var22.setType(TypeRegistry.asType(OWL.Class));
        Term var3 = new BlankNodeTerm("_:b");
        var3.setType(new ListType(TypeRegistry.asType(XSD.xstring)));

        Term var1b1 = new IRITerm("example.org/var1");
        Term var1b2 = new IRITerm("example.org/var1");
        Term var21b = new IRITerm("example.org/var21");
        Term var22b = new IRITerm("example.org/var22");
        Term var3b = new BlankNodeTerm("_:b");

        Term lst1 = new ListTerm(var21b, var22b);
        Term lst2 = new ListTerm(new ListTerm(
                var3b, new ListTerm(typedLiteral("val1", XSD.xstring))));

        Instance i1 = Instance.builder()
            .iri("triple")
            .argument(Argument.builder().term(var1b1).build())
            .argument(Argument.builder().term(new IRITerm("example.org/hasValue")).build())
            .argument(Argument.builder().term(lst1).build())
            .build();

        Instance i2 = Instance.builder()
            .iri("triple")
            .argument(Argument.builder().term(var1b2).build())
            .argument(Argument.builder().term(new IRITerm("example.org/hasCommentList")).build())
            .argument(Argument.builder().term(lst2).build())
            .build();

        Template tpl = Template.builder()
            .iri("t1")
            .parameter(Parameter.builder().term(var1).build())
            .parameter(Parameter.builder().term(var21).build())
            .parameter(Parameter.builder().term(var22).build())
            .parameter(Parameter.builder().term(var3).build())
            .instance(i1)
            .instance(i2)
            .build();

        assertEquals(var1.getType(), var1b1.getType());
        assertEquals(var1.getType(), var1b2.getType());
        assertEquals(var21.getType(), var21b.getType());
        assertEquals(var22.getType(), var22b.getType());
        assertEquals(var3.getType(), var3b.getType());
    }
}
