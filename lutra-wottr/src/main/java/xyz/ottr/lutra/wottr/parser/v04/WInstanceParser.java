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

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.InstanceParser;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.parser.TripleInstanceFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class WInstanceParser implements InstanceParser<Model> {

    @Override
    public ResultStream<Instance> apply(Model model) {

        List<Resource> instanceResources = ModelSelector.getSubjects(model, WOTTR.of);
        ResultStream<Instance> instances = parseInstances(model, instanceResources);

        // Get triples which are not part of any of the instances:
        WTripleSerialiser tripleSerialiser = new WTripleSerialiser(model);
        Model instanceModel = ModelFactory.createDefaultModel();
        instanceResources.forEach(i -> instanceModel.add(tripleSerialiser.serialiseInstance(i)));
        Model tripleModel = model.difference(instanceModel);
        ResultStream<Instance> rdfInstances = new TripleInstanceFactory(tripleModel).get();

        return ResultStream.concat(instances, rdfInstances);
    }

    private ResultStream<Instance> parseInstances(Model model, List<Resource> templateInstances) {
        Stream<Result<Instance>> parsedInstances = templateInstances.stream()
            .map(instance -> parseInstance(model, instance));
        return new ResultStream<>(parsedInstances);
    }

    protected Result<Instance> parseInstance(Model model, RDFNode instanceNode) {

        // cast to resource
        Result<Resource> instanceResource = RDFNodes.cast(instanceNode, Resource.class);

        // get signature URI
        Result<String> signatureURI = instanceResource
            .flatMap(ins -> ModelSelector.getRequiredURIResourceObject(model, ins, WOTTR.of))
            .map(Resource::getURI);

        // parse argument list
        Result<ArgumentList> argumentList = instanceResource
            .flatMap(ins -> parseArguments(model, ins));

        return Result.zip(signatureURI, argumentList, Instance::new);
    }

    private Result<ArgumentList> parseArguments(Model model, Resource instance) {

        Result<ArgumentList.Expander> expander = getExpander(model, instance);

        // An instance must have arguments or values, but not both
        Result<RDFList> arguments = ModelSelector.getRequiredListObject(model, instance, WOTTR.arguments);
        Result<RDFList> values = ModelSelector.getRequiredListObject(model, instance, WOTTR.values);

        Result<ArgumentList> argumentList;
        if (arguments.isPresent() == values.isPresent()) { // true if both exist or both are missing
            return Result.error("An instance must have either one " + RDFNodes.toString(WOTTR.arguments)
                + " or one " + RDFNodes.toString(WOTTR.values) + ".");
        } else if (arguments.isPresent()) {
            argumentList = arguments.flatMap(args -> getArguments(model, args, expander));
        } else { //if (values.isPresent()) {
            argumentList = values.flatMap(vals -> getValues(vals, expander));
        }

        argumentList.ifPresent(list -> {
            if (list.hasListExpander() == list.getExpanderValues().isEmpty()) { // xor = not equal
                argumentList.addMessage(Message.error(
                    "An instance must have a list expander if and only if it has one or more expander values."));
            }
        });

        return argumentList;
    }

    /**
     * Note that the expander system may be null both when there is no
     * expanders and when are errors.
     */
    private Result<ArgumentList.Expander> getExpander(Model model, Resource instance) {
        return ModelSelector.getOptionalResourceObject(model, instance, WOTTR.modifier)
            .flatMap(r -> WOTTR.listExpanders.keySet().contains(r)
                ? Result.ofNullable(WOTTR.listExpanders.get(r))
                : Result.error("Unknown expander " + RDFNodes.toString(r) + " in instance " + RDFNodes.toString(instance) + "."));
    }

    private Result<ArgumentList> getArguments(Model model, RDFList arguments, Result<ArgumentList.Expander> expander) {
        WArgumentParser argumentParser = new WArgumentParser(model);
        Result<List<Term>> termList = parseTermsWith(arguments, argumentParser);
        return getArgumentList(termList, expander, argumentParser.getExpanderValues());
    }

    private Result<ArgumentList> getValues(RDFList values, Result<ArgumentList.Expander> expander) {
        Function<RDFNode, Result<Term>> termFactory = new TermFactory(WOTTR.theInstance);
        Result<List<Term>> termList = parseTermsWith(values, termFactory);
        return getArgumentList(termList, expander,null);
    }

    private Result<ArgumentList> getArgumentList(Result<List<Term>> argumentList, Result<ArgumentList.Expander> expander,
                                                 Set<Term> expanderValues) {
        return Result.conditionalZip(argumentList, expander,
            (terms, exp) -> terms.isPresent(),
            (terms, exp) -> new ArgumentList(terms, expanderValues, exp));
    }

    // Finds URIs that are in the ottr namespace, except ottr:none which is allowed.
    private static final Predicate<RDFNode> illegalArgumentValue = node ->
        node.isURIResource()
            && node.asResource().getNameSpace().equals(OTTR.namespace)
            && !node.asResource().equals(WOTTR.none);

    private static Result<List<Term>> parseTermsWith(RDFList lstRes, Function<RDFNode, Result<Term>> parser) {

        List<Result<Term>> x = ResultStream
            .innerOf(lstRes.asJavaList())
            .peek(node -> {
                if (node.isPresent() && illegalArgumentValue.test(node.get())) {
                    node.addMessage(Message.error("Illegal argument, value " + RDFNodes.toString(node.get())
                        + " is in the ottr namespace: " + OTTR.namespace));
                }
            })
            .mapFlatMap(parser)
            .collect(Collectors.toList());
        return Result.aggregate(x);
    }
}
