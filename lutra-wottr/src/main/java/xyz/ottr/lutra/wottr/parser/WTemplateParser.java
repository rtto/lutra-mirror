package xyz.ottr.lutra.wottr.parser;

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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.parser.TemplateBuilder;
import xyz.ottr.lutra.parser.TemplateParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;

// TODO Most methods take Model as an input. Should we convert the class from a Function to a Supplier
// that takes the model as a constructor argument?

public class WTemplateParser implements TemplateParser<Model> {

    //private static final List<Resource> templateTypes = List.of(WOTTR.Template, WOTTR.Signature, WOTTR.BaseTemplate);

    private final WInstanceParser instanceParser;
    private final PrefixMapping prefixes;

    public WTemplateParser() {
        this.instanceParser = new WInstanceParser();
        this.prefixes = PrefixMapping.Factory.create();
    }

    @Override
    public Map<String, String> getPrefixes() {
        return this.prefixes.getNsPrefixMap();
    }

    @Override
    public ResultStream<Signature> apply(Model model) {

        this.prefixes.setNsPrefixes(model);

        var signatures = parseSignatureType(model, WOTTR.Signature, t -> parseSignature(model, t));
        var baseTemplates = parseSignatureType(model, WOTTR.BaseTemplate, t -> parseBaseTemplate(model, t));
        var templates = parseSignatureType(model, WOTTR.Template, t -> parseTemplate(model, t));

        return ResultStream.concat(List.of(templates, signatures, baseTemplates));
    }

    private ResultStream<Signature> parseSignatureType(Model model, Resource type, Function<Resource, Result<Signature>> parser) {
        return ResultStream.innerOf(ModelSelector.getInstancesOfClass(model, type))
            .mapFlatMap(parser);
    }

    private Result<Signature> parseSignature(Model model, Resource signature) {

        var result = TemplateBuilder.signatureBuilder()
            .iri(this.parseSignatureIRI(signature))
            .parameters(this.parseParameters(model, signature))
            .build();

        if (model.contains(signature, WOTTR.annotation, (RDFNode) null)) {
            result.addWarning("The signature " + RDFNodes.toString(signature) + " contains an  "
                + RDFNodes.toString(WOTTR.annotation) + ", but this is not yet supported.");
        }

        return result;
    }

    private Result<Signature> parseTemplate(Model model, Resource template) {
        return TemplateBuilder.templateBuilder()
            .signature(this.parseSignature(model, template))
            .instances(this.parsePattern(model, template))
            .build()
            .map(t -> (Signature)t); // TODO is there a better way?! <? extends Signature> ?
    }

    private Result<Signature> parseBaseTemplate(Model model, Resource baseTemplate) {

        var result = TemplateBuilder.baseTemplateBuilder()
            .signature(this.parseSignature(model, baseTemplate))
            .build();

        if (model.contains(baseTemplate, WOTTR.pattern, (RDFNode) null)) {
            result.addWarning("The base template " + RDFNodes.toString(baseTemplate) + " contains a "
                + RDFNodes.toString(WOTTR.pattern) + ", but this is not permitted.");
        }

        return result
            .map(t -> (Signature)t); // TODO is there a better way?! <? extends Signature> ?
    }

    private Result<String> parseSignatureIRI(Resource signature) {
        return RDFNodes.castURIResource(signature)
            .map(Resource::getURI);
    }

    private Result<List<Parameter>> parseParameters(Model model, Resource signature) {

        WParameterParser parser = new WParameterParser(model);

        List<Result<Parameter>> parameters = ModelSelector.getRequiredListObject(model, signature, WOTTR.parameters)
            .map(RDFList::asJavaList)
            .mapToStream(ResultStream::innerOf)
            .mapFlatMap(parser)
            .collect(Collectors.toList());

        return Result.aggregate(parameters);
    }

    private Result<Set<Instance>> parsePattern(Model model, Resource template) {

        var instances = ResultStream.innerOf(model.listObjectsOfProperty(template, WOTTR.pattern))
            .mapFlatMap(node -> RDFNodes.cast(node, Resource.class))
            .mapFlatMap(ins -> this.instanceParser.parseInstance(model, ins))
            .collect(Collectors.toSet());

        return Result.aggregate(instances);
    }

}
