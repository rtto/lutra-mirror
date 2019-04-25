package xyz.ottr.lutra.bottr.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.source.JDBCSource;
import xyz.ottr.lutra.bottr.source.SPARQLEndpointSource;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.util.ModelSelector;

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

@SuppressWarnings("rawtypes")
public class BSourceParser implements BiFunction<Model, Resource, Result<Source>> {

    private static final List<Resource> supportedSources = Arrays.asList(BOTTR.SQLSource);
    
    private static Map<Integer, Result<Source>> sources = new HashMap<>();

    public BSourceParser() {}
    
    @Override
    public Result<Source> apply(Model model, Resource source) {
        int key = Objects.hash(model, source);
        if (!sources.containsKey(key)) {
            sources.put(key, this.compute(model, source));
        }
        return sources.get(key);
    }
    
    private Result<Source> compute(Model model, Resource source) {
        
        List<RDFNode> sourceTypes = ModelSelector.listObjectsOfProperty(model, source, RDF.type);
        
        sourceTypes.removeIf(t -> !supportedSources.contains(t));
        
        if (sourceTypes.size() != 1) {
            return Result.empty(Message.error(
                    "Expected exactly one source type, but found " + sourceTypes.size() 
                    + ": " + sourceTypes.toString()));
        }
        
        RDFNode sourceType = sourceTypes.get(0);
 
        if (BOTTR.SQLSource.equals(sourceType)) {
            return parseSQLSource(model, source);
        } else if (BOTTR.SPARQLSource.equals(sourceType)) {
            return parseSPARQLEndpointSource(model, source);
        }
        return Result.empty(Message.error("Error parsing source. Source type " + sourceType + " is not supported."));
    }

    private Result<Source> parseSQLSource(Model model, Resource source) {
        Result<String> username = BModelSelector.getRequiredStringOfProperty(model, source, BOTTR.username);
        Result<String> password = BModelSelector.getRequiredStringOfProperty(model, source, BOTTR.password);
        Result<String> jdbcDriver = BModelSelector.getRequiredStringOfProperty(model, source, BOTTR.jdbcDriver);
        Result<String> jdbcDatabaseURL = BModelSelector.getRequiredStringOfProperty(model, source, BOTTR.jdbcDatabaseURL);

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
    
    private Result<Source> parseSPARQLEndpointSource(Model model, Resource source) {
        Result<String> endpoint = BModelSelector.getRequiredStringOfProperty(model, source, BOTTR.sparqlEndpoint);
        return endpoint.map(SPARQLEndpointSource::new); 
    }



    
}
