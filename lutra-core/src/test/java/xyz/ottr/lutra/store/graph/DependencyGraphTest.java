package xyz.ottr.lutra.store.graph;

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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static xyz.ottr.lutra.model.terms.ObjectTerm.cons;
import static xyz.ottr.lutra.model.terms.ObjectTerm.var;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.ObjectTerm;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.TemplateStoreNew;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

public class DependencyGraphTest {

    private void expandAndCheckEquality(Set<Template> toExpand, Set<Template> shouldEqual) {
        
        BaseTemplate base = BaseTemplate.builder()
            .iri("base")
            .parameter(Parameter.builder().term(var("x")).build())
            .parameter(Parameter.builder().term(var("y")).build())
            .build();

        TemplateStoreNew store = new TemplateManager(null);
        store.addBaseTemplate(base);

        for (Template tmpl : toExpand) {
            store.addTemplate(tmpl);
        }

        Expander expander = new NewNoChecksExpander(store);
        Result<? extends TemplateStoreNew> graphRes = expander.expandAll();
        assertTrue(graphRes.isPresent());
        store = graphRes.get();

        ResultStream<Template> tempRes = store.getAllTemplates();

        Set<Template> expanded = new HashSet<>();
        ResultConsumer<Template> consumer = new ResultConsumer<>(expanded::add);
        tempRes.forEach(consumer);

        Assertions.noErrors(consumer);

        assertEquals(expanded, shouldEqual);
    }

    @Test
    public void simpleExpansion() {

        Set<Template> toExpand = Set.of(
            Template.builder()
                .iri("t1")
                .parameter(Parameter.builder().term(var("a")).build())
                .parameter(Parameter.builder().term(var("b")).build())
                .instance(Instance.builder()
                    .iri("base")
                    .argument(Argument.builder().term(var("a")).build())
                    .argument(Argument.builder().term(cons(1)).build())
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .argument(Argument.builder().term(cons(2)).build())
                    .argument(Argument.builder().term(var("b")).build())
                    .build())
                .build(),
            Template.builder()
                .iri("t2")
                .parameters(Parameter.listOf(var("v"), var("u")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("v"), cons(3)))
                    .build())
                .instance(Instance.builder()
                    .iri("t1")
                    .arguments(Argument.listOf(cons(4), var("u")))
                    .build())
                .build());

