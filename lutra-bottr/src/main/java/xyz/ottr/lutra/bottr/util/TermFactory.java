package xyz.ottr.lutra.bottr.util;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.xerces.util.URI;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.terms.TermList;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.WOTTRVocabulary;

// TODO suggest to move this to core.mode.terms, see issue #190 and align with xyz.ottr.lutra.wottr.parser.TermFactory.

public class TermFactory {

    private final Map<RDFList, Result<TermList>> createdLists = new HashMap<>();
    private final Map<String, BlankNodeTerm> labelledBlanks = new HashMap<>();

    private final WOTTRVocabulary vocabulary;
    private final PrefixMapping prefixMapping;

    public TermFactory(WOTTRVocabulary vocabulary, PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
        this.vocabulary = vocabulary;
    }

    public static Result<BlankNodeTerm> createBlankNode() {
        return Result.of(new BlankNodeTerm());
    }

    public Result<BlankNodeTerm> createBlankNode(String value) {
        return Result.of(this.labelledBlanks.computeIfAbsent(value, __ -> new BlankNodeTerm()));
    }

    public Result<BlankNodeTerm> createBlankNode(BlankNodeId blankNodeId) {
        return createBlankNode(blankNodeId.getLabelString());
    }

    public static Result<Term> createNone() {
        return Result.of(new NoneTerm());
    }

    public Result<IRITerm> createIRI(String value) {
        return Result.of(value)
            .map(this.prefixMapping::expandPrefix)
            .flatMap(DataParser::asURI)
            .map(URI::toString)
            .map(IRITerm::new);
    }

    public static Result<LiteralTerm> createTypedLiteral(String value, String datatype) {
        return DataParser.asURI(datatype)
            .map(URI::toString)
            .map(iri -> LiteralTerm.typedLiteral(value, iri));
    }

    public static Result<LiteralTerm> createLangLiteral(String value, String languageTag) {
        return DataParser.asLanguageTagString(languageTag)
            .map(tag -> LiteralTerm.taggedLiteral(value, tag));
    }

    public static Result<LiteralTerm> createPlainLiteral(String value) {
        return Result.of(new LiteralTerm(value));
    }

    public static Result<LiteralTerm> createLiteral(String value, String datatype, String language) {

        if (StringUtils.isNotEmpty(language) && !RDF.langString.getURI().equals(datatype)) {
            return Result.error("Error creating literal. Cannot have a language tag: " + language
                + " and the datatype: " + datatype);
        } else if (StringUtils.isNotEmpty(language)) {
            return createLangLiteral(value, language);
        } else if (StringUtils.isNotEmpty(datatype)) {
            return createTypedLiteral(value, datatype);
        } else {
            return createPlainLiteral(value);
        }
    }

    public Result<Term> createTermByType(String value, BasicType type) {
        if (type.isSubTypeOf(TypeFactory.IRI)) {
            return createIRI(value).map(t -> (Term)t);
        } else if (type.equals(TypeFactory.LITERAL)) {
            return createPlainLiteral(value).map(t -> (Term)t);
        } else if (type.isProperSubTypeOf(TypeFactory.LITERAL)) {
            return createTypedLiteral(value, type.getIRI()).map(t -> (Term)t);
        } else {
            Result<LiteralTerm> result = createPlainLiteral(value);
            result.addMessage(Message.warning("Type " + RDFNodes.toString(type.getIRI())
                + " too generic to materialise, defaulting to "
                + RDFNodes.toString(TypeFactory.LITERAL.getIRI())));
            return result.map(t -> (Term)t);
        }
    }

    public Result<Term> createTerm(RDFNode node) {

        if (node.canAs(RDFList.class)) {
            return createTermList(node.as(RDFList.class)).map(t -> (Term)t);
        } else {
            return createTerm(node.asNode());
        }
    }

    public Result<Term> createTerm(Node node) {

        if (node.isLiteral()) {
            return createLiteral(node.getLiteralLexicalForm(), node.getLiteralDatatypeURI(), node.getLiteralLanguage())
                .map(t -> (Term)t);
        } else if (node.isBlank()) {
            return createBlankNode(node.getBlankNodeId()).map(t -> (Term)t);
        } else {
            return createTermByURI(node.getURI());
        }
    }

    public Result<TermList> createTermList(RDFList list) {
        return this.createdLists.computeIfAbsent(list,
            l -> ResultStream.innerOf(l.asJavaList())
                .mapFlatMap(this::createTerm)
                .aggregate()
                .map(stream -> stream.collect(Collectors.toList()))
                .map(cast -> (List<Term>)cast)
                .map(TermList::new));
    }

    public Result<Term> createTermByURI(String uri) {

        if (uri.equals(this.vocabulary.getNoneResource().getURI())) {
            return Result.of(new NoneTerm());
        } else if (uri.equals(RDF.nil.getURI())) {
            return Result.of(new TermList());
        } else {
            return Result.of(new IRITerm(uri));
        }
    }



}
