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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;
import org.junit.Test;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.Files;
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
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Message.Severity;
import xyz.ottr.lutra.system.MessageHandler;

public class WriterTest {

    private static final String BR = System.lineSeparator();
    private String resourcePath = "src/test/resources/WriterTests/";

    private PrefixMapping createPrefixes() {
        var prefixes = PrefixMapping.Factory.create();
        prefixes.withDefaultMappings(OTTR.getDefaultPrefixes());
        prefixes.setNsPrefix("my", "http://base.org/");
        return prefixes;
    }

    private Instance i1 = Instance.builder()
        .iri("http://base.org/T1")
        .arguments(Argument.listOf(
            LiteralTerm.createTypedLiteral("true", XSD.xboolean.getURI()),
            new NoneTerm(),
            new IRITerm("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            new IRITerm("http://some.uri/with#part"),
            LiteralTerm.createLanguageTagLiteral("hello", "no"))
        ).build();

    private Instance i2 = Instance.builder()
        .iri("http://base2.org/T2")
        .arguments(Argument.listOf(
            new BlankNodeTerm("myLabel"),
            LiteralTerm.createPlainLiteral("one"),
            LiteralTerm.createPlainLiteral("two"),
            LiteralTerm.createPlainLiteral("three"))
        ).build();

    private Instance i3 = Instance.builder()
        .iri("http://base.org/T1")
        .arguments(Argument.listOf(
            LiteralTerm.createPlainLiteral("1"),
            LiteralTerm.createPlainLiteral("2"),
            LiteralTerm.createPlainLiteral("3"))
        ).build();

    private Instance i4 = Instance.builder()
        .iri("http://base.org/T1")
        .listExpander(ListExpander.cross)
        .arguments(Argument.listOf(
            LiteralTerm.createPlainLiteral("1"),
            LiteralTerm.createPlainLiteral("1"),
            LiteralTerm.createPlainLiteral("1"))
        ).build();

    private Instance i5 = Instance.builder()
        .iri("http://base.org/T1")
        .arguments(Argument.listOf(
            LiteralTerm.createPlainLiteral("1"),
            LiteralTerm.createPlainLiteral("1"),
            LiteralTerm.createPlainLiteral("1"))
        ).build();
    
    @Test
    public void testInstances1() throws IOException {

        var instances = List.of(i1, i2, i3, i4, i5);

        var output =
            "@prefix my: <http://base.org/> ." + BR
                + "@prefix ottr: <http://ns.ottr.xyz/0.4/> ." + BR
                + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + BR
                + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." + BR
                + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." + BR
                + "@prefix owl: <http://www.w3.org/2002/07/owl#> ." + BR
                + BR
                + "my:T1(\"true\"^^xsd:boolean, none, rdf:type, <http://some.uri/with#part>, \"hello\"@no) ." + BR
                + "<http://base2.org/T2>(_:myLabel, \"one\", \"two\", \"three\") ."  + BR
                + "my:T1(\"1\", \"2\", \"3\") ." + BR
                + "cross | my:T1(\"1\", \"1\", \"1\") ." + BR
                + "my:T1(\"1\", \"1\", \"1\") ." + BR;
                
                

        testWriteInstances(instances, output);
    }
    
    //only file write operations are tested, file contents are verified in lutra-api FormatEquivalenceTest
    @Test
    public void testSignatures1() {
        var b1 = BaseTemplate.builder()
            .iri("http://base.org/base2")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).type(TypeRegistry.asType(OWL.Class)).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).type(TypeRegistry.asType(OWL.Class)).build())
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

        var t3 = Template.builder()
            .iri("http://aaaexample.org/temp3")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).optional(true).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("z")).build())
            .instance(i4)
            .instance(i5)
            .build();

        var t4 = Template.builder()
            .iri("http://aaaexample.org/temp4")
            .parameter(Parameter.builder().term(new BlankNodeTerm("x")).optional(true).nonBlank(true).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("y")).build())
            .parameter(Parameter.builder().term(new BlankNodeTerm("z")).build())
            .annotation(i1)
            .annotation(i2)
            .annotation(i3)
            .instance(i4)
            .instance(i5)
            .build();

        List<Signature> list = List.of(b1, t1, b2, s1, s2, t2, t3, t4);

        /*
        var output = "@prefix my: <http://base.org/> ." + BR
            + "@prefix ottr: <http://ns.ottr.xyz/0.4/> ." + BR
            + "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> ." + BR
            + "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ." + BR
            + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ." + BR
            + "@prefix owl: <http://www.w3.org/2002/07/owl#> ." + BR
            + BR
            + "my:base1[" + BR
            + "    ! rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y" + BR
            + "] :: BASE ." + BR
            + BR
            + "my:base2[" + BR
            + "    ! owl:Class ?x," + BR
            + "    owl:Class ?y" + BR
            + "] :: BASE ." + BR
            + BR
            + "<http://aaa.org/sig2>[" + BR
            + "    ! rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y" + BR
            + "] ." + BR
            + BR
            + "my:sig1[" + BR
            + "    ! rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y" + BR
            + "] ." + BR
            + BR
            + "<http://aaaexample.org/temp2>[" + BR
            + "    !? rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y," + BR
            + "    rdfs:Resource ?z" + BR
            + "] :: {" + BR
            + "    # Empty pattern" + BR
            + "} ." + BR
            + "" + BR
            + "<http://aaaexample.org/temp3>[" + BR
            + "    !? rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y," + BR
            + "    rdfs:Resource ?z" + BR
            + "] :: {" + BR
            + "    my:T1(\"1\", \"1\", \"1\")," + BR
            + "    cross | my:T1(\"1\", \"1\", \"1\")" + BR
            + "} ." + BR
            + "" + BR
            + "<http://aaaexample.org/temp4>[" + BR
            + "    !? rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y," + BR
            + "    rdfs:Resource ?z" + BR
            + "]" + BR
            + "@@my:T1(" + BR
            + "    \"1\"," + BR
            + "    \"2\"," + BR
            + "    \"3\")," + BR
            + "@@my:T1(" + BR
            + "    \"true\"^^xsd:boolean," + BR
            + "    none," + BR
            + "    rdf:type," + BR
            + "    <http://some.uri/with#part>," + BR
            + "    \"hello\"@no)," + BR
            + "@@<http://base2.org/T2>(" + BR
            + "    _:myLabel," + BR
            + "    \"one\"," + BR
            + "    \"two\"," + BR
            + "    \"three\")" + BR
            + " :: {" + BR
            + "    my:T1(\"1\", \"1\", \"1\")," + BR
            + "    cross | my:T1(\"1\", \"1\", \"1\")" + BR
            + "} ." + BR
            + "" + BR
            + "<http://example.org/temp1>[" + BR
            + "    ! rdfs:Resource ?x," + BR
            + "    rdfs:Resource ?y," + BR
            + "    rdfs:Resource ?z=\"default\"" + BR
            + "] :: {" + BR
            + "    # Empty pattern" + BR
            + "} .";
        */
        testWriteSignatures(list);
    }

    private void testWriteSignatures(List<Signature> signatures) {
        String folderPath = this.resourcePath + "template_folder";
        var writer = new STemplateWriter(this.createPrefixes());
        
        BiFunction<String, String, Optional<Message>> writerFunc = (iri, str) -> {
            return Files.writeTemplatesTo(iri, str, folderPath, ".suffix");
        };
        
        writer.setWriterFunction(writerFunc);
        signatures.forEach(writer::accept);
        
        MessageHandler msgs = writer.getMessages(); 
        if (msgs.getMostSevere().isGreaterThan(Severity.WARNING)) {
            fail(msgs.getMessages().toString()); //template writing failed
        }
        
        deleteDirectory(new File(folderPath));
    }
    
    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
    
    
    private void testWriteInstances(List<Instance> instances, String expectedOutput) throws IOException {
        
        
        String filePath = this.resourcePath + "instances";
        SInstanceWriter writer = new SInstanceWriter(this.createPrefixes());
        writer.init(filePath, null);
        instances.forEach(writer::accept);
        writer.flush();
        MessageHandler msgs = writer.close();
        if (msgs.getMostSevere().isGreaterThan(Severity.WARNING)) { //fail if file write was not possible
            fail(msgs.getMessages().toString());
        }
        String fileContents = java.nio.file.Files.readString(Paths.get(filePath), Charset.forName("UTF-8"));
        assertThat(fileContents, is(expectedOutput));
        
        new File(filePath).delete();
    }
    
}

