package xyz.ottr.lutra.stottr.io;

/*-
 * #%L
 * lutra-stottr
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

import java.util.*;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import xyz.ottr.lutra.model.*;
import xyz.ottr.lutra.model.terms.*;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.stottr.parser.SInstanceParser;
import xyz.ottr.lutra.stottr.parser.SParserUtils;
import xyz.ottr.lutra.stottr.parser.SPrefixParser;
import xyz.ottr.lutra.stottr.parser.STemplateParser;
import xyz.ottr.lutra.system.*;

import static org.junit.Assert.*;

public class ParserTest {

    ///
    /// PREFIXES
    ///

    private Result<Map<String, String>> parsePrefixes() {

        SPrefixParser prefixParser = new SPrefixParser();
        
        String prefixes = "@prefix ex: <http://example.org/> .\n"
            + "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "@prefix ottr:  <http://ns.ottr.xyz/0.4/> .\n"
            + "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> . \n"
            + "@prefix :      <http://base.org/> .";

        return SParserUtils.parseString(prefixes, prefixParser);
    }
        
    private Map<String, String> makePrefixes() {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("ex", "http://example.org/");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixes.put("ottr", "http://ns.ottr.xyz/0.4/");
        prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        prefixes.put("", "http://base.org/");

        return prefixes;
    }

    @Test
    public void prefixParserTest() {
        Result<Map<String, String>> parsed = parsePrefixes();
        assertTrue(parsed.isPresent());
        assertEquals(makePrefixes(), parsed.get());
    }

    @Test
    public void testIncorrectPrefixes() {
        SPrefixParser prefixParser = new SPrefixParser();

        List<String> prefixes = Arrays.asList("@prefix ex <http://example.org/> .",
                "@prefix rdf : <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .",
                "@prefix ottr:  <http://ns.ottr.xyz/0.4/>  ");

        for (String p : prefixes) {
            Result<Map<String, String>> parsed = SParserUtils.parseString(p, prefixParser);
            Assertions.atLeast(parsed, Message.Severity.ERROR);
        }
    }

    ///
    /// Terms
    ///

    private List<Result<Instance>> parseInstances() {

        SInstanceParser parser = new SInstanceParser(makePrefixes(), new HashMap<>());

        String instances = ":T1(true, none, rdf:type, <http://some.uri/with#part>) . "
            + "cross | ex:T2(\"hello\"@no, ++ (\"one\", \"two\", \"three\")) . "
            + ":T3(42, 42.01, \"42.02\"^^xsd:int) . ";

        return parser.parseString(instances).getStream().collect(Collectors.toList());
    }

    private List<Instance> makeInstances() {


        var i1 = Instance.builder()
            .iri("http://base.org/T1")
            .arguments(Argument.listOf(
                LiteralTerm.createTypedLiteral("true", XSD.xboolean.getURI()),
                new NoneTerm(),
                new IRITerm("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                new IRITerm("http://some.uri/with#part"))
            ).build();

        var i2 = Instance.builder()
            .iri("http://example.org/T2")
            .argument(Argument.builder().term(new ListTerm(
                    LiteralTerm.createLanguageTagLiteral("hello", "no")))
                .build())
            .argument(Argument.builder().term(new ListTerm(
                    LiteralTerm.createPlainLiteral("one"),
                    LiteralTerm.createPlainLiteral("two"),
                    LiteralTerm.createPlainLiteral("three")))
                .listExpander(true)
                .build())
            .listExpander(ListExpander.cross)
            .build();

        var i3 = Instance.builder()
            .iri("http://base.org/T3")
            .arguments(Argument.listOf(
                LiteralTerm.createTypedLiteral("42", XSD.integer.getURI()),
                LiteralTerm.createTypedLiteral("42.01", XSD.decimal.getURI()),
                LiteralTerm.createTypedLiteral("42.02", XSD.decimal.getURI()))
            ).build();

        return List.of(i1, i2, i3);

    }

    @Test
    public void termParserTest() {

        List<Result<Instance>> parsed = parseInstances();
        List<Instance> made = makeInstances();

        for (int i = 0; i < parsed.size(); i++) {
            Result<Instance> insRes = parsed.get(i);
            assertTrue(insRes.isPresent());
            Instance pins = insRes.get();
            Instance mins = made.get(i);

            //assertEquals(mins, pins); // Fails for lists
            // Thus, need to check structural equality:
            // TODO: Should be implemented on objects in future

            assertEquals(mins.getIri(), pins.getIri());
            List<Term> pterms = pins.getArguments().stream().map(Argument::getTerm).collect(Collectors.toList());
            List<Term> mterms = mins.getArguments().stream().map(Argument::getTerm).collect(Collectors.toList());
            assertEquals(mterms.size(), pterms.size());

            // TODO: Check equality of terms
        }
    }

    private void testSignatureParsing(String signatureString) {

        STemplateParser parser = new STemplateParser();

        ResultConsumer<Signature> consumer = new ResultConsumer<>();
        parser.parseString(signatureString).forEach(consumer);

        Assertions.noErrors(consumer);
    }

    @Test
    public void testBaseTemplate1() {
        String signature = "<http://example.com#T1> [ ] :: BASE .";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(0, parsed.getParameters().size());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testBaseTemplate2() {
        String signature = "<http://example.com#T1> [ ?s ] :: BASE .";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);
        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testBaseTemplate3() {
        String signature = "<http://example.com#T1> [ ?s ] @@<http://example.com#T2>(true) :: BASE .";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(1, parsed.getAnnotations().size());
    }

    @Test
    public void testBaseTemplate4() {
        String signature = "<http://example.com#T1> [ ?s ] "
                + "@@<http://example.com#T2>(true), "
                + "@@<http://example.com#T2>(true)"
                + " :: BASE .";

        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(1, parsed.getAnnotations().size());
    }

    @Test
    public void testIncorrectBaseTemplates() {
        String expectedString = "syntax error";

        List<String> signatures = Arrays.asList(
                "<http://example.com#T1> [  ] :: BASE ",
                "<http://example.com#T1> [  ] BASE "
        );

        for (String s : signatures) {
            STemplateParser parser = new STemplateParser();
            ResultConsumer<Signature> consumer = new ResultConsumer<>();
            parser.parseString(s).forEach(consumer);
            assertTrue(containsSubstring(consumer.getMessageHandler().getMessages(), expectedString));
        }
    }

    @Test
    public void testSignature1() {
        String signature = "<http://example.com#T1> [ ??s ].";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertTrue(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignature2() {
        String signature = "<http://example.com#T1> [ !?s ].";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertTrue(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignature3() {
        String signature = "<http://example.com#T1> [ ?!?s ].";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertTrue(firstParam.isOptional());
        assertTrue(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueClass() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "@prefix p: <http://tpl.ottr.xyz/pizza/0.1/> ."
                + "ex:NamedPizza [ ?pizza = p:pizza] .";

        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("http://tpl.ottr.xyz/pizza/0.1/pizza", firstParam.getDefaultValue().getIdentifier().toString());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueInt() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza [ ?pizza = 2] .";

        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("\"2\"^^http://www.w3.org/2001/XMLSchema#integer", firstParam.getDefaultValue().getIdentifier().toString());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueString() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza [ ?pizza = 'pizza'] .";
        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("\"'pizza'\"^^http://www.w3.org/2001/XMLSchema#string", firstParam.getDefaultValue().getIdentifier().toString());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueList() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza ["
                + "  ?pizza = 'pizza' ,"
                + "  ?country = ('Italy', 'Spain') ,  "
                + "  ?toppings = ((()))] .";

        testSignatureParsing(signature);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);
        Parameter secondParam = parsed.getParameters().get(1);
        Parameter thirdParam = parsed.getParameters().get(2);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(3, parsed.getParameters().size());
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("\"'pizza'\"^^http://www.w3.org/2001/XMLSchema#string", firstParam.getDefaultValue().getIdentifier().toString());
        // TODO: verify second and third parameter
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testIncorrectSignatures() {
        String expectedString = "syntax error";

        List<String> signatures = Arrays.asList(
                "<http://example.com#T1> [  ]",
                "<http://example.com#T1> [  ] :: { ",
                "<http://example.com#T1 [  ] . "
        );

        for (String s : signatures) {
            STemplateParser parser = new STemplateParser();
            ResultConsumer<Signature> consumer = new ResultConsumer<>();
            parser.parseString(s).forEach(consumer);
            assertTrue(containsSubstring(consumer.getMessageHandler().getMessages(), expectedString));
        }
    }


    @Test
    public void testTemplate() {

        String template = "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
            + "@prefix foaf:   <http://xmlns.com/foaf/0.1/> . "
            + "@prefix ex:     <http://example.com/ns#> . "
            + "@prefix ottr:   <http://ns.ottr.xyz/0.4/> . "
            + " ex:Person[ ?firstName, ?lastName, ?email ] :: { "
            + "  ottr:Triple (_:person, rdf:type, foaf:Person ), "
            + "  ottr:Triple (_:person, foaf:firstName, ?firstName ), "
            + "  ottr:Triple (_:person, foaf:lastName, ?lastName ), "
            + "  ottr:Triple (_:person, foaf:mbox, ?email ) "
            + "} .";

        testSignatureParsing(template);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(template);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);
        Parameter secondParam = parsed.getParameters().get(1);
        Parameter thirdParam = parsed.getParameters().get(2);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#Person", parsed.getIri());
        assertEquals(3, parsed.getParameters().size());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertFalse(secondParam.hasDefaultValue());
        assertFalse(thirdParam.isOptional());
        assertFalse(thirdParam.isNonBlank());
        assertFalse(thirdParam.hasDefaultValue());
        assertEquals(4, ((Template) parsed).getPattern().size());
        assertEquals(0, parsed.getAnnotations().size());

    }

    @Test
    public void testTemplateEmptPattern() {
        String signature = "@prefix ex: <http://example.com/ns#> . " +
                "           @prefix ottr: <http://ns.ottr.xyz/0.4/> ." +
                "               ex:T1[ ottr:IRI ?x ] :: {} .";

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(signature);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(TypeRegistry.IRI, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertEquals(0, parsed.getAnnotations().size());
        assertEquals(0, ((Template) parsed).getPattern().size());
    }

    @Test
    public void testTemplateDefaultList() {

        String template = "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
                + "@prefix foaf:   <http://xmlns.com/foaf/0.1/> . "
                + "@prefix ex:     <http://example.com/ns#> . "
                + "@prefix ottr:   <http://ns.ottr.xyz/0.4/> . "
                + " ex:Types[ ?ins, ?class = (ex:Person, ex:Employee) ] :: { "
                + "  cross | ottr:Triple(?ins, rdf:Type, ++?class) "
                + "} .";

        testSignatureParsing(template);

        STemplateParser parser = new STemplateParser();
        ResultStream<Signature> resultStream = parser.parseString(template);
        Signature parsed = resultStream.collect(Collectors.toList()).get(0).get();

        Parameter firstParam = parsed.getParameters().get(0);
        Parameter secondParam = parsed.getParameters().get(1);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#Types", parsed.getIri());
        assertEquals(2, parsed.getParameters().size());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertTrue(secondParam.hasDefaultValue());
        assertEquals(1, ((Template) parsed).getPattern().size());
        assertEquals(0, parsed.getAnnotations().size());
    }

    public boolean containsSubstring(List<Message> messages, String substring) {
        String modifiedSubstring = substring.trim().toLowerCase();

        for (Message m : messages) {
            String s = m.getMessage().toLowerCase();
            return s.contains(modifiedSubstring);
        }
        return false;
    }

}

