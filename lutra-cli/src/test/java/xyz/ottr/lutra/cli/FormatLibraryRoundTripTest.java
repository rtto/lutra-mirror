package xyz.ottr.lutra.cli;

import java.sql.ClientInfoStatus;
import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class FormatLibraryRoundTripTest {

    private static final String ROOT = "src/test/resources/issues/FormatLibraryRoundTrip/";


    private String cliFormatString(String inFormat, String outFormat, String inFile, String outFolder) {
        return "-m formatLibrary"
                + " -f"
                + " -L " + inFormat
                + " -O " + outFormat
                + " -l " + inFile
                + " -o " + outFolder;
    }

    @Test
    public void roundtripNamedPizzaWottr2Stottr() {

        var IRI = "tpl.ottr.xyz/pizza/0.1/NamedPizza";
        var folder = "NamedPizza/"; // all test output is written to this folder

        // reformat wottr to wottr to normalise original
        CLIRunner.run(cliFormatString("wottr", "wottr", ROOT + "NamedPizza.ttl", ROOT + folder + "1wottr"));
        // reformat normalised original to stottr
        CLIRunner.run(cliFormatString("wottr", "stottr", ROOT + folder + "1wottr/" + IRI + ".ttl", ROOT + folder + "2stottr"));
        // reformat stottr to wottr
        CLIRunner.run(cliFormatString("stottr", "wottr", ROOT + folder + "2stottr/" + IRI + ".stottr", ROOT + folder + "3wottr"));

        // compare normalised original with round-tripped
        Model normalised = RDFIO.fileReader().parse(ROOT + folder + "1wottr/" + IRI + ".ttl").get();
        Model roundtripped = RDFIO.fileReader().parse(ROOT + folder + "3wottr/" + IRI + ".ttl").get();

        TestUtils.testIsomorphicModels(roundtripped, normalised);
    }


}
