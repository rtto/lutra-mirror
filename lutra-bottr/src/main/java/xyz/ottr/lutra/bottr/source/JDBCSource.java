package xyz.ottr.lutra.bottr.source;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.bottr.model.Row;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
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

    private final Logger log = LoggerFactory.getLogger(JDBCSource.class);
    
    private final String databaseDriver;
    private final String databaseURL;
    private final String username;
    private final String password;

    private Connection conn;

    public JDBCSource(String databaseDriver, String databaseURL, String username, String password) {
        this.databaseDriver = databaseDriver;
        this.databaseURL = databaseURL;
        this.username = username;
        this.password = password;
    }

    private void openConnection() throws ClassNotFoundException, SQLException {
        Class.forName(this.databaseDriver);
        conn = DriverManager.getConnection(this.databaseURL, this.username, this.password);
    }
    
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultStream<Row> execute(String query) {
        
        // open connection if necessary, and abort if not possible
        try {
            if (conn == null || conn.isClosed()) {
                openConnection();
            }
        } catch (ClassNotFoundException | SQLException e) {
            return ResultStream.of(Result.empty(Message.fatal(
                    "Cannot connect to database " + this.databaseURL 
                    + " with driver " + this.databaseDriver
                    + " and with user " + this.username 
                    + ": " + e.getMessage())));
        }

        // Collect results in list
        List<Row> result = new ArrayList<>();
        
        // Execute query
        try {
            Statement stmt = conn.createStatement();
            
            log.info("Running query: " + query);
            ResultSet rs = stmt.executeQuery(query);

            // Parse the data
            int colcount = rs.getMetaData().getColumnCount();

            while (rs.next()) {
                List<String> rowAsList = new ArrayList<>();
                for (int i = 1; i <= colcount; i++) {
                    rowAsList.add(rs.getString(i));
                }
                result.add(new Row(rowAsList));
            }
            log.info("Rows collected: " + result.size());

            // Clean up
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            return ResultStream.of(Result.empty(Message.error(
                    "Error running query " + query 
                    + " over database " + this.databaseURL 
                    + ": " + e.getMessage())));
        }

        return ResultStream.innerOf(result);
    }
}
