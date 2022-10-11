package xyz.ottr.lutra.cli;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class Issue242FormatLibraryTest {

    private static final String ROOT = "src/test/resources/issues/242FormatLibrary/";

    @Test
    public void formatWottrToStottr() {
        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();

        // format wottr to stottr
        String args = "-m formatLibrary -L wottr -O stottr "
                + " -l " + ROOT + "templates.wottr "
                + "-o " + ROOT + "out";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);

        // format stottr back to wottr
        String args2 = "-m formatLibrary -L stottr -O wottr "
                + " -l " + ROOT + "out/example.com/ns/Person.stottr "
                + "-o " + ROOT + "out";

        cli.executeArgs(args2.trim().split("\\s+"));
        Assertions.noErrors(msgs);

        // compare result with original
        Model actual = RDFIO.fileReader().parse(ROOT + "out/example.com/ns/Person.ttl").get();
        Model expected = RDFIO.fileReader().parse(ROOT + "templates.wottr").get();

        // original template missing parameter type
        TestUtils.testIsomorphicModels(actual, expected);

    }

}
