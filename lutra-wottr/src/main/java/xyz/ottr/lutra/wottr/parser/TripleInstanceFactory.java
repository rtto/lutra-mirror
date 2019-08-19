package xyz.ottr.lutra.wottr.parser;

/*-
 * #%L
 * lutra-wottr
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
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class TripleInstanceFactory implements Supplier<ResultStream<Instance>> {

    private static final TermFactory rdfTermFactory = new TermFactory(WOTTR.theInstance);

    private final Model model;

    public TripleInstanceFactory(Model model) {
        this.model = model;
    }

    @Override
    public ResultStream<Instance> get() {

        Set<Result<Instance>> parsedTriples = this.model.listStatements()
            .mapWith(TripleInstanceFactory::createTripleInstance)
            .toSet();
        return new ResultStream<>(parsedTriples);
    }

    private static Result<Instance> createTripleInstance(Statement stmt) {

        List<Result<Term>> args = Stream.of(stmt.getSubject(), stmt.getPredicate(), stmt.getObject())
            .map(TripleInstanceFactory::createTerm)
            .collect(Collectors.toList());

        return Result.aggregate(args)
            .map(ArgumentList::new)
            .map(asVal -> new Instance(OTTR.BaseURI.NullableTriple, asVal));
    }

    /**
     * Make sure that we do not create any term lists.
     */
    private static Result<Term> createTerm(RDFNode node) {

        if (node.isURIResource()) {
            return rdfTermFactory.createTerm(node.asResource().getURI())
                .map(t -> (Term) t);
        } else if (node.isAnon()) {
            return rdfTermFactory.createBlankNodeTerm(node.asResource().getId().getBlankNodeId())
                .map(t -> (Term) t);
        } else if (node.isLiteral()) {
            return rdfTermFactory.createLiteralTerm(node.asLiteral())
                .map(t -> (Term) t);
        } else {
            throw new IllegalArgumentException("Error converting RDFNode " + RDFNodes.toString(node) + " to Term. ");
        }
    }
}
