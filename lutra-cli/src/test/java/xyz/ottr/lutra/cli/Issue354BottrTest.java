package xyz.ottr.lutra.cli;

/*-
 * #%L
 * lutra-cli
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
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

import org.junit.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;

public class Issue354BottrTest {

    private static final String ROOT = "src/test/resources/issues/354Bottr/";

    @Test
    public void expandH2Source() {
        String args = " "
                + " --library " + ROOT + "templates.stottr"
                + " --libraryFormat stottr"
                + " --inputFormat bottr"
                + " " + ROOT + "instanceMapH2Source.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);
    }

    @Test
    public void expandRDFSource() {
        String args = " "
                + " --inputFormat bottr"
                + " " + ROOT + "instanceMapRDFSource.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);
    }

    @Test
    public void expandSPARQLSource() {
        String args = " "
                + " --inputFormat bottr"
                + " " + ROOT + "instanceMapSPARQL.ttl";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);
    }

}
