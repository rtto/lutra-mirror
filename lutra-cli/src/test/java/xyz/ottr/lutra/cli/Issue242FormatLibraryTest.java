package xyz.ottr.lutra.cli;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class Issue242FormatLibraryTest {

    private static final String ROOT = "src/test/resources/issues/242FormatLibrary/";

    @Test
    public void wottr_to_stottr_to_wottr() {
        // format original wottr templates to stottr
        String args = "-f -m formatLibrary -L wottr -O stottr "
                + " -l " + ROOT + "templates.wottr "
                + "-o " + ROOT + "out";

        CLIRunner.run(args);

        // format stottr templates back to wottr
        String args2 = "-m formatLibrary -L stottr -O wottr "
                + " -l " + ROOT + "out/example.com/ns/Person.stottr "
                + "-o " + ROOT + "out";

        CLIRunner.run(args2);

        // add missing types to original wottr templates
        String args3 = "-m formatLibrary -L wottr -O wottr "
                + " -l " + ROOT + "templates.wottr "
                + "-o " + ROOT + "original_processed";

        CLIRunner.run(args3);

        // compare round-tripped result templates with processed original templates
        Model actual = RDFIO.fileReader().parse(ROOT + "out/example.com/ns/Person.ttl").get();
        Model expected = RDFIO.fileReader().parse(ROOT + "original_processed/example.com/ns/Person.ttl").get();

        TestUtils.testIsomorphicModels(actual, expected);
    }

}
