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
import java.util.stream.StreamSupport;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.ArgumentMap;
import xyz.ottr.lutra.bottr.model.ArgumentMaps;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public abstract class AbstractSPARQLSource implements Source<RDFNode> {

    // Used for ASK queries, disabled for now.
    //private static final Result<LiteralTerm> TRUE = TermFactory.createTypedLiteral("true", XSDDatatype.XSDboolean.getURI());
    //private static final Result<LiteralTerm> FALSE = TermFactory.createTypedLiteral("false", XSDDatatype.XSDboolean.getURI());

    protected abstract Result<QueryExecution> getQueryExecution(String query);

    private final PrefixMapping prefixes;

    protected AbstractSPARQLSource(PrefixMapping prefixes) {
        this.prefixes = prefixes;
    }

    protected AbstractSPARQLSource() {
        this(PrefixMapping.Factory.create());
    }

    protected Result<Query> getQuery(String queryString) {

        // Add prefixes to an empty query to get syntax correct
        Query prefixesOnly = QueryFactory.create();
        prefixesOnly.setPrefixMapping(this.prefixes);

        try {
            return Result.of(QueryFactory.create(prefixesOnly.serialize() + queryString));
        } catch (JenaException ex) {
            return Result.error("Error creating SPARQL query: " + ex.getMessage());
        }
    }


    public ResultStream<List<RDFNode>> execute(String query) {
        return streamQuery(query, Result::of);
    }

    public ResultStream<List<Argument>> execute(String query, ArgumentMaps argumentMaps) {
        return streamQuery(query, argumentMaps);
    }

    private void addQueryLimit(Query query) {
        int globalLimit = BOTTR.Settings.getRDFSourceQueryLimit();
        long currentLimit = query.getLimit();
        if (globalLimit > 0 && globalLimit < currentLimit) {
            query.setLimit(globalLimit);
        }
    }

    private <X> ResultStream<X> streamQuery(String query, Function<List<RDFNode>, Result<X>> translationFunction) {
        return getQueryExecution(query)
                .mapToStream(exec -> {
                    Query q = exec.getQuery();
                    if (q.isSelectType()) {
                        addQueryLimit(q);
                        ResultSet resultSet = exec.execSelect();
                        return getResultSetStream(resultSet, translationFunction);
                    //} else if (q.isAskType()) {
                    //    boolean system = exec.execAsk();
                    //    return ResultStream.innerOf(system ? TRUE : FALSE);
                    } else {
                        return ResultStream.of(Result.empty(Message.error(
                                "Unsupported SPARQL query type. Query must be SELECT.")));
                    }
                });
    }

    private <X> ResultStream<X> getResultSetStream(ResultSet resultSet, Function<List<RDFNode>, Result<X>> translationFunction) {

        List<String> columns = resultSet.getResultVars();
        // TODO: does this work when a get returns null? will there be a hole in the list? Must test.
        Function<QuerySolution, Result<X>> rowCreator = (sol) ->
            Result.of(
                columns.stream()
                    .map(sol::get)
                    .collect(Collectors.toList()))
            .flatMap(translationFunction);

        return new ResultStream<>(StreamSupport.stream(
            new Spliterators.AbstractSpliterator<>(Long.MAX_VALUE, Spliterator.ORDERED) {
                @Override
                public boolean tryAdvance(Consumer<? super Result<X>> action) {

                    if (!resultSet.hasNext()) {
                        return false;
                    } else {
                        action.accept(rowCreator.apply(resultSet.next()));
                        return true;
                    }
                }
            }, false));
    }

    @Override
    public ArgumentMap<RDFNode> createArgumentMap(PrefixMapping prefixMapping) {
        return new RDFNodeArgumentMap(prefixMapping);
    }

}
