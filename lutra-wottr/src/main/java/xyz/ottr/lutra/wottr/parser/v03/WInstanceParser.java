package xyz.ottr.lutra.wottr.parser.v03;

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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.parser.TripleInstanceFactory;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelector;
import xyz.ottr.lutra.wottr.vocabulary.v03.WOTTR;

public class WInstanceParser implements InstanceParser<Model> {

    private static List<Resource> getInstances(Model model) {
        return ModelSelector.listResourcesWithProperty(model, WOTTR.templateRef);
    }

    private static boolean isTemplateDefinition(Model model) {
        return model.contains(null, RDF.type, WOTTR.Template);
    }

    @Override
    public ResultStream<Instance> apply(Model model) {

        Model canonModel = WParserUtils.getCanonicalModel(model);

        if (isTemplateDefinition(canonModel)) {
            return ResultStream.empty();
        } else {
            List<Resource> ins = getInstances(canonModel);
            ResultStream<Instance> parsedInstances = parseInstances(canonModel, ins);

            Model triples = WParserUtils.getNonTemplateTriples(canonModel, null, new LinkedList<>(), ins);
            return ResultStream.concat(parsedInstances, new TripleInstanceFactory(triples).get());
        }
    }

    private ResultStream<Instance> parseInstances(Model model, List<Resource> templateInstances) {
        Stream<Result<Instance>> parsedInstances = templateInstances.stream()
            .map(instance -> parseInstance(model, instance));
        return new ResultStream<>(parsedInstances);
    }

    public Result<Instance> parseInstance(Model model, RDFNode templateInstanceNode) {

        if (!templateInstanceNode.isResource()) {
            return Result.empty(Message.error(
                        "Expected instance to be a resource, found non-resource "
                        + templateInstanceNode.toString() + "."));
        }

        Resource templateInstance = templateInstanceNode.asResource();
        Resource templateRef = ModelSelector.getRequiredResourceOfProperty(model, templateInstance,
            WOTTR.templateRef);

        WParameterListParser parameterListParser = new WParameterListParser(model);
        Result<ArgumentList> resArgumentList;
        if (model.contains(templateInstance, WOTTR.hasArgument, (RDFNode) null)) {
            List<Resource> arguments = ModelSelector.listResourcesOfProperty(model, templateInstance,
                WOTTR.hasArgument);
            resArgumentList = parameterListParser.parseArguments(arguments);
        } else if (model.contains(templateInstance, WOTTR.withValues, (RDFNode) null)) {
            Resource argsList = ModelSelector.getRequiredResourceOfProperty(model, templateInstance,
                WOTTR.withValues);
            resArgumentList = parameterListParser.parseValues(argsList);
        } else {
            return Result.empty(Message.error("Found no arguments for instance of "
                    + templateRef.getURI()));
        }

        String toURI = templateRef.getURI(); 

        return resArgumentList.map(args -> new Instance(toURI, args));
    }
}
