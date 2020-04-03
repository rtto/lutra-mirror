package xyz.ottr.lutra.parser;

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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.BlankNodeId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.util.DataValidator;
import xyz.ottr.lutra.writer.RDFNodeWriter;


public class TermParser {

    // TODO: Verify that this is correct. This only gives correct results if blank nodes across Jena models are unique.
    private static final Map<RDFList, Result<ListTerm>> createdLists = new HashMap<>();
    private static final Map<String, BlankNodeTerm> createdBlanks = new HashMap<>();

    private final PrefixMapping prefixMapping;

    public TermParser() {
        this(PrefixMapping.Factory.create());
    }

    public TermParser(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }

    public static Result<Term> noneTerm() {
        return Result.of(new NoneTerm());
    }

    public Result<Term> term(RDFNode node) {
        if (node.isResource()) {
            return term(node.asResource());
        } else if (node.isLiteral()) {
            return literalTerm(node.asLiteral()).map(tl -> (Term) tl);
        } else {
            return Result.error("Unable to parse RDFNode " + RDFNodeWriter.toString(node) + " to Term.");
        }
    }

    public Result<Term> term(Resource node) {

        if (node.isURIResource()) {
            return term(node.getURI());
        } else if (node.canAs(RDFList.class)) {
            return listTerm(node.as(RDFList.class)).map(tl -> (Term) tl);
        } else if (node.isAnon()) {
            return blankNodeTerm(node.getId().getBlankNodeId()).map(tl -> (Term) tl);
        } else {
            return Result.error("Unable to parse resource " + RDFNodeWriter.toString(node) + " to Term.");
        }
    }

    public static Result<Term> term(String uri) {

        if (uri.equals(OTTR.none)) {
            return noneTerm();
        } else if (uri.equals(RDF.nil.getURI())) {
            return Result.of(new ListTerm());
        } else {
            return Result.of(new IRITerm(uri));
        }
    }

    public Result<Term> term(String value, BasicType type) {
        if (type.isSubTypeOf(TypeRegistry.IRI)) {
            return iriTerm(value).map(t -> (Term)t);
        } else if (type.isProperSubTypeOf(TypeRegistry.LITERAL)) {
            return typedLiteralTerm(value, type.getIri()).map(t -> (Term)t);
        } else {
            Result<LiteralTerm> result = plainLiteralTerm(value);
            if (!type.equals(TypeRegistry.LITERAL)) {
                result.addMessage(Message.warning("Unknown literal datatype " + RDFNodeWriter.toString(type.getIri())
                    + ", defaulting to " + RDFNodeWriter.toString(TypeRegistry.LITERAL.getIri())));
            }
            return result.map(t -> (Term)t);
        }
    }

    public Result<IRITerm> iriTerm(String value) {
        return Result.of(value)
            .map(this.prefixMapping::expandPrefix)
            .flatMap(DataValidator::asURI)
            .map(IRITerm::new);
    }

    private Result<ListTerm> listTerm(RDFList list) {

        if (createdLists.containsKey(list)) {
            return createdLists.get(list);
        } else {
            List<Result<Term>> terms = list.asJavaList().stream()
                .map(this::term)
                .collect(Collectors.toList());
            Result<List<Term>> aggTerms = Result.aggregate(terms);
            Result<ListTerm> resTermList = aggTerms.map(ListTerm::new);
            createdLists.put(list, resTermList);
            return resTermList;
        }
    }

    public static Result<LiteralTerm> literalTerm(Literal literal) {
        return literalTerm(literal.getLexicalForm(), literal.getDatatypeURI(), literal.getLanguage());
    }

    public static Result<LiteralTerm> literalTerm(String value, String datatype, String language) {

        if (StringUtils.isNotEmpty(language) && !RDF.langString.getURI().equals(datatype)) {
            return Result.error("Error creating literal. Cannot have a language tag: " + language
                + " and the datatype: " + datatype);
        } else if (StringUtils.isNotEmpty(language)) {
            return langLiteralTerm(value, language);
        } else if (StringUtils.isNotEmpty(datatype)) {
            return typedLiteralTerm(value, datatype);
        } else {
            return plainLiteralTerm(value);
        }
    }

    public static Result<LiteralTerm> typedLiteralTerm(String value, String datatype) {
        return DataValidator.asURI(datatype)
            .map(iri -> LiteralTerm.createTypedLiteral(value, iri));
    }

    public static Result<LiteralTerm> langLiteralTerm(String value, String languageTag) {
        return DataValidator.asLanguageTagString(languageTag)
            .map(tag -> LiteralTerm.createLanguageTagLiteral(value, tag));
    }

    public static Result<LiteralTerm> plainLiteralTerm(String value) {
        return Result.of(LiteralTerm.createPlainLiteral(value));
    }

    public static Result<BlankNodeTerm> blankNodeTerm() {
        return Result.of(new BlankNodeTerm());
    }

    public Result<BlankNodeTerm> blankNodeTerm(String value) {
        return Result.of(createdBlanks.computeIfAbsent(value, _fresh -> new BlankNodeTerm()));
    }

    public Result<BlankNodeTerm> blankNodeTerm(BlankNodeId blankNodeId) {
        return blankNodeTerm(blankNodeId.getLabelString());
    }
}
