package xyz.ottr.lutra.bottr.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

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

public class JDBCSourceTest {
  
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void prototypeTest() throws ClassNotFoundException, SQLException {

        String driver = "org.h2.Driver";
        String url = "jdbc:h2:" + this.testFolder.getRoot().getAbsolutePath() + "/db";
        String user = "user";
        String pass = "pass";

        Class.forName(driver);
        Connection conn = DriverManager.getConnection(url, user, pass);
        Statement stmt = conn.createStatement();
        
        //Create table
        stmt.execute("DROP TABLE IF EXISTS CUSTOMER");
        stmt.execute("CREATE TABLE CUSTOMER (id number, name varchar(20), age number, address varchar(20), salary number);");
        stmt.execute("INSERT into CUSTOMER values (1, 'Paulo', 32, 'Niteroi', 2500);");
        stmt.execute("INSERT into CUSTOMER values (2, 'Pedro', 33, 'Porto Alegre', 2700);");
        stmt.execute("INSERT into CUSTOMER values (3, 'Joao', 22, 'Sao Paulo', 2800);");
        stmt.execute("INSERT into CUSTOMER values (4, 'Maria', 24, 'Novo Hamburgo', 2000);");
        stmt.execute("INSERT into CUSTOMER values (5, 'Joselito', 36, 'Santa Maria', 1500);");
        stmt.execute("INSERT into CUSTOMER values (6, 'Linhares', 42, 'Viamao', 2200);");
        stmt.execute("INSERT into CUSTOMER values (7, 'Lagreca', 28, 'Sao Paulo', 1000);");

        //Create expected system
        Set<List<String>> expected = new HashSet<>();
        expected.add(List.of("1", "Paulo", "2500"));
        expected.add(List.of("2", "Pedro", "2700"));
        expected.add(List.of("3", "Joao", "2800"));
        expected.add(List.of("4", "Maria", "2000"));
        expected.add(List.of("5", "Joselito", "1500"));
        expected.add(List.of("6", "Linhares", "2200"));
        expected.add(List.of("7", "Lagreca", "1000"));

        // Run the source
        JDBCSource jdbcTest = new JDBCSource(driver, url, user, pass);

        ArgumentMaps<String> argMaps = new ArgumentMaps<>(PrefixMapping.Standard, jdbcTest);

        ResultStream<ArgumentList> rowStream = jdbcTest.execute("SELECT ID, NAME, SALARY FROM CUSTOMER;", argMaps);

        Set<List<String>> dbOutput = rowStream
            .getStream()
            .filter(Result::isPresent)
            .map(Result::get)
            .map(ArgumentList::asList)
            .map(list -> list.stream()
                .map(t -> (LiteralTerm)t)
                .map(LiteralTerm::getPureValue)
                .collect(Collectors.toList()))
            .collect(Collectors.toSet());

        //Compare dbOutput to expected system
        Assert.assertEquals(expected, dbOutput);

        //Clean up
        stmt.close();
        conn.close();
    }

}