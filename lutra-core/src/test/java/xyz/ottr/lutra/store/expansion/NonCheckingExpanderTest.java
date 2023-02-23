package xyz.ottr.lutra.store.expansion;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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

import static xyz.ottr.lutra.model.terms.ObjectTerm.var;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Result;

public class NonCheckingExpanderTest {

    @Test
    public void testExpandAll() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"a", "b"});
        manager.addTemplate(template1);

        List<Parameter> parameterList = buildParameters();

        Template template2 =  Template.builder()
                .iri("iri-2")
                .parameters(parameterList)
                .instance(Instance.builder()
                        .iri("iri-1")
                        .argument(Argument.builder().term(var("a")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("1")).build())
                        .build())
                .instance(Instance.builder()
                        .iri("iri-1")
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("2")).build())
                        .argument(Argument.builder().term(var("b")).build())
                        .build())
                .build();

        manager.addTemplate(template2);

        Result<? extends TemplateStore> newStore = expander.expandAll();
        Assertions.assertTrue(newStore.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(manager.getAllBaseTemplates().getStream().count(), newStore.get().getAllBaseTemplates().getStream().count(),
                "Number of BaseTemplates should be the same in both stores");
        Assertions.assertEquals(manager.getAllTemplates().getStream().count(), newStore.get().getAllTemplates().getStream().count(),
                "Number of Templates should be the same in both stores");
    }

    @Test
    public void testExpandInstance() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"a", "b"});
        manager.addTemplate(template1);

        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(var("x")).build())
                .argument(Argument.builder().term(var("y")).build())
                .build();

        Result<List<Instance>> results = expander.expandInstance(instance).aggregate().map(s -> s.collect(Collectors.toList()));
        Assertions.assertTrue(results.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(2, results.get().size(), "Number of results of expansion should be as expected");
    }

    @Test
    public void testExpandInstanceNonExpandable() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Instance instance = Instance.builder().iri(OTTR.BaseURI.Triple)
                .argument(Argument.builder().term(var("x")).build())
                .argument(Argument.builder().term(var("y")).build())
                .build();

        Result<List<Instance>> results = expander.expandInstance(instance).aggregate().map(s -> s.collect(Collectors.toList()));
        // TODO check if this is correct and intended - no result only if errors?
        Assertions.assertTrue(results.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(1, results.get().size(), "Number of results of expansion should be as expected");
    }

    @Test
    public void testExpandInstanceWithListExpander() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"a", "b"});
        manager.addTemplate(template1);

        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(var("x")).build())
                .argument(Argument.builder().term(
                        ListTerm.builder()
                                .term(var("y"))
                                .term(var("z"))
                                .build())
                        .listExpander(true)
                        .build())
                .listExpander(ListExpander.cross)
                .build();

        Result<List<Instance>> results = expander.expandInstance(instance).aggregate().map(s -> s.collect(Collectors.toList()));
        Assertions.assertTrue(results.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(4, results.get().size(), "Number of results of expansion should be as expected");
    }

    /**
        Test for issue #333: Avoid fetching ottr:Triple
        Fix: If instance is BaseTemplate, call expandInstance without fetching
     */
    @Test
    public void testExpandInstanceFetch() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Instance instance = Instance.builder().iri(OTTR.BaseURI.Triple)
                .argument(Argument.builder().term(var("x")).build())
                .argument(Argument.builder().term(var("y")).build())
                .argument(Argument.builder().term(var("z")).build())
                .build();

        Result<List<Instance>> results = expander.expandInstanceFetch(instance).aggregate().map(s -> s.collect(Collectors.toList()));

        Assertions.assertTrue(results.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(1, results.get().size(), "Number of results of expansion should be as expected");
        Assertions.assertTrue(results.getAllMessages().isEmpty(), "No fetching message should be present");
    }

    @Test
    public void testExpandTemplate() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"a", "b"});
        manager.addTemplate(template1);

        List<Parameter> parameterList = buildParameters();

        Template template2 =  Template.builder()
                .iri("iri-2")
                .parameters(parameterList)
                .instance(Instance.builder()
                        .iri("iri-1")
                        .argument(Argument.builder().term(var("a")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("1")).build())
                        .build())
                .instance(Instance.builder()
                        .iri("iri-1")
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("2")).build())
                        .argument(Argument.builder().term(var("b")).build())
                        .build())
                .build();

        manager.addTemplate(template2);

        Result<Template> result = expander.expandTemplate(template2);
        Assertions.assertTrue(result.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(4, result.get().getPattern().size(), "Number of results of expansion should be as expected");
    }

    @Test
    public void testExpandTemplateWithListExpander() {
        TemplateStore manager = new StandardTemplateStore(null);
        manager.addOTTRBaseTemplates();
        NonCheckingExpander expander = new NonCheckingExpander(manager);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"a", "b"});
        manager.addTemplate(template1);

        List<Parameter> parameterList = buildParameters();

        Instance instance2 = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(var("a")).build())
                .argument(Argument.builder().term(
                        ListTerm.builder()
                                .term(LiteralTerm.createPlainLiteral("2"))
                                .term(LiteralTerm.createPlainLiteral("3"))
                                .term(LiteralTerm.createPlainLiteral("4"))
                                .build())
                        .listExpander(true)
                        .build())
                .listExpander(ListExpander.cross)
                .build();

        Template template2 =  Template.builder()
                .iri("iri-2")
                .parameters(parameterList)
                .instance(Instance.builder()
                        .iri("iri-1")
                        .argument(Argument.builder().term(var("b")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("1")).build())
                        .build())
                .instance(instance2)
                .build();

        manager.addTemplate(template2);

        Result<Template> results = expander.expandTemplate(template2);
        Assertions.assertTrue(results.isPresent(), "Result should be present after expansion");
        Assertions.assertEquals(8, results.get().getPattern().size(), "Number of results of expansion should be as expected");
    }

    private List<Parameter> buildParameters() {
        String[] parameters = {"a", "b"};
        List<Parameter> parameterList = new ArrayList<>(parameters.length);
        for (String s : parameters) {
            parameterList.add(Parameter.builder().term(var(s)).build());
        }
        return parameterList;
    }

    private Template buildDummyTemplate(String iri, String[] parameters) {
        List<Parameter> parameterList = new ArrayList<>(parameters.length);
        for (String s : parameters) {
            parameterList.add(Parameter.builder().term(var(s)).build());
        }

        return Template.builder()
                .iri(iri)
                .parameters(parameterList)
                .instance(Instance.builder()
                        .iri(OTTR.BaseURI.Triple)
                        .argument(Argument.builder().term(var("a")).build())
                        .argument(Argument.builder().term(new IRITerm("iri-cons-1")).build())
                        .argument(Argument.builder().term(var("b")).build())
                        .build())
                .instance(Instance.builder()
                        .iri(OTTR.BaseURI.Triple)
                        .argument(Argument.builder().term(var("b")).build())
                        .argument(Argument.builder().term(new IRITerm("iri-cons-2")).build())
                        .argument(Argument.builder().term(var("a")).build())
                        .build())
                .build();
    }

}
