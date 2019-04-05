package xyz.ottr.lutra.bottr.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

import xyz.ottr.lutra.bottr.model.Row;
import xyz.ottr.lutra.bottr.source.JDBCSource;
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

public class JDBCSourceTest {

    @Test
    public void prototypeTest() throws ClassNotFoundException, SQLException {

        final String driver = "org.h2.Driver";
        final String url = "jdbc:h2:~/test";
        final String user = "user";
        final String pass = "pass";

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

        //Create expected result
        Set<Row> expected = new HashSet<>();
        expected.add(new Row(Arrays.asList("1", "Paulo", "2500")));
        expected.add(new Row(Arrays.asList("2", "Pedro", "2700")));
        expected.add(new Row(Arrays.asList("3", "Joao", "2800")));
        expected.add(new Row(Arrays.asList("4", "Maria", "2000")));
        expected.add(new Row(Arrays.asList("5", "Joselito", "1500")));
        expected.add(new Row(Arrays.asList("6", "Linhares", "2200")));
        expected.add(new Row(Arrays.asList("7", "Lagreca", "1000")));

        //Run the source
        JDBCSource jdbcTest = new JDBCSource(driver, url, user, pass);
        ResultStream<Row> rowStream = jdbcTest.execute("SELECT ID, NAME, SALARY FROM CUSTOMER;");
        Set<Row> dbOutput = rowStream.innerCollect(Collectors.toSet());

        //Compare dbOutput to expected result
        Assert.assertEquals(dbOutput, expected);

        //Clean up
        stmt.close();
        conn.close();
    }

}