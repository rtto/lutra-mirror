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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.bottr.model.ArgumentMap;
import xyz.ottr.lutra.bottr.model.Record;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;

public abstract class AbstractSPARQLSource implements Source<RDFNode> {

    private static final Literal TRUE = ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean);
    private static final Literal FALSE = ResourceFactory.createTypedLiteral("false", XSDDatatype.XSDboolean);

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

    @Override
    public ResultStream<Record<RDFNode>> execute(String query) {
        return getQueryExecution(query)
                .mapToStream(exec -> {
                    Query q = exec.getQuery();
                    if (q.isSelectType()) {
                        ResultSet resultSet = exec.execSelect();
                        return getResultSetStream(resultSet);
                    } else if (q.isAskType()) {
                        boolean result = exec.execAsk();
                        return ResultStream.innerOf(new Record<>(result ? TRUE : FALSE));
                    } else {
                        return ResultStream.of(Result.empty(Message.error(
                                "Unsupported SPARQL query type. Query must be SELECT or ASK.")));
                    }
                });
    }

    private ResultStream<Record<RDFNode>> getResultSetStream(ResultSet resultSet) {

        final List<String> columns = resultSet.getResultVars();
        // TODO: does this work when a get returns null? will there be a hole in the list? Must test.
        final Function<QuerySolution, Result<Record<RDFNode>>> rowCreator = (sol) -> Result.of(new Record<>(
                columns.stream()
                .map(sol::get)
                .collect(Collectors.toList())));

        return new ResultStream<>(StreamSupport.stream(
                new Spliterators.AbstractSpliterator<Result<Record<RDFNode>>>(Long.MAX_VALUE, Spliterator.ORDERED) {
                    @Override
                    public boolean tryAdvance(Consumer<? super Result<Record<RDFNode>>> action) {
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
