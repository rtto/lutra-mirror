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

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.bottr.BOTTR;
import xyz.ottr.lutra.bottr.model.Source;
import xyz.ottr.lutra.bottr.source.H2Source;
import xyz.ottr.lutra.bottr.source.JDBCSource;
import xyz.ottr.lutra.bottr.source.RDFSource;
import xyz.ottr.lutra.bottr.source.SPARQLEndpointSource;
import xyz.ottr.lutra.bottr.util.DataParser;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;

class BSourceParser implements Function<Resource, Result<Source<?>>> {

    private final Model model;
    private final String filePath; // @Nullable

    BSourceParser(Model model, String filePath) {
        this.model = model;
        this.filePath = filePath;
    }

    private static final Map<Resource, Result<Source<?>>> sources = new HashMap<>();

    @Override
    public Result<Source<?>> apply(Resource source) {
        return sources.computeIfAbsent(source, this::parseSource);
    }
    
    private Result<Source<?>> parseSource(Resource source) {

        // We allow a source to have other types than just the source classes, therefore
        // cannot use ModelSelector.getRequiredResourceObject.
        List<RDFNode> sourceTypes = this.model.listObjectsOfProperty(source, RDF.type)
            .filterKeep(BOTTR.sources::contains)
            .toList();

        if (sourceTypes.size() != 1) {
            return Result.error("Expected exactly one source type, but found " + sourceTypes.size()
                    + ": " + RDFNodes.toString(sourceTypes));
        }
        
        RDFNode sourceType = sourceTypes.get(0);
 
        if (BOTTR.JDBCSource.equals(sourceType)) {
            return getSQLSource(source);
        } else if (BOTTR.SPARQLEndpointSource.equals(sourceType)) {
            return getSPARQLEndpointSource(source);
        } else if (BOTTR.RDFFileSource.equals(sourceType)) {
            return getRDFSource(source);
        } else if (BOTTR.H2Source.equals(sourceType)) {
            return getH2Source(source);
        }
        return Result.error("Error parsing source. Source type " + RDFNodes.toString(sourceType) + " is not a supported source: "
            + RDFNodes.toString(BOTTR.sources));
    }

    private Result<Source<?>> getSQLSource(Resource source) {

        Result<JDBCSource.JDBCSourceBuilder> builder = Result.of(JDBCSource.builder());
        builder.addResult(getRequiredLiteralString(source, BOTTR.sourceURL), JDBCSource.JDBCSourceBuilder::databaseURL);
        builder.addResult(getRequiredLiteralString(source, BOTTR.jdbcDriver), JDBCSource.JDBCSourceBuilder::databaseDriver);
        builder.addResult(getRequiredLiteralString(source, BOTTR.username), JDBCSource.JDBCSourceBuilder::username);
        builder.addResult(getRequiredLiteralString(source, BOTTR.password), JDBCSource.JDBCSourceBuilder::password);
        return builder.map(JDBCSource.JDBCSourceBuilder::build);
    }
    
    private Result<Source<?>> getSPARQLEndpointSource(Resource source) {
        return getRequiredLiteralString(source, BOTTR.sourceURL)
            .map(url -> new SPARQLEndpointSource(this.model, url));
    }

    private Result<Source<?>> getRDFSource(Resource source) {

        // RDFFileSource takes a *list* of sourceURLs, therefore a bit more work than for SPARQLEndpointSource.
        NodeIterator sourceNodes = this.model.listObjectsOfProperty(source, BOTTR.sourceURL);

        List<Result<String>> urlStrings = ResultStream.innerOf(sourceNodes)
            .mapFlatMap(node -> RDFNodes.cast(node, Literal.class))
            .innerMap(Literal::getLexicalForm)
            .innerMap(this::getPath)
            .collect(Collectors.toList());

        return Result.aggregate(urlStrings)
            .map(url -> new RDFSource(this.model, url));
    }

    private Result<Source<?>> getH2Source(Resource source) {
        return ModelSelector.getOptionalLiteralObject(this.model, source, BOTTR.sourceURL)
            .map(Literal::getLexicalForm)
            .map(this::getPath)
            .mapOrElse(H2Source::new, new H2Source());
    }

    /**
     * Use this.filePath (if it exists) to create an absolute path of the input, if input is not a URL.
     * Otherwise return input unaltered.
     */
    private String getPath(String file) {

        if (Objects.isNull(this.filePath) || DataParser.asURI(file).isPresent()) {
            return file;
        } else {
            return Paths.get(this.filePath).resolveSibling(file).toAbsolutePath().toString();
        }
    }

    // Utility method
    private Result<String> getRequiredLiteralString(Resource subject, Property predicate) {
        return ModelSelector.getRequiredLiteralObject(this.model, subject, predicate)
            .map(Literal::getLexicalForm);
    }


}
