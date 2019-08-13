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
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.LiteralTerm;
import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.vocabulary.WOTTRVocabulary;

public class TermFactory implements Function<RDFNode, Result<Term>> {

    // TODO: Verify that this is correct. This only gives correct results if blank nodes
    // across Jena models are unique.
    private static final Map<RDFList, Result<TermList>> createdLists = new HashMap<>();
    private static final Map<String, BlankNodeTerm> createdBlanks = new HashMap<>();

    private final WOTTRVocabulary vocabulary;

    public TermFactory(WOTTRVocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public Result<Term> apply(RDFNode node) {

        if (node.isURIResource()) {
            if (node.toString().equals(this.vocabulary.getNoneResource().getURI())) {
                return Result.of(new NoneTerm());
            } else if (node.equals(RDF.nil)) {
                return Result.of(new TermList());
            } else {
                return Result.of(createIRITerm(node.asResource()));
            }
        } else if (node.canAs(RDFList.class)) {
            return createTermList(node.as(RDFList.class)).map(tl -> (Term) tl); // Need to cast to Result<Term>
        } else if (node.isAnon()) {
            return Result.of(createBlankNodeTerm(node.asResource()));
        } else if (node.isLiteral()) {
            return Result.of(createLiteralTerm(node.asLiteral()));
        } else {
            return Result.error("Unable to parse RDFNode " + node.toString() + " to Term.");
        }
    }

    public Result<TermList> createTermList(RDFList list) {

        if (createdLists.containsKey(list)) {
            return createdLists.get(list);
        } else {
            List<Result<Term>> terms = list.asJavaList().stream()
                .map(this)
                .collect(Collectors.toList());
            Result<List<Term>> aggTerms = Result.aggregate(terms);
            Result<TermList> resTermList = aggTerms.map(TermList::new);
            createdLists.put(list, resTermList);
            return resTermList;
        }
    }
    
    public static LiteralTerm createLiteralTerm(Literal literal) {

        // collect all "components" of literal, some may be blank or null
        String value = literal.getLexicalForm();
        String datatype = literal.getDatatypeURI();
        String language = literal.getLanguage();

        // determine type of literal based on available "components"
        if (StringUtils.isNotEmpty(language)) {
            return LiteralTerm.taggedLiteral(value, language);
        } else if (StringUtils.isNotEmpty(datatype)) {
            return LiteralTerm.typedLiteral(value, datatype);
        } else {
            return new LiteralTerm(value);
        }
    }

    public static IRITerm createIRITerm(Resource resource) {

        return new IRITerm(resource.getURI());
    }

    public static BlankNodeTerm createBlankNodeTerm(Resource resource) {

        // Mint new labels, but keep map of which term was created
        // for which original (system) label
        String id = resource.getId().getBlankNodeId().getLabelString();
        return createdBlanks.computeIfAbsent(id, _fresh -> new BlankNodeTerm());
    }
}
