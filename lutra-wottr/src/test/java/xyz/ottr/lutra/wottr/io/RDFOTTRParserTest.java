package xyz.ottr.lutra.wottr.io;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
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
public class RDFOTTRParserTest {

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

    public RDFOTTRParserTest(String filename) {
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

        assertTrue(ottrModel.isIsomorphicWith(rdfModel));
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
