package xyz.ottr.lutra.cli;

import org.junit.Test;
import xyz.ottr.lutra.system.MessageHandler;

public class Issue251AddTemplate {

    private static final String ROOT = "src/test/resources/issues/251AddTemplate/";

    @Test
    public void test() {

        String args = " "
                + " --debugStackTrace"
                + " --library " + ROOT + "templates.stottr"
                + " -L stottr"
                + " -f"
                + " --stdout"
                + " --inputFormat stottr"
                + " " + ROOT + "instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));

        // CLI message handler should have error message
        //assertNotEquals(0, msgs.getMessages().size());
        //assertEquals(Message.Severity.ERROR, msgs.getMostSevere());
        //assertNotEquals("Exit code should not be 0 with error messages", 0, exitCode);
    }
}
