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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import lombok.Builder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
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

    // For closing sources once all results are parsed
    private Connection connection;
    private Statement statement;
    private ResultSet queryResults;

    @Builder
    protected JDBCSource(String databaseDriver, String databaseURL, String username, String password) {
        this.dataSource = new BasicDataSource();

        this.dataSource.setDriverClassName(databaseDriver);
        this.dataSource.setUsername(username);
        this.dataSource.setPassword(password);
        this.dataSource.setUrl(databaseURL);
    }

    private <X> Spliterators.AbstractSpliterator<Result<X>> getAbstractSpliterator(
            ResultSet resultSet, Function<ResultSet, Result<X>> rowCreator) {
        return new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {

            @Override
            public boolean tryAdvance(Consumer<? super Result<X>> action) {

                try {
                    if (resultSet.next()) {
                        action.accept(rowCreator.apply(resultSet));
                        return true;
                    }
                } catch (SQLException ex) {
                    action.accept(
                        Result.error("ERROR: Error when fetching results from query: ", ex)
                    );
                } 

                try {
                    queryResults.close();
                    statement.close();
                    connection.close();
                } catch (SQLException ex) {
                    action.accept(
                        Result.error("ERROR: Error when closing connection to database: ", ex)
                    );
                }
                return false;
            }
        };
    }

    private Result<String> getValue(ResultSet res, int c) {
        try {
            return Result.of(res.getString(c));
        } catch (SQLException ex) {
            return Result.error("Error extracting value from result:", ex);
        }
    }

    private Result<List<String>> getRow(ResultSet res, int columns) {

        return Result.aggregate(
                IntStream.range(1, columns + 1) // ResultSets count from 1 (not 0)
                    .mapToObj(c -> getValue(res, c))
                    .collect(Collectors.toList())
                );
    }

    private <X> ResultStream<X> getResultSetStream(ResultSet resultSet, Function<List<String>, Result<X>> translationFunction)
        throws SQLException {

        int columns = resultSet.getMetaData().getColumnCount();
        Function<ResultSet, Result<X>> rowCreator = (res) -> getRow(res, columns).flatMap(translationFunction);

        return new ResultStream<>(StreamSupport.stream(
                    getAbstractSpliterator(resultSet, rowCreator), false));
    }

    private <X> ResultStream<X> streamQuery(String query, Function<List<String>, Result<X>> translationFunction) {

        var queryExcerpt = StringUtils.abbreviate(StringUtils.normalizeSpace(query), 40);

        try {
            this.connection = this.dataSource.getConnection();

            this.log.info("Running query: " + queryExcerpt);

            this.statement = this.connection.createStatement();
            this.queryResults = statement.executeQuery(query);

            if (!queryResults.isBeforeFirst()) {
                return ResultStream.of(Result.info("Query '" + queryExcerpt + "' returned no results."));
            }

            return getResultSetStream(queryResults, translationFunction);

        } catch (SQLException ex) {
            return ResultStream.of(Result.error(
                "Error running query '" + queryExcerpt + "' over database " + this.dataSource.getUrl() + ".", ex));
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
