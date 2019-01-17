package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Model;

import org.dyreriket.gaupa.rdf.ModelIO;
import org.dyreriket.gaupa.rdf.ModelIOException;

import org.junit.Test;

import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.MessageHandler;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultConsumer;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.store.DependencyGraph;
import xyz.ottr.lutra.store.TemplateStore;

import xyz.ottr.lutra.wottr.io.WFileReader;
import xyz.ottr.lutra.wottr.io.WInstanceParser;
import xyz.ottr.lutra.wottr.io.WInstanceWriter;
import xyz.ottr.lutra.wottr.io.WTemplateParser;

public class BlankNodeTest {


    // Tests if a template including a blank node is correctly instantiated.

    // Input:
    //   * instances: correct/instances/blank1/in.ttl
    //   * template definition: correct/definitions/Blank.ttl
    // Check that the expansion is isomorphic to:
    // instances: correct/instances/blank1/out.ttl

    @Test
    public void shouldBeIsomorphic() {

        TemplateStore store = new DependencyGraph();

        // Read templates
        TemplateReader tempReader = new TemplateReader(new WFileReader(), new WTemplateParser());
        ResultStream<String> tempIRI = ResultStream.innerOf("src/test/resources/correct/definitions/core/Blank.ttl");
        MessageHandler errorMessages = tempReader.populateTemplateStore(store, tempIRI);
        assertFalse(Message.moreSevere(errorMessages.printMessages(),
                Message.ERROR)); // No errors when parsing

        // Read in-instances and expand
        InstanceReader insReader = new InstanceReader(new WFileReader(), new WInstanceParser());
        ResultStream<Instance> expandedInInstances = insReader
            .apply("src/test/resources/correct/instances/blank1/in.ttl")
            .innerFlatMap(ins -> store.expandInstance(ins));

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<Instance>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        assertFalse(Message.moreSevere(expansionErrors.getMessageHandler().printMessages(),
                Message.ERROR)); // No errors when expanding
        Model in = insWriter.writeToModel();

        // Read out-model
        Result<Model> outRes = new WFileReader().parse("src/test/resources/correct/instances/blank1/out.ttl");
        if (!outRes.isPresent()) {
            System.err.println("ERROR:");
            System.err.println(outRes.toString());
        }
        assertTrue(outRes.isPresent());
        Model out = outRes.get();

        try {
            ModelIO.printModel(in, ModelIO.Format.TURTLE);
            ModelIO.printModel(out, ModelIO.Format.TURTLE);
        } catch (ModelIOException ex) {
            ex.printStackTrace();
        }

        assertTrue(in.isIsomorphicWith(out));
    }
}
