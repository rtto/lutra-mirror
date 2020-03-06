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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.junit.Assert;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.graph.DependencyGraph;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFFileReader;
import xyz.ottr.lutra.wottr.parser.WInstanceParser;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;

public enum ModelUtils {

    ; // singleton enum utility class

    public static void testIsomorphicModels(Model actual, Model expected) {

        boolean isIsomorphic = actual.isIsomorphicWith(expected);

        if (isIsomorphic) {
            Assert.assertTrue(isIsomorphic);
        } else { // if error, i.e., models are different, print nice error message

            // clear prefixes to better diff-ing
            actual.clearNsPrefixMap();
            expected.clearNsPrefixMap();

            String rdfActual = ModelIO.writeModel(actual, Lang.TURTLE);
            String rdfExpected = ModelIO.writeModel(expected, Lang.TURTLE);

            Assert.assertThat(rdfActual, is(rdfExpected));
        }
    }


    // read RDF file, expand instances (only base instances), and return OTTR parsed RDF model
    public static Model getOTTRParsedRDFModel(String filename) {

        TemplateStore store = new DependencyGraph(null);
        store.addOTTRBaseTemplates();

        InstanceReader insReader = new InstanceReader(new RDFFileReader(), new WInstanceParser());
        ResultStream<Instance> expandedInInstances = insReader
            .apply(filename)
            .innerFlatMap(store::expandInstance);

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        assertFalse(Message.moreSevere(expansionErrors.getMessageHandler().printMessages(),
            Message.ERROR)); // No errors when expanding
        return insWriter.writeToModel();
    }


}
