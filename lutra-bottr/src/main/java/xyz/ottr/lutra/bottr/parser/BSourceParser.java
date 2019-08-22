package xyz.ottr.lutra.bottr.parser;

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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.source.JDBCSource;
import xyz.ottr.lutra.bottr.source.SPARQLEndpointSource;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;

class BSourceParser implements Function<Resource, Result<Source<?>>> {

    private Model model;

    public BSourceParser(Model model) {
        this.model = model;
    }

    private static Map<Resource, Result<Source<?>>> sources = new HashMap<>();

    @Override
    public Result<Source<?>> apply(Resource source) {
        return sources.computeIfAbsent(source, this::parseSource);
    }
    
    private Result<Source<?>> parseSource(Resource source) {

        // We allow a source to have other types than just the source classes, therefore
        // cannot use ModelSelector.getRequiredResourceObject
        List<RDFNode> sourceTypes = model.listStatements(source, RDF.type, (RDFNode) null)
            .mapWith(Statement::getObject)
            .filterKeep(BOTTR.sources::contains)
            .toList();

        if (sourceTypes.size() != 1) {
            return Result.error("Expected exactly one source type, but found " + sourceTypes.size()
                    + ": " + RDFNodes.toString(sourceTypes));
        }
        
        RDFNode sourceType = sourceTypes.get(0);
 
        if (BOTTR.JDBCSource.equals(sourceType)) {
            return parseSQLSource(source);
        } else if (BOTTR.SPARQLSource.equals(sourceType)) {
            return parseSPARQLEndpointSource(source);
        }
        return Result.error("Error parsing source. Source type " + RDFNodes.toString(sourceType) + " is not a supported source: "
         + RDFNodes.toString(BOTTR.sources));
    }

    private Result<Source<?>> parseSQLSource(Resource source) {

        Result<JDBCSource.Builder> builder = Result.of(new JDBCSource.Builder());
        builder.addResult(getRequiredLiteralString(source, BOTTR.sourceURL), JDBCSource.Builder::setDatabaseURL);
        builder.addResult(getRequiredLiteralString(source, BOTTR.jdbcDriver), JDBCSource.Builder::setDatabaseDriver);
        builder.addResult(getRequiredLiteralString(source, BOTTR.username), JDBCSource.Builder::setUsername);
        builder.addResult(getRequiredLiteralString(source, BOTTR.password), JDBCSource.Builder::setPassword);
        return builder.map(JDBCSource.Builder::build);
    }
    
    private Result<Source<?>> parseSPARQLEndpointSource(Resource source) {
        return getRequiredLiteralString(source, BOTTR.sourceURL)
            .map(SPARQLEndpointSource::new);
    }

    /*
    private Result<Source<?>> parseRDFSource(Resource source) {

        List<RDFNode> urlNodes = model.listStatements(source, BOTTR.sourceURL, (RDFNode) null)
            .mapWith(Statement::getObject)
            .toList();

        List<Result<String>> urlStrings = ResultStream.innerOf(urlNodes)
            .mapFlatMap(node -> RDFNodes.cast(node, Literal.class))
            .innerMap(Literal::getLexicalForm)
            .collect(Collectors.toList());

        return Result.aggregate(urlStrings)
            .map(RDFSource::new);
    }*/

    // Utility method
    private Result<String> getRequiredLiteralString(Resource subject, Property predicate) {
        return ModelSelector.getRequiredLiteralObject(model, subject, predicate).map(Literal::getLexicalForm);
    }


}
