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

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import xyz.ottr.lutra.io.InstanceReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.store.expansion.NonCheckingExpander;
import xyz.ottr.lutra.system.Assertions;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.io.RDFIO;
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

            String rdfActual = RDFIO.writeToString(actual);
            String rdfExpected = RDFIO.writeToString(expected);

            Assert.assertThat(rdfActual, is(rdfExpected));
        }
    }


    // read RDF file, expand instances (only base instances), and return OTTR parsed RDF model
    public static Model getOTTRParsedRDFModel(String filename) {

        TemplateStore store = new StandardTemplateStore(null);
        store.addOTTRBaseTemplates();
        Expander expander = new NonCheckingExpander(store); 

        InstanceReader insReader = new InstanceReader(RDFIO.fileReader(), new WInstanceParser());
        ResultStream<Instance> expandedInInstances = insReader
            .apply(filename)
            .innerFlatMap(expander::expandInstance);

        // Write expanded instances to model
        WInstanceWriter insWriter = new WInstanceWriter();
        ResultConsumer<Instance> expansionErrors = new ResultConsumer<>(insWriter);
        expandedInInstances.forEach(expansionErrors);
        Assertions.noErrors(expansionErrors);
        return insWriter.writeToModel();
    }


}
