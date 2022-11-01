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
import org.antlr.v4.runtime.CharStream;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.terms.IRITerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.BaseTemplateBuilder;
import xyz.ottr.lutra.parser.SignatureBuilder;
import xyz.ottr.lutra.parser.TemplateBuilder;
import xyz.ottr.lutra.parser.TemplateParser;
import xyz.ottr.lutra.stottr.antlr.stOTTRParser;
import xyz.ottr.lutra.system.Result;

public class STemplateParser extends SDocumentParser<Signature> implements TemplateParser<CharStream> {

    private SParameterParser paramsParser;

    @Override
    protected void initSubParsers(Map<String, String> prefixes) {
        super.initSubParsers(prefixes);
        this.paramsParser = new SParameterParser(getTermParser());
    }

    public Result visitInstance(stOTTRParser.InstanceContext ctx) {
        return SParserUtils.ignoreStatement("instance", ctx);
    }

    public Result<Signature> visitSignature(stOTTRParser.SignatureContext ctx) {

        SInstanceParser instanceParser = new SInstanceParser(getPrefixes(), Map.of());

        return SignatureBuilder.builder()
            .iri(parseIRI(ctx))
            .parameters(parseParameters(ctx))
            .annotations(parseAnnotations(ctx, instanceParser))
            .build();
    }

    public Result<Signature> visitBaseTemplate(stOTTRParser.BaseTemplateContext ctx) {
        return BaseTemplateBuilder.builder()
            .signature(visitSignature(ctx.signature()))
            .build()
            .map(t -> (Signature)t);
    }

    public Result<Signature> visitTemplate(stOTTRParser.TemplateContext ctx) {

        var signature = visitSignature(ctx.signature());

        Map<String, Term> variables = getVariableMap(signature);
        SInstanceParser instanceParser = new SInstanceParser(getPrefixes(), variables);

        return TemplateBuilder.builder()
            .signature(signature)
            .instances(parsePattern(ctx, instanceParser))
            .build()
            .map(t -> (Signature)t);
    }

    private Result<String> parseIRI(stOTTRParser.SignatureContext ctx) {
        return getTermParser().visit(ctx.templateName())
            .map(term -> (IRITerm) term)
            .map(IRITerm::getIri);
    }

    private Result<List<Parameter>> parseParameters(stOTTRParser.SignatureContext ctx) {
        return ctx.parameterList().parameter().stream()
            .map(param -> this.paramsParser.visit(param))
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


    private Result<Set<Instance>> parsePattern(stOTTRParser.TemplateContext ctx, SInstanceParser parser) {
        return parseInstances(ctx.patternList().instance(), parser);
    }

    private Result<Set<Instance>> parseAnnotations(stOTTRParser.SignatureContext ctx, SInstanceParser parser) {

        if (ctx.annotationList() == null) {
            return Result.empty();
        }

        return ctx.annotationList()
            .annotation()
            .stream()
            .map(stOTTRParser.AnnotationContext::instance)
            .collect(Collectors.collectingAndThen(Collectors.toList(), list -> parseInstances(list, parser)));
    }

    private Result<Set<Instance>> parseInstances(List<stOTTRParser.InstanceContext> ctxs, SInstanceParser parser) {
        return ctxs.stream()
            .map(parser::visitInstance)
            .collect(Collectors.collectingAndThen(Collectors.toSet(), Result::aggregate));
    }
}
