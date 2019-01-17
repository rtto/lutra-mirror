package xyz.ottr.lutra.wottr.io;

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.dyreriket.gaupa.rdf.ModelSelector;
import org.dyreriket.gaupa.rdf.ModelSelectorException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.io.InstanceParser;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.IRITerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.WTermFactory;

public class WInstanceParser implements InstanceParser<Model> {

    //private final Logger log = LoggerFactory.getLogger(WInstanceParser.class);

    private static Map<Resource, ArgumentList.Expander> expanders;

    static {
        expanders = new HashMap<>();
        expanders.put(WOTTR.cross, ArgumentList.Expander.CROSS);
        expanders.put(WOTTR.zipMin, ArgumentList.Expander.ZIPMIN);
        expanders.put(WOTTR.zipMax, ArgumentList.Expander.ZIPMAX);
    }

    public WInstanceParser() {
    }

    @Override
    public ResultStream<Instance> apply(Model model) {

        List<Resource> ins = ModelSelector.listResourcesWithProperty(model, WOTTR.of);
        ResultStream<Instance> parsedInstances = parseInstances(model, ins);
        
        Model triples = WReader.getNonTemplateTriples(model, null, new LinkedList<>(), ins);
        return ResultStream.concat(parsedInstances, new TripleParser().apply(triples));
    }

    private ResultStream<Instance> parseInstances(Model model, List<Resource> templateInstances) {
        Stream<Result<Instance>> parsedInstances = templateInstances
            .stream()
            .map(i -> parseInstance(model, i));
        return new ResultStream<>(parsedInstances);
    }

    protected Result<Instance> parseInstance(Model model, RDFNode instanceNode) {

        if (!instanceNode.isResource()) {
            return Result.empty(Message.error(
                        "Expected instance to be a resource, found non-resource "
                        + instanceNode.toString() + " in model " + model.toString() + "."));
        }

        Resource instance = instanceNode.asResource();

        Resource ofIRIRes;
        try {
            ofIRIRes = ModelSelector.getRequiredResourceOfProperty(model, instance, WOTTR.of);
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error(
                    "Error parsing template IRI of instance: " + ex.getMessage()));
        }

        Result<ArgumentList> resArgumentList = parseArguments(model, instance);
        String ofIRI = ofIRIRes.getURI();
        Result<Instance> ins = resArgumentList.map(args -> new Instance(ofIRI, args));

        if (ofIRI == null) {
            ins.addMessage(Message.error("Instance references template with non-IRI."));
        }

        return ins;
    }

    private Result<ArgumentList> parseArguments(Model model, Resource instance) {
        
        Result<ArgumentList> resArgumentList;

        try {
            Result<ArgumentList.Expander> expander = parseExpander(model, instance);
            if (model.contains(instance, WOTTR.arguments)) {
                Resource arguments = ModelSelector.getRequiredResourceOfProperty(model, instance, WOTTR.arguments);
                resArgumentList = parseArgumentTerms(model, arguments, expander);
                if (model.contains(instance, WOTTR.values, (RDFNode) null)) {
                    resArgumentList.addMessage(Message.error(
                            "An instance cannot have both arguments (via " + WOTTR.arguments.toString()
                                + ") and values (via " + WOTTR.values + ")."));
                }
            } else {
                Resource arguments = ModelSelector.getRequiredResourceOfProperty(model, instance, WOTTR.values);
                resArgumentList = parseValueTerms(arguments, expander);
            }
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error(
                    "Error parsing argument list of instance of template with IRI: " + ex.getMessage()));
        }

        checkForExpanderErrors(resArgumentList);
        return resArgumentList;
    }

    public Result<List<Term>> parseTermsWith(Resource lstRes, Function<RDFNode, Result<Term>> parser) {
        List<Result<Term>> parsedRes = lstRes
            .as(RDFList.class)
            .asJavaList()
            .stream()
            .map(parser)
            .map(termRes -> termRes.flatMap(term -> {
                Result<Term> toAddErr = Result.of(term);
                if (term instanceof IRITerm && ((IRITerm) term).getIRI().startsWith(WOTTR.namespace)) {
                    toAddErr.addMessage(Message.error("Term with ottr-prefix occurs as argument to insance."));
                }
                return toAddErr;
            }))
            .collect(Collectors.toList());
        return Result.aggregate(parsedRes);
    }

    private Result<ArgumentList> parseArgumentTerms(Model model, Resource argsRes,
        Result<ArgumentList.Expander> expander) {
        
        WArgumentParser argumentParser = new WArgumentParser(model);
        Result<List<Term>> resParsedArguments = parseTermsWith(argsRes, argumentParser);
        if (expander != null) {
            return Result.zip(resParsedArguments, expander,
                (terms, exp) -> new ArgumentList(terms, argumentParser.getExpanderValues(), exp));
        } else {
            return resParsedArguments.map(
                terms -> new ArgumentList(terms, argumentParser.getExpanderValues(), null));
        }
    }

    private Result<ArgumentList> parseValueTerms(Resource valuesRes, Result<ArgumentList.Expander> expander) {
        
        WTermFactory termFactory = new WTermFactory();
        Result<List<Term>> resParsedValues = parseTermsWith(valuesRes, termFactory);
        if (expander != null) {
            return Result.zip(resParsedValues, expander,
                (terms, exp) -> new ArgumentList(terms, null, exp));
        } else {
            return resParsedValues.map(terms -> new ArgumentList(terms, null, null));
        }
    }

    private Result<ArgumentList.Expander> parseExpander(Model model, Resource instance) {

        Resource listExpanderRes =
            ModelSelector.getOptionalResourceOfProperty(model, instance, WOTTR.modifier);
        if (listExpanderRes == null) {
            return null;
        }
        
        ArgumentList.Expander listExpander = expanders.get(listExpanderRes);

        if (listExpander == null) {
            Message msg = Message.error(
                "Instance of template has unknown list expander " + listExpanderRes.toString() + ".");
            return Result.empty(msg);
        }
        return Result.of(listExpander);
    }

    private void checkForExpanderErrors(Result<ArgumentList> resArgumentList) {

        if (!resArgumentList.isPresent()) {
            return;
        }

        boolean hasListExpander = resArgumentList.get().hasListExpander();
        boolean hasExpanderValues = !resArgumentList.get().getExpanderValues().isEmpty();

        if (hasListExpander && !hasExpanderValues) {
            Message msg = Message.error(
                "Instance of template has list expander, but no expander values.");
            resArgumentList.addMessage(msg);
        } else if (!hasListExpander && hasExpanderValues) {
            Message msg = Message.error(
                "Instance of template has no list expander, but has expander values.");
            resArgumentList.addMessage(msg);
        } 
    }
}
