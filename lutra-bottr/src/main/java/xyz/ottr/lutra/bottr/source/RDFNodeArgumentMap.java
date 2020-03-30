package xyz.ottr.lutra.bottr.source;

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

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.bottr.model.ArgumentMap;
import xyz.ottr.lutra.bottr.util.TypeFactory;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Result;

public class RDFNodeArgumentMap extends ArgumentMap<RDFNode> {

    private static final TermType DEFAULT_TYPE = TypeRegistry.TOP;

    public RDFNodeArgumentMap(PrefixMapping prefixes, TermType type) {
        super(prefixes, type);
    }

    public RDFNodeArgumentMap(PrefixMapping prefixes) {
        this(prefixes, DEFAULT_TYPE);
    }

    @Override
    protected String toString(RDFNode value) {
        return value != null && value.isLiteral()
            ? value.asLiteral().getLexicalForm()
            : super.toString(value);
    }

    @Override
    protected RDFNode toRDFNode(RDFNode value) {
        return value;
    }

    @Override
    protected Result<Term> getBasicTerm(RDFNode value, BasicType type) {

        if (this.literalLangTag != null) {
            return this.termParser.langLiteralTerm(toString(value), this.literalLangTag)
                .map(t -> (Term)t);
        } else {
            TermType valueType = TypeFactory.getTermType(value);
            return valueType.isCompatibleWith(type)
                ? this.termParser.term(value)
                : this.termParser.term(toString(value), type);
        }
    }

    @Override
    protected Result<Term> getListElementTerm(String value, BasicType type) {
        return this.termParser.term(value, type);
    }
}
