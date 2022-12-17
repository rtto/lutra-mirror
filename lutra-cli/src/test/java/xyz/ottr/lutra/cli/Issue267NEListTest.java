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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;

@Isolated
public class Issue267NEListTest {
    private static final String ROOT = "src/test/resources/issues/267NEList/";

    private CLI cli;
    private MessageHandler msgHandler;

    @BeforeEach
    public void init() {
        cli = new CLI();
        msgHandler = cli.getMessageHandler();
    }

    @Test
    public void correct_arguments() {

        String args = " "
                + " --library " + ROOT + "templates.stottr"
                + " --libraryFormat stottr"
                + " --inputFormat stottr"
                + " " + ROOT + "instances.stottr";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgHandler);
    }

    @Test
    public void empty_list_applied_to_NEList_parameter() {

        String args = " "
                + " --library " + ROOT + "templates.stottr"
                + " --libraryFormat stottr"
                + " --inputFormat stottr"
                + " " + ROOT + "instances_empty_list.stottr";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.atLeast(msgHandler, Message.Severity.ERROR);
    }

}
