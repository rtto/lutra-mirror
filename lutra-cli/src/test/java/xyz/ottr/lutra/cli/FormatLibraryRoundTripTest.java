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

import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import xyz.ottr.lutra.wottr.io.RDFIO;

@Isolated
public class FormatLibraryRoundTripTest {

    private static final String ROOT = "src/test/resources/issues/FormatLibraryRoundTrip/";


    private String cliFormatString(String inFormat, String outFormat, String inFile, String outFolder) {
        return "-m formatLibrary"
                + " -f"
                + " -L " + inFormat
                + " -O " + outFormat
                + " -l " + inFile
                + " -o " + outFolder;
    }

    @Test
    public void roundtripNamedPizzaWottrStottr() throws IOException {

        var iri = "tpl.ottr.xyz/pizza/0.1/NamedPizza";
        var folder = "NamedPizza/"; // all test output is written to this folder

        // reformat wottr to wottr to normalise original
        CLIRunner.run(cliFormatString("wottr", "wottr", ROOT + "NamedPizza.ttl", ROOT + folder + "1wottr"));
        // reformat normalised original to stottr
        CLIRunner.run(cliFormatString("wottr", "stottr", ROOT + folder + "1wottr/" + iri + ".ttl", ROOT + folder + "2stottr"));
        // reformat stottr to wottr
        CLIRunner.run(cliFormatString("stottr", "wottr", ROOT + folder + "2stottr/" + iri + ".stottr", ROOT + folder + "3wottr"));
        // reformat wottr to stottr again
        //CLIRunner.run(cliFormatString("wottr", "stottr", ROOT + folder + "3wottr/" + iri + ".ttl", ROOT + folder + "4stottr"));

        // wOTTR: compare normalised original with round-tripped
        Model normalised = RDFIO.fileReader().parse(ROOT + folder + "1wottr/" + iri + ".ttl").get();
        Model roundtripped = RDFIO.fileReader().parse(ROOT + folder + "3wottr/" + iri + ".ttl").get();
        TestUtils.testIsomorphicModels(roundtripped, normalised);

        /*
        // stOTTR: compare 1st time translated with 2nd time translated
        String firstTime = Files.readString(Path.of(ROOT + folder + "2stottr/" + iri + ".stottr"));
        String secondTime = Files.readString(Path.of(ROOT + folder + "4stottr/" + iri + ".stottr"));

        assertEquals(firstTime, secondTime);
        */
    }
}
