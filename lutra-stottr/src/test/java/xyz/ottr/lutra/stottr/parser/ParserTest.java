package xyz.ottr.lutra.stottr.parser;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.vocabulary.XSD;
import org.junit.jupiter.api.Test;
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
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;

public class ParserTest {

    ///
    /// PREFIXES
    ///

    private String prefixes = "@prefix ex: <http://example.org/> .\n"
            + "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n"
            + "@prefix ottr:  <http://ns.ottr.xyz/0.4/> .\n"
            + "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> . \n"
            + "@prefix :      <http://base.org/> . \n"
            + "@prefix ax: <http://tpl.ottr.xyz/owl/axiom/0.1/> . ";

    private Result<Map<String, String>> parsePrefixes() {

        SPrefixParserVisitor prefixParser = new SPrefixParserVisitor();
        return SParserUtils.parseString(this.prefixes, prefixParser);
    }

    private Map<String, String> makePrefixes() {

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("ex", "http://example.org/");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        prefixes.put("ottr", "http://ns.ottr.xyz/0.4/");
        prefixes.put("xsd", "http://www.w3.org/2001/XMLSchema#");
        prefixes.put("", "http://base.org/");
        prefixes.put("ax", "http://tpl.ottr.xyz/owl/axiom/0.1/");

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
        SPrefixParserVisitor prefixParser = new SPrefixParserVisitor();

        List<String> prefixes = Arrays.asList("@prefix ex <http://example.org/> .",
                "@prefix rdf : <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .",
                "@prefix ottr:  <http://ns.ottr.xyz/0.4/>  "
        );

        for (String p : prefixes) {
            Result<Map<String, String>> parsed = SParserUtils.parseString(p, prefixParser);
            Assertions.atLeast(parsed, Message.Severity.ERROR);
        }
    }

    ///
    /// Comment
    ///

    @Test
    public void testComment() {
        String prefixes = "@prefix ex: <http://example.org/> # This is a comment \n"
                + " # This is a single line comment \n"
                + " @prefix foaf: <http://xmlns.com/foaf/0.1/>. # This is a comment too"
                + " /***"
                + " This"
                + " is a"
                + " multi-line"
                + " comment"
                + " ***/";

        SPrefixParserVisitor prefixParser = new SPrefixParserVisitor();
        Result<Map<String, String>> res = SParserUtils.parseString(prefixes, prefixParser);

        assertEquals(res.get().size(), 2);
        assertEquals(res.get().get("ex"), "http://example.org/");
        assertEquals(res.get().get("foaf"), "http://xmlns.com/foaf/0.1/");
    }

    ///
    /// Terms
    ///

    private List<Result<Instance>> parseInstances() {

        SInstanceParser parser = new SInstanceParser();

        String instances = this.prefixes + " :T1(true, none, rdf:type, <http://some.uri/with#part>) . "
            + "cross | ex:T2(\"hello\"@no, ++ (\"one\", \"two\", \"three\")) . "
            + ":T3(42, 42.01, \"42.02\"^^xsd:decimal) . "
            + "ax:EquivObjectIntersectionOf(?intersection, (?A, ?B)) . "; // test for #156: variables in list

        return parser.apply(instances)
                .getStream()
                .collect(Collectors.toList());
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

        var i4 = Instance.builder()
                .iri("http://tpl.ottr.xyz/owl/axiom/0.1/EquivObjectIntersectionOf")
                .arguments(Argument.listOf(
                        new BlankNodeTerm(),
                        new ListTerm(new BlankNodeTerm(), new BlankNodeTerm()))
                ).build();

        return List.of(i1, i2, i3, i4);
    }

    @Test
    public void termParserTest() {

        List<Result<Instance>> parsed = parseInstances();
        List<Instance> made = makeInstances();

        for (int i = 0; i < parsed.size(); i++) {
            Result<Instance> insRes = parsed.get(i);
            Assertions.noErrors(insRes);
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

    @Test
    public void testIncorrectInstances() {
        String expectedString = "syntax error";

        List<String> instances = Arrays.asList(
                " :T1 ",
                " :T1( , ) .  ",
                " :T1(true) ",
                " :T1(true, ) . ",
                " :T1(true, , false ) .",
                " :T1(true, , , false ) . ",
                " cross :T1(\"hello\"@no, ++ (\"one\", \"two\", \"three\")) ",
                " :T2(\"2\"^^xsd:int, \"42\"^^) . " // #412 NullPointerException
        );

        for (String s : instances) {
            SInstanceParser parser = new SInstanceParser();

            ResultConsumer<Instance> consumer = new ResultConsumer<>();
            parser.apply(this.prefixes + s).forEach(consumer);
            Assertions.containsMessageFragment(consumer.getMessageHandler(), Message.Severity.ERROR, expectedString);
        }

    }

    ///
    /// Signatures
    ///
    @Test
    public void testSignature1() {
        String signature = "<http://example.com#T1> [ ??s ].";
        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertTrue(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals("s", firstParam.getTerm().getIdentifier());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignature2() {
        String signature = "<http://example.com#T1> [ !?s ].";
        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertTrue(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals("s", firstParam.getTerm().getIdentifier());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignature3() {
        String signature = "<http://example.com#T1> [ ?!?s ].";
        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("s", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertTrue(firstParam.isOptional());
        assertTrue(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignature4() {
        String signature = "@prefix ottr:   <http://ns.ottr.xyz/0.4/> ."
                + " @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . "
                + " <http://example.com#T1> [ ! ottr:IRI ?a, ? xsd:integer ?b = 5 ] .";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(2, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals("a", firstParam.getTerm().getIdentifier());
        assertEquals(TypeRegistry.IRI, firstParam.getType());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertTrue(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());

        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals("b", secondParam.getTerm().getIdentifier());
        assertEquals(TypeRegistry.INTEGER, secondParam.getType());
        assertTrue(secondParam.getTerm().isVariable());
        assertTrue(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertTrue(secondParam.hasDefaultValue());

        Term defaultValue = secondParam.getDefaultValue();
        assertEquals("\"5\"^^http://www.w3.org/2001/XMLSchema#integer", defaultValue.getIdentifier());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureWithAnnotations() {
        String signature = "@prefix ex:     <http://example.com#> . "
                + "ex:T1 [ ] "
                + "@@ex:Template1(ex:Template4, \"arg\"),"
                + "@@ex:Template1(ex:Template4, \"other arg\")"
                + ".";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(0, parsed.getParameters().size());
        assertEquals(2, parsed.getAnnotations().size());
    }

    @Test
    public void testOptionalCommaBetweenAnnotations() {
        String signature = "@prefix ex:     <http://example.com#> . "
                + "ex:T1 [ ] "
                + "@@ex:Template1(ex:Template4, \"arg\") "
                + "@@ex:Template1(ex:Template4, \"other arg\") "
                + ".";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(0, parsed.getParameters().size());
        assertEquals(2, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueClass() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "@prefix p: <http://tpl.ottr.xyz/pizza/0.1/> ."
                + "ex:NamedPizza [ ?pizza = p:pizza] .";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("pizza", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("http://tpl.ottr.xyz/pizza/0.1/pizza", firstParam.getDefaultValue().getIdentifier());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueInt() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza [ ?pizza = 2] .";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("pizza", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("\"2\"^^http://www.w3.org/2001/XMLSchema#integer", firstParam.getDefaultValue().getIdentifier());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueString() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza [ ?pizza = \"pizza\"] .";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("pizza", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("\"pizza\"^^http://www.w3.org/2001/XMLSchema#string", firstParam.getDefaultValue().getIdentifier());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testSignatureDefaultValueList() {
        String signature = "@prefix ex:     <http://example.com/ns#> . "
                + "ex:NamedPizza ["
                + "  ?pizza = \"pizza\" ,"
                + "  ?country = (\"Italy\", \"Spain\") ,  "
                + "  ?toppings = ((()))] .";

        Signature parsed = parseCorrectSignature(signature);

        assertEquals("http://example.com/ns#NamedPizza", parsed.getIri());
        assertEquals(3, parsed.getParameters().size());
        assertEquals(0, parsed.getAnnotations().size());

        // verify first parameter
        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("pizza", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertTrue(firstParam.hasDefaultValue());
        assertEquals("\"pizza\"^^http://www.w3.org/2001/XMLSchema#string", firstParam.getDefaultValue().getIdentifier());

        // verify second parameter
        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals(TypeRegistry.TOP, secondParam.getType());
        assertEquals("country", secondParam.getTerm().getIdentifier());
        assertTrue(secondParam.getTerm().isVariable());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertTrue(secondParam.hasDefaultValue());

        List<Term> firstDefaultVal = ((ListTerm)secondParam.getDefaultValue()).asList();
        assertEquals(2, firstDefaultVal.size());

        Term firstArg = firstDefaultVal.get(0);
        Term secondArg = firstDefaultVal.get(1);
        assertEquals("\"Italy\"^^http://www.w3.org/2001/XMLSchema#string", firstArg.getIdentifier());
        assertEquals("\"Spain\"^^http://www.w3.org/2001/XMLSchema#string", secondArg.getIdentifier());


        // verify third parameter
        Parameter thirdParam = parsed.getParameters().get(2);
        assertEquals(TypeRegistry.TOP, thirdParam.getType());
        assertEquals("toppings", thirdParam.getTerm().getIdentifier());
        assertTrue(thirdParam.getTerm().isVariable());
        assertFalse(thirdParam.isOptional());
        assertFalse(thirdParam.isNonBlank());
        assertTrue(thirdParam.hasDefaultValue());

        ListTerm secondDefaultValTerm = (ListTerm)thirdParam.getDefaultValue();
        List<Term> secondDefaultVal =  secondDefaultValTerm.asList();
        assertEquals(1, secondDefaultVal.size());
        assertTrue(secondDefaultValTerm.getType() instanceof NEListType);

        ListTerm innerListTerm = (ListTerm)secondDefaultVal.get(0);
        List<Term> innerList =  innerListTerm.asList();
        assertEquals(1, innerList.size());
        assertTrue(innerListTerm.getType() instanceof NEListType);

        ListTerm innerInnerListTerm = (ListTerm)innerList.get(0);
        List<Term> innerInnerList =  innerInnerListTerm.asList();
        assertTrue(innerInnerList.isEmpty());
        assertTrue(innerInnerListTerm.getType() instanceof ListType);

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
            parseIncorrectSignature(s, expectedString);
        }
    }


    ///
    /// Templates and Base templates
    ///

    @Test
    public void testBaseTemplate1() {
        String signature = "<http://example.com#T1> [ ] :: BASE .";

        Signature parsed = parseCorrectSignature(signature);

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(0, parsed.getParameters().size());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testBaseTemplate2() {
        String signature = "<http://example.com#T1> [ ?s, ?t ] :: BASE .";

        Signature parsed = parseCorrectSignature(signature);

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(2, parsed.getParameters().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("s", firstParam.getTerm().getIdentifier());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());

        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals(TypeRegistry.TOP, secondParam.getType());
        assertEquals("t", secondParam.getTerm().getIdentifier());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertFalse(secondParam.hasDefaultValue());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testBaseTemplate3() {
        String signature = "<http://example.com#T1> [ ?s ] @@<http://example.com#T2>(true) :: BASE .";

        Signature parsed = parseCorrectSignature(signature);

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(1, parsed.getAnnotations().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("s", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
    }

    @Test
    public void testBaseTemplate4() {
        String signature = "<http://example.com#T1> [ ?s ] "
                + "@@<http://example.com#T1>(true), "
                + "@@<http://example.com#T2>(true)"
                + " :: BASE .";

        Signature parsed = parseCorrectSignature(signature);

        assertTrue(parsed instanceof BaseTemplate);
        assertEquals("http://example.com#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(2, parsed.getAnnotations().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("s", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());
    }

    @Test
    public void testIncorrectBaseTemplates() {
        String expectedString = "syntax error";

        List<String> signatures = Arrays.asList(
                "<http://example.com#T1> [  ] :: BASE ",
                "<http://example.com#T1> [  ] BASE "
        );

        for (String s : signatures) {
            parseIncorrectSignature(s, expectedString);
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

        Signature parsed = parseCorrectSignature(template);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#Person", parsed.getIri());
        assertEquals(3, parsed.getParameters().size());
        assertEquals(4, ((Template) parsed).getPattern().size());
        assertEquals(0, parsed.getAnnotations().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("firstName", firstParam.getTerm().getIdentifier());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());

        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals(TypeRegistry.TOP, secondParam.getType());
        assertEquals("lastName", secondParam.getTerm().getIdentifier());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertFalse(secondParam.hasDefaultValue());

        Parameter thirdParam = parsed.getParameters().get(2);
        assertEquals(TypeRegistry.TOP, thirdParam.getType());
        assertEquals("email", thirdParam.getTerm().getIdentifier());
        assertFalse(thirdParam.isOptional());
        assertFalse(thirdParam.isNonBlank());
        assertFalse(thirdParam.hasDefaultValue());
    }

    @Test
    public void testOptionalCommaBetweenInstances() {

        String template = "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
                + "@prefix foaf:   <http://xmlns.com/foaf/0.1/> . "
                + "@prefix ex:     <http://example.com/ns#> . "
                + "@prefix ottr:   <http://ns.ottr.xyz/0.4/> . "
                + " ex:Person[ ?firstName, ?email ] :: { "
                + "  ottr:Triple (_:person, rdf:type, foaf:Person ) "
                + "  ottr:Triple (_:person, foaf:firstName, ?firstName ) "
                + "  ottr:Triple (_:person, foaf:mbox, ?email ) , "
                + "} .";

        Signature parsed = parseCorrectSignature(template);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#Person", parsed.getIri());
        assertEquals(2, parsed.getParameters().size());
        assertEquals(3, ((Template) parsed).getPattern().size());
        assertEquals(0, parsed.getAnnotations().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("firstName", firstParam.getTerm().getIdentifier());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());

        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals(TypeRegistry.TOP, secondParam.getType());
        assertEquals("email", secondParam.getTerm().getIdentifier());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertFalse(secondParam.hasDefaultValue());
    }

    @Test
    public void testTemplateEmptyPattern() {
        String signature = "@prefix ex: <http://example.com/ns#> . "
                + "           @prefix ottr: <http://ns.ottr.xyz/0.4/> ."
                + "               ex:T1[ ottr:IRI ?x ] :: {} .";

        Signature parsed = parseCorrectSignature(signature);


        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#T1", parsed.getIri());
        assertEquals(1, parsed.getParameters().size());
        assertEquals(0, parsed.getAnnotations().size());
        assertEquals(0, ((Template) parsed).getPattern().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.IRI, firstParam.getType());
        assertEquals("x", firstParam.getTerm().getIdentifier());
        assertTrue(firstParam.getTerm().isVariable());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());

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


        Signature parsed = parseCorrectSignature(template);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#Types", parsed.getIri());
        assertEquals(2, parsed.getParameters().size());
        assertEquals(1, ((Template) parsed).getPattern().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("ins", firstParam.getTerm().getIdentifier());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());

        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals(TypeRegistry.TOP, secondParam.getType());
        assertEquals("class", secondParam.getTerm().getIdentifier());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertTrue(secondParam.hasDefaultValue());

        List<Term> defaultValue = ((ListTerm)secondParam.getDefaultValue()).asList();
        assertEquals(2, defaultValue.size());

        Term firstArg = defaultValue.get(0);
        Term secondArg = defaultValue.get(1);
        assertEquals("http://example.com/ns#Person", firstArg.getIdentifier());
        assertEquals("http://example.com/ns#Employee", secondArg.getIdentifier());

        assertEquals(1, ((Template) parsed).getPattern().size());
        assertEquals(0, parsed.getAnnotations().size());
    }

    @Test
    public void testTemplateWithAnnotationAndInstance() {

        String template = "@prefix rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
                + "@prefix ex:     <http://example.com/ns#> . "
                + "@prefix ottr:   <http://ns.ottr.xyz/0.4/> . "
                + " ex:Template [ ?a, ?b ] "
                + "  @@ex:Template2 (ottr:none, 23) "
                + " ::"
                + "{ ex:Template3 ( true, ex:A ) } .";

        Signature parsed = parseCorrectSignature(template);

        assertTrue(parsed instanceof Template);
        assertEquals("http://example.com/ns#Template", parsed.getIri());
        assertEquals(2, parsed.getParameters().size());
        assertEquals(1, ((Template) parsed).getPattern().size());
        assertEquals(1, parsed.getAnnotations().size());

        Parameter firstParam = parsed.getParameters().get(0);
        assertEquals(TypeRegistry.TOP, firstParam.getType());
        assertEquals("a", firstParam.getTerm().getIdentifier());
        assertFalse(firstParam.isOptional());
        assertFalse(firstParam.isNonBlank());
        assertFalse(firstParam.hasDefaultValue());

        Parameter secondParam = parsed.getParameters().get(1);
        assertEquals(TypeRegistry.TOP, secondParam.getType());
        assertEquals("b", secondParam.getTerm().getIdentifier());
        assertFalse(secondParam.isOptional());
        assertFalse(secondParam.isNonBlank());
        assertFalse(secondParam.hasDefaultValue());
    }

    @Test
    public void testTemplateCycle() {
        String templates = "@prefix ex:     <http://example.com/ns#> ."
                + "ex:A[?x] :: { ex:B(?x) } ."
                + "ex:B[?x] :: { ex:C(?x) } ."
                + "ex:C[?x] :: { ex:A(?x) } ."
                + "ex:A(ex:uri) .";

        parseCorrectSignature(templates);
    }

    private Signature parseCorrectSignature(String signatureString) {

        var parser = new STemplateParser();
        var resultStream = parser.apply(signatureString);
        var result = resultStream.findFirst();

        Assertions.noErrors(result);

        return result.get();
    }

    private void parseIncorrectSignature(String signatureString, String expectedError) {
        STemplateParser parser = new STemplateParser();
        ResultConsumer<Signature> consumer = new ResultConsumer<>();
        parser.apply(signatureString).forEach(consumer);
        Assertions.containsMessageFragment(consumer.getMessageHandler(), Message.Severity.ERROR, expectedError);
    }

}

