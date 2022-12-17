package xyz.ottr.lutra.cli;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-cli
 * %%
 * Copyright (C) 2018 - 2022 University of Oslo
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

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;

@Isolated
public class Issue324DummyInputTest {

    private static final String ROOT = "src/test/resources/issues/324DummyInput/";

    @Test
    public void parseCorrectFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " -L stottr "
                + ROOT + "correct.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.noErrors(msgs);
    }

    @Test
    public void parseDummyFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " -L stottr "
                + ROOT + "dummy.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));

        assertNotEquals(0, msgs.getMessages().size());
        assertNotEquals(0, exitCode, "Exit code should not be 0 with error messages");
    }

    @Test
    public void parseFaultyFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " -L stottr "
                + ROOT + "faulty.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));

        assertNotEquals(0, msgs.getMessages().size());
        assertNotEquals(0, exitCode, "Exit code should not be 0 with error messages");
    }

    @Test
    public void emptyInstanceFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " -L stottr "
                + ROOT + "empty.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

    @Test
    public void emptyTemplateFile() {
        String args = " "
                + " -l " + ROOT + "templates/emptyTemplate.stottr"
                + " -L stottr "
                + ROOT + "correct.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

    @Test
    public void emptyInstanceFileInFolder() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr "
                + " -L stottr "
                + ROOT + "instances";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

    @Test
    public void emptyTemplateFileInFolder() {
        String args = " "
                + " -l " + ROOT + "templates "
                + " -L stottr "
                + ROOT + "correct.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

    @Test
    public void emptyTemplateFolder() {
        String args = " "
                + " -l " + ROOT + "emptyFolder "
                + " -L stottr "
                + ROOT + "correct.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

    @Test
    public void emptyInstanceFolder() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr "
                + " -L stottr "
                + ROOT + "emptyFolder";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

}
