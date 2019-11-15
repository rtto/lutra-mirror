package xyz.ottr.lutra.model.terms;

/*-
 * #%L
 * lutra-core
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

import java.util.Objects;
import java.util.Optional;

import lombok.Getter;
import lombok.NonNull;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeRegistry;

@Getter
public class LiteralTerm extends Term {

    public static final String LANG_STRING_DATATYPE = RDF.dtLangString.getURI();
    public static final String PLAIN_STRING_DATATYPE = XSD.xstring.toString();

    @NonNull private final String value;
    @NonNull private final String datatype;
    private final String languageTag;

    private LiteralTerm(String value, String datatype, String languageTag) {
        super(getTermType(datatype), false);
        this.value = value;
        this.datatype = datatype;
        this.languageTag = languageTag;
    }

    private static TermType getTermType(String datatype) {
        return Objects.requireNonNullElse(TypeRegistry.getType(datatype), TypeRegistry.LITERAL);
    }

    public static LiteralTerm createLanguageTagLiteral(String value, @NonNull String languageTag) {
        return new LiteralTerm(value, LANG_STRING_DATATYPE, languageTag);
    }

    public static LiteralTerm createTypedLiteral(String value, String datatype) {
        return new LiteralTerm(value, datatype, null);
    }

    public static LiteralTerm createPlainLiteral(String value) {
        return new LiteralTerm(value, PLAIN_STRING_DATATYPE, null);
    }

    public boolean isLanguageTagged() {
        return Objects.nonNull(languageTag);
    }

    public boolean isPlainLiteral() {
        return PLAIN_STRING_DATATYPE.equals(datatype);
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public LiteralTerm shallowClone() {
        LiteralTerm term = new LiteralTerm(this.value, this.datatype, this.languageTag);
        term.setVariable(isVariable());
        return term;
    }

    @Override
    public Optional<Term> unify(Term other) {

        if (!(other instanceof LiteralTerm)) {
            return Optional.empty();
        }

        if (isVariable() || !other.isVariable() && equals(other)) {
            return Optional.of(other);
        }

        return Optional.empty();
    }

    @Override
    public String getIdentifier() {
        return "\"" + this.value + "\""
            + (Objects.nonNull(this.languageTag)
            ? "@" + this.languageTag
            : "^^" + this.datatype);
    }
}
