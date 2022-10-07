package xyz.ottr.lutra.cli;

import org.junit.Test;
import xyz.ottr.lutra.system.MessageHandler;

public class Issue242FormatLibraryTest {

    private static final String ROOT = "src/test/resources/issues/242FormatLibrary/";

    @Test
    public void formatWottrToStottr() {
        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();

        String args = "-m formatLibrary -L wottr -O stottr "
                + " -l " + ROOT + "templates.wottr "
                + "-o " + ROOT + "out";

        cli.executeArgs(args.trim().split("\\s+"));

        String args2 = "-m formatLibrary -L stottr -O wottr "
                + " -l " + ROOT + "out/example.com/ns/Person.stottr "
                + "-o " + ROOT + "out";

        cli.executeArgs(args2.trim().split("\\s+"));

    }

}
