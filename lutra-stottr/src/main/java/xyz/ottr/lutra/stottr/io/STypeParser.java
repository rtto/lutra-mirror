package xyz.ottr.lutra.stottr.io;

/*-
 * #%L
 * lutra-stottr
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

import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class STypeParser extends stOTTRBaseVisitor<Result<TermType>> {

    public STermParser termParser;

    public STypeParser(STermParser termParser) {
        this.termParser = termParser;
    }

    @Override
    public Result<TermType> visitListType(stOTTRParser.ListTypeContext ctx) {
        return visitChildren(ctx);
    }
    
    @Override
    public Result<TermType> visitLubType(stOTTRParser.LubTypeContext ctx) {
        return visitChildren(ctx);
    }
    
    @Override
    public Result<TermType> visitBasicType(stOTTRParser.BasicTypeContext ctx) {
        Result<String> iriRes = this.termParser.visit(ctx).map(term -> ((IRITerm) term).getIRI());
        return iriRes.map(iri -> TypeFactory.getByIRI(iri));
    }
}
