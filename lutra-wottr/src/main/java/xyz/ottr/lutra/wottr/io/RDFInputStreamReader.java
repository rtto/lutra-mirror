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

import java.io.InputStream;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ottr.lutra.io.InputReader;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class RDFInputStreamReader implements InputReader<InputStream, Model> {

    private static final Logger log = LoggerFactory.getLogger(RDFInputStreamReader.class);
    private final PrefixMapping prefixes; // Gathers prefixes parsed for later output
    
    public RDFInputStreamReader() {
        this.prefixes = PrefixMapping.Factory.create();
    }

    public PrefixMapping getPrefixes() {
        return this.prefixes;
    }

    public Result<Model> parse(InputStream input) {

        Result<Model> result;
        try {
            Model model = ModelFactory.createDefaultModel().read(input, null, "TTL");
            this.prefixes.setNsPrefixes(model);
            result = Result.ofNullable(model);
            log.info("Adding model " + model.hashCode() + " from input stream.");
        } catch (JenaException | HttpException ex) {
            // TODO: Correct Message level?
            // TODO: Make messages for other exceptions(?)
            result = Result.error("Unable to parse model from input stream: " + input, ex);
        }
        return result;
    }

    public ResultStream<Model> apply(InputStream input) {
        return ResultStream.of(parse(input));
    }
}
