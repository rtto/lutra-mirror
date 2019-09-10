package xyz.ottr.lutra.store;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.ObjectTerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;

public class DependencyGraphTest {

    private void expandAndCheckEquality(Set<Template> toExpand, Set<Template> shouldEqual) {
        
        TemplateSignature base = new TemplateSignature(
                "base",
                new ParameterList(new ObjectTerm("x", true), new ObjectTerm("y", true)),
                true);

        DependencyGraph graph = new DependencyGraph(null);
        graph.addTemplateSignature(base);

        for (Template tmpl : toExpand) {
            graph.addTemplate(tmpl);
        }

        Result<DependencyGraph> graphRes = graph.expandAll();
        assertTrue(graphRes.isPresent());
        graph = graphRes.get();

        ResultStream<Template> tempRes = graph.getAllTemplates();

        Set<Template> expanded = new HashSet<>();
        ResultConsumer<Template> consumer = new ResultConsumer<Template>(tmpl -> expanded.add(tmpl));
        tempRes.forEach(consumer);
        assertFalse(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));

        assertEquals(expanded, shouldEqual);
    }

    @Test
    public void simpleExpansion() {

        Set<Template> toExpand = Stream.of(
            new Template(
                "t1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet())),
            new Template(
                "t2",
                new ParameterList(new ObjectTerm("v", true), new ObjectTerm("u", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("v", true), new ObjectTerm(3))),
                          new Instance("t1",
                                       new ArgumentList(new ObjectTerm(4), new ObjectTerm("u", true))))
                .collect(Collectors.toSet())))
            .collect(Collectors.toSet());

        Set<Template> shouldEqual = Stream.of(
            new Template(
                "t1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet())),
            new Template(
                "t2",
                new ParameterList(new ObjectTerm("v", true), new ObjectTerm("u", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("v", true), new ObjectTerm(3))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm("4", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("u", true))))
                .collect(Collectors.toSet())))
            .collect(Collectors.toSet());

        expandAndCheckEquality(toExpand, shouldEqual);
    }

    @Test
    public void undefinedTemplateError() {

        DependencyGraph graph = new DependencyGraph(null);

        graph.addTemplate(
            new Template(
                "t1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("t2",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet()))
        );

        graph.addTemplateSignature(
            new TemplateSignature(
                "base",
                new ParameterList(new ObjectTerm("x", true), new ObjectTerm("y", true)),
                true)
        );

        Result<DependencyGraph> graphRes = graph.expandAll();
        ResultConsumer<DependencyGraph> consumer = new ResultConsumer<>();
        consumer.accept(graphRes);
        assertTrue(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));
    }

    @Test
    public void optionalSafe() {

        Set<Template> toExpand = Stream.of(
            new Template(
                "t1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet())),
            new Template(
                "t2",
                new ParameterList(Arrays.asList(new ObjectTerm("v", true), new ObjectTerm("u", true)),
                                  null,
                                  new HashSet<>(Arrays.asList(new ObjectTerm("u", true))),
                                  null),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("v", true), new ObjectTerm(3))),
                          new Instance("t1",
                                       new ArgumentList(new ObjectTerm(4), new ObjectTerm("u", true))))
                .collect(Collectors.toSet())))
            .collect(Collectors.toSet());

        expandAndCheckEquality(toExpand, new HashSet<>(toExpand));
    }

    @Test
    public void expanderSafe() {

        TermList toListExpand = new TermList(Arrays.asList(new ObjectTerm("v1", true),
                                                           new ObjectTerm("v2", true)),
                                             true);
        Set<Template> toExpand = Stream.of(
            new Template(
                "t1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet())),
            new Template(
                "t2",
                new ParameterList(toListExpand, new ObjectTerm("u", true)),
                Stream.of(new Instance("t1",
                                       new ArgumentList(Arrays.asList(toListExpand, new ObjectTerm(3)),
                                                        new HashSet<>(Arrays.asList(toListExpand)),
                                                        ArgumentList.Expander.CROSS)),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(4), new ObjectTerm("u", true))))
                .collect(Collectors.toSet())))
            .collect(Collectors.toSet());

        expandAndCheckEquality(toExpand, new HashSet<>(toExpand));
    }

    private void expandInstanceAndCheckEquality(Instance ins, Set<Instance> shouldEqual,
                                                Set<Template> templates) {
        
        TemplateSignature base = new TemplateSignature(
                "base",
                new ParameterList(new ObjectTerm("x", true), new ObjectTerm("y", true)),
                true);

        DependencyGraph graph = new DependencyGraph(null);
        graph.addTemplateSignature(base);

        for (Template tmpl : templates) {
            graph.addTemplate(tmpl);
        }

        ResultStream<Instance> expandedInsRes = graph.expandInstance(ins);

        Set<Instance> expandedIns = new HashSet<>();
        ResultConsumer<Instance> consumer = new ResultConsumer<Instance>(in -> expandedIns.add(in));
        expandedInsRes.forEach(consumer);
        assertFalse(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));

        assertEquals(expandedIns, shouldEqual);

        Result<DependencyGraph> graphRes = graph.expandAll();
        assertTrue(graphRes.isPresent());
        graph = graphRes.get();

        ResultStream<Instance> expandedInsRes2 = graph.expandInstance(ins);

        Set<Instance> expandedIns2 = new HashSet<>();
        ResultConsumer<Instance> consumer2 = new ResultConsumer<Instance>(in -> expandedIns2.add(in));
        expandedInsRes2.forEach(consumer2);
        assertFalse(Message.moreSevere(consumer2.getMessageHandler().printMessages(), Message.ERROR));

        assertEquals(expandedIns2, shouldEqual);
    }


    @Test
    public void simpleInstanceExpandion() {

        Set<Template> templates = Stream.of(
            new Template(
                "t1",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet())),
            new Template(
                "t2",
                new ParameterList(new ObjectTerm("v", true), new ObjectTerm("u", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("v", true), new ObjectTerm(3))),
                          new Instance("t1",
                                       new ArgumentList(new ObjectTerm(4), new ObjectTerm("u", true))))
                .collect(Collectors.toSet())))
            .collect(Collectors.toSet());
        
        Instance ins = new Instance("t2", new ArgumentList(new ObjectTerm(1), new ObjectTerm(2)));

        Set<Instance> expandedIns =
            new HashSet<>(Arrays.asList(new Instance("base",
                                                     new ArgumentList(new ObjectTerm(1),
                                                                      new ObjectTerm(3))),
                                        new Instance("base",
                                                     new ArgumentList(new ObjectTerm(4),
                                                                      new ObjectTerm(1))),
                                        new Instance("base",
                                                     new ArgumentList(new ObjectTerm(2),
                                                                      new ObjectTerm(2)))));
        expandInstanceAndCheckEquality(ins, expandedIns, templates);
    }

    @Test
    public void instanceExpansionErrors() {

        DependencyGraph graph = new DependencyGraph(null);
        graph.addTemplateSignature(
            new TemplateSignature(
                "base",
                new ParameterList(new ObjectTerm("x", true), new ObjectTerm("y", true)),
                true)
        );

        ObjectTerm toListExpand = new ObjectTerm("a", true);
        graph.addTemplate(
            new Template(
                "withCross",
                new ParameterList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                Stream.of(new Instance("base",
                                       new ArgumentList(Arrays.asList(toListExpand, new ObjectTerm(1)),
                                                        new HashSet<>(Arrays.asList(toListExpand)),
                                                        ArgumentList.Expander.CROSS)),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet()))
        );
        graph.addTemplateSignature(
            new TemplateSignature(
                "signature",
                    new ParameterList(new ObjectTerm("v", true), new ObjectTerm("u", true)))
        );
        
        List<Instance> inss = Arrays.asList(
            new Instance("withCross", new ArgumentList(new BlankNodeTerm(), new ObjectTerm(2))),
            new Instance("signature", new ArgumentList(new ObjectTerm(1), new ObjectTerm(2))),
            new Instance("undefined", new ArgumentList(new ObjectTerm(1), new ObjectTerm(2)))
        );

        for (Instance ins : inss) {

            ResultConsumer<Instance> consumer = new ResultConsumer<>();
            graph.expandInstance(ins).forEach(consumer);
            assertTrue(Message.moreSevere(consumer.getMessageHandler().printMessages(), Message.ERROR));
        }
    }

    @Test
    public void optionalInstanceExpansion() {

        Set<Template> templates = Stream.of(
            new Template(
                "t1",
                new ParameterList(Arrays.asList(new ObjectTerm("a", true), new ObjectTerm("b", true)),
                                  null,
                                  new HashSet<>(Arrays.asList(new ObjectTerm("b", true))),
                                  null),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("a", true), new ObjectTerm(1))),
                          new Instance("base",
                                       new ArgumentList(new ObjectTerm(2), new ObjectTerm("b", true))))
                .collect(Collectors.toSet())),
            new Template(
                "t2",
                new ParameterList(Arrays.asList(new ObjectTerm("v", true), new ObjectTerm("u", true)),
                                  null,
                                  new HashSet<>(Arrays.asList(new ObjectTerm("u", true))),
                                  null),
                Stream.of(new Instance("base",
                                       new ArgumentList(new ObjectTerm("v", true), new ObjectTerm(3))),
                          new Instance("t1",
                                       new ArgumentList(new ObjectTerm(4), new ObjectTerm("u", true))))
                .collect(Collectors.toSet())))
            .collect(Collectors.toSet());

        Instance ins = new Instance("t2", new ArgumentList(new ObjectTerm(1), new NoneTerm()));

        Set<Instance> expandedIns =
            new HashSet<>(Arrays.asList(new Instance("base",
                                                     new ArgumentList(new ObjectTerm(1),
                                                                      new ObjectTerm(3))),
                                        new Instance("base",
                                                     new ArgumentList(new ObjectTerm(4),
                                                                      new ObjectTerm(1)))));
        expandInstanceAndCheckEquality(ins, expandedIns, templates);
    }
}
