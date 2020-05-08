package xyz.ottr.lutra.cli;

import static org.hamcrest.CoreMatchers.is;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import xyz.ottr.lutra.wottr.io.RDFIO;

public class TestUtils {

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
}
