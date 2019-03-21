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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.StringJoiner;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;

public abstract class ModelIO {

    public enum Format {
        RDFXML(FileUtils.langXMLAbbrev), 
        TURTLE(FileUtils.langTurtle), 
        N3(FileUtils.langN3), 
        NTRIPLES(FileUtils.langNTriple);
        
        private final String lang;

        private Format(final String lang) {
            this.lang = lang;
        }
    }

    public static void printModel(Model model, ModelIO.Format format) throws ModelIOException {
        System.out.println(writeModel(model, format));
    }

    public static Model readModel(String file) {
        return readModel(file, FileUtils.guessLang(file, ModelIO.Format.TURTLE.lang));
    }

    public static Model readModel(String file, ModelIO.Format serialisation) {
        return readModel(file, serialisation.lang);
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
        StringWriter str = new StringWriter();
        model.write(str, format.lang);
        String modelString = str.toString();
        str.flush();
        try {
            str.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return modelString;
    }

}