        Set<Template> shouldEqual = Set.of(
            Template.builder()
                .iri("t1")
                .parameters(Parameter.listOf(var("a"), var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("a"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build(),
            Template.builder()
                .iri("t2")
                .parameters(Parameter.listOf(var("v"), var("u")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("v"), cons(3)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("4"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("u")))
                    .build())
                .build());

        expandAndCheckEquality(toExpand, shouldEqual);
    }

    @Test
    public void simpleTripleExpansion()  {

        var tripleInstance = Instance.builder()
            .iri(OTTR.BaseURI.Triple)
            .arguments(Argument.listOf(
                new IRITerm("http://example.com#subject"),
                new IRITerm("http://example.com#predicate"),
                new IRITerm("http://example.com#object")))
            .build();

        TemplateStoreNew store = new TemplateManager(null);
        store.addOTTRBaseTemplates();
        Expander expander = new NewNoChecksExpander(store);

        var expanded = expander.expandInstance(tripleInstance).collect(Collectors.toList());

        assertThat(expanded.size(), is(1));
        assertThat(expanded.get(0).get(), is(tripleInstance));
    }

    @Test
    public void undefinedTemplateError() {

        TemplateStoreNew store = new TemplateManager(null);

        store.addTemplate(
            Template.builder()
                .iri("t1")
                .parameters(Parameter.listOf(var("a"), var("b")))
                .instance(Instance.builder()
                    .iri("t2")
                    .arguments(Argument.listOf(var("a"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build());

        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("base")
                .parameters(Parameter.listOf(var("x"), var("y")))
                .build()
        );

        Expander expander = new NewNoChecksExpander(store);
        Result<TemplateStoreNew> graphRes = (Result<TemplateStoreNew>) expander.expandAll();
        ResultConsumer<TemplateStoreNew> consumer = new ResultConsumer<>();
        consumer.accept(graphRes);

        Assertions.atLeast(consumer, Message.Severity.ERROR);
    }

    @Test
    public void optionalSafe() {

        Set<Template> toExpand = Set.of(
            Template.builder()
                .iri("t1")
                .parameters(Parameter.listOf(var("a"), var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("a"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build(),
            Template.builder()
                .iri("t2")
                .parameter(Parameter.builder().term(var("v")).build())
                .parameter(Parameter.builder().term(var("u")).optional(true).build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("v"), cons(3)))
                    .build())
                .instance(Instance.builder()
                    .iri("t1")
                    .arguments(Argument.listOf(cons(4), var("u")))
                    .build())
                .build());

        expandAndCheckEquality(toExpand, new HashSet<>(toExpand));
    }

    @Test
    public void expanderSafe() {

        ListTerm toListExpand = new ListTerm(List.of(var("v1"), var("v2")), true);

        Set<Template> toExpand = Set.of(
            Template.builder()
                .iri("t1")
                .parameters(Parameter.listOf(var("a"), var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("a"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build(),
            Template.builder()
                .iri("t2")
                .parameters(Parameter.listOf(toListExpand, var("u")))
                .instance(Instance.builder()
                    .iri("t1")
                    .listExpander(ListExpander.cross)
                    .argument(Argument.builder().term(toListExpand).listExpander(true).build())
                    .argument(Argument.builder().term(cons(3)).build())
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(4), var("u")))
                    .build())
                .build());

        expandAndCheckEquality(toExpand, new HashSet<>(toExpand));
    }

    private void expandInstanceAndCheckEquality(Instance ins, Set<Instance> shouldEqual, Set<Template> templates) {
        
        BaseTemplate base = BaseTemplate.builder()
            .iri("base")
            .parameters(Parameter.listOf(var("x"), var("y")))
            .build();

        TemplateStoreNew store = new TemplateManager(null);
        store.addBaseTemplate(base);

        for (Template tmpl : templates) {
            store.addTemplate(tmpl);
        }

        Expander expander = new NewNoChecksExpander(store);
        ResultStream<Instance> expandedInsRes = expander.expandInstance(ins);

        Set<Instance> expandedIns = new HashSet<>();
        ResultConsumer<Instance> consumer = new ResultConsumer<>(expandedIns::add);
        expandedInsRes.forEach(consumer);

        Assertions.noErrors(consumer);

        assertThat(expandedIns, is(shouldEqual));

        Result<? extends TemplateStoreNew> graphRes = expander.expandAll();
        assertTrue(graphRes.isPresent());
        store = graphRes.get();

        ResultStream<Instance> expandedInsRes2 = expander.expandInstance(ins);

        Set<Instance> expandedIns2 = new HashSet<>();
        ResultConsumer<Instance> consumer2 = new ResultConsumer<>(expandedIns2::add);
        expandedInsRes2.forEach(consumer2);
        Assertions.noErrors(consumer2);

        assertEquals(expandedIns2, shouldEqual);
    }


    @Test
    public void simpleInstanceExpansion() {

        Set<Template> templates = Set.of(
            Template.builder()
                .iri("t1")
                .parameters(Parameter.listOf(var("a"), var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("a"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build(),
            Template.builder()
                .iri("t2")
                .parameters(Parameter.listOf(var("v"), var("u")))
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("v"), cons(3)))
                    .build())
                .instance(Instance.builder()
                    .iri("t1")
                    .arguments(Argument.listOf(cons(4), var("u")))
                    .build())
                .build());
        
        Instance ins = Instance.builder().iri("t2").arguments(Argument.listOf(cons(1), cons(2))).build();

        Set<Instance> expandedIns = Set.of(
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(1), cons(3)))
                .build(),
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(4), cons(1)))
                .build(),
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(2), cons(2)))
                .build());

        expandInstanceAndCheckEquality(ins, expandedIns, templates);
    }

    @Test
    public void instanceExpansionErrors() {

        TemplateStoreNew store = new TemplateManager(null);
        store.addBaseTemplate(
            BaseTemplate.builder()
                .iri("base")
                .parameters(Parameter.listOf(var("x"), var("y")))
                .build()
        );

        ObjectTerm toListExpand = var("a");
        store.addTemplate(
            Template.builder()
                .iri("withCross")
                .parameters(Parameter.listOf(var("a"), var("b")))
                .instance(Instance.builder()
                    .iri("base")
                    .listExpander(ListExpander.cross)
                    .argument(Argument.builder().term(toListExpand).listExpander(true).build())
                    .argument(Argument.builder().term(cons(1)).build())
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build());

        store.addSignature(
            Signature.superbuilder()
                .iri("signature")
                .parameters(Parameter.listOf(var("v"), var("u")))
                .build());
        
        List<Instance> inss = List.of(
            // TODO this case needs specification:
            // is the BlankNodeTerm supposed to cause an error if we cannot expand it or this it working as intended?
            // TODO https://gitlab.com/ottr/spec/rOTTR/-/issues/16
            //Instance.builder().iri("withCross").arguments(Argument.listOf(new BlankNodeTerm(), cons(2))).build(),
            Instance.builder().iri("signature").arguments(Argument.listOf(cons(1), cons(2))).build(),
            Instance.builder().iri("undefined").arguments(Argument.listOf(cons(1), cons(2))).build()
        );

        Expander expander = new NewNoChecksExpander(store);
        for (Instance ins : inss) {
            ResultConsumer<Instance> consumer = new ResultConsumer<>();
            expander.expandInstance(ins).forEach(consumer);
            Assertions.atLeast(consumer, Message.Severity.ERROR);
        }
    }

    @Test
    public void optionalInstanceExpansion() {

        Set<Template> templates = Set.of(
            Template.builder()
                .iri("t1")
                .parameter(Parameter.builder().term(var("a")).build())
                .parameter(Parameter.builder().term(var("b")).optional(true).build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("a"), cons(1)))
                    .build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(cons(2), var("b")))
                    .build())
                .build(),
            Template.builder()
                .iri("t2")
                .parameter(Parameter.builder().term(var("v")).build())
                .parameter(Parameter.builder().term(var("u")).optional(true).build())
                .instance(Instance.builder()
                    .iri("base")
                    .arguments(Argument.listOf(var("v"), cons(3)))
                    .build())
                .instance(Instance.builder()
                    .iri("t1")
                    .arguments(Argument.listOf(cons(4), var("u")))
                    .build())
                .build());

        Instance ins = Instance.builder().iri("t2").arguments(Argument.listOf(cons(1), new NoneTerm())).build();

        Set<Instance> expandedIns = Set.of(
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(1), cons(3)))
                .build(),
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(4), cons(1)))
                .build());

        expandInstanceAndCheckEquality(ins, expandedIns, templates);
    }

    @Test // testing #212
    public void listExpansion() {

        Set<Template> templates = Set.of();

        Instance ins = Instance.builder().iri("base")
            .argument(Argument.builder().term(cons(1)).build())
            .argument(Argument.builder().term(new ListTerm(List.of(cons("A"), cons("B")))).listExpander(true).build())
            .listExpander(ListExpander.cross)
            .build();

        Set<Instance> expandedIns = Set.of(
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(1), cons("A")))
                .build(),
            Instance.builder()
                .iri("base")
                .arguments(Argument.listOf(cons(1), cons("B")))
                .build());

        expandInstanceAndCheckEquality(ins, expandedIns, templates);
    }
}
