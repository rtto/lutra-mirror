package xyz.ottr.lutra.store.graph;

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
import org.junit.Assert;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Result;

public class NewExpanderTest {

    @Test
    public void testExpandInstance() {
        TemplateStore manager = new TemplateManager(null);
        manager.addOTTRBaseTemplates();
        NewExpander expander = new NewExpander(manager);

        Template template1 = buildDummyTemplate("iri-1", new String[] {"a", "b"});
        manager.addTemplate(template1);

        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(var("x")).build())
                .argument(Argument.builder().term(var("y")).build())
                .build();

        Result<List<Instance>> results = expander.expandInstance(instance).aggregate().map(s -> s.collect(Collectors.toList()));
        Assert.assertTrue(results.isPresent());
        Assert.assertEquals(2, results.get().size());
    }

    private Signature buildDummySignature(String iri, String[] parameters) {
        List<Parameter> parameterList = new ArrayList<>(parameters.length);
        for (String s : parameters) {
            parameterList.add(Parameter.builder().term(var(s)).build());
        }

        return Signature.superbuilder()
                .iri(iri)
                .parameters(parameterList)
                .build();
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
