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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class BottrTest {

    private static final String ROOT = "../lutra-bottr/src/test/resources/maps/";
    
    @Disabled("Relative source paths inside query don't work.")
    public void expandH2Source() {
        runCLI("-I bottr -f --stdout " + ROOT + "instanceMapH2Source.ttl");
    }

    @Test
    public void expandRDFSource() {
        runCLI("-I bottr -f --stdout -p " + ROOT + "instanceMapRDFSource.ttl " + ROOT + "instanceMapRDFSource.ttl");
    }

    @Test
    public void expandSPARQLSource() {
        runCLI("-I bottr -f --stdout " + ROOT + "instanceMapSPARQL.ttl");
    }

    public void runCLI(String cmd) {
        CLIRunner.run(cmd.split(" "));
    }
}
