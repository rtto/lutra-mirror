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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RDFWriterBuilder;

/**
 * RDF IO. All things using RDFDataMgr. Use this for all IO so that we can standardise on
 * default settings.
 */
public enum RDFIO {
    ;

    public static final Lang DEFAULT_LANG = Lang.TURTLE;


    public static RDFWriterBuilder writerBuilder() {
        return RDFWriter.create();
    }

    public static RDFParserBuilder readerBuilder() {
        return RDFParser.create()
            .lang(DEFAULT_LANG);
    }

    public static RDFReader<String> fileReader() {
        return new RDFReader<>() {
            @Override
            void setSource(String source) {
                this.getParserBuilder()
                    .lang(RDFLanguages.filenameToLang(source, DEFAULT_LANG))
                    .source(source);
            }
        };
    }

    public static RDFReader<InputStream> inputStreamReader() {
        return new RDFReader<>() {
            @Override
            void setSource(InputStream source) {
                this.getParserBuilder()
                    .source(source);
            }
        };
    }

    public static RDFWriter getWriter(Model model, Lang language) {
        return writerBuilder()
            .source(model)
            .lang(language)
            .build();
    }

    public static String writeToString(Model model) {
        return getWriter(model, DEFAULT_LANG).asString();
    }

    public static String writeToString(Model model, Lang language) {
        return getWriter(model, language).asString();
    }

}
