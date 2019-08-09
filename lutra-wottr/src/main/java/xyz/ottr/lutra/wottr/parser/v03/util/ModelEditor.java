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

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

public abstract class ModelEditor {

    // private static Logger log = LoggerFactory.getLogger(ModelEditor.class);

    public static void substituteBlanks(Model model) {
        for (RDFNode node : ModelSelector.getRDFNodes(model)) {
            if (node.isAnon()) {
                substituteNode(model, node, ResourceFactory.createResource());
            }
        }
    }

    public static void substituteNode(Graph graph, Node old, Node fresh) throws ModelEditorException {
        // subjects
        Set<Triple> subjects = graph.find(old, Node.ANY, Node.ANY).toSet();
        if (!subjects.isEmpty() && fresh.isLiteral()) {
            throw new ModelEditorException("Cannot put literal + '" + fresh + "' + in subject position.");
        }
        for (Triple t : subjects) {
            graph.delete(t);
            graph.add(new Triple(fresh, t.getPredicate(), t.getObject()));

        }
        // predicate
        Set<Triple> predicates = graph.find(Node.ANY, old, Node.ANY).toSet();
        if (!predicates.isEmpty() && (fresh.isBlank() || fresh.isLiteral())) {
            throw new ModelEditorException("Cannot put literal or blank node '" + fresh + "' in predicate position.");
        }
        for (Triple t : predicates) {
            graph.delete(t);
            graph.add(new Triple(t.getSubject(), fresh, t.getObject()));
        }
        // object, anything goes
        for (Triple t : graph.find(Node.ANY, Node.ANY, old).toList()) {
            graph.delete(t);
            graph.add(new Triple(t.getSubject(), t.getPredicate(), fresh));
        }
    }

    public static void substituteNode(Model model, Node old, Node fresh) throws ModelEditorException {
        try {
            substituteNode(model.getGraph(), old, fresh);
        } catch (ModelEditorException e) {
            throw new ModelEditorException(
                    "Error replacing " + old.toString() + " with " + fresh.toString() + ": " + e.getMessage());
        }
    }

    public static void substituteNode(Model model, RDFNode old, RDFNode fresh) throws ModelEditorException {
        substituteNode(model, old.asNode(), fresh.asNode());
    }

}
