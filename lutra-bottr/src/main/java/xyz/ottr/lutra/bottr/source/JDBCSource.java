package xyz.ottr.lutra.bottr.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import xyz.ottr.lutra.bottr.model.Source;
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

public class JDBCSource implements Source {
    
    private final String databaseDriver;
    private final String databaseURL;
    private final String username;
    private final String password;

    public JDBCSource(String databaseDriver, String databaseURL, String username, String password) {
        this.databaseDriver = databaseDriver;
        this.databaseURL = databaseURL;
        this.username = username;
        this.password = password;
    }
   
    public ResultStream<Row> execute(String query) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ResultStream<Row> rowStream = ResultStream.empty();

        try {
            //Register driver
            Class.forName(this.databaseDriver);

            //Open connection
            conn = DriverManager.getConnection(this.databaseURL, this.username, this.password);

            //Execute query
            stmt = conn.prepareStatement("?");
            stmt.setString(1, query);
            rs = stmt.executeQuery();

            //Parse the data
            int colcount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                List<String> rowAsList = new ArrayList<>();
                for (int i = 1; i <= colcount; i++) {
                    rowAsList.add(rs.getString(i));
                }
                rowStream = ResultStream.concat(rowStream, (ResultStream<Row>) Stream.of(new Row(rowAsList)));
            }

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
        return rowStream;
    }

}
