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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.vocabulary.XSD;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Result;

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
        assertTrue(makePrefixes().equals(parsed.get()));
    }

    ///
    /// Terms
    ///

    private List<Result<Instance>> parseInstances() {

        SInstanceParser parser = new SInstanceParser(makePrefixes(), new HashMap<String, Term>());

        String instances = ":T1(true, none, rdf:type, <http://some.uri/with#part>) . "
            + "cross | ex:T2(\"hello\"@no, ++ (\"one\", \"two\", \"three\")) . "
            + ":T3(42, 42.01, \"42.02\"^^xsd:int) . ";

        return parser.parseString(instances).getStream().collect(Collectors.toList());
    }

    private List<Instance> makeInstances() {

        TermList lst = new TermList(new LiteralTerm("one"), new LiteralTerm("two"), new LiteralTerm("three"));
        Set<Term> toExpand = new HashSet<>();
        toExpand.add(lst);

        return Stream.of(
            new Instance("http://base.org/T1",
                new ArgumentList(
                    LiteralTerm.typedLiteral("true", XSD.xboolean.getURI()),
                    new NoneTerm(),
                    new IRITerm("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    new IRITerm("http://some.uri/with#part"))),
            new Instance("http://example.org/T2",
                new ArgumentList(
                    new TermList(LiteralTerm.taggedLiteral("hello", "no"), lst),
                    toExpand, ArgumentList.Expander.CROSS)),
            new Instance("http://base.org/T3",
                new ArgumentList(
                    LiteralTerm.typedLiteral("42", XSD.integer.getURI()),
                    LiteralTerm.typedLiteral("42.01", XSD.decimal.getURI()),
                    LiteralTerm.typedLiteral("42.02", XSD.decimal.getURI())))
        ).collect(Collectors.toList());
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
            // TODO: Should be implemeted on objects in future

            assertEquals(mins.getIRI(), pins.getIRI());
            List<Term> pterms = pins.getArguments().asList();
            List<Term> mterms = mins.getArguments().asList();
            assertEquals(mterms.size(), pterms.size());

            // TODO: Check equality of terms
        }
    }

    // TODO: Add unit tests for templates
}

