package xyz.ottr.lutra.wottr.parser.v03.util;

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

import java.util.Set;

import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.wottr.util.PrefixMappings;

public enum Models {

    ; // singleton emum, utility class

    // private static Logger log = LoggerFactory.getLogger(Models.class);

    public enum BlankCopy {
        KEEP, FRESH
    }

    public static final Model EMPTY = empty();

    /**
     * Add all statements from source to target. Add also prefix mappings from
     * source not in target.
     *
     * @param target
     * @param source
     * @return
     */
    public static void addStatements(Model source, Model target) {
        // copy triples:
        // this overwrites prefixes: target.add(source);
        target.add(source.listStatements());
        // copy prefix mapping:
        PrefixMappings.addPrefixes(target, source);
    }

    /**
     * Create a detached copy of the model, possibly keeping blank nodes as is, and
     * including the model's prefix mapping.
     *
     * @param model
     *            the model to copy
     * @param blankCopyStrategy
     *            strategy for copying blank nodes
     * @return the copy of the model
     */
    public static Model duplicate(Model model, BlankCopy blankCopyStrategy) {
        Model copy = empty();
        addStatements(model, copy);
        if (blankCopyStrategy == BlankCopy.FRESH) {
            ModelEditor.substituteBlanks(copy);
        }
        return copy;
    }

    /**
     * Get a fresh empty model.
     *
     * @return an empty model
     */
    public static Model empty() {
        return ModelFactory.createDefaultModel();
    }

    /**
     * Get an empty model with given prefix mapping.
     *
     * @param mapping
     * @return the empty model
     */
    public static Model empty(PrefixMapping mapping) {
        Model empty = empty();
        empty.setNsPrefixes(mapping);
        return empty;
    }

    public static Set<Triple> toTripleSet(Model model) {
        return GraphUtil.findAll(model.getGraph()).toSet();    
    }

}
