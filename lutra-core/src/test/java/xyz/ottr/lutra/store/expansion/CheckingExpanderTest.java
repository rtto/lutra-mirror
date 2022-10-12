package xyz.ottr.lutra.store.expansion;

import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.*;
import xyz.ottr.lutra.model.terms.*;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

import java.util.stream.Collectors;

import static xyz.ottr.lutra.model.terms.ObjectTerm.var;

public class CheckingExpanderTest {

    @Test
    public void argument_numbers_mismatch() {
        String expectedString = "Number of arguments do not match";

        TemplateStore store = new StandardTemplateStore(null);
        CheckingExpander expander = new CheckingExpander(store);

        // build template with 1 parameter
        Term var1 = new IRITerm("example.org/var1");

        Template template = Template.builder()
                .iri("iri-1")
                .parameter(Parameter.builder().term(var1).build())
                .instance(Instance.builder()
                        .iri(OTTR.BaseURI.Triple)
                        .argument(Argument.builder().term(var1).build())
                        .argument(Argument.builder().term(new IRITerm("example.org/hasValue")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("value")).build())
                        .build())
                .build();

        store.addTemplate(template);

        // build instance with 2 arguments
        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder().term(var("x")).build())
                .argument(Argument.builder().term(var("y")).build())
                .build();

        ResultStream<Instance> resultStream = expander.expandInstance(instance);
        Result<Instance> emptyResult = resultStream.collect(Collectors.toList()).get(0);
        Assertions.containsExpectedString(emptyResult.getMessageHandler(), expectedString);
    }

    @Test
    public void incompatible_argument_types() {
        String expectedString = "Incompatible types";

        TemplateStore store = new StandardTemplateStore(null);
        CheckingExpander expander = new CheckingExpander(store);

        // build template, parameter type is IRI
        Term var1 = new IRITerm("example.org/var1");

        Template template = Template.builder()
                .iri("iri-1")
                .parameter(Parameter.builder().term(var1).build())
                .instance(Instance.builder()
                        .iri(OTTR.BaseURI.Triple)
                        .argument(Argument.builder().term(var1).build())
                        .argument(Argument.builder().term(new IRITerm("example.org/hasValue")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("value")).build())
                        .build())
                .build();

        store.addTemplate(template);

        // build instance, argument type is literal
        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder()
                        .term(LiteralTerm.createPlainLiteral("var1")).build())
                .build();

        ResultStream<Instance> resultStream = expander.expandInstance(instance);
        Result<Instance> emptyResult = resultStream.collect(Collectors.toList()).get(0);
        Assertions.containsExpectedString(emptyResult.getMessageHandler(), expectedString);
    }

    @Test
    public void blank_node_given_to_non_blank_argument() {
        String expectedString1 = "Incompatible argument";
        String expectedString2 = "blank node";

        TemplateStore store = new StandardTemplateStore(null);
        CheckingExpander expander = new CheckingExpander(store);

        // build template with non-blank parameter
        Term var1 = new IRITerm("example.org/var1");

        Template template = Template.builder()
                .iri("iri-1")
                .parameter(Parameter.builder().term(var1).nonBlank(true).build())
                .instance(Instance.builder()
                        .iri(OTTR.BaseURI.Triple)
                        .argument(Argument.builder().term(var1).build())
                        .argument(Argument.builder().term(new IRITerm("example.org/hasValue")).build())
                        .argument(Argument.builder().term(LiteralTerm.createPlainLiteral("var2")).build())
                        .build())
                .build();

        store.addTemplate(template);

        // build instance with blank node
        Instance instance = Instance.builder().iri("iri-1")
                .argument(Argument.builder()
                        .term(new BlankNodeTerm()).build())
                .build();

        ResultStream<Instance> resultStream = expander.expandInstance(instance);
        Result<Instance> emptyResult = resultStream.collect(Collectors.toList()).get(0);
        Assertions.containsExpectedString(emptyResult.getMessageHandler(), expectedString1);
        Assertions.containsExpectedString(emptyResult.getMessageHandler(), expectedString2);
    }

}
