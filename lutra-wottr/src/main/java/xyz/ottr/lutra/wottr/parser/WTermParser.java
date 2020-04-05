package xyz.ottr.lutra.wottr.parser;

/*-
 * #%L
 * lutra-wottr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.TermParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class WTermParser {

    private static final Map<RDFList, Result<ListTerm>> createdLists = new HashMap<>();

    public static Result<Term> toTerm(RDFNode node) {
        if (node.isResource()) {
            return toTerm(node.asResource());
        } else if (node.isLiteral()) {
            return toLiteralTerm(node.asLiteral()).map(tl -> (Term) tl);
        } else {
            return Result.error("Unable to parse RDFNode " + RDFNodeWriter.toString(node) + " to Term.");
        }
    }

    public static Result<Term> toTerm(Resource node) {

        if (node.isURIResource()) {
            return TermParser.toTerm(node.getURI());
        } else if (node.canAs(RDFList.class)) {
            return toListTerm(node.as(RDFList.class)).map(tl -> (Term) tl);
        } else if (node.isAnon()) {
            return toBlankNodeTerm(node.getId().getBlankNodeId()).map(tl -> (Term) tl);
        } else {
            return Result.error("Unable to parse resource " + RDFNodeWriter.toString(node) + " to Term.");
        }
    }

    private static Result<ListTerm> toListTerm(RDFList list) {

        if (createdLists.containsKey(list)) {
            return createdLists.get(list);
        } else {
            List<Result<Term>> terms = list.asJavaList().stream()
                .map(WTermParser::toTerm)
                .collect(Collectors.toList());
            Result<List<Term>> aggTerms = Result.aggregate(terms);
            Result<ListTerm> resTermList = aggTerms.map(ListTerm::new);
            createdLists.put(list, resTermList);
            return resTermList;
        }
    }

    public static Result<LiteralTerm> toLiteralTerm(Literal literal) {
        return TermParser.toLiteralTerm(literal.getLexicalForm(), literal.getDatatypeURI(), literal.getLanguage());
    }

    public static Result<BlankNodeTerm> toBlankNodeTerm(BlankNodeId blankNodeId) {
        return TermParser.toBlankNodeTerm(blankNodeId.getLabelString());
    }
}
