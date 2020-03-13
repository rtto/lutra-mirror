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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.RDFNodes;

public class TermSerializer implements Function<RDFNode, Result<Term>> {

    // TODO: Verify that this is correct. This only gives correct results if blank nodes across Jena models are unique.
    private static final Map<RDFList, Result<ListTerm>> createdLists = new HashMap<>();
    private static final Map<String, BlankNodeTerm> createdBlanks = new HashMap<>();

    public Result<Term> apply(RDFNode node) {
        return toTerm(node);
    }

    private Result<Term> toTerm(RDFNode node) {

        if (node.isResource()) {
            return toTerm(node.asResource());
        } else if (node.isLiteral()) {
            return toLiteralTerm(node.asLiteral()).map(tl -> (Term) tl);
        } else {
            return Result.error("Unable to parse RDFNode " + RDFNodes.toString(node) + " to Term.");
        }
    }

    private Result<Term> toTerm(Resource node) {

        if (node.isURIResource()) {
            return toTerm(node.getURI());
        } else if (node.canAs(RDFList.class)) {
            return toTermList(node.as(RDFList.class)).map(tl -> (Term) tl);
        } else if (node.isAnon()) {
            return toBlankNodeTerm(node.getId().getBlankNodeId()).map(tl -> (Term) tl);
        } else {
            return Result.error("Unable to parse resource " + RDFNodes.toString(node) + " to Term.");
        }
    }

    Result<Term> toTerm(String uri) {

        if (uri.equals(WOTTR.none.getURI())) {
            return Result.of(new NoneTerm());
        } else if (uri.equals(RDF.nil.getURI())) {
            return Result.of(new ListTerm());
        } else {
            return Result.of(new IRITerm(uri));
        }
    }

    private Result<ListTerm> toTermList(RDFList list) {

        if (createdLists.containsKey(list)) {
            return createdLists.get(list);
        } else {
            List<Result<Term>> terms = list.asJavaList().stream()
                .map(this)
                .collect(Collectors.toList());
            Result<List<Term>> aggTerms = Result.aggregate(terms);
            Result<ListTerm> resTermList = aggTerms.map(ListTerm::new);
            createdLists.put(list, resTermList);
            return resTermList;
        }
    }
    
    Result<LiteralTerm> toLiteralTerm(Literal literal) {

        // collect all "components" of literal, some may be blank or null
        String value = literal.getLexicalForm();
        String datatype = literal.getDatatypeURI();
        String language = literal.getLanguage();

        LiteralTerm literalTerm;
        // determine type of literal based on available "components"
        if (StringUtils.isNotEmpty(language)) {
            literalTerm = LiteralTerm.createLanguageTagLiteral(value, language);
        } else if (StringUtils.isNotEmpty(datatype)) {
            literalTerm = LiteralTerm.createTypedLiteral(value, datatype);
        } else {
            literalTerm = LiteralTerm.createPlainLiteral(value);
        }
        return Result.of(literalTerm);
    }


    Result<BlankNodeTerm> toBlankNodeTerm(BlankNodeId blankNodeId) {

        // Mint new labels, but keep map of which term was created
        // for which original (system) label
        String id = blankNodeId.getLabelString();
        return Result.of(createdBlanks.computeIfAbsent(id, _fresh -> new BlankNodeTerm()));
    }
}