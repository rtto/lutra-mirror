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

import java.util.Optional;

import org.apache.jena.vocabulary.XSD;
import xyz.ottr.lutra.model.types.TypeFactory;

public class LiteralTerm extends ResourceTerm {

    private final String value;
    private final String datatype; // Might be an undefined TermType, thus not used for type-checking
    private final String langtag;

    public LiteralTerm(String value) {
        this(value, XSD.xstring.toString(), null);
    }

    // TODO: Remove, and replace calls with this#typedLiteral
    public LiteralTerm(String value, String datatype) {
        this.value = value;
        this.datatype = datatype;
        this.langtag = null;
        super.type = TypeFactory.getConstantType(this);
    }

    private LiteralTerm(String value, String datatype, String langtag) {
        this.value = value;
        this.datatype = datatype;
        this.langtag = langtag;
        super.type = TypeFactory.getConstantType(this);
    }

    public static LiteralTerm taggedLiteral(String value, String langtag) {
        return new LiteralTerm(value, null, langtag);
    }

    public static LiteralTerm typedLiteral(String value, String datatype) {
        return new LiteralTerm(value, datatype, null);
    }

    public String getDatatype() {
        return this.datatype;
    }

    public String getLangTag() {
        return this.langtag;
    }

    public String getValue() {

        String suffix = "";
        if (getLangTag() != null) {
            suffix = "@" + getLangTag();
        } else if (getDatatype() != null) {
            suffix = " : " + getDatatype();
        }
        return "\"" + this.value + "\"" + suffix;
    }

    public String getPureValue() {
        return this.value;
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public LiteralTerm shallowClone() {
        LiteralTerm trm = new LiteralTerm(this.value, getDatatype(), getLangTag());
        trm.setIsVariable(super.isVariable());
        return trm;
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
        return getValue();
    }
}
