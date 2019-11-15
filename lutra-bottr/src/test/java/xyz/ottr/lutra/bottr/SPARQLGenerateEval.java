package xyz.ottr.lutra.bottr;

/*-
 * #%L
 * lutra-bottr
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

/*
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.XSD;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.source.H2Source;
import xyz.ottr.lutra.bottr.source.StringArgumentMap;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;

import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.v04.WTemplateParser;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.writer.v04.WInstanceWriter;

 */


public class SPARQLGenerateEval {

    /*
       TODO: problem: cannot create a DependencyGraph without depending on lutra-cli which holds the
       ReaderRegistryImpl.

    private final Path testRoot = Paths.get("src", "test", "resources", "sparql-generate-eval");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void prototypeTest() throws IOException {

        // define prefixes
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        prefixes.setNsPrefixes(PrefixMapping.Standard);
        prefixes.setNsPrefix("ex", "http://example.com/ns#");
        prefixes.setNsPrefix("foaf", "http://xmlns.com/foaf/0.1/");
        prefixes.setNsPrefix("schema", "http://schema.org/");

        // H2 database to load CSV file
        H2Source h2 = new H2Source();
        
        String inFile = "persons-100.csv";
        
        Path csvFile = this.testRoot.resolve(inFile);

        // Set up map to translate source to triple instances
        ArgumentMaps<String> valMap = new ArgumentMaps<>(prefixes, h2, List.of(
            new StringArgumentMap(prefixes, TypeRegistry.IRI),
            new StringArgumentMap(prefixes),
            new StringArgumentMap(prefixes, TypeRegistry.IRI),
            new StringArgumentMap(prefixes, TypeRegistry.IRI),
            new StringArgumentMap(prefixes, TypeRegistry.getType(XSD.dateTime.toString())),
            new StringArgumentMap(prefixes, TypeRegistry.getType(XSD.decimal.toString())),
            new StringArgumentMap(prefixes, TypeRegistry.getType(XSD.decimal.toString()))
        ));

        // map data to triples
        InstanceMap<String> map = InstanceMap.<String>builder()
            .source(h2)
            .query("SELECT CONCAT('http://example.com/person/', ROWNUM()), Name, CONCAT('tel:', Phone), CONCAT('mailto:', Email), Birthdate, Height, Weight "
                        + "FROM CSVREAD('" + csvFile.toAbsolutePath() + "');")
            .templateIRI("http://example.com/ns#Person")
            .argumentMaps(valMap)
            .build();

        assertEquals(100, map.get().getStream().filter(Result::isPresent).count());

        TemplateStore store = new DependencyGraph();
        store.addOTTRBaseTemplates();

        // Read templates
        TemplateReader tempReader = new TemplateReader(new RDFFileReader(), new WTemplateParser());
        ResultStream<String> tempIRI = ResultStream.innerOf(this.testRoot.resolve("person.ttl").toString());
        MessageHandler errorMessages = tempReader.populateTemplateStore(store, tempIRI);
        assertFalse(Message.moreSevere(errorMessages.printMessages(), Message.ERROR)); // No errors when parsing

        ResultStream<Instance> expandedInInstances = 
                map.get()
                .innerFlatMap(store::expandInstance);

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        assertFalse(Message.moreSevere(expansionErrors.getMessageHandler().printMessages(),
                Message.ERROR)); // No errors when expanding
        Model in = insWriter.writeToModel();
        in.setNsPrefixes(prefixes);

        // print model
        String outFile = inFile + ".out.ttl";
        //ModelIO.printModel(in);
        Files.write(this.testRoot.resolve(outFile), ModelIO.writeModel(in).getBytes(Charset.forName("UTF-8")), StandardOpenOption.CREATE);
    }

     */
}
