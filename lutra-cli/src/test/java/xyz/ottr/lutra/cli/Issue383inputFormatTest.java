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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;

public class Issue383inputFormatTest {
    private static final String ROOT = "src/test/resources/issues/383inputFormat/";
    private CLI cli;
    private MessageHandler msgHandler;

    public boolean containsSubstring(MessageHandler msgHandler, String substring) {
        String modifiedSubstring = substring.trim().toLowerCase(Locale.ENGLISH);

        for (Message m : msgHandler.getMessages()) {
            String s = m.getMessage().toLowerCase(Locale.ENGLISH);

            if (s.contains(modifiedSubstring)) {
                return true;
            }
        }
        return false;
    }

    @BeforeEach
    public void init() {
        cli = new CLI();
        msgHandler = cli.getMessageHandler();
    }

    @Test
    public void testCorrect() {
        String args = " "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat stottr "
                + ROOT + "pizza_instances.stottr ";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgHandler);
    }

    @Test
    public void testTabottr() {
        String expectedMessage = "Error parsing";

        String args = " "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat tabottr "
                + ROOT + "pizza_instances.stottr ";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.atLeast(msgHandler, Message.Severity.ERROR);
        assertTrue(containsSubstring(msgHandler, expectedMessage));
    }

    @Test
    public void testBottr() {
        String expectedMessage = "Error parsing";

        String args = " "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat bottr "
                + ROOT + "pizza_instances.stottr ";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.atLeast(msgHandler, Message.Severity.ERROR);
        assertTrue(containsSubstring(msgHandler, expectedMessage));
    }

    @Test
    public void testWottr() {
        String expectedMessage = "Error parsing";

        String args = " "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat wottr "
                + ROOT + "pizza_instances.stottr ";

        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.atLeast(msgHandler, Message.Severity.ERROR);
        assertTrue(containsSubstring(msgHandler, expectedMessage));
    }
}
