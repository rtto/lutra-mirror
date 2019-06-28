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

public class IssuesTest {

    private static final String ROOT = "src/test/resources/issues/";

    public void runCLI(String cmd) {
        CLI.main(cmd.split(" "));
    }


    // ****
    // ISSUE 156: Formal parameters of templates are not supported in list operators

    // Verifying issue:
    @Test
    public void issue156stottr() {
        runCLI("-I stottr -f -F wottr -l "
                + ROOT + "156/lib.stottr" + " -L stottr --stdout "
                + ROOT + "156/instance.stottr/instance1.stottr");
    }

    // Assuming bug is due to error in stOTTR grammar; testing wOTTR equivalent:
    @Test
    public void issue156wottr() {
        runCLI("-I wottr -f -F wottr -l "
                + ROOT + "156/lib.wottr" + " -L wottr --stdout "
                + ROOT + "156/instance.wottr/instance1.wottr");
    }


}
