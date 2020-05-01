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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.bottr.model.ArgumentMap;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class JDBCSource implements Source<String> {

    private static final Logger log = LoggerFactory.getLogger(JDBCSource.class);

    private final BasicDataSource dataSource;

    @Builder
    protected JDBCSource(String databaseDriver, String databaseURL, String username, String password) {
        this.dataSource = new BasicDataSource();

        this.dataSource.setDriverClassName(databaseDriver);
        this.dataSource.setUsername(username);
        this.dataSource.setPassword(password);
        this.dataSource.setUrl(databaseURL);
    }

    private <X> ResultStream<X> streamQuery(String query, Function<List<String>, Result<X>> translationFunction) {
        try (Connection conn = this.dataSource.getConnection()) {
            this.log.info("Running query: " + query);

            List<Object[]> queryResult = new QueryRunner().query(conn, query, new ArrayListHandler());

            Stream<Result<X>> stream = queryResult.stream()
                .map(array -> Arrays.stream(array)
                    .map(value -> Objects.toString(value, null)) // the string value of null is (the object) null.
                    .collect(Collectors.toList()))
                .map(translationFunction);

            return new ResultStream<>(stream);

        } catch (SQLException ex) {
            return ResultStream.of(Result.error(
                "Error running query " + query + " over database " + this.dataSource.getUrl() + ": " + ex.getMessage()));
        }
    }

    @Override
    public ResultStream<List<String>> execute(String query) {
        return streamQuery(query, Result::of);
    }

    @Override
    public ResultStream<List<Argument>> execute(String query, ArgumentMaps<String> argumentMaps) {
        return streamQuery(query, argumentMaps);
    }

    @Override
    public ArgumentMap<String> createArgumentMap(PrefixMapping prefixMapping) {
        return new StringArgumentMap(prefixMapping);
    }

}
