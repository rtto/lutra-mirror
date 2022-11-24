package xyz.ottr.lutra.cli;

/*-
 * #%L
 * lutra-cli
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

import static org.hamcrest.CoreMatchers.is;

import org.apache.jena.rdf.model.Model;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class TestUtils {

    public static void testIsomorphicModels(Model actual, Model expected) {

        boolean isIsomorphic = actual.isIsomorphicWith(expected);

        if (isIsomorphic) {
            Assertions.assertTrue(isIsomorphic);
        } else { // if error, i.e., models are different, print nice error message

            // clear prefixes to better diff-ing
            actual.clearNsPrefixMap();
            expected.clearNsPrefixMap();

            String rdfActual = RDFIO.writeToString(actual);
            String rdfExpected = RDFIO.writeToString(expected);

            MatcherAssert.assertThat(rdfActual, is(rdfExpected));
        }
    }
}
