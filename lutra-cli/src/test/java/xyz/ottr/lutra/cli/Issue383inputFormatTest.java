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

import org.junit.Test;

public class Issue383inputFormatTest {
    private static final String ROOT = "src/test/resources/issues/383inputFormat/";

    @Test
    public void testCorrect() {
        CLIRunner.run(" "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat stottr "
                + ROOT + "pizza_instances.stottr ");
    }

    @Test
    public void testTabottr() {
        CLIRunner.run(" "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat tabottr "
                + ROOT + "pizza_instances.stottr ");
    }

    @Test
    public void testBottr() {
        CLIRunner.run(" "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat bottr "
                + ROOT + "pizza_instances.stottr ");
    }

    @Test
    public void testWottr() {
        CLIRunner.run(" "
                + " --library " + ROOT + "pizza_templates.stottr "
                + " --libraryFormat stottr"
                + " --inputFormat wottr "
                + ROOT + "pizza_instances.stottr ");
    }
}
