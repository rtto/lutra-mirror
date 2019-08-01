package xyz.ottr.lutra.wottr.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;

/*-
 * #%L
 * lutra-wottr
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

import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.wottr.WTemplateFactory;
import xyz.ottr.lutra.wottr.util.ModelIO;

@RunWith(Parameterized.class)
public class RDFtoOTTRtoRDFParserTest {

    @Parameters(name = "{index}: {0}")
    public static List<String> data() throws IOException {
        Path folder = Paths.get("src",  "test", "resources", "w3c-rdf-tests");   

        return Files.walk(folder)
                .filter(Files::isRegularFile)
                .map(path -> path.toString())
                .sorted()
                .collect(Collectors.toList());
    }

    private String filename;

    public RDFtoOTTRtoRDFParserTest(String filename) {
        this.filename = filename;
    }

    @Test
    public void test() {

        // Try parse file with Jena.
        Model rdfModel = null;
        try {
            rdfModel = ModelIO.readModel(this.filename);
        } catch (Exception ex) {
            // Do nothing, we ignore Jena parser errors, which we assume 
            // are caused by negative tests.
        }
        // Continue test only if model is correctly parsed by Jena
        assumeNotNull(rdfModel); 

        Model ottrModel = getOTTRParsedRDFModel(this.filename);

        boolean isIsoMorphic = ottrModel.isIsomorphicWith(rdfModel);

        if (!ottrModel.isIsomorphicWith(rdfModel)) {
            // if error, print nice error message:
            rdfModel.clearNsPrefixMap();
            ottrModel.clearNsPrefixMap();

            String expected = ModelIO.writeModel(rdfModel, Lang.TURTLE);
            String actual = ModelIO.writeModel(ottrModel, Lang.TURTLE);
            Assert.assertThat(actual, is(expected));
        } else {
            Assert.assertTrue(isIsoMorphic);
        }
    }

    // read RDF file and return OTTR parsed RDF model
    private Model getOTTRParsedRDFModel(String filename) {

        TemplateStore store = new DependencyGraph();
        store.addTemplateSignature(WTemplateFactory.createTripleTemplateHead());

        InstanceReader insReader = new InstanceReader(new WFileReader(), new WInstanceParser());
        ResultStream<Instance> expandedInInstances = insReader
                .apply(filename)
                .innerFlatMap(ins -> store.expandInstance(ins));

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<Instance>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        assertFalse(Message.moreSevere(expansionErrors.getMessageHandler().printMessages(),
                Message.ERROR)); // No errors when expanding
        Model ottrModel = insWriter.writeToModel();
        return ottrModel;
    }

}
