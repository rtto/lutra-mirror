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

import org.antlr.v4.runtime.CharStream;

import xyz.ottr.lutra.io.TemplateParser;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;

public class STemplateParser extends SParser<TemplateSignature> implements TemplateParser<CharStream> {

    @Override
    public ResultStream<TemplateSignature> apply(CharStream in) {
        return parseDocument(in); 
    }

    @Override
    public Result<TemplateSignature> visitStatement(stOTTRParser.StatementContext ctx) {

        if (ctx.instance() != null) { // An (outer) instance
            return Result.empty(); // TODO: Decide on error or ignore?
        }
        
        return visitChildren(ctx);
    }

    private Result<String> parseName(stOTTRParser.TemplateNameContext ctx) {

        return getTermParser().visit(ctx).flatMap(term -> Result.of(((IRITerm) term).getIRI()));
    }

    private Result<TemplateSignature> makeSignature(stOTTRParser.TemplateNameContext name,
        stOTTRParser.ParameterListContext params, boolean isBase) {
        
        Result<String> iriRes = parseName(name);
        Result<ParameterList> parametersRes = new SParameterListParser(getTermParser()).visit(params);

        return Result.zip(iriRes, parametersRes, (iri, params) -> new TemplateSignature(iri, params, isBase));
    }

    @Override
    public Result<TemplateSignature> visitSignature(stOTTRParser.SignatureContext ctx) {
        return makeSignature(ctx.templateName(), ctx.parameterList, false);
    }
    
    @Override
    public Result<TemplateSignature> visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {
        stOTTRParser.SignatureContext sigCtx = ctx.signature();
        return makeSignature(sigCtx.templateName(), sigCtx.parameterList, true);
    }
    
    @Override
    public Result<TemplateSignature> visitTemplate(stOTTRParser.TemplateContext ctx) {
        return visitChildren(ctx);
    }
    
    //@Override
    //public Result<TemplateSignature> visitPatternList(stOTTRParser.PatternListContext ctx) {
    //    return visitChildren(ctx);
    //}
}
