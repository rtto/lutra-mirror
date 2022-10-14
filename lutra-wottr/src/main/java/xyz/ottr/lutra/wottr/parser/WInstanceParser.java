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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.parser.ArgumentBuilder;
import xyz.ottr.lutra.parser.InstanceBuilder;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.writer.RDFNodeWriter;

public class WInstanceParser implements InstanceParser<Model> {

    @Override
    public ResultStream<Instance> apply(Model model) {

        List<Resource> instanceResources = ModelSelector.getSubjects(model, WOTTR.of);

        // Keep map of resource -> instance in order to filter on correctly parsed.
        Map<Resource, Result<Instance>> instanceMap = instanceResources.stream()
            .collect(Collectors.toMap(Function.identity(), i -> parseInstance(model, i)));

        var correctInstances = instanceMap.keySet().stream()
            .filter(resource -> instanceMap.get(resource).isPresent())
            .collect(Collectors.toList());

        var tripleInstances = parseTripleInstances(model, correctInstances);

        return ResultStream.concat(ResultStream.of(instanceMap.values()), tripleInstances);
    }

    // Get triples which are not part of any of the instances.
    private ResultStream<Instance> parseTripleInstances(Model model, List<Resource> instanceResources) {

        WTripleSerialiser tripleSerialiser = new WTripleSerialiser(model);

        Model instanceModel = ModelFactory.createDefaultModel();

        instanceResources.stream()
            .map(tripleSerialiser::serialiseInstance)
            .forEach(instanceModel::add);

        Model tripleModel = model.difference(instanceModel);

        return new WTripleInstanceParser().apply(tripleModel);
    }

    private ResultStream<Instance> parseInstances(Model model, List<Resource> templateInstances) {
        Stream<Result<Instance>> parsedInstances = templateInstances.stream()
            .map(instance -> parseInstance(model, instance));
        return new ResultStream<>(parsedInstances);
    }

    Result<Instance> parseInstance(Model model, Resource instanceNode) {
        return InstanceBuilder.builder()
            .iri(parseSignatureIRI(model, instanceNode))
            .arguments(parseArgumentList(model, instanceNode))
            .listExpander(parseListExpander(model, instanceNode))
            .build();
    }

    private Result<String> parseSignatureIRI(Model model, Resource instance) {
        return ModelSelector.getRequiredURIResourceObject(model, instance, WOTTR.of)
            .map(Resource::getURI);
    }

    private Result<ListExpander> parseListExpander(Model model, Resource instance) {
        return ModelSelector.getOptionalResourceObject(model, instance, WOTTR.modifier)
            .flatMap(r -> WOTTR.listExpanders.keySet().contains(r)
                ? Result.ofNullable(WOTTR.listExpanders.get(r))
                : Result.error("Unknown listExpander " + RDFNodeWriter.toString(r)
                    + " in instance " + RDFNodeWriter.toString(instance) + "."));
    }

    private Result<List<Argument>> parseArgumentList(Model model, Resource instance) {

        // An instance must have arguments or values, but not both
        Result<RDFList> arguments = ModelSelector.getRequiredListObject(model, instance, WOTTR.arguments);
        Result<RDFList> values = ModelSelector.getRequiredListObject(model, instance, WOTTR.values);

        if (arguments.isPresent() == values.isPresent()) { // true if both exist or both are missing
            return Result.error("An instance must have either one " + RDFNodeWriter.toString(WOTTR.arguments)
                + " or one " + RDFNodeWriter.toString(WOTTR.values) + ".");
        } else if (arguments.isPresent()) {
            return arguments.flatMap(args -> parseArguments(args, new WArgumentParser(model)));
        } else {
            // create a parser for values to simple arguments:
            Function<RDFNode, Result<Argument>> parser = value -> ArgumentBuilder.builder()
                .term(WTermParser.toTerm(value))
                .build();

            return values.flatMap(args -> parseArguments(args, parser));
        }
    }

    private Result<List<Argument>> parseArguments(RDFList argumentList, Function<RDFNode, Result<Argument>> parser) {

        List<Result<Argument>> arguments = argumentList.asJavaList().stream()
            .map(parser)
            .collect(Collectors.toList());

        return Result.aggregate(arguments);
    }

}
