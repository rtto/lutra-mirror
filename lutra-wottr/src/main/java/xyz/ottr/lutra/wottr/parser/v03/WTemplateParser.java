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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.io.TemplateParser;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.BlankNodeTerm;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.TermList;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelector;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelectorException;
import xyz.ottr.lutra.wottr.vocabulary.v03.WOTTR;

public class WTemplateParser implements TemplateParser<Model> {

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

        Model canonModel = WParserUtils.getCanonicalModel(model);
        if (canonModel.listStatements((Resource) null, WOTTR.hasPattern, (RDFNode) null).hasNext()) {
            return parseTemplatesWithExplicitBody(canonModel)
                .innerMap(this::changeListVariablesToBlanks);
        } else {
            return ResultStream.of(parseTemplateWithImplicitBody(canonModel))
                .innerMap(this::changeListVariablesToBlanks);
        }
    }

    private Result<TemplateSignature> makeTemplateFromResults(String uri, Result<ParameterList> params, Result<Set<Instance>> ins) {
        return Result.zip(params, ins, (ps, is) -> (TemplateSignature) new Template(uri, ps, is));
    }

    public Result<TemplateSignature> parseTemplateWithImplicitBody(Model model) {
        // Parse template's head
        Resource template;
        try {
            template = ModelSelector.getRequiredInstanceOfClass(model, WOTTR.Template);
        } catch (ModelSelectorException ex) {
            return Result.empty(new Message(Message.ERROR, "No element of type " + WOTTR.Template + " found in model."));
        }
        String templateURI = template.getURI();

        WParameterListParser parameterListParser = new WParameterListParser(model);
        Result<ParameterList> parsedParameters;
        Model withoutHead;
        if (model.contains(template, WOTTR.hasParameter)) {
            List<Resource> parameters = ModelSelector.listResourcesOfProperty(model, template, WOTTR.hasParameter);
            parsedParameters = parameterListParser.parseParameters(parameters);
            withoutHead = model.difference(WParserUtils.getTemplateHeadWParam(model, template, parameters));
        } else if (model.contains(template, WOTTR.withVariables)) {
            Resource parameters = ModelSelector.getRequiredResourceOfProperty(model, template, WOTTR.withVariables);
            parsedParameters = parameterListParser.parseVariables(parameters);
            withoutHead = model.difference(WParserUtils.getTemplateHeadWVars(model, template));
        } else {
            return Result.error("Template with IRI " + templateURI + " does not have any parameters.");
        }

        // Parse template's body's instances
        withoutHead.setNsPrefixes(model);
        Result<Set<Instance>> instances = this.instanceParser.apply(withoutHead)
            .aggregate()
            .map(strm -> strm.collect(Collectors.toSet()));

        // TODO: Decide if should use zip or zipNullables, if one is empty, should we still make template?
        return makeTemplateFromResults(templateURI, parsedParameters, instances);
    }

    public ResultStream<TemplateSignature> parseTemplatesWithExplicitBody(Model model) {
        
        Stream.Builder<Result<TemplateSignature>> templates = Stream.builder();

        for (Resource template : ModelSelector.listInstancesOfClass(model, WOTTR.Template)) {

            String templateURI = template.getURI();
            List<Resource> parameters = ModelSelector.listResourcesOfProperty(model, template, WOTTR.hasParameter);
            WParameterListParser parameterListParser = new WParameterListParser(model);
            Result<ParameterList> parsedParameters = parameterListParser.parseParameters(parameters);

            Set<Result<Instance>> instancesRes = model.listObjectsOfProperty(template, WOTTR.hasPattern)
                .mapWith(res -> this.instanceParser.parseInstance(model, res))
                .toSet();
            Result<Set<Instance>> instances = Result.aggregate(instancesRes);

            templates.add(makeTemplateFromResults(templateURI, parsedParameters, instances));
        }
        return new ResultStream<>(templates.build());
    }

    private TemplateSignature changeListVariablesToBlanks(TemplateSignature template) {

        ParameterList params = template.getParameters();
        Map<List<Term>, Term> listToBlanks = new HashMap<>();

        ParameterList newParamList = parametersListToBlank(params, listToBlanks);

        if (template instanceof Template) {
            Set<Instance> newInstances = new HashSet<>();
            for (Instance ins : ((Template) template).getBody()) {
                String of = ins.getIRI();
                ArgumentList arguments = ins.getArguments();
                List<Term> newArgs = new LinkedList<>();
                Set<Term> newExpanderValues = new HashSet<>();

                for (Term arg : arguments.asList()) {
                    Term newArg;
                    if (arg instanceof TermList
                        && listToBlanks.containsKey(((TermList) arg).asList())) {

                        newArg = listToBlanks.get(((TermList) arg).asList());
                    } else {
                        newArg = arg;
                    }
                    newArgs.add(newArg);
                    if (arguments.hasListExpander(arg)) {
                        newExpanderValues.add(newArg);
                    }
                }
                ArgumentList newArguments = new ArgumentList(
                    new TermList(newArgs), newExpanderValues, arguments.getListExpander());
                newInstances.add(new Instance(of, newArguments));
            }
            return new Template(template.getIRI(), newParamList, newInstances);
        } else {
            return new TemplateSignature(template.getIRI(), newParamList, template.isBaseTemplate());
        }
    }


    private ParameterList parametersListToBlank(ParameterList params, Map<List<Term>, Term> listToBlanks) {

        List<Term> newParams = new LinkedList<>();
        Set<Term> newNonBlanks = new HashSet<>();
        Set<Term> newOptionals = new HashSet<>();
        Map<Term, Term> newDefaultValues = new HashMap<>();

        int i = 0;
        
        for (Term param : params.asList()) {
            Term newParam;
            if (param instanceof TermList) {
                Term blank = new BlankNodeTerm("listVariable" + i);
                blank.setType(param.getType());
                i++;
                listToBlanks.put(((TermList) param).asList(), blank);
                newParam = blank;
            } else {
                newParam = param;
            }
            newParams.add(newParam);
            if (params.isNonBlank(param)) {
                newNonBlanks.add(newParam);
            }
            if (params.isOptional(param)) {
                newOptionals.add(newParam);
            }
            if (params.hasDefaultValue(param)) {
                newDefaultValues.put(newParam, params.getDefaultValue(param));
            }
        }

        return new ParameterList(
            new TermList(newParams), newNonBlanks, newOptionals, newDefaultValues);
    }
}
