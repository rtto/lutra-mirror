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

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;

// TODO: delete file
// equivalent test added in CheckingExpanderTest

public class Issues186Test {

    private static final String ROOT = "src/test/resources/issues/186/";
    
    @Test
    public void test_compatible_types() {
        String args = " -f "
                + " -l " + ROOT + "/lib"
                + " -L stottr -I tabottr"
                + " " + ROOT + "instances.xlsx"
                + " -o " + ROOT + "out.ttl"
                + " -O wottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        assertTrue("Output file not found.", Files.isRegularFile(Paths.get(ROOT + "out.ttl")));
        Assertions.noErrors(msgs);
    }

    @Test
    public void test_incompatible_types() {
        String args = " -f "
                + " -l " + ROOT + "/lib_incompatible_types"
                + " -L stottr -I tabottr"
                + " " + ROOT + "instances.xlsx"
                + " -o " + ROOT + "out.ttl"
                + " -O wottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));

        Assertions.atLeast(msgs, Message.Severity.ERROR);
    }

}
