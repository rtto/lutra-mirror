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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import xyz.ottr.lutra.TemplateManager;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;

@Isolated
public class Issues356readLibraryTest {

    private static final String ROOT = "src/test/resources/issues/";
    private static final String stottrLibPath = ROOT + "356readLibrary/templates.stottr";
    private static final String wottrLibPath = ROOT + "356readLibrary/templates.wottr";

    @Test
    public void specifyLibraryAndInputFormat() {
        String args = "-I stottr -l "
                + stottrLibPath + " -L stottr "
                + ROOT + "356readLibrary/instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);
    }

    @Test
    public void doNotSpecifyWottrLibraryFormat() {
        String args = "-I stottr -l " + wottrLibPath + " "
                + ROOT + "356readLibrary/instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);
    }

    @Test
    public void doNotSpecifyStottrLibraryFormat() {
        String args = "-I stottr -l " + stottrLibPath + " "
                + ROOT + "356readLibrary/instances.stottr";

        CLI cli = new CLI();
        MessageHandler msgs = cli.getMessageHandler();
        cli.executeArgs(args.trim().split("\\s+"));
        Assertions.noErrors(msgs);
    }

    @Test
    public void readWottrLibrary() {
        TemplateManager templateManager = new StandardTemplateManager();
        templateManager.readLibrary(null, List.of(wottrLibPath));
        assertEquals(1, templateManager.getTemplateStore().getAllTemplates().getStream().count());
    }

    @Test
    public void readStottrLibrary() {
        TemplateManager templateManager = new StandardTemplateManager();
        templateManager.readLibrary(null, List.of(stottrLibPath));
        assertEquals(1, templateManager.getTemplateStore().getAllTemplates().getStream().count());
    }
}
