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

import static org.junit.Assert.assertEquals;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;

public class TypeFactoryTest {

    private LiteralTerm typedLiteral(String val, Resource type) {
        return new LiteralTerm(val, TypeFactory.getByIRI(type).getIRI());
    }

    @Test
    public void simpleTypeSetting1() {
        assertEquals(
                new IRITerm("example.com/v").getType(),
                new LUBType(TypeFactory.getByIRI(OTTR.Types.IRI)));
    }

    @Test
    public void simpleTypeSetting2() {
        assertEquals(
                new BlankNodeTerm().getType(),
                new LUBType(TypeFactory.getTopType()));
    }

    @Test
    public void simpleTypeSetting3() {
        assertEquals(
                new LiteralTerm("test").getType(),
                TypeFactory.getByIRI(RDFS.Literal));
    }

    @Test
    public void simpleTypeSetting4() {
        assertEquals(
                typedLiteral("1", XSD.integer).getType(),
                TypeFactory.getByIRI(XSD.integer));
    }

    @Test
    public void simpleTypeSetting5() {
        assertEquals(
                new LiteralTerm("val", "example.com/mytype").getType(),
                TypeFactory.getByIRI(RDFS.Literal));
    }

    @Test
    public void listTypeSetting() {

        assertEquals(
                new TermList(new IRITerm("example.com/v1"), new IRITerm("example.com/v2")).getType(),
                //new NEListType(new LUBType(TypeFactory.getByName("IRI"))));
                new NEListType(new LUBType(TypeFactory.getTopType())));
        assertEquals(
                new TermList(new IRITerm("example.com/v1"), new BlankNodeTerm()).getType(),
                new NEListType(new LUBType(TypeFactory.getTopType())));
        assertEquals(
                new TermList(new IRITerm("example.com/v1"), new LiteralTerm("v2")).getType(),
                new NEListType(new LUBType(TypeFactory.getTopType())));
    }

    @Test
    public void templateVariableTypes() {

        Term var1 = new IRITerm("example.org/var1");
        var1.setType(TypeFactory.getByIRI(OWL.Class));
        Term var2 = typedLiteral("1", XSD.integer);
        Term var3 = new BlankNodeTerm("_:b");
        var3.setType(new ListType(TypeFactory.getByIRI(XSD.xstring)));

        Term var1b1 = new IRITerm("example.org/var1");
        Term var1b2 = new IRITerm("example.org/var1");
        Term var2b = typedLiteral("1", XSD.integer);
        Term var3b = new BlankNodeTerm("_:b");

        Instance i1 = new Instance(
                "triple",
                new ArgumentList(
                        var1b1,
                        new IRITerm("example.org/hasValue"),
                        var2b));
        Instance i2 = new Instance(
                "triple",
                new ArgumentList(
                        var1b2,
                        new IRITerm("example.org/hasCommentList"),
                        var3b));

        Template tmp = new Template(
                "t1",
                new ParameterList(var1, var2, var3),
                Stream.of(i1, i2).collect(Collectors.toSet()));

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
        var1.setType(TypeFactory.getByIRI(OWL.Class));
        Term var21 = new IRITerm("example.org/var21");
        var21.setType(TypeFactory.getByIRI(OWL.Class));
        Term var22 = new IRITerm("example.org/var22");
        var22.setType(TypeFactory.getByIRI(OWL.Class));
        Term var3 = new BlankNodeTerm("_:b");
        var3.setType(new ListType(TypeFactory.getByIRI(XSD.xstring)));

        Term var1b1 = new IRITerm("example.org/var1");
        Term var1b2 = new IRITerm("example.org/var1");
        Term var21b = new IRITerm("example.org/var21");
        Term var22b = new IRITerm("example.org/var22");
        Term var3b = new BlankNodeTerm("_:b");

        Term lst1 = new TermList(var21b, var22b);
        Term lst2 = new TermList(new TermList(
                var3b, new TermList(typedLiteral("val1", XSD.xstring))));

        Instance i1 = new Instance(
                "triple",
                new ArgumentList(
                        var1b1,
                        new IRITerm("example.org/hasValue"),
                        lst1));
        Instance i2 = new Instance(
                "triple",
                new ArgumentList(
                        var1b2,
                        new IRITerm("example.org/hasCommentList"),
                        lst2));

        Template tmp = new Template(
                "t1",
                new ParameterList(var1, var21, var22, var3),
                Stream.of(i1, i2).collect(Collectors.toSet()));

        assertEquals(var1.getType(), var1b1.getType());
        assertEquals(var1.getType(), var1b2.getType());
        assertEquals(var21.getType(), var21b.getType());
        assertEquals(var22.getType(), var22b.getType());
        assertEquals(var3.getType(), var3b.getType());
    }
}
