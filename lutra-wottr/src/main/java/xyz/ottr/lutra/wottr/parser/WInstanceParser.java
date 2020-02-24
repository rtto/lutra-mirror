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
import xyz.ottr.lutra.parser.ArgumentParser;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.WOTTR;

public class WInstanceParser extends InstanceParser<Model> {

    @Override
    public ResultStream<Instance> apply(Model model) {

        List<Resource> instanceResources = ModelSelector.getSubjects(model, WOTTR.of);
        ResultStream<Instance> instances = parseInstances(model, instanceResources);

        // Get triples which are not part of any of the instances:
        WTripleSerialiser tripleSerialiser = new WTripleSerialiser(model);
        Model instanceModel = ModelFactory.createDefaultModel();
        instanceResources.forEach(i -> instanceModel.add(tripleSerialiser.serialiseInstance(i)));
        Model tripleModel = model.difference(instanceModel);
        ResultStream<Instance> rdfInstances = new TripleInstanceParser().apply(tripleModel);

        return ResultStream.concat(instances, rdfInstances);
    }

    private ResultStream<Instance> parseInstances(Model model, List<Resource> templateInstances) {
        Stream<Result<Instance>> parsedInstances = templateInstances.stream()
            .map(instance -> parseInstance(model, instance));
        return new ResultStream<>(parsedInstances);
    }

    Result<Instance> parseInstance(Model model, Resource instanceNode) {
        return builder()
            .iri(getSignatureIRI(model, instanceNode))
            .arguments(getArgumentList(model, instanceNode))
            .listExpander(getListExpander(model, instanceNode))
            .build();
    }

    private Result<String> getSignatureIRI(Model model, Resource instance){
        return ModelSelector.getRequiredURIResourceObject(model, instance, WOTTR.of)
            .map(Resource::getURI);
    }

    private Result<ListExpander> getListExpander(Model model, Resource instance) {
        return ModelSelector.getOptionalResourceObject(model, instance, WOTTR.modifier)
            .flatMap(r -> WOTTR.listExpanders.keySet().contains(r)
                ? Result.ofNullable(WOTTR.listExpanders.get(r))
                : Result.error("Unknown listExpander " + RDFNodes.toString(r) + " in instance " + RDFNodes.toString(instance) + "."));
    }

    private Result<List<Argument>> getArgumentList(Model model, Resource instance) {

        // An instance must have arguments or values, but not both
        Result<RDFList> arguments = ModelSelector.getRequiredListObject(model, instance, WOTTR.arguments);
        Result<RDFList> values = ModelSelector.getRequiredListObject(model, instance, WOTTR.values);

        if (arguments.isPresent() == values.isPresent()) { // true if both exist or both are missing
            return Result.error("An instance must have either one " + RDFNodes.toString(WOTTR.arguments)
                + " or one " + RDFNodes.toString(WOTTR.values) + ".");
        } else if (arguments.isPresent()) {
            return arguments.flatMap(args -> parseArguments(model, args, new WArgumentParser(model)));
        } else {
            // create a parser for values to simple arguments:
            var parser = new TermFactory()
                .andThen(termResult -> ArgumentParser.builder().term(termResult).build());

            return values.flatMap(args -> parseArguments(model, args, parser));
        }
    }

    private Result<List<Argument>> parseArguments(Model model, RDFList argumentList, Function<RDFNode, Result<Argument>> parser) {

        List<Result<Argument>> arguments = argumentList.asJavaList().stream()
            .map(parser)
            .collect(Collectors.toList());

        return Result.aggregate(arguments);
    }

}
