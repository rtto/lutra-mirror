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
import xyz.ottr.lutra.system.MessageHandler;

public class Issue251AddTemplate {

    private static final String ROOT = "src/test/resources/issues/251AddTemplate/";

    @Test
    public void test() {

        String args = " "
                //+ " --debugStackTrace"
                + " -l " + ROOT + "templates.stottr"
                + " -L stottr"
                + " --stdout"
                + " -I stottr"
                + " " + ROOT + "instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        // CLI message handler should have warning message
        assertNotEquals(0, msgs.getMessages().size());
    }

    @Test
    public void testExpand() {

        String args = " "
                + " --debugStackTrace"
                + " -m expand"
                + " -l " + ROOT + "templates.stottr"
                + " -L stottr"
                + " --inputFormat stottr"
                + " " + ROOT + "instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        // CLI message handler should have error message
        assertNotEquals(0, msgs.getMessages().size());
    }
}
