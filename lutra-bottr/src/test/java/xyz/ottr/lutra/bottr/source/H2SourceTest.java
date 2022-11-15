package xyz.ottr.lutra.bottr.source;

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

import static org.hamcrest.CoreMatchers.is;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.WOTTR;

public class H2SourceTest {

    private static final Path ROOT = Paths.get("src", "test", "resources");

    @TempDir
    private Path testFolder;

    @Test
    public void prototypeTest() throws IOException {

        // define prefixes
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        prefixes.setNsPrefix("ex", "http://example.com/ns#");

        // Write CSV file
        String root = testFolder.toAbsolutePath().toString();
        String csvFilename = root + "/data.csv";
        String csvContent = "Subject,Predicate,Object\n" // first row contains column names
            + "ex:A1,ex:B1,ex:C1\n"
            + "ex:A2,ex:B2,ex:C2\n"
            + "ex:A3,ex:B3,ex:C3\n";
        Files.write(Paths.get(csvFilename), csvContent.getBytes(Charset.forName("UTF-8")));

        // H2 database to load CSV file
        Source<String> csvSource = new H2Source();

        // Set up map to translate source to triple instances
        ArgumentMaps<String> valMap = new ArgumentMaps(prefixes, csvSource);

        // map data to triples
        InstanceMap<String> map = InstanceMap.<String>builder()
            .source(csvSource)
            .query("SELECT * FROM CSVREAD('" + csvFilename + "');")
            .templateIRI(WOTTR.triple.toString())
            .argumentMaps(valMap)
            .build();

        // there should be three triples
        MatcherAssert.assertThat(map.get().getStream().filter(Result::isPresent).count(), is(3L));
    }

    private Set<List<String>> getExpectedResult() {
        //Create expected system
        Set<List<String>> expected = new HashSet<>();
        expected.add(List.of("1", "Paulo", "2500"));
        expected.add(List.of("2", "Pedro", "2700"));
        expected.add(List.of("3", "Joao", "2800"));
        expected.add(List.of("4", "Maria", "2000"));
        expected.add(List.of("5", "Joselito", "1500"));
        expected.add(List.of("6", "Linhares", "2200"));
        expected.add(List.of("7", "Lagreca", "1000"));
        return expected;
    }

    @Test
    public void noHeader() {
        String input = getAbsolutePath("sources/csv/noheader.csv");
        H2Source csvTest = new H2Source();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "',"
                + "'ID,NAME,AGE,ADDRESS,SALARY', 'fieldSeparator=,');"));
    }

    @Test
    public void linuxSeparator() {
        String input = getAbsolutePath("sources/csv/linux.csv");
        H2Source csvTest = new H2Source();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "');"));
    }

    @Test
    public void windowsSeparator() {
        String input = getAbsolutePath("sources/csv/win.csv");
        H2Source csvTest = new H2Source();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "');"));
    }

    private String getAbsolutePath(String file) {
        return ROOT.resolve(file).toAbsolutePath().toString();
    }

    private void testAgainstExpectedResult(ResultStream<List<String>> actualResult) {
        Set<List<String>> dbOutput = actualResult.getStream()
            .filter(Result::isPresent)
            .map(Result::get)
            .collect(Collectors.toSet());
        MatcherAssert.assertThat(dbOutput, is(getExpectedResult()));
    }

}