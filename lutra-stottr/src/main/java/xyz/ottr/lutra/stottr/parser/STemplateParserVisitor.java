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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.BaseTemplateBuilder;
import xyz.ottr.lutra.parser.SignatureBuilder;
import xyz.ottr.lutra.parser.TemplateBuilder;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class STemplateParserVisitor extends SBaseParserVisitor<Signature>  {

    private Map<String, String> prefixes;
    private STermParserVisitor termParser;
    private SInstanceParserVisitor annotationsInstanceParser;
    private SParameterParserVisitor parameterParser;


    STemplateParserVisitor(Map<String, String> prefixes) {
        this.prefixes = prefixes;
        this.termParser = new STermParserVisitor(this.prefixes);
        this.annotationsInstanceParser = new SInstanceParserVisitor(this.termParser);
        this.parameterParser = new SParameterParserVisitor(this.termParser);
    }

    @Override
    public Result visitInstance(stOTTRParser.InstanceContext ctx) {
        return SParserUtils.ignoreStatement("instance", "signatures, base templates and templates", ctx);
    }

    @Override
    public Result<Signature> visitSignature(stOTTRParser.SignatureContext ctx) {
        return SignatureBuilder.builder()
            .iri(parseIRI(ctx))
            .parameters(parseParameters(ctx))
            .annotations(parseAnnotations(ctx, this.annotationsInstanceParser))
            .build();
    }

    @Override
    public Result<Signature> visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {

        if (ctx.signature() == null) {
            return Result.error("Unrecognized base template: " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return BaseTemplateBuilder.builder()
            .signature(visitSignature(ctx.signature()))
            .build()
            .map(t -> (Signature)t);
    }

    @Override
    public Result<Signature> visitTemplate(stOTTRParser.TemplateContext ctx) {

        if (ctx.signature() == null) {
            return Result.error("Unrecognized template " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        var signature = visitSignature(ctx.signature());

        Map<String, Term> variables = getVariableMap(signature);
        SInstanceParserVisitor instanceParser = new SInstanceParserVisitor(new STermParserVisitor(this.prefixes, variables));

        return TemplateBuilder.builder()
            .signature(signature)
            .instances(parsePattern(ctx, instanceParser))
            .build()
            .map(t -> (Signature)t);
    }

    private Result<String> parseIRI(stOTTRParser.SignatureContext ctx) {

        if (ctx.templateName() == null) {
            return Result.error("Unrecognized signature IRI " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return this.termParser.visit(ctx.templateName())
            .map(term -> (IRITerm) term)
            .map(IRITerm::getIri);
    }

    private Result<List<Parameter>> parseParameters(stOTTRParser.SignatureContext ctx) {

        if (ctx.parameterList() == null || ctx.parameterList().parameter() == null) {
            return Result.error("Unrecognized signature parameters " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return ctx.parameterList().parameter().stream()
            .map(param -> this.parameterParser.visit(param))
            .collect(Collectors.collectingAndThen(Collectors.toList(), Result::aggregate));
    }

    private Map<String, Term> getVariableMap(Result<Signature> signature) {
        return signature
            .map(s -> s.getParameters().stream()
                .map(Parameter::getTerm)
                .collect(Collectors.toUnmodifiableMap(
                    term -> term.getIdentifier().toString(),
                    Function.identity())))
            .orElse(new HashMap<>());
    }


    private Result<Set<Instance>> parsePattern(stOTTRParser.TemplateContext ctx, SInstanceParserVisitor parser) {

        if (ctx.patternList() == null || ctx.patternList().instance() == null) {
            return Result.error("Unrecognized template pattern " + SParserUtils.getTextWithLineAndColumnString(ctx));
        }

        return parseInstances(ctx.patternList().instance(), parser);
    }

    private Result<Set<Instance>> parseAnnotations(stOTTRParser.SignatureContext ctx, SInstanceParserVisitor parser) {

        if (ctx.annotationList() == null) {
            return Result.empty();
        }

        return ctx.annotationList()
            .annotation()
            .stream()
            .map(stOTTRParser.AnnotationContext::instance)
            .collect(Collectors.collectingAndThen(Collectors.toList(), list -> parseInstances(list, parser)));
    }

    private Result<Set<Instance>> parseInstances(List<stOTTRParser.InstanceContext> ctxs, SInstanceParserVisitor parser) {
        return ctxs.stream()
            .map(parser::visitInstance)
            .collect(Collectors.collectingAndThen(Collectors.toSet(), Result::aggregate));
    }
}
