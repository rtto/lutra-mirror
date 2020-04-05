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
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.ListTerm;
import xyz.ottr.lutra.model.terms.LiteralTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.util.DataValidator;

public class TermParser {

    // TODO: Verify that this is correct. This only gives correct results if blank nodes across Jena models are unique.
    private static final Map<String, Result<BlankNodeTerm>> createdBlanks = new HashMap<>();


    /*
    private final PrefixMapping prefixMapping;

    public TermParser() {
        this(PrefixMapping.Factory.create());
    }

    public TermParser(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }
     */

    public static Result<Term> newNoneTerm() {
        return Result.of(new NoneTerm());
    }

    public static Result<Term> toTerm(String iri) {

        if (iri.equals(OTTR.none)) {
            return newNoneTerm();
        } else if (iri.equals(RDF.nil.getURI())) {
            return Result.of(new ListTerm());
        } else {
            return Result.of(new IRITerm(iri));
        }
    }

    public static Result<IRITerm> toIRITerm(String value) {
        return Result.of(value)
            .flatMap(DataValidator::asURI)
            .map(IRITerm::new);
    }

    public static Result<LiteralTerm> toLiteralTerm(String value, String datatype, String language) {

        if (StringUtils.isNotEmpty(language) && !RDF.langString.getURI().equals(datatype)) {
            return Result.error("Error creating literal. Cannot have a language tag: " + language
                + " and the datatype: " + datatype);
        } else if (StringUtils.isNotEmpty(language)) {
            return toLangLiteralTerm(value, language);
        } else if (StringUtils.isNotEmpty(datatype)) {
            return toTypedLiteralTerm(value, datatype);
        } else {
            return toPlainLiteralTerm(value);
        }
    }

    public static Result<LiteralTerm> toTypedLiteralTerm(String value, String datatype) {
        return DataValidator.asURI(datatype)
            .map(iri -> LiteralTerm.createTypedLiteral(value, iri));
    }

    public static Result<LiteralTerm> toLangLiteralTerm(String value, String languageTag) {
        return DataValidator.asLanguageTagString(languageTag)
            .map(tag -> LiteralTerm.createLanguageTagLiteral(value, tag));
    }

    public static Result<LiteralTerm> toPlainLiteralTerm(String value) {
        return Result.of(LiteralTerm.createPlainLiteral(value));
    }

    public static Result<BlankNodeTerm> newBlankNodeTerm() {
        return Result.of(new BlankNodeTerm());
    }

    public static Result<BlankNodeTerm> toBlankNodeTerm(String value) {
        return createdBlanks.computeIfAbsent(value, _fresh -> newBlankNodeTerm());
    }

}
