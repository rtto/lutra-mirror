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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.ObjectTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.store.QueryEngine;
import xyz.ottr.lutra.store.graph.DependencyGraph;
import xyz.ottr.lutra.store.graph.DependencyGraphEngine;
import xyz.ottr.lutra.system.Message;

public class CheckLibraryTest {

    private static final Logger log = LoggerFactory.getLogger(CheckLibraryTest.class);

    private DependencyGraph initStore() {
        
        DependencyGraph store = new DependencyGraph(null);
        store.addTemplateSignature(
            Signature.builder()
                .iri("base2")
                .parameters(Parameter.of(
                    ObjectTerm.var("x"),
                    ObjectTerm.var("y")))
                .build());

        store.addTemplateSignature(
            Signature.builder()
                .iri("base3")
                .parameters(Parameter.of(
                    ObjectTerm.var("x"),
                    ObjectTerm.var("y"),
                    ObjectTerm.var("z")))
                .build());
        return store;
    }

    private void check(QueryEngine<DependencyGraph> engine, int numErrors, int severity) {

        List<Message> msgs = CheckLibrary.allChecks
            .stream()
            .flatMap(c -> c.check(engine))
            .filter(msg -> Message.moreSevere(msg.getLevel(), severity))
            .collect(Collectors.toList());

        String assStr = "Should give " + numErrors + " messages of higher severity than "
            + severity + " but gave " + msgs.size();

        if (msgs.size() != numErrors) {
            msgs.forEach(m -> m.log(this.log));
        }

        assertTrue(assStr, msgs.size() == numErrors); 
    }

