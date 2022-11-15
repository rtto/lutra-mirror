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

import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;

public class Issue367PrefixColonTest {
    private static final String ROOT = "src/test/resources/issues/367PrefixColon/";

    @Test
    public void missingColon() {
        String args = ""
                + " -l " + ROOT + "/template.stottr"
                + " -L stottr -I stottr"
                + " " + ROOT + "missingColon.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.ERROR);
    }

    @Test
    public void spaceBeforeColon() {
        String args = ""
                + " -l " + ROOT + "/template.stottr"
                + " -L stottr -I stottr"
                + " " + ROOT + "spaceBeforeColon.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.ERROR);
    }
}
