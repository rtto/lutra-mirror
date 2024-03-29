package xyz.ottr.lutra.store.expansion;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2022 University of Oslo
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

import static xyz.ottr.lutra.model.terms.ObjectTerm.cons;
import static xyz.ottr.lutra.model.terms.ObjectTerm.var;

import java.util.stream.Collectors;
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
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class CheckingExpanderTest {

    @Test
    public void argument_numbers_mismatch() {
        String expectedString = "Wrong number of arguments.";

        TemplateStore store = new StandardTemplateStore(null);
        CheckingExpander expander = new CheckingExpander(store);

        // build template with 2 parameters
        Template template = buildDummyTemplate("iri-1");
        store.addTemplate(template);

        // build instance with 1 arguments
        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(var("x")).build())
                .build();

        ResultStream<Instance> resultStream = expander.expandInstance(instance);
        Result<Instance> emptyResult = resultStream.collect(Collectors.toList()).get(0);
        Assertions.containsMessageFragment(emptyResult.getMessageHandler(), Message.Severity.ERROR, expectedString);
    }

    @Test
    public void incompatible_argument_types() {
        String expectedString = "Incompatible argument type.";

        TemplateStore store = new StandardTemplateStore(null);
        CheckingExpander expander = new CheckingExpander(store);

        // build template, second parameter type is NEList
        Template template = buildDummyTemplate("iri-1");
        store.addTemplate(template);

        // build instance, second argument is empty list
        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(new IRITerm("x")).build())
                .argument(Argument.builder().term(new ListTerm()).build())
                .build();

        ResultStream<Instance> resultStream = expander.expandInstance(instance);
        Result<Instance> emptyResult = resultStream.collect(Collectors.toList()).get(0);
        Assertions.containsMessageFragment(emptyResult.getMessageHandler(), Message.Severity.ERROR, expectedString);
    }

    @Test
    public void blank_node_given_to_non_blank_argument() {
        String expectedString = "Incompatible blank node argument.";

        TemplateStore store = new StandardTemplateStore(null);
        CheckingExpander expander = new CheckingExpander(store);

        // build template with non-blank parameter
        Template template = buildDummyTemplate("iri-1");
        store.addTemplate(template);

        // build instance with blank node
        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder()
                        .term(new BlankNodeTerm()).build())
                .argument(Argument.builder()
                        .term(new ListTerm(cons("c1"), cons("c2"))).build())
                .build();

        ResultStream<Instance> resultStream = expander.expandInstance(instance);
        Result<Instance> emptyResult = resultStream.collect(Collectors.toList()).get(0);
        Assertions.containsMessageFragment(emptyResult.getMessageHandler(), Message.Severity.ERROR, expectedString);
    }

    private Template buildDummyTemplate(String iri) {
        Term var1 = new IRITerm("var1");
        Term var2 = new IRITerm("var2");
        var2.setType(TypeRegistry.NELIST_TYPE);

        return Template.builder()
                .iri(iri)
                .parameter(Parameter.builder().term(var1).nonBlank(true).build())
                .parameter(Parameter.builder().term(var2).build())
                .instance(Instance.builder()
                        .iri(OTTR.BaseURI.Triple)
                        .argument(Argument.builder().term(var1).build())
                        .argument(Argument.builder().term(new IRITerm("hasValue")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("value")).build())
                        .build())
                .build();
    }

}
