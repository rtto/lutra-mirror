package xyz.ottr.lutra.bottr.source;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.bottr.model.Record;
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

public class JDBCSource implements Source<String> {

    private final Logger log = LoggerFactory.getLogger(JDBCSource.class);

    private BasicDataSource dataSource;

    public JDBCSource(String databaseDriver, String databaseURL, String username, String password) {
        this.dataSource = new BasicDataSource();

        this.dataSource.setDriverClassName(databaseDriver);
        this.dataSource.setUsername(username);
        this.dataSource.setPassword(password);
        this.dataSource.setUrl(databaseURL);
    }
    
    @Override
    public ResultStream<Record<String>> execute(String query) {

        try (Connection conn = this.dataSource.getConnection()) {
            log.info("Running query: " + query);
            
            List<Record<String>> rows = new QueryRunner().query(conn, query, new ArrayListHandler())
                    .stream()
                    .map(array -> Arrays.asList(array)
                            .stream()
                            .map(value -> value.toString())
                            .collect(Collectors.toList()))
                    .map(Record::new)
                    .collect(Collectors.toList());
            
            return ResultStream.innerOf(rows);

        } catch (SQLException ex) {
            return ResultStream.of(Result.empty(Message.error(
                    "Error running query " + query 
                    + " over database " + this.dataSource.getUrl() 
                    + ": " + ex.getMessage())));
        }
    }
}
