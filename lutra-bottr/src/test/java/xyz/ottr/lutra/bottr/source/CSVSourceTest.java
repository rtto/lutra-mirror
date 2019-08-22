package xyz.ottr.lutra.bottr.source;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.model.ValueMap;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.TabOTTR;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

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

public class CSVSourceTest {

    private static final Path ROOT = Paths.get("src", "test", "resources");

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void prototypeTest() throws IOException {

        // define prefixes
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        prefixes.setNsPrefix("ex", "http://example.com/ns#");

        // Write CSV file
        String root = this.testFolder.getRoot().getAbsolutePath();
        String csvFilename = root + "/data.csv";
        String csvContent = "Subject,Predicate,Object\n" // first row contains column names
            + "ex:A1,ex:B1,ex:C1\n"
            + "ex:A2,ex:B2,ex:C2\n"
            + "ex:A3,ex:B3,ex:C3\n";
        Files.write(Paths.get(csvFilename), csvContent.getBytes(Charset.forName("UTF-8")));

        // Set up map to translate source to triple instances
        ValueMap valMap = new ValueMap(prefixes, Arrays.asList(TabOTTR.TYPE_IRI, TabOTTR.TYPE_IRI, TabOTTR.TYPE_IRI));

        // H2 database to load CSV file
        Source<String> csvSource = new CSVSource();

        // map data to triples
        InstanceMap map = new InstanceMap(
            csvSource,
            "SELECT * FROM CSVREAD('" + csvFilename + "');",
            WOTTR.triple.toString(),
            valMap
        );

        // there should be three triples
        assertEquals(3, map.get().getStream().filter(Result::isPresent).count());
    }

    private Set<Record<String>> getExpectedResult() {
        //Create expected result
        Set<Record<String>> expected = new HashSet<>();
        expected.add(new Record<>(Arrays.asList("1", "Paulo", "2500")));
        expected.add(new Record<>(Arrays.asList("2", "Pedro", "2700")));
        expected.add(new Record<>(Arrays.asList("3", "Joao", "2800")));
        expected.add(new Record<>(Arrays.asList("4", "Maria", "2000")));
        expected.add(new Record<>(Arrays.asList("5", "Joselito", "1500")));
        expected.add(new Record<>(Arrays.asList("6", "Linhares", "2200")));
        expected.add(new Record<>(Arrays.asList("7", "Lagreca", "1000")));
        return expected;
    }

    @Test
    public void noHeader() {
        String input = getAbsolutePath("sources/csv/noheader.csv");
        CSVSource csvTest = new CSVSource();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "',"
                + "'ID,NAME,AGE,ADDRESS,SALARY', 'fieldSeparator=,');"));
    }

    @Test
    public void linuxSeparator() {
        String input = getAbsolutePath("sources/csv/linux.csv");
        CSVSource csvTest = new CSVSource();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "');"));
    }

    @Test
    public void windowsSeparator() {
        String input = getAbsolutePath("sources/csv/win.csv");
        CSVSource csvTest = new CSVSource();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "');"));
    }

    private String getAbsolutePath(String file) {
        return ROOT.resolve(file).toAbsolutePath().toString();
    }

    private void testAgainstExpectedResult(ResultStream<Record<String>> actualResult) {
        Set<Record<String>> dbOutput = actualResult.getStream()
            .filter(Result::isPresent)
            .map(Result::get)
            .collect(Collectors.toSet());
        assertEquals(getExpectedResult(), dbOutput);
    }

}