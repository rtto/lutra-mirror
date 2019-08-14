package xyz.ottr.lutra.wottr.parser.v04;

/*-
 * #%L
 * lutra-wottr
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.TemplateParser;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class WTemplateParser implements TemplateParser<Model> {

    private static final List<Resource> templateTypes = Arrays.asList(WOTTR.Template, WOTTR.TemplateSignature, WOTTR.BaseTemplate);

    private final WInstanceParser instanceParser;
    private final PrefixMapping prefixes;

    public WTemplateParser() {
        this.instanceParser = new WInstanceParser();
        this.prefixes = OTTR.getDefaultPrefixes();
    }

    @Override
    public Map<String, String> getPrefixes() {
        return this.prefixes.getNsPrefixMap();
    }

    @Override
    public ResultStream<TemplateSignature> apply(Model model) {

        this.prefixes.setNsPrefixes(model);

        ResultStream<TemplateSignature> allSignatures = ResultStream.empty(); // for accumulating parsed objects

        // parse each of the types of templates:
        for (Resource type : templateTypes) {
            ResultStream<TemplateSignature> newSignatures = ResultStream.innerOf(ModelSelector.getInstancesOfClass(model, type))
                .mapFlatMap(res -> this.parseSignature(model, res, type));
            allSignatures = ResultStream.concat(allSignatures, newSignatures);
        }

        return allSignatures;
    }

    private Result<TemplateSignature> parseSignature(Model model, Resource signatureResource, Resource type) {

        Result<String> signatureURI = RDFNodes.castURIResource(signatureResource)
            .map(Resource::getURI);

        Result<ParameterList> parameterList = ModelSelector.getRequiredListObject(model, signatureResource, WOTTR.parameters)
            .flatMap(list -> parseParameters(model, list));

        Result<TemplateSignature> signature = Result.zip(signatureURI, parameterList,
            (sURI, pList) -> new TemplateSignature(sURI, pList, type.equals(WOTTR.BaseTemplate)));

        // include pattern if template, or check that no pattern exists
        if (type.equals(WOTTR.Template)) {
            Result<Set<Instance>> pattern = parsePattern(model, signatureResource);
            signature = Result.zip(signature, pattern, Template::new);
        } else if (model.contains(signatureResource, WOTTR.pattern, (RDFNode) null)) {
            signature.addMessage(Message.error(
                RDFNodes.toString(type) + " " + signatureURI.orElse("") + " cannot have a pattern."));
        }

        if (model.contains(signatureResource, WOTTR.annotation, (RDFNode) null)) {
            signature.addMessage(Message.warning("Annotations are not yet supported."));
        }

        return signature;
    }

    private Result<ParameterList> parseParameters(Model model, Resource paramsRes) {
        
        WParameterParser parameterParser = new WParameterParser(model);
        List<Result<Term>> parsedParameters  = paramsRes
            .as(RDFList.class)
            .asJavaList()
            .stream()
            .map(parameterParser)
            .collect(Collectors.toList());

        return Result.aggregate(parsedParameters)
            .map(terms ->
                new ParameterList(
                    terms,
                    parameterParser.getNonBlanks(),
                    parameterParser.getOptionals(),
                    parameterParser.getDefaultValues()));
    }

    private Result<Set<Instance>> parsePattern(Model model, Resource template) {

        Set<Result<Instance>> instancesRes = model.listObjectsOfProperty(template, WOTTR.pattern)
            .mapWith(ins -> this.instanceParser.parseInstance(model, ins))
            .toSet();
        return Result.aggregate(instancesRes);
    }

}
