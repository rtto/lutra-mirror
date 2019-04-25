package xyz.ottr.lutra.bottr;

/*-
 * #%L
 * lutra-bottr
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.jena.shared.PrefixMapping;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.model.ValueMap;
import xyz.ottr.lutra.bottr.source.JDBCSource;
import xyz.ottr.lutra.tabottr.TabOTTR;
import xyz.ottr.lutra.wottr.WOTTR;

public class H2CSVTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void test() throws IOException {

        String root = testFolder.getRoot().getAbsolutePath();

        // define prefixes
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        prefixes.setNsPrefix("ex", "http://example.com/ns#");

        // Write CSV file
        String csvFilename = root + "/data.csv";
        String csvContent = "Subject,Predicate,Object\n" // first row contains column names
                + "ex:A1,ex:B1,ex:C1\n"
                + "ex:A2,ex:B2,ex:C2\n"
                + "ex:A3,ex:B3,ex:C3\n";
        Files.write(Paths.get(csvFilename), csvContent.getBytes());
        
        // Set up map to translate source to triple instances
        ValueMap valMap = new ValueMap(prefixes, Arrays.asList(TabOTTR.TYPE_IRI, TabOTTR.TYPE_IRI, TabOTTR.TYPE_IRI));

        // H2 database to load CSV file
        Source<String> h2 = new JDBCSource(
                "org.h2.Driver",
                "jdbc:h2:" + root + "/db",
                "user",
                "");
        
        // map data to triples
        InstanceMap map = new InstanceMap(
                h2,
                "SELECT * FROM CSVREAD('" + csvFilename + "');",
                WOTTR.triple.toString(),
                valMap
                );

        // there should be three triples
        assertEquals(3, map.get().getStream().filter(r -> r.isPresent()).count());
    }

}
