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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
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

import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.model.ValueMap;
import xyz.ottr.lutra.bottr.source.JDBCSource;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.tabottr.TabOTTR;
import xyz.ottr.lutra.wottr.WTemplateFactory;
import xyz.ottr.lutra.wottr.io.WFileReader;
import xyz.ottr.lutra.wottr.io.WInstanceWriter;
import xyz.ottr.lutra.wottr.io.WTemplateParser;
import xyz.ottr.lutra.wottr.util.ModelIO;

public class SPARQLGenerateEval {

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
        String temp = tempFolder.getRoot().getAbsolutePath();
        Source h2 = new JDBCSource(
                "org.h2.Driver",
                "jdbc:h2:" + temp + "/db",
                "user",
                "");
        
        String inFile = "persons-100.csv";
        
        Path csvFile = testRoot.resolve(inFile);

        // Set up map to translate source to triple instances
        ValueMap valMap = new ValueMap(prefixes, Arrays.asList(
                TabOTTR.TYPE_IRI,
                XSD.xstring.toString(),
                TabOTTR.TYPE_IRI, 
                TabOTTR.TYPE_IRI,
                XSD.dateTime.toString(),
                XSD.decimal.toString(),
                XSD.decimal.toString()
                ));

        // map data to triples
        InstanceMap map = new InstanceMap(
                h2,
                "SELECT CONCAT('http://example.com/person/', ROWNUM()), Name, CONCAT('tel:', Phone), CONCAT('mailto:', Email), Birthdate, Height, Weight " 
                        + "FROM CSVREAD('" + csvFile.toAbsolutePath().toString() + "');",
                        "http://example.com/ns#Person",
                        valMap
                );

        assertEquals(100, map.get().getStream().filter(r -> r.isPresent()).count());

        TemplateStore store = new DependencyGraph();
        store.addTemplateSignature(WTemplateFactory.createTripleTemplateHead());

        // Read templates
        TemplateReader tempReader = new TemplateReader(new WFileReader(), new WTemplateParser());
        ResultStream<String> tempIRI = ResultStream.innerOf(testRoot.resolve("person.ttl").toString());
        MessageHandler errorMessages = tempReader.populateTemplateStore(store, tempIRI);
        assertFalse(Message.moreSevere(errorMessages.printMessages(),
                Message.ERROR)); // No errors when parsing

        ResultStream<Instance> expandedInInstances = 
                map.get()
                .innerFlatMap(ins -> store.expandInstance(ins));

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<Instance>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        assertFalse(Message.moreSevere(expansionErrors.getMessageHandler().printMessages(),
                Message.ERROR)); // No errors when expanding
        Model in = insWriter.writeToModel();
        in.setNsPrefixes(prefixes);

        // print model
        String outFile = inFile + ".out.ttl";
        //ModelIO.printModel(in);
        Files.write(testRoot.resolve(outFile), ModelIO.writeModel(in).getBytes(), StandardOpenOption.CREATE);
    }
}
