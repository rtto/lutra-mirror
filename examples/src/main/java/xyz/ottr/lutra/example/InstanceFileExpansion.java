package xyz.ottr.lutra.example;

/*-
 * #%L
 * examples
 * %%
 * Copyright (C) 2018 - 2025 University of Oslo
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

import java.util.List;
import java.util.function.Function;
import xyz.ottr.lutra.api.StandardFormat;
import xyz.ottr.lutra.api.StandardTemplateManager;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.ResultStream;

/**
 * Demonstrates how to read, expand, and write OTTR instance files using the Lutra Java API.
 * Key steps in this example:
 * <ul>
 *   <li>Initialize a {@code StandardTemplateManager} with online template fetching enabled.</li>
 *   <li>Read input instances in stOTTR format from the local resource folder.</li>
 *   <li>Expand instances.</li>
 *   <li>Output the expanded instances in RDF format to the console.</li>
 *   <li>Print any messages (warnings or errors) encountered during processing.</li>
 * </ul>
 */
public class InstanceFileExpansion {

    public static void main(String[] args) {

        // The folder where our input files are kept.
        String resourceFolder = "examples/src/main/resources/";

        // The StandardTemplateManager comes preloaded with the templates in tlp.ottr.xyz and all standard formats
        // (with their readers and writers). For an empty template manager, use TemplateManager().
        StandardTemplateManager templateManager = new StandardTemplateManager();

        // Allow the template manager to fetch templates published online at the template URI.
        templateManager.setFetchMissingDependencies(true);

        // Read instances, which we know are on stOTTR format.
        ResultStream<Instance> instances = templateManager.readInstances(
                StandardFormat.stottr.name(),
                List.of(resourceFolder + "pizza-instances.stottr")); // the method expects a list of file/folder names

        // Get template instance expander from template manager.
        Function<Instance, ResultStream<Instance>> expander = templateManager.makeExpander();

        // Apply expander to read instances.
        ResultStream<Instance> expandedInstances = instances.innerFlatMap(expander);

        // Write expanded instances to stdout, and collect any messages generated.
        MessageHandler msgsInstances = templateManager.writeInstances(
                expandedInstances,
                StandardFormat.wottr.name(),
                null, // we don't write to file
                System.out);

        // Print any messages. Note that this will print any errors after any output is written.
        msgsInstances.printMessages();
    }
}