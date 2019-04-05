package xyz.ottr.lutra.bottr.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import xyz.ottr.lutra.bottr.model.Source.Row;
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
    public void test() {

        final String driver = "org.h2.Driver";
        final String url = "jdbc:h2:~/test";
        final String user = "user";
        final String pass = "pass";

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ResultStream<Row> rowStream = ResultStream.empty();
        ResultStream<Row> expected = ResultStream.empty();
        
        try {
            //Register driver
            Class.forName(driver);

            //Open connection
            conn = DriverManager.getConnection(url, user, pass);
            stmt = conn.createStatement();

            //Create table
            rs = stmt.executeQuery("CREATE TABLE CUSTOMER (id number, name varchar(20), age number, address varchar(20), salary number);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (1, 'Paulo', 32, 'Niteroi', 2500);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (2, 'Pedro', 33, 'Porto Alegre', 2700);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (3, 'Joao', 22, 'Sao Paulo', 2800);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (4, 'Maria', 24, 'Novo Hamburgo', 2000);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (5, 'Joselito', 36, 'Santa Maria', 1500);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (6, 'Linhares', 42, 'Viamao', 2200);");
            rs = stmt.executeQuery("INSERT into CUSTOMER values (7, 'Lagreca', 28, 'Sao Paulo', 1000);");

            //Create expected result
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("1", "Paulo", "2500"))));
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("2", "Pedro", "2700"))));
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("3", "Joao", "2800"))));
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("4", "Maria", "2000"))));
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("5", "Joselito", "1500"))));
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("6", "Linhares", "2200"))));
            expected = ResultStream.concat(expected, (ResultStream<Row>) Stream.of(new Row(Arrays.asList("7", "Lagreca", "1000"))));
            
            //Run the source
            JDBCSource jdbcTest = new JDBCSource(driver, url, user, pass);
            rowStream = jdbcTest.execute("SELECT ID, NAME, SALARY FROM CUSTOMERS;");
            
            //Compare rowStream to expected result
            Assert.assertEquals(expected.collect(Collectors.toList()),rowStream.collect(Collectors.toList()));
            
            //Clean up
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
                se2.printStackTrace();
            } //nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            } //end finally try
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            } //end finally try
        } //end try
        
    }

}