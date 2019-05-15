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

import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.result.Result;
//import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.stottr.antlr.stOTTRBaseVisitor;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class SParameterListParser extends stOTTRBaseVisitor<Result<ParameterList>> {

    private STypeParser typeParser;
    private STermParser termParser;

    public SParameterListParser(STermParser termParser) {
        this.termParser = termParser;
        this.typeParser = new STypeParser(termParser);
    }

    public Result<ParameterList> visitParameterList(stOTTRParser.ParameterListContext ctx) {
        return visitChildren(ctx);
    }
    
    //@Override
    //public Result<TemplateSignature> visitParameter(stOTTRParser.ParameterContext ctx) {
    //    return visitChildren(ctx);
    //}
    //
    //@Override
    //public Result<TemplateSignature> visitDefaultValue(stOTTRParser.DefaultValueContext ctx) {
    //    return visitChildren(ctx);
    //}
    //
}