    @Test
    public void variableNotUsedWarning() {

        DependencyGraph store = initStore();
        store.addTemplate(
            Template.superbuilder()
                .iri("test")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base2")
                    .arguments(Argument.of(
                            ObjectTerm.var("a"),
                            ObjectTerm.cons(1)))
                    .build())
            .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.WARNING);
    }

    @Test
    public void variableUsedInsideList() {

        DependencyGraph store = initStore();
        store.addTemplate(
            Template.superbuilder().iri("test")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder().iri("base2")
                        .arguments(Argument.of(
                            new ListTerm(ObjectTerm.var("a")),
                                new ListTerm(ObjectTerm.cons(1),
                                    new ListTerm(ObjectTerm.var("b")))))
                    .build())
            .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 0, Message.WARNING);
    }

    @Test
    public void variableDefinedTwiceError() {

        DependencyGraph store = initStore();
        store.addTemplate(
            Template.superbuilder()
                .iri("test")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("a")))
                .instance(Instance.builder()
                    .iri("base2")
                    .arguments(Argument.of(
                        ObjectTerm.var("a"),
                        ObjectTerm.cons(1)))
                    .build())
            .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void incorrectNumberOfArgumentsError() {

        DependencyGraph store = initStore();
        store.addTemplate(
            Template.superbuilder().iri("test")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base3")
                    .arguments(Argument.of(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
            .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void nonNonBlankUsedAsNonBlankError() {

        DependencyGraph store = new DependencyGraph(null);
        store.addTemplateSignature(
            Template.superbuilder()
                .iri("base")
                .parameter(Parameter.builder().term(ObjectTerm.var("x")).nonBlank(true).build())
                .parameter(Parameter.builder().term(ObjectTerm.var("y")).build())
                .build());

        store.addTemplate(
            Template.superbuilder()
                .iri("test")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.of(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void cyclicDependencyError() {

        DependencyGraph store = initStore();

        store.addTemplate(
            Template.superbuilder()
                .iri("test1")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("base2")
                    .arguments(Argument.of(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .instance(Instance.builder()
                    .iri("test3")
                    .arguments(Argument.of(
                        ObjectTerm.var("b"),
                        ObjectTerm.var("a")))
                    .build())
                .build());

        store.addTemplate(
            Template.superbuilder().iri("test2")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("test1")
                    .arguments(Argument.of(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .build());

        store.addTemplate(
            Template.superbuilder()
                .iri("test3")
                .parameters(Parameter.of(
                    ObjectTerm.var("a"),
                    ObjectTerm.var("b")))
                .instance(Instance.builder()
                    .iri("test2")
                    .arguments(Argument.of(
                        ObjectTerm.var("a"),
                        ObjectTerm.var("b")))
                    .build())
                .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 3, Message.ERROR); // One cycle for each testN
    }

    @Test
    public void correctConsistentTypeUsageTest() {
        DependencyGraph store = new DependencyGraph(null);

        Term varBase1 = new IRITerm("ex.com/var1");
        varBase1.setType(TypeRegistry.getType(OTTR.TypeURI.IRI));
        Term varBase2 = new IRITerm("ex.com/var2");
        varBase2.setType(TypeRegistry.getType(OWL.ObjectProperty));
        Term varBase3 = LiteralTerm.createTypedLiteral("7", TypeRegistry.getType(XSD.integer).getIri());

        store.addTemplateSignature(
            Signature.builder()
                .iri("hasInt")
                .parameters(Parameter.of(varBase1, varBase2, varBase3))
                .build());

        Term varC1 = new IRITerm("ex.com/iri");
        varC1.setType(TypeRegistry.getType(OTTR.TypeURI.IRI));
        Term varC2 = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        Term varC1b = new IRITerm("ex.com/iri");
        Term varC2b = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        Term constC1 = new IRITerm("ex.com/nicepropiri");
        Term constC2 = new IRITerm("ex.com/niceonlyprop");

        store.addTemplate(
            Template.superbuilder()
                .iri("testCorrect")
                .parameters(Parameter.of(varC1, varC2))
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.of(varC1b, constC1, varC2b))
                    .build())
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.of(constC1, constC2, varC2b))
                    .build()
                ).build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 0, Message.WARNING);
    }

    @Test
    public void inconsistentTypeUsageTest() {

        // Using a constant as both Class and ObjectProperty
        DependencyGraph store = new DependencyGraph(null);

        Term classVar = new IRITerm("ex.com/classVar");
        classVar.setType(TypeRegistry.getType(OWL.Class));
        Term objpropVar = new IRITerm("ex.com/objpropVar");
        objpropVar.setType(TypeRegistry.getType(OWL.ObjectProperty));
        Term intVar = LiteralTerm.createTypedLiteral("7", TypeRegistry.getType(XSD.integer).getIri());

        store.addTemplateSignature(
            Signature.builder()
                .iri("hasInt")
                .parameters(Parameter.of(classVar, objpropVar, intVar))
                .build());

        Term classVar2 = new IRITerm("ex.com/class");
        classVar2.setType(TypeRegistry.getType(OWL.Class));
        Term intVar2 = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        Term classVar2b = new IRITerm("ex.com/class");
        Term intVar21b = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());
        Term intVar22b = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        Term propClass1 = new IRITerm("ex.com/nicepropclass");
        Term propClass2 = new IRITerm("ex.com/nicepropclass");
        Term prop = new IRITerm("ex.com/niceonlyprop");

        store.addTemplate(
            Template.superbuilder()
                .iri("testIncorrect")
                .parameters(Parameter.of(classVar2, intVar2))
                .instance(Instance.builder()
                        .iri("hasInt")
                        .arguments(Argument.of(classVar2b, propClass1, intVar21b))
                        .build())
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.of(propClass2, prop, intVar22b))
                    .build())
            .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void incorrectTypeUsage() {

        // Using a variable with type IRI to a parameter with type Class
        DependencyGraph store = new DependencyGraph(null);

        Term varBase1 = new IRITerm("ex.com/var1");
        varBase1.setType(TypeRegistry.getType(OWL.Class));
        Term varBase2 = new IRITerm("ex.com/var2");
        varBase2.setType(TypeRegistry.getType(OWL.ObjectProperty));
        Term varBase3 = LiteralTerm.createTypedLiteral("7", TypeRegistry.getType(XSD.integer).getIri());

        store.addTemplateSignature(
            Signature.builder()
                .iri("hasInt")
                .parameters(Parameter.of(varBase1, varBase2, varBase3))
                .build());

        Term var1 = new IRITerm("ex.com/iri");
        var1.setType(TypeRegistry.getType(OTTR.TypeURI.IRI));
        Term var2 = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        Term var1b = new IRITerm("ex.com/iri");
        Term var2b = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        Term cons1 = new IRITerm("ex.com/prop1");

        store.addTemplate(
            Template.superbuilder()
                .iri("testIncorrect")
                .parameters(Parameter.of(var1, var2))
                .instance(Instance.builder()
                    .iri("hasInt")
                    .arguments(Argument.of(var1b, cons1, var2b))
                    .build())
                .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void correctListTypeUsage() {

        // Using a variable with type IRI to a parameter with type Class
        DependencyGraph store = new DependencyGraph(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(TypeRegistry.getType(OWL.Class)));

        store.addTemplateSignature(
            Signature.builder()
                .iri("areClasses")
                .parameters(Parameter.of(varBase))
                .build());

        Term var = new BlankNodeTerm("_:class");
        var.setType(TypeRegistry.getType(OWL.Class));

        Term cons = new BlankNodeTerm("_:b");

        store.addTemplate(
            Template.superbuilder()
                .iri("testCorrect1")
                .parameters(Parameter.of(var))
                .instance(Instance.builder()
                    .iri("areClasses")
                    .arguments(Argument.of(new ListTerm(cons, var)))
                    .build())
                .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 0, Message.ERROR);
    }

    @Test
    public void incorrectListTypeUsage() {

        // Using a list of a variable of type Class and a an integer as argument
        // to a parameter of type NEList<Class>
        DependencyGraph store = new DependencyGraph(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(TypeRegistry.getType(OWL.Class)));

        store.addTemplateSignature(
            Signature.builder()
                .iri("areClasses")
                .parameters(Parameter.of(varBase))
                .build());

        Term varClass = new BlankNodeTerm("_:class");
        varClass.setType(TypeRegistry.getType(OWL.Class));

        Term one = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        store.addTemplate(
            Template.superbuilder().iri("testCorrect1")
                .parameters(Parameter.of(varClass))
                .instance(Instance.builder()
                    .iri("areClasses")
                    .arguments(Argument.of(new ListTerm(one, varClass)))
                    .build())
                .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }

    @Test
    public void incorrectDeepListTypeUsage() {

        DependencyGraph store = new DependencyGraph(null);

        Term varBase = new BlankNodeTerm("_:classes");
        varBase.setType(new NEListType(new NEListType(TypeRegistry.getType(OWL.Class))));

        store.addTemplateSignature(
            Signature.builder()
                .iri("deepLists")
                .parameters(Parameter.of(varBase))
                .build());

        Term varClass = new BlankNodeTerm("_:class");
        varClass.setType(TypeRegistry.getType(OWL.Class));

        Term one = LiteralTerm.createTypedLiteral("1", TypeRegistry.getType(XSD.integer).getIri());

        store.addTemplate(
            Template.superbuilder().iri("testCorrect1")
                .parameters(Parameter.of(varClass))
                .instance(Instance.builder()
                    .iri("areClasses")
                    .arguments(Argument.of(new ListTerm(
                                new ListTerm(new BlankNodeTerm(), varClass), // (_:a ?var) OK
                                new ListTerm(one, varClass))))              // (1 ?var)   ERR
                    .build())
                .build());

        QueryEngine<DependencyGraph> engine = new DependencyGraphEngine(store);

        check(engine, 1, Message.ERROR);
    }
}
