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
import xyz.ottr.lutra.RDFTurtle;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.model.types.TypeRegistry;

@Getter
public class LiteralTerm extends AbstractTerm<String> {

    private final @NonNull String value;
    private final @NonNull String datatype;
    private final String languageTag;

    private LiteralTerm(String value, String datatype, String languageTag) {
        super(getIdentifier(value, datatype, languageTag), getIntrinsicType(datatype));
        this.value = value;
        this.datatype = datatype;
        this.languageTag = languageTag;
    }

    private static String getIdentifier(String value, String datatype, String languageTag) {
        return RDFTurtle.literal(value)
            + (Objects.nonNull(languageTag)
                ? RDFTurtle.literalLangSep + languageTag
                : RDFTurtle.literalTypeSep + datatype);
    }

    private static Type getIntrinsicType(String datatype) {
        return Objects.requireNonNullElse(TypeRegistry.asType(datatype), TypeRegistry.LITERAL);
    }

    public static LiteralTerm createLanguageTagLiteral(String value, @NonNull String languageTag) {
        return new LiteralTerm(value, RDFTurtle.langStringDatatype, languageTag);
    }

    public static LiteralTerm createTypedLiteral(String value, String datatype) {
        return new LiteralTerm(value, datatype, null);
    }

    public static LiteralTerm createPlainLiteral(String value) {
        return new LiteralTerm(value, RDFTurtle.plainLiteralDatatype, null);
    }

    public boolean isLanguageTagged() {
        return Objects.nonNull(this.languageTag);
    }

    public boolean isPlainLiteral() {
        return RDFTurtle.plainLiteralDatatype.equals(this.datatype);
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
    
}
