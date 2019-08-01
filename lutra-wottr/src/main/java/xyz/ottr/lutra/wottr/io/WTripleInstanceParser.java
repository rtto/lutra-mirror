package xyz.ottr.lutra.wottr.io;

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

import java.util.function.Supplier;

import org.apache.jena.rdf.model.Model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.WTermFactory;

public class WTripleInstanceParser implements Supplier<ResultStream<Instance>> {

    private final Model model;

    public WTripleInstanceParser(Model model) {
        this.model = model;
    }

    @Override
    public ResultStream<Instance> get() {

        ExtendedIterator<Result<Instance>> parsedTriples = this.model.listStatements()
            .filterDrop(statement -> isPartOfRDFList(statement))
            .mapWith(WTripleInstanceParser::createTripleInstance);
        return new ResultStream<>(parsedTriples.toSet());
    }

    /**
     * Returns true if the argument is a redundant list-triple, that is,
     * on one of the forms "(:a :b) rdf:first :a" or "(:a :b) rdf:rest (:b)".
     * These statements are redundant as they will be parsed as part of a listterm.
     */
    private boolean isPartOfRDFList(Statement statement) {

        Resource subject = statement.getSubject();
        Property predicate = statement.getPredicate();

        return subject.canAs(RDFList.class)
            && (predicate.equals(RDF.first) && this.model.contains(subject, RDF.rest))
                || predicate.equals(RDF.rest) && this.model.contains(subject, RDF.first);
    }

    private static Result<Instance> createTripleInstance(Statement stmt) {

        WTermFactory rdfTermFactory = new WTermFactory();
        Result<Term> sub = rdfTermFactory.apply(stmt.getSubject());
        Result<Term> pred = rdfTermFactory.apply(stmt.getPredicate());
        Result<Term> obj = rdfTermFactory.apply(stmt.getObject());

        ArgumentList as = sub.isPresent() && pred.isPresent() && obj.isPresent()
            ? new ArgumentList(sub.get(), pred.get(), obj.get()) : null;
        Result<ArgumentList> asRes = Result.ofNullable(as);
        asRes.addMessages(sub.getMessages());
        asRes.addMessages(pred.getMessages());
        asRes.addMessages(obj.getMessages());

        return asRes.map(asVal -> new Instance(OTTR.Bases.Triple, asVal));
    }
}
