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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.io.ReaderRegistry;
import xyz.ottr.lutra.io.TemplateParser;
import xyz.ottr.lutra.io.TemplateReader;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.ModelSelectorException;

public class WTemplateParser implements TemplateParser<Model> {

    //private final Logger log = LoggerFactory.getLogger(WOTTRParser.class);
    private final WInstanceParser instanceParser;
    private final PrefixMapping prefixes;

    public WTemplateParser() {
        this.instanceParser = new WInstanceParser();
        this.prefixes = PrefixMapping.Factory.create();
        ReaderRegistry.registerTemplateReader(new TemplateReader(new WFileReader(), this));
    }

    @Override
    public Map<String, String> getPrefixes() {
        return this.prefixes.getNsPrefixMap();
    }

    @Override
    public ResultStream<TemplateSignature> apply(Model model) {

        ResultStream<TemplateSignature> sigs = ResultStream
            .innerOf(ModelSelector.listInstancesOfClass(model, WOTTR.TemplateSignature))
            .mapFlatMap(res -> this.parseNonDefintion(model, res, false));
            
        ResultStream<TemplateSignature> bases = ResultStream
            .innerOf(ModelSelector.listInstancesOfClass(model, WOTTR.BaseTemplate))
            .mapFlatMap(res -> this.parseNonDefintion(model, res, true));
            
        ResultStream<TemplateSignature> tpls = ResultStream
            .innerOf(ModelSelector.listInstancesOfClass(model, WOTTR.Template))
            .mapFlatMap(res -> this.parseTemplateDefinition(model, res));

        this.prefixes.setNsPrefixes(model);
            
        return ResultStream.concat(sigs, ResultStream.concat(bases, tpls));
    }

    private Result<TemplateSignature> parseNonDefintion(Model model, Resource res, boolean isBase) {
        Result<TemplateSignature> signature = parseSignature(model, res, isBase);

        if (model.contains(res, WOTTR.pattern, (RDFNode) null)) {
            String type = isBase ? "Base template " : "Template signature ";
            signature.addMessage(Message.error(type + res.getURI() + " should not have a pattern."));
        }
        return signature;
    }

    private Result<TemplateSignature> parseSignature(Model model, Resource res, boolean isBase) {

        String templateURI = res.getURI();
        Resource paramsRes;
        try {
            paramsRes = ModelSelector.getRequiredResourceOfProperty(model, res, WOTTR.parameters);
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error(
                    "Error when parsing parameters of template with IRI " + templateURI + ": " + ex.getMessage()));
        }
        if (!paramsRes.canAs(RDFList.class)) {
            return Result.empty(Message.error(
                    "Parameters of template with IRI " + templateURI + " is not a proper RDF-list."));
        }

        Result<ParameterList> parsedParameters = parseParameters(model, paramsRes);

        if (model.contains(res, WOTTR.annotation, (RDFNode) null)) {
            parsedParameters.addMessage(Message.error("Annotations are not yet supported."));
        }
        if (templateURI == null) {
            parsedParameters.addMessage(Message.error(
                    "Template name was blank node " + res.toString()
                    + ", but a template should be denoted by an IRI."));
        }

        return parsedParameters.map(params -> new TemplateSignature(templateURI, params, isBase));
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

    private Result<TemplateSignature> parseTemplateDefinition(Model model, Resource res) {
        
        Result<TemplateSignature> signature = parseSignature(model, res, false);
        Set<Result<Instance>> instancesRes = model.listObjectsOfProperty(res, WOTTR.pattern)
            .mapWith(ins -> instanceParser.parseInstance(model, ins))
            .toSet();
        Result<Set<Instance>> instances = Result.aggregate(instancesRes);
        return Result.zip(signature, instances, (sig, is) -> (TemplateSignature) new Template(sig, is));
    }

    // public Result<TemplateSignature> parseTemplateWithImplicitBody(Model model) {
    //     // Parse template's head
    //     Resource template;
    //     try {
    //         template = ModelSelector.getRequiredInstanceOfClass(model, WOTTR.Template);
    //     } catch (ModelSelectorException ex) {
    //         return Result.empty(Message.error( "No element of type " + WOTTR.Template + " found in model."));
    //     }
    //     String templateURI = template.getURI();

    //     List<Resource> parameters = ModelSelector.listResourcesOfProperty(model, template, WOTTR.hasParameter);
    //     WParameterListParser rdfParameterListParser = new WParameterListParser(model);
    //     Result<ParameterList> parsedParameters = rdfParameterListParser.parseParameters(parameters);

    //     // Parse template's body's instances
    //     Model withoutHead = model.difference(WReader.getTemplateHead(model, template, parameters));
    //     withoutHead.setNsPrefixes(model);
    //     Result<Set<Instance>> instances = instanceParser.apply(withoutHead)
    //         .aggregate()
    //         .map(strm -> strm.collect(Collectors.toSet()));

    //     return makeTemplateFromResults(templateURI, parsedParameters, instances);
    // }
}
