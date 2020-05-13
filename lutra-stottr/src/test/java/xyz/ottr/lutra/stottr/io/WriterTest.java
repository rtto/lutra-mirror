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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
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
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;

public class WriterTest {

    private PrefixMapping createPrefixes() {
        var prefixes = PrefixMapping.Factory.create();
        prefixes.withDefaultMappings(OTTR.getDefaultPrefixes());
        prefixes.setNsPrefix("my", "http://base.org/");
        return prefixes;
    }

    @Test
    public void testInstances1() {

        var i1 = Instance.builder()
            .iri("http://base.org/T1")
            .arguments(Argument.listOf(
                LiteralTerm.createTypedLiteral("true", XSD.xboolean.getURI()),
                new NoneTerm(),
                new IRITerm("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                new IRITerm("http://some.uri/with#part"),
                LiteralTerm.createLanguageTagLiteral("hello", "no"))
            ).build();

        var i2 = Instance.builder()
            .iri("http://base2.org/T2")
            .arguments(Argument.listOf(
                new BlankNodeTerm("myLabel"),
                LiteralTerm.createPlainLiteral("one"),
                LiteralTerm.createPlainLiteral("two"),
                LiteralTerm.createPlainLiteral("three"))
            ).build();

        var i3 = Instance.builder()
            .iri("http://base.org/T1")
            .arguments(Argument.listOf(
                LiteralTerm.createPlainLiteral("1"),
                LiteralTerm.createPlainLiteral("2"),
                LiteralTerm.createPlainLiteral("3"))
            ).build();

        var i4 = Instance.builder()
            .iri("http://base.org/T1")
            .listExpander(ListExpander.cross)
            .arguments(Argument.listOf(
                LiteralTerm.createPlainLiteral("1"),
                LiteralTerm.createPlainLiteral("1"),
                LiteralTerm.createPlainLiteral("1"))
            ).build();

        var i5 = Instance.builder()
            .iri("http://base.org/T1")
            .arguments(Argument.listOf(
                LiteralTerm.createPlainLiteral("1"),
                LiteralTerm.createPlainLiteral("1"),
                LiteralTerm.createPlainLiteral("1"))
            ).build();

        var instances = List.of(i1, i2, i3, i4, i5);

        var output =
            "@prefix my: <http://base.org/> ." + System.lineSeparator()
            + "@prefix ottr: <http://ns.ottr.xyz/0.4/> ." + System.lineSeparator()
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + System.lineSeparator()
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." + System.lineSeparator()
            + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." + System.lineSeparator()
            + "@prefix owl: <http://www.w3.org/2002/07/owl#> ." + System.lineSeparator()
            + System.lineSeparator()
            + "my:T1(\"1\", \"1\", \"1\") ." + System.lineSeparator()
            + "my:T1(\"1\", \"2\", \"3\") ." + System.lineSeparator()
            + "my:T1(\"true\"^^xsd:boolean, none, rdf:type, <http://some.uri/with#part>, \"hello\"@no) ." + System.lineSeparator()
            + "cross | my:T1(\"1\", \"1\", \"1\") ." + System.lineSeparator()
            + "<http://base2.org/T2>(_:myLabel, \"one\", \"two\", \"three\") ."  + System.lineSeparator();

        testWriteInstances(instances, output);
    }

    @Test
    public void testSignatures1() {
        var b1 = BaseTemplate.builder()
            .iri("http://base.org/base2")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).type(TypeRegistry.getType(OWL.Class)).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).type(TypeRegistry.getType(OWL.Class)).build())
            .build();

        var b2 = BaseTemplate.builder()
            .iri("http://base.org/base1")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .build();

        var s1 = Signature.superbuilder()
            .iri("http://base.org/sig1")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .build();

        var s2 = Signature.superbuilder()
            .iri("http://aaa.org/sig2")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .build();

        var t1 = Template.builder()
            .iri("http://example.org/temp1")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("z")).defaultValue(LiteralTerm.createPlainLiteral("default")).build())
            .isEmptyPattern(true)
            .build();

        var t2 = Template.builder()
            .iri("http://aaaexample.org/temp2")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).optional(true).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("z")).build())
            .isEmptyPattern(true)
            .build();

        List<Signature> list = List.of(b1, t1, b2, s1, s2, t2);


        var output = "@prefix my: <http://base.org/> ." + System.lineSeparator()
            + "@prefix ottr: <http://ns.ottr.xyz/0.4/> ." + System.lineSeparator()
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + System.lineSeparator()
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." + System.lineSeparator()
            + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." + System.lineSeparator()
            + "@prefix owl: <http://www.w3.org/2002/07/owl#> ." + System.lineSeparator()
            + System.lineSeparator()
            + "my:base1[! rdfs:Resource ?x, rdfs:Resource ?y] :: BASE ." + System.lineSeparator()
            + System.lineSeparator()
            + "my:base2[! owl:Class ?x, owl:Class ?y] :: BASE ." + System.lineSeparator()
            + System.lineSeparator()
            + "<http://aaa.org/sig2>[! rdfs:Resource ?x, rdfs:Resource ?y] ." + System.lineSeparator()
            + System.lineSeparator()
            + "my:sig1[! rdfs:Resource ?x, rdfs:Resource ?y] ." + System.lineSeparator()
            + System.lineSeparator()
            + "<http://aaaexample.org/temp2>[!? rdfs:Resource ?x, rdfs:Resource ?y, rdfs:Resource ?z] :: {" + System.lineSeparator()
            + "    # Empty pattern" + System.lineSeparator()
            + "} ." + System.lineSeparator()
            + "" + System.lineSeparator()
            + "<http://example.org/temp1>[! rdfs:Resource ?x, rdfs:Resource ?y, rdfs:Resource ?z=\"default\"] :: {" + System.lineSeparator()
            + "    # Empty pattern" + System.lineSeparator()
            + "} .";

        testWriteSignatures(list, output);
    }

    private void testWriteSignatures(List<Signature> signatures, String expectedOutput) {

        var writer = new STemplateWriter(this.createPrefixes());
        signatures.forEach(writer::accept);
        var output = writer.write();
        assertThat(output, is(expectedOutput));
    }

    private void testWriteInstances(List<Instance> instances, String expectedOutput) {

        var writer = new SInstanceWriter(this.createPrefixes());
        instances.forEach(writer::accept);
        var output = writer.write();
        assertThat(output, is(expectedOutput));
    }

}

