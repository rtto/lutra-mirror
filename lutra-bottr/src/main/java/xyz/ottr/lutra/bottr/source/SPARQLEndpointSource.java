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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

public class SPARQLEndpointSource implements Source<RDFNode> {
    
    private static final Literal TRUE = ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean);
    private static final Literal FALSE = ResourceFactory.createTypedLiteral("false", XSDDatatype.XSDboolean);

    private String endpointURL;

    public SPARQLEndpointSource(String endpointURL) {
        this.endpointURL = endpointURL;
    }

    @Override
    public ResultStream<Record<RDFNode>> execute(String query) {

        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.endpointURL, query);
        Query q = qexec.getQuery();
        if (q.isSelectType()) {
            ResultSet resultSet = qexec.execSelect();
            return new ResultStream<Record<RDFNode>>(streamResultSet(resultSet).map(Result::of));
        } else if (q.isAskType()) {
            boolean result = qexec.execAsk();
            return ResultStream.innerOf(new Record<RDFNode>(result ? TRUE : FALSE));
        } else {
            return ResultStream.of(Result.empty(Message.error(
                    "Unsupported SPARQL query type. Query must be SELECT or ASK.")));
        }
    }

    private Stream<Record<RDFNode>> streamResultSet(ResultSet resultSet) {

        final List<String> columns = resultSet.getResultVars();
        // TODO: does this work when a get returns null? will there be a hole in the list? Must test.
        final Function<QuerySolution, Record<RDFNode>> rowCreator = (sol) -> new Record<>(
                columns.stream()
                .map(c -> sol.get(c))
                .collect(Collectors.toList()));

        Stream<Record<RDFNode>> stream = StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Record<RDFNode>>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    @Override
                    public boolean tryAdvance(Consumer<? super Record<RDFNode>> action) {
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
