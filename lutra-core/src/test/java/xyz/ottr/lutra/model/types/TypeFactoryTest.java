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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

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

    private LiteralTerm typedLiteral(String val, String type) {
        return new LiteralTerm(val, TypeFactory.getByName(type).getIRI());
    }

    @Test
    public void simpleTypeSetting() {

        assertEquals(
            new IRITerm("example.com/v").getType(),
            new LUBType(TypeFactory.getByName("IRI")));
        assertEquals(
            new BlankNodeTerm().getType(),
            new LUBType(TypeFactory.getTopType()));
        assertEquals(
            new LiteralTerm("test").getType(),
            TypeFactory.getByName("Literal"));
        assertEquals(
            typedLiteral("1", "integer").getType(),
            TypeFactory.getByName("integer"));
        assertEquals(
            new LiteralTerm("val", "example.com/mytype").getType(),
            TypeFactory.getByName("Literal"));
    }

    @Test
    public void listTypeSetting() {

        assertEquals(
            new TermList(new IRITerm("example.com/v1"), new IRITerm("example.com/v2")).getType(),
            new NEListType(new LUBType(TypeFactory.getByName("IRI"))));
        assertEquals(
            new TermList(new IRITerm("example.com/v1"), new BlankNodeTerm()).getType(),
            new NEListType(TypeFactory.getTopType()));
        assertEquals(
            new TermList(new IRITerm("example.com/v1"), new LiteralTerm("v2")).getType(),
            new NEListType(TypeFactory.getTopType()));
        assertEquals(
            new TermList(
                typedLiteral("1", "integer"),
                typedLiteral("2", "integer")).getType(),
            new NEListType(TypeFactory.getByName("integer")));
        assertEquals(
            new TermList(
                typedLiteral("1", "integer"),
                typedLiteral("val", "string")).getType(),
            new NEListType(TypeFactory.getByName("literal")));
        assertEquals(
            new TermList(
                new TermList(
                    typedLiteral("1", "integer"),
                    typedLiteral("2", "integer")),
                new TermList(
                    typedLiteral("3", "integer"),
                    typedLiteral("4", "integer"))).getType(),
            new NEListType(new NEListType(TypeFactory.getByName("integer"))));
        assertEquals(
            new TermList(
                new TermList(
                    typedLiteral("1", "integer"),
                    typedLiteral("2", "integer")),
                new TermList(
                    typedLiteral("3", "integer"),
                    typedLiteral("four", "string"))).getType(),
            new NEListType(new NEListType(TypeFactory.getByName("literal"))));
        assertEquals(
            new TermList(
                new TermList(
                    typedLiteral("1", "integer"),
                    typedLiteral("2", "integer")),
                typedLiteral("3", "integer")).getType(),
            new NEListType(TypeFactory.getTopType()));
        assertEquals(
            new TermList(
                new TermList(
                    typedLiteral("1", "integer"),
                    typedLiteral("2", "integer")),
                new TermList()).getType(),
            new NEListType(new ListType(TypeFactory.getByName("integer"))));
    }

    @Test
    public void templateVariableTypes() {

        Term var1 = new IRITerm("example.org/var1");
        var1.setType(TypeFactory.getByName("class"));
        Term var2 = typedLiteral("1", "integer");
        Term var3 = new BlankNodeTerm("_:b");
        var3.setType(new ListType(TypeFactory.getByName("string")));

        Term var1b1 = new IRITerm("example.org/var1");
        Term var1b2 = new IRITerm("example.org/var1");
        Term var2b = typedLiteral("1", "integer");
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

        Term var1 = new IRITerm("example.org/var1");
        var1.setType(TypeFactory.getByName("class"));
        Term var21 = new IRITerm("example.org/var21");
        var21.setType(TypeFactory.getByName("class"));
        Term var22 = new IRITerm("example.org/var22");
        var22.setType(TypeFactory.getByName("class"));
        Term var3 = new BlankNodeTerm("_:b");
        var3.setType(new ListType(TypeFactory.getByName("string")));

        Term var1b1 = new IRITerm("example.org/var1");
        Term var1b2 = new IRITerm("example.org/var1");
        Term var21b = new IRITerm("example.org/var21");
        Term var22b = new IRITerm("example.org/var22");
        Term var3b = new BlankNodeTerm("_:b");

        Term lst1 = new TermList(var21b, var22b);
        Term lst2 = new TermList(new TermList(
                var3b, new TermList(typedLiteral("val1", "string"))));

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
        assertEquals(lst1.getType(), new NEListType(TypeFactory.getByName("class")));
        assertEquals(lst2.getType(), new NEListType(new NEListType(new ListType(TypeFactory.getByName("string")))));
    }
}
