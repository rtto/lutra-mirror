package xyz.ottr.lutra.docttr.writer;

/*-
 * #%L
 * lutra-api
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.Files;
import xyz.ottr.lutra.io.FormatManager;
import xyz.ottr.lutra.store.graph.DependencyGraph;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.parser.WTemplateParser;

public class DTemplateWriterTest {

    private static final String outputFolder = "src/test/resources/.output";

    @Test
    public void testNamedPizza() {

        var templateIRI = "http://tpl.ottr.xyz/pizza/0.1/NamedPizza";

        var docttrWriter = new DTemplateWriter(new DependencyGraph(new FormatManager()), OTTR.getDefaultPrefixes());

        RDFIO.fileReader().parse(templateIRI)      // Read the RDF file at the templateIRI to a Model
            .mapToStream(new WTemplateParser())    // Parse the Model to a Template
            .innerForEach(docttrWriter);           // Load the Template into the DTemplateWriter

        var docttrOutput = docttrWriter.write(templateIRI);  // Get the docttrOutput by its IRI.

        //System.out.println(docttrOutput);

        Files.writeTemplatesTo(templateIRI, docttrOutput, ".html", outputFolder);


    }

}
