package xyz.ottr.lutra.wottr.parser;

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

import static org.junit.Assert.assertTrue;

import org.apache.jena.rdf.model.Model;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.expansion.NonCheckingExpander;
import xyz.ottr.lutra.store.graph.StandardTemplateStore;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;

public class BlankNodeTest {

    private static final Logger log = LoggerFactory.getLogger(BlankNodeTest.class);

    // Tests if a template including a blank node is correctly instantiated.

    // Input:
    //   * instances: correct/instances/blank1/in.ttl
    //   * template definition: correct/definitions/Blank.ttl
    // Check that the expansion is isomorphic to:
    // instances: correct/instances/blank1/out.ttl

    @Test
    public void shouldBeIsomorphic() {

        TemplateStore store = new StandardTemplateStore(null);
        store.addOTTRBaseTemplates();
        Expander expander = new NonCheckingExpander(store);

        // Read templates
        TemplateReader tempReader = new TemplateReader(RDFIO.fileReader(), new WTemplateParser());
        ResultStream<String> tempIRI = ResultStream.innerOf("src/test/resources/correct/definitions/core/Blank.ttl");
        MessageHandler errorHandler = tempReader.populateTemplateStore(store, tempIRI);
        Assertions.noErrors(errorHandler);

        // Read in-instances and expand
        InstanceReader insReader = new InstanceReader(RDFIO.fileReader(), new WInstanceParser());
        ResultStream<Instance> expandedInInstances = insReader
            .apply("src/test/resources/correct/instances/blank1/in.ttl")
            .innerFlatMap(expander::expandInstance);

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        Assertions.noErrors(expansionErrors);

        Model in = insWriter.writeToModel();

        // Read out-model
        Result<Model> outRes = RDFIO.fileReader().parse("src/test/resources/correct/instances/blank1/out.ttl");
        if (!outRes.isPresent()) {
            log.error(outRes.toString());
        }
        assertTrue(outRes.isPresent());
        Model out = outRes.get();

        ModelUtils.testIsomorphicModels(in, out);
    }
}
