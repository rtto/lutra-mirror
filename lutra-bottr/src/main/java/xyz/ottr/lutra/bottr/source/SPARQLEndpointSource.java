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

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import xyz.ottr.lutra.bottr.model.Row;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

public class SPARQLEndpointSource implements Source {

    private String endpointURL;

    public SPARQLEndpointSource(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    @Override
    public ResultStream<Row> execute(String query) {

        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.endpointURL, query);
        Query q = qexec.getQuery();
        if (q.isSelectType()) {
            ResultSet resultSet = qexec.execSelect();
            return new ResultStream<Row>(streamResultSet(resultSet).map(Result::of));
        } else if (q.isAskType()) {
            boolean result = qexec.execAsk();
            return ResultStream.innerOf(new Row(result));
        } else {
            return ResultStream.of(Result.empty(Message.error(
                    "Unsupported SPARQL query type. Query must be SELECT or ASK.")));
        }
    }

    private Stream<Row> streamResultSet(ResultSet resultSet) {

        final List<String> columns = resultSet.getResultVars();
        // TODO: does this work when a get returns null? will there be a hole in the list? Must test.
        final Function<QuerySolution, Row> rowCreator = (sol) -> new Row(
                columns.stream()
                .map(c -> sol.get(c))
                .collect(Collectors.toList()));

        Stream<Row> stream = StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Row>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    @Override
                    public boolean tryAdvance(Consumer<? super Row> action) {
                        if (!resultSet.hasNext()) { 
                            return false;
                        } else { 
                            action.accept(rowCreator.apply(resultSet.next()));
                            return true;
                        }
                    }
                }, false);

        return stream;
    }
    
}
