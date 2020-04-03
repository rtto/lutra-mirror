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
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.bottr.model.ArgumentMap;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.model.types.TypeRegistry;
import xyz.ottr.lutra.system.Result;

public class StringArgumentMap extends ArgumentMap<String> {

    private static final Type DEFAULT_TYPE = TypeRegistry.LITERAL;

    public StringArgumentMap(PrefixMapping prefixes, Type type) {
        super(prefixes, type);
    }

    @Override
    protected RDFNode toRDFNode(String value) {
        return ResourceFactory.createPlainLiteral(value);
    }

    public StringArgumentMap(PrefixMapping prefixes) {
        this(prefixes, DEFAULT_TYPE);
    }

    @Override
    protected Result<Term> getBasicTerm(String value, BasicType type) {
        return Result.ofNullable(this.literalLangTag)
            .flatMapOrElse(
                tag -> this.termParser.langLiteralTerm(toString(value), tag).map(t -> (Term)t),
                this.termParser.term(toString(value), type));
    }

    @Override
    protected Result<Term> getListElementTerm(String value, BasicType type) {
        return getBasicTerm(value, type);
    }
}
