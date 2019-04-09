package xyz.ottr.lutra.bottr.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.InstanceMap;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.model.ValueMap;
import xyz.ottr.lutra.bottr.source.JDBCSource;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.ModelSelectorException;

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

public class BInstanceMapParser implements Function<Model, ResultStream<InstanceMap>> {

    private Map<Resource, Result<Source>> parsedSources;

    public BInstanceMapParser() {
        this.parsedSources = new HashMap<>();
    }

    @Override
    public ResultStream<InstanceMap> apply(Model model) {
        List<Resource> instanceMaps = ModelSelector.listInstancesOfClass(model, BOTTR.InstanceMap);
        ResultStream<InstanceMap> parsedInstanceMaps = parseInstanceMaps(model, instanceMaps);
        return parsedInstanceMaps;
    }

    private ResultStream<InstanceMap> parseInstanceMaps(Model model, List<Resource> instancesMaps) {
        Stream<Result<InstanceMap>> parsedInstances = instancesMaps
                .stream()
                .map(i -> parseInstanceMap(model, i));
        return new ResultStream<>(parsedInstances);
    }

    protected Result<InstanceMap> parseInstanceMap(Model model, Resource instanceMap) {

        Result<Resource> resTemplate = getRequiredResourceOfProperty(model, instanceMap, BOTTR.template);
        if (resTemplate.isPresent() && !resTemplate.get().isURIResource()) {
            resTemplate.addMessage(Message.error("InstanceMap's template reference is not an IRI."));
        }
        Result<String> resQuery = getRequiredStringOfProperty(model, instanceMap, BOTTR.query);
        Result<Source> resSource = parseSource(model, instanceMap);
        Result<ValueMap> resValueMap = parseValueMap(model, instanceMap);

        // TODO: is this the way to generate a complete error results
        List<Result<?>> inputs = Arrays.asList(resTemplate, resQuery, resSource, resValueMap);
        if (inputs.stream().anyMatch(r -> !r.isPresent())) {
            Result<InstanceMap> resMap = Result.empty();
            inputs.forEach(i -> resSource.addMessages(i.getAllMessages()));
            resSource.addMessage(Message.error("Error parsing InstanceMap " + instanceMap.getURI()));
            return resMap;
        } else {
            return Result.of(new InstanceMap(model, resSource.get(), resQuery.get(), resTemplate.get().getURI(), resValueMap.get()));
        }
    }

    private Result<Source> parseSource(Model model, Resource instanceMap) {

        Result<Resource> resSourceIRI = getRequiredResourceOfProperty(model, instanceMap, BOTTR.source);
        if (!resSourceIRI.isPresent()) {
            return Result.empty(Message.error("Error parsing InstanceMap's source"), resSourceIRI);
        } else {
            Resource source = resSourceIRI.get();
            // keep sources in HashMap, since sources can be shared by InstanceMaps.
            if (!this.parsedSources.containsKey(source)) {
                List<RDFNode> sourceTypes = ModelSelector.listObjectsOfProperty(model, source, RDF.type);

                // TODO: check for disjoint sourceTypes
                Result<Source> resSource;

                if (sourceTypes.contains(BOTTR.SQLSource)) {
                    resSource = parseSQLSource(model, source);
                } else { // no support source type found
                    resSource = Result.empty(Message.error(
                            "Expected type for source: " + source.toString() + ", but found none."));
                }
                this.parsedSources.put(source, resSource);
            }
            return this.parsedSources.get(source);
        }
    }

    private Result<Source> parseSQLSource(Model model, Resource source) {
        Result<String> username = getRequiredStringOfProperty(model, source, BOTTR.username);
        Result<String> password = getRequiredStringOfProperty(model, source, BOTTR.password);
        Result<String> jdbcDriver = getRequiredStringOfProperty(model, source, BOTTR.jdbcDriver);
        Result<String> jdbcDatabaseURL = getRequiredStringOfProperty(model, source, BOTTR.jdbcDatabaseURL);

        // TODO: is this the way to generate a complete error results
        List<Result<?>> inputs = Arrays.asList(username, password, jdbcDriver, jdbcDatabaseURL);
        if (inputs.stream().anyMatch(r -> !r.isPresent())) {
            Result<Source> resSource = Result.empty();
            inputs.forEach(i -> resSource.addMessages(i.getAllMessages()));
            resSource.addMessage(Message.error("Error parsing SQLSource " + source.getURI()));
            return resSource;
        } else {
            return Result.of(new JDBCSource(username.get(), password.get(), jdbcDriver.get(), jdbcDatabaseURL.get()));    
        }
    }

    private Result<ValueMap> parseValueMap(Model model, Resource instanceMap) {
        Result<Resource> resValueMapIRI = getRequiredResourceOfProperty(model, instanceMap, BOTTR.valueMap);
        if (!resValueMapIRI.isPresent()) {
            return Result.empty(Message.error("Error parsing InstanceMap's source"));
        } else if (resValueMapIRI.isPresent() && !resValueMapIRI.get().canAs(RDFList.class)) {
            return Result.empty(Message.error("InstanceMap's valueMap is not a list."));
        } else {
            List<RDFNode> valueMapList = resValueMapIRI.get().as(RDFList.class).asJavaList();
            List<Result<String>> typeList = valueMapList.stream()
                    .map(n -> n.asResource())
                    .map(r -> getRequiredResourceOfProperty(model, r, BOTTR.type))
                    .map(r -> r.map(g -> g.getURI())) // TODO: is this correct?
                    .collect(Collectors.toList());
            return Result.aggregate(typeList).map(ValueMap::new);
        }
    }

    // TODO: Move this to ModelSelector?
    private Result<String> getRequiredStringOfProperty(Model model, Resource subject, Property property) {
        try {
            return Result.of(ModelSelector.getRequiredLiteralOfProperty(model, subject, property).getLexicalForm());
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error(
                    "Error parsing property " + property.getLocalName() + " of " + subject.getURI() + ": " + ex.getMessage()));
        }
    }

    // TODO: Move this to ModelSelector?
    private Result<Resource> getRequiredResourceOfProperty(Model model, Resource subject, Property property) {
        try {
            return Result.of(ModelSelector.getRequiredResourceOfProperty(model, subject, property));
        } catch (ModelSelectorException ex) {
            return Result.empty(Message.error("Error parsing property " + property.getLocalName()
                    + " of " + subject.getURI() + ": " + ex.getMessage()));
        }
    }
}
