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

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;


public class Issue324DummyInput {

    private static final String ROOT = "src/test/resources/issues/324DummyInput/";

    @Test
    public void parseCorrectFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " --stdout"
                + " " + ROOT + "instances/correct.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.noErrors(msgs);
    }

    @Test
    public void parseDummyFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " " + ROOT + "instances/dummy.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));

        assertNotEquals(0, msgs.getMessages().size());
        assertNotEquals("Exit code should not be 0 with error messages", 0, exitCode);
    }

    @Test
    public void parseFaultyFile() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " " + ROOT + "instances/faulty.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        int exitCode = cli.executeArgs(args.trim().split("\\s+"));

        assertNotEquals(0, msgs.getMessages().size());
        assertNotEquals("Exit code should not be 0 with error messages", 0, exitCode);
    }

    @Test
    public void readEmptyInstance() {
        String args = " "
                + " -l " + ROOT + "templates/personTemplate.stottr"
                + " " + ROOT + "instances/empty.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }

    @Test
    public void readEmptyTemplate() {
        String args = " "
                + " -l " + ROOT + "templates/emptyTemplate.stottr"
                + " " + ROOT + "instances/correct.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.WARNING);
    }
}
