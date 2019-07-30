package xyz.ottr.lutra.bottr.source;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.bottr.source.CSVSource;
import xyz.ottr.lutra.result.ResultStream;

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
        String input = getAbsolutePath("noheader.csv");
        CSVSource csvTest = new CSVSource();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "',"
                + "'ID,NAME,AGE,ADDRESS,SALARY', 'fieldSeparator=,');"));
    }

    @Test
    public void linuxSeparator() {
        String input = getAbsolutePath("linux.csv");
        CSVSource csvTest = new CSVSource();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "');"));
    }

    @Test
    public void windowsSeparator() {
        String input = getAbsolutePath("win.csv");
        CSVSource csvTest = new CSVSource();
        testAgainstExpectedResult(csvTest.execute("SELECT ID, NAME, SALARY FROM CSVREAD('" + input + "');"));
    }

    private String getAbsolutePath(String file) {
        return ROOT.resolve(file).toAbsolutePath().toString();
    }

    private void testAgainstExpectedResult(ResultStream<Record<String>> actualResult) {
        Set<Record<String>> dbOutput = actualResult.innerCollect(Collectors.toSet());
        Assert.assertEquals(getExpectedResult(), dbOutput);
    }

}