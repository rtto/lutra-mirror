package xyz.ottr.lutra.wottr.parser.v04;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.Models;

// TODO: Remove this class, rename to WReaderUtils or move methods into other classes
public class WReader {



    public static Model getNonTemplateTriples(Model model, Resource template, List<Resource> parameters,
            List<Resource> templateInstances) {

        Model modelCopy = Models.duplicate(model, Models.BlankCopy.KEEP);
        modelCopy.remove(getNeighbourhood(model, template));
        // Remove head
        for (Resource p : parameters) {
            modelCopy.remove(getNeighbourhood(model, p));
        }
        // Remove all instances
        for (Resource i : templateInstances) {
            List<Statement> ineigh = getNeighbourhood(model, i);
            modelCopy.remove(ineigh);
            for (Statement s : ineigh) {
                if (s.getPredicate().equals(WOTTR.arguments)
                    || s.getPredicate().equals(WOTTR.values)) {

                    RDFNode o = s.getObject();
                    modelCopy.remove(getNeighbourhood(model, o.asResource()));
                    if (s.getPredicate().equals(WOTTR.arguments)) {
                        model.listStatements(o.asResource(), WOTTR.value, (Resource) null)
                            .forEachRemaining(valStmt ->
                                modelCopy.remove(getNeighbourhood(model, valStmt.getObject().asResource())));
                    }
                }
            }
        }
        modelCopy.setNsPrefixes(model);
        return modelCopy;
    }

    public static Model getTemplateHead(Model model, Resource template, List<Resource> parameters) {

        Model head = Models.empty();
        head.add(getNeighbourhood(model, template));
        parameters.stream().forEach(r -> head.add(getNeighbourhood(model, r)));
        head.setNsPrefixes(model);
        return head;
    }

    private static List<Statement> getNeighbourhood(Model model, Resource iri) {
        List<Statement> neighbourhood = new ArrayList<>();
        if (iri == null) {
            // Jena treats null as variable, and would return all triples
            return neighbourhood;
        }

        for (StmtIterator it = model.listStatements(iri, null, (RDFNode) null); it.hasNext();) {
            Statement t = it.next();
            neighbourhood.add(t);
            // if object is a list, we add the whole list:
            RDFNode object = t.getObject();
            if (object.canAs(RDFList.class)) {
                neighbourhood.addAll(ModelSelector.getAllListStatements(object.as(RDFList.class)));
            }
            if (object.isResource()) {
                addTermStatements(model, object.asResource(), neighbourhood);
            }
        }

        return neighbourhood;
    }

    private static void addTermStatements(Model model, Resource res, List<Statement> neighbourhood) {
        if (model.contains(res, WOTTR.modifier, (Resource) null)) {
            neighbourhood.addAll(
                model.listStatements(res, WOTTR.modifier, (Resource) null).toList());
        }
        if (model.contains(res, WOTTR.type, (Resource) null)) {
            neighbourhood.addAll(
                model.listStatements(res, WOTTR.type, (Resource) null).toList());
        }
        if (model.contains(res, WOTTR.defaultVal, (Resource) null)) {
            neighbourhood.addAll(
                model.listStatements(res, WOTTR.defaultVal, (Resource) null).toList());
        }
        if (model.contains(res, WOTTR.value, (Resource) null)) {
            neighbourhood.addAll(
                model.listStatements(res, WOTTR.value, (Resource) null).toList());
        }
        if (model.contains(res, WOTTR.variable, (Resource) null)) {
            neighbourhood.addAll(
                model.listStatements(res, WOTTR.variable, (Resource) null).toList());
        }
    }
}
