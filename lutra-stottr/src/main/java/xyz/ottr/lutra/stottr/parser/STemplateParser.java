package xyz.ottr.lutra.stottr.parser;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.TemplateParser;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class STemplateParser extends SParser<Signature> implements TemplateParser<CharStream> {
    
    private SParameterListParser paramsParser;

    public STemplateParser() {
        super();
        this.paramsParser = new SParameterListParser(getTermParser());
    }

    @Override
    protected void initSubParsers() {
        this.paramsParser = new SParameterListParser(getTermParser());
    }

    @Override
    public ResultStream<Signature> apply(CharStream in) {
        return parseDocument(in);
    }

    @Override
    public Result<Signature> visitStatement(stOTTRParser.StatementContext ctx) {
        return visitChildren(ctx);
    }

    private Result<String> parseName(stOTTRParser.TemplateNameContext ctx) {

        return getTermParser().visit(ctx).flatMap(term -> Result.of(((IRITerm) term).getIRI()));
    }

    private Result<Signature> makeSignature(stOTTRParser.TemplateNameContext nameCtx,
                                            stOTTRParser.ParameterListContext paramsCtx, boolean isBase) {
        
        Result<String> iriRes = parseName(nameCtx);
        Result<ParameterList> paramsRes = this.paramsParser.visit(paramsCtx);
        return Result.zip(iriRes, paramsRes, (iri, params) -> new Signature(iri, params, isBase));
    }

    @Override
    public Result<Signature> visitSignature(stOTTRParser.SignatureContext ctx) {
        return makeSignature(ctx.templateName(), ctx.parameterList(), false);
    }
    
    @Override
    public Result<Signature> visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {

        stOTTRParser.SignatureContext sigCtx = ctx.signature();
        return makeSignature(sigCtx.templateName(), sigCtx.parameterList(), true);
    }
    
    @Override
    public Result<Signature> visitTemplate(stOTTRParser.TemplateContext ctx) {

        Result<Signature> sigRes = visitSignature(ctx.signature());
        Map<String, Term> variables = makeVariablesMap(sigRes);

        SInstanceParser instanceParser = new SInstanceParser(getPrefixes(), variables);
        Set<Result<Instance>> resBody = ctx.patternList()
            .instance()
            .stream()
            .map(instanceParser::visitInstance)
            .collect(Collectors.toSet());

        Result<Set<Instance>> bodyRes = Result.aggregate(resBody);

        return Result.zip(sigRes, bodyRes, Template::new);
    }

    private Map<String, Term> makeVariablesMap(Result<Signature> resSig) {

        Map<String, Term> variables = new HashMap<>();
        if (!resSig.isPresent()) {
            return variables;
        }

        for (Term var : resSig.get().getParameters().asList()) {
            variables.put(((BlankNodeTerm) var).getLabel(), var);
        }
        return variables;
    }
}
