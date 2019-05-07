package xyz.ottr.lutra.wottr.legacy.io;

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

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.PrefixMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.ottr.lutra.io.InputReader;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.wottr.util.ModelIO;

public class WFileReader implements InputReader<String, Model> {

    private final Logger log = LoggerFactory.getLogger(WFileReader.class);
    private final PrefixMapping prefixes; // Gathers prefixes parsed for later output

    public WFileReader() {
        this.prefixes = PrefixMapping.Factory.create();
    }

    public PrefixMapping getPrefixes() {
        return this.prefixes;
    }

    public Result<Model> parse(String url) {
        url = FilenameUtils.separatorsToSystem(url);
        Result<Model> result = null;
        try {
            Model model = WReader.getCanonicalModel(ModelIO.readModel(url));
            this.prefixes.setNsPrefixes(model);
            result = Result.ofNullable(model);
            log.info("Adding model " + model.hashCode() + " with URI " + url);
        } catch (JenaException ex) {
            // TODO: Correct Message level?
            result = Result.empty(new Message(Message.ERROR, "Unable to parse model " + url + ": " + ex.getMessage()));
        } catch (Exception e) { // TODO: Make messages for other exceptions(?)
            result = Result.empty(new Message(Message.ERROR, "Unable to parse model " + url + ": " + e.getMessage()));
        }
        return result;
    }

    public ResultStream<Model> apply(String url) {
        return ResultStream.of(parse(url));
    }
}
