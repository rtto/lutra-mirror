package xyz.ottr.lutra.cli;

import org.junit.Test;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class Issue324DummyInput {

    private static final String ROOT = "src/test/resources/issues/324DummyInput/";

    @Test
    public void parseDummyFile() {

        String args = " "
                + " --debugStackTrace"
                + " -l " + ROOT + "dummy.ttl"
                + " " + ROOT + "dummy.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));

        //assertNotEquals(0, msgs.getMessages().size());
        //assertEquals(Message.Severity.ERROR, msgs.getMostSevere());
        //assertNotEquals("Exit code should not be 0 with error messages", 0, exitCode);


        //var result = RDFIO.fileReader().parse(ROOT + "dummy.ttl");
        //System.out.println(result);
    }

    @Test
    public void parseFaultyFile() {

        String args = " "
                + " --debugStackTrace"
                + " -l " + ROOT + "faultyRDF.ttl"
                + " " + ROOT + "faultyRDF.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));
        //var result = RDFIO.fileReader().parse(ROOT + "faultyRDF.ttl");
        //System.out.println(result);
    }

    @Test
    public void parseFaultyFileRDFReader() {

        var result = RDFIO.fileReader().parse(ROOT + "faultyRDF.ttl");
        System.out.println(result);
    }


    @Test
    public void parseEmptyFile() {
        var result = RDFIO.fileReader().parse(ROOT + "empty.ttl");
        System.out.println(result);
    }

    @Test
    public void parseCorrectFile() {
        var result = RDFIO.fileReader().parse(ROOT + "correct.ttl");
        System.out.println(result);
    }
}
