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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.ArgumentBuilder;
import xyz.ottr.lutra.parser.InstanceBuilder;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.parser.TermParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class WTripleInstanceParser implements InstanceParser<Model> {

    @Override
    public ResultStream<Instance> apply(Model model) {
        return ResultStream.of(model.listStatements()
                .mapWith(WTripleInstanceParser::instance)
        );
    }

    private static Result<Instance> instance(Statement stmt) {

        var arguments = Stream.of(stmt.getSubject(), stmt.getPredicate(), stmt.getObject())
            .map(WTripleInstanceParser::term)
            .map(t -> ArgumentBuilder.builder().term(t).build())
            .collect(Collectors.toList());

        return InstanceBuilder.builder()
            .iri(Result.of(OTTR.BaseURI.NullableTriple))
            .arguments(Result.aggregate(arguments))
            .build();
    }

    /**
     * Make sure that we do not create any term lists.
     */
    private static Result<Term> term(RDFNode node) {

        if (node.isURIResource()) {
            return TermParser.toIRITerm(node.asResource().getURI())
                .map(t -> (Term) t);
        } else if (node.isAnon()) {
            return WTermParser.toBlankNodeTerm(node.asResource().getId())
                .map(t -> (Term) t);
        } else if (node.isLiteral()) {
            return WTermParser.toLiteralTerm(node.asLiteral())
                .map(t -> (Term) t);
        } else {
            throw new IllegalArgumentException("Error converting RDFNode " + RDFNodeWriter.toString(node) + " to Term. ");
        }
    }


}
