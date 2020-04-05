package xyz.ottr.lutra.wottr.io;

/*-
 * #%L
 * lutra-wottr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import lombok.Getter;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.io.InputReader;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

@Getter
public abstract class RDFReader<X> implements InputReader<X, Model> {

    private final PrefixMapping prefixes;
    protected final RDFParserBuilder parserBuilder;

    RDFReader() {
        this.prefixes = PrefixMapping.Factory.create();

        // set defaults here:
        this.parserBuilder = RDFIO.readerBuilder();
    }

    public ResultStream<Model> apply(X input) {
        return ResultStream.of(parse(input));
    }

    public Result<Model> parse(X source) {
        try {
            return Result.of(parseToModel(source));
        } catch (JenaException | HttpException ex) {
            // TODO: Correct Message level?
            // TODO: Make messages for other exceptions(?)
            return Result.error("Error reading RDF input from "
                + source.getClass().getSimpleName() + " " + source + ": "
                + ex.getMessage());
        }
    }

    private Model parseToModel(X source) {

        setSource(source);
        var model = ModelFactory.createDefaultModel();
        this.parserBuilder.parse(model);

        this.prefixes.setNsPrefixes(model);

        return model;
    }

    abstract void setSource(X source);

}
