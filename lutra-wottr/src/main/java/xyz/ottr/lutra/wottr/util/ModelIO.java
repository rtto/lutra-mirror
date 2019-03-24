package xyz.ottr.lutra.wottr.util;

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

import io.bdrc.jena.sttl.CompareComplex;
import io.bdrc.jena.sttl.ComparePredicates;
import io.bdrc.jena.sttl.STTLWriter;

import java.util.List;
import java.util.SortedMap;
import java.util.StringJoiner;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.XSD;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.wottr.WOTTR;

public abstract class ModelIO {
    
    private static Context ctx;
    private static Lang sttl;
    
    static {
        // See https://github.com/buda-base/jena-stable-turtle:
        // register the STTL writer
        sttl = STTLWriter.registerWriter();
        // build a map of namespace priorities
        SortedMap<String, Integer> nsPrio = ComparePredicates.getDefaultNSPriorities();
        nsPrio.put(OWL.getURI(), 1);
        nsPrio.put(XSD.getURI(), 1);
        nsPrio.put(OTTR.namespace, 2);
        
        // build a list of predicates URIs to be used (in order) for blank node comparison
        List<String> predicatesPrio = CompareComplex.getDefaultPropUris();
        predicatesPrio.add(WOTTR.values.getURI());
        predicatesPrio.add(WOTTR.parameters.getURI());
        
        // pass the values through a Context object
        ctx = new Context();
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "nsPriorities"), nsPrio);
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "nsDefaultPriority"), 2);
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "complexPredicatesPriorities"), predicatesPrio);
        // the base indentation, defaults to 4
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "nsBaseIndent"), 4);
        // the minimal predicate width, defaults to 14
        ctx.set(Symbol.create(STTLWriter.SYMBOLS_NS + "predicateBaseWidth"), 14);
    }

    public enum Format {
        RDFXML("RDF/XML-ABBREV", Lang.RDFXML), 
        TURTLE("TURTLE", sttl), 
        NTRIPLES("NT", Lang.NTRIPLES);
        
        private final String readFormat;
        private final Lang writeLang;

        private Format(final String readFormat, final Lang writeLang) {
            this.readFormat = readFormat;
            this.writeLang = writeLang;
        }
    }
    
    public static void printModel(Model model, ModelIO.Format format) throws ModelIOException {
        System.out.println(writeModel(model, format));
    }

    public static Model readModel(String file) {
        return readModel(file, FileUtils.guessLang(file, ModelIO.Format.TURTLE.readFormat));
    }

    public static Model readModel(String file, ModelIO.Format serialisation) {
        return readModel(file, serialisation.readFormat);
    }

    private static Model readModel(String file, String serialisation) {
        return FileManager.get().loadModel(file, serialisation);
    }

    public static String shortForm(Model model, List<? extends RDFNode> nodes) {
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (RDFNode node : nodes) {
            sj.add(shortForm(model, node));
        }
        return sj.toString();
    }

    public static String shortForm(Model model, Node node) {
        if (node.isVariable()) {
            return node.toString();
        }
        return model.shortForm(node.toString());
    }

    public static String shortForm(Model model, RDFNode node) {
        if (node.canAs(RDFList.class)) {
            return shortForm(model, node.as(RDFList.class).asJavaList());
        } else {
            return shortForm(model, node.asNode());
        }
    }

    public static String shortForm(RDFNode node) {
        Model model = node.getModel();
        return model == null ? node.toString() : shortForm(model, node.asNode());
    }

    public static String writeModel(Model model, ModelIO.Format format) throws ModelIOException {
        return writeRDFModel(model, format);
    }

    private static String writeRDFModel(Model model, ModelIO.Format format) {
        RDFWriter writer = RDFWriter.create()
                .source(model.getGraph())
                .context(ctx)
                .lang(format.writeLang)
                .build();
        return writer.asString();
    }

}
