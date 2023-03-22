package xyz.ottr.lutra.store.checks;

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

import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.ObjectTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.system.Message;

public class CheckLibraryTest {

    private static final Logger log = LoggerFactory.getLogger(CheckLibraryTest.class);

    private StandardTemplateStore initStore() {

        StandardTemplateStore store = new StandardTemplateStore(null);
        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("base2")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("x"),
                    ObjectTerm.var("y")))
                .build());

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("base3")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("x"),
                    ObjectTerm.var("y"),
                    ObjectTerm.var("z")))
                .build());
        return store;
    }

    private void check(StandardQueryEngine engine, int numErrors, Message.Severity severity) {

        List<Message> msgs = CheckLibrary.allChecks
            .stream()
            .flatMap(c -> c.check(engine))
            .filter(msg -> msg.getSeverity().isGreaterEqualThan(severity))
            .collect(Collectors.toList());

        String assStr = "Expected " + numErrors + " messages of severity " + severity + " or higher"
             + ", but got " + msgs.size() + " messages: " + msgs;

        assertEquals(numErrors, msgs.size(), assStr);
    }

    @Test
    public void variableNotUsedWarning() {

        StandardTemplateStore store = initStore();
        store.addTemplate(
            Template.builder()
                .iri("test")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base2")
                    .arguments(Argument.listOf(
                            ObjectTerm.var("a"),
                            ObjectTerm.cons(1)))
                    .build())
            .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.WARNING);
    }

    @Test
    public void variableUsedInsideList() {

        StandardTemplateStore store = initStore();
        store.addTemplate(
            Template.builder().iri("test")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder().iri("base2")
                        .arguments(Argument.listOf(
                            new ListTerm(ObjectTerm.var("a")),
                                new ListTerm(ObjectTerm.cons(1),
                                    new ListTerm(ObjectTerm.var("b")))))
                    .build())
            .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 0, Message.Severity.WARNING);
    }

    @Disabled("This test is now performed during parsing.")
    @Test
    public void variableDefinedTwiceError() {

        StandardTemplateStore store = initStore();
        store.addTemplate(
            Template.builder()
                .iri("test")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("a")))
                .instance(Instance.builder()
                    .iri("base2")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("a"),
                        ObjectTerm.cons(1)))
                    .build())
            .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void incorrectNumberOfArgumentsError() {

        StandardTemplateStore store = initStore();
        store.addTemplate(
            Template.builder().iri("test")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base3")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
            .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void nonNonBlankUsedAsNonBlankError() {

        StandardTemplateStore store = new StandardTemplateStore(null);
        store.addTemplate(
            Template.builder()
                .iri("base")
                .parameter(Parameter.builder().term(ObjectTerm.var("x")).nonBlank(true).build())
                .parameter(Parameter.builder().term(ObjectTerm.var("y")).build())
                .isEmptyPattern(true)
                .build());

        store.addTemplate(
            Template.builder()
                .iri("test")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void cyclicDependencyError() {

        StandardTemplateStore store = initStore();

        store.addTemplate(
            Template.builder()
                .iri("test1")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base2")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .instance(Instance.builder()
                    .iri("test3")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("b"),
                        ObjectTerm.var("a")))
                    .build())
                .build());

        store.addTemplate(
            Template.builder().iri("test2")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("test1")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .build());

        store.addTemplate(
            Template.builder()
                .iri("test3")
                .parameters(Parameter.listOf(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("test2")
                    .arguments(Argument.listOf(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 3, Message.Severity.ERROR); // One cycle for each testN
    }

    @Test
    public void correctConsistentTypeUsageTest() {
        StandardTemplateStore store = new StandardTemplateStore(null);

        Term varBase1 = new IRITerm("ex.com/var1");
        varBase1.setType(TypeRegistry.IRI);
        Term varBase2 = new IRITerm("ex.com/var2");
        varBase2.setType(TypeRegistry.asType(OWL.ObjectProperty));
        Term varBase3 = LiteralTerm.createTypedLiteral("7", TypeRegistry.asType(XSD.integer).getIri());

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("hasInt")
                .parameters(Parameter.listOf(varBase1, varBase2, varBase3))
                .build());

        Term varC1 = new IRITerm("ex.com/iri");
        varC1.setType(TypeRegistry.IRI);
        Term varC2 = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        Term varC1b = new IRITerm("ex.com/iri");
        Term varC2b = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        Term constC1 = new IRITerm("ex.com/nicepropiri");
        Term constC2 = new IRITerm("ex.com/niceonlyprop");

        store.addTemplate(
            Template.builder()
                .iri("testCorrect")
                .parameters(Parameter.listOf(varC1, varC2))
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.listOf(varC1b, constC1, varC2b))
                    .build())
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.listOf(constC1, constC2, varC2b))
                    .build()
                ).build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 0, Message.Severity.WARNING);
    }

    @Test
    public void inconsistentTypeUsageTest() {

        // Using a constant as both Class and ObjectProperty
        StandardTemplateStore store = new StandardTemplateStore(null);

        Term classVar = new IRITerm("ex.com/classVar");
        classVar.setType(TypeRegistry.asType(OWL.Class));
        Term objpropVar = new IRITerm("ex.com/objpropVar");
        objpropVar.setType(TypeRegistry.asType(OWL.ObjectProperty));
        Term intVar = LiteralTerm.createTypedLiteral("7", TypeRegistry.asType(XSD.integer).getIri());

        store.addBaseTemplate(
                BaseTemplate.builder()
                .iri("hasInt")
                .parameters(Parameter.listOf(classVar, objpropVar, intVar))
                .build());

        Term classVar2 = new IRITerm("ex.com/class");
        classVar2.setType(TypeRegistry.asType(OWL.Class));
        Term intVar2 = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        Term classVar2b = new IRITerm("ex.com/class");
        Term intVar21b = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());
        Term intVar22b = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        Term propClass1 = new IRITerm("ex.com/nicepropclass");
        Term propClass2 = new IRITerm("ex.com/nicepropclass");
        Term prop = new IRITerm("ex.com/niceonlyprop");

        store.addTemplate(
            Template.builder()
                .iri("testIncorrect")
                .parameters(Parameter.listOf(classVar2, intVar2))
                .instance(Instance.builder()
                        .iri("hasInt")
                        .arguments(Argument.listOf(classVar2b, propClass1, intVar21b))
                        .build())
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.listOf(propClass2, prop, intVar22b))
                    .build())
            .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void incorrectTypeUsage() {

        // Using a variable with type IRI to a parameter with type Class
        StandardTemplateStore store = new StandardTemplateStore(null);

        Term varBase1 = new IRITerm("ex.com/var1");
        varBase1.setType(TypeRegistry.asType(OWL.Class));
        Term varBase2 = new IRITerm("ex.com/var2");
        varBase2.setType(TypeRegistry.asType(OWL.ObjectProperty));
        Term varBase3 = LiteralTerm.createTypedLiteral("7", TypeRegistry.asType(XSD.integer).getIri());

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("hasInt")
                .parameters(Parameter.listOf(varBase1, varBase2, varBase3))
                .build());

        Term var1 = new IRITerm("ex.com/iri");
        var1.setType(TypeRegistry.IRI);
        Term var2 = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        Term var1b = new IRITerm("ex.com/iri");
        Term var2b = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        Term cons1 = new IRITerm("ex.com/prop1");

        store.addTemplate(
            Template.builder()
                .iri("testIncorrect")
                .parameters(Parameter.listOf(var1, var2))
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.listOf(var1b, cons1, var2b))
                    .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void correctListTypeUsage() {

        // Using a variable with type IRI to a parameter with type Class
        StandardTemplateStore store = new StandardTemplateStore(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(TypeRegistry.asType(OWL.Class)));

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("areClasses")
                .parameters(Parameter.listOf(varBase))
                .build());

        Term var = new BlankNodeTerm("_:class");
        var.setType(TypeRegistry.asType(OWL.Class));

        Term cons = new BlankNodeTerm("_:b");

        store.addTemplate(
            Template.builder()
                .iri("testCorrect1")
                .parameters(Parameter.listOf(var))
                .instance(Instance.builder()
                    .iri("areClasses")
                    .arguments(Argument.listOf(new ListTerm(cons, var)))
                    .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 0, Message.Severity.ERROR);
    }

    @Test
    public void incorrectListTypeUsage() {

        // Using a list of a variable of type Class and a an integer as argument
        // to a parameter of type NEList<Class>
        StandardTemplateStore store = new StandardTemplateStore(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(TypeRegistry.asType(OWL.Class)));

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("areClasses")
                .parameters(Parameter.listOf(varBase))
                .build());

        Term varClass = new BlankNodeTerm("_:class");
        varClass.setType(TypeRegistry.asType(OWL.Class));

        Term one = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        store.addTemplate(
            Template.builder().iri("testCorrect1")
                .parameters(Parameter.listOf(varClass))
                .instance(Instance.builder()
                    .iri("areClasses")
                    .arguments(Argument.listOf(new ListTerm(one, varClass)))
                    .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void incorrectDeepListTypeUsage() {

        StandardTemplateStore store = new StandardTemplateStore(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(new NEListType(TypeRegistry.asType(OWL.Class))));

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("deepLists")
                .parameters(Parameter.listOf(varBase))
                .build());

        Term varClass = new BlankNodeTerm("_:class");
        varClass.setType(TypeRegistry.asType(OWL.Class));

        Term one = LiteralTerm.createTypedLiteral("1", TypeRegistry.asType(XSD.integer).getIri());

        store.addTemplate(
            Template.builder().iri("testCorrect1")
                .parameters(Parameter.listOf(varClass))
                .instance(Instance.builder()
                    .iri("areClasses")
                    .arguments(Argument.listOf(new ListTerm(
                                new ListTerm(new BlankNodeTerm(), varClass), // (_:a ?var) OK
                                new ListTerm(one, varClass))))              // (1 ?var)   ERR
                    .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);

        check(engine, 1, Message.Severity.ERROR);
    }

    @Test
    public void correctDeepListTypeUsage() {

        StandardTemplateStore store = new StandardTemplateStore(null);

        Term varBase = new BlankNodeTerm("_:iriList");
        varBase.setType(new NEListType(TypeRegistry.IRI));

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("baseNL")
                .parameters(Parameter.listOf(varBase))
                .build());

        Term varTemp = new BlankNodeTerm("_:iriListList");
        varTemp.setType(new NEListType(new NEListType(TypeRegistry.IRI)));

        store.addTemplate(
            Template.builder()
                .iri("tempNL")
                .parameters(Parameter.listOf(varTemp))
                .instance(
                    Instance.builder()
                        .iri("baseNL")
                        .listExpander(ListExpander.cross)
                        .argument(Argument.builder()
                                    .term(varTemp)
                                    .listExpander(true)
                                    .build())
                        .build())
                .build());

        StandardQueryEngine engine = new StandardQueryEngine(store);
        check(engine, 0, Message.Severity.ERROR);
    }
}
