package xyz.ottr.lutra.wottr.parser.v03;

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
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.io.ReaderException;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.Models;
import xyz.ottr.lutra.wottr.vocabulary.v03.WOTTR;

@SuppressWarnings("CPD-START")
public class WParserUtils {


    /**
     * Constructs a canonical representation of the argument model by expanding
     * Turtle lists (a b c ...) into its full RDF-representation using rdf:first and
     * rdf:rest.
     */

    public static Model getCanonicalModel(Model model) {
        Model canonical = Models.duplicate(model, Models.BlankCopy.KEEP);

        // replacing t:with... list properties with indexed lists:
        for (Map.Entry<Property, List<Property>> m : WOTTR.listPropertiesMap.entrySet()) {
            for (Statement t : canonical.listStatements(null, m.getKey(), (RDFNode) null).toList()) {
                Resource s = t.getSubject();
                RDFNode o = t.getObject();
                if (!o.canAs(RDFList.class) || o.canAs(RDFList.class) && o.as(RDFList.class).isEmpty()) {
                    throw new ReaderException("Error parsing value of " + m.getKey().getLocalName()
                            + ". Expecting a non-empty rdf:List, but found: " + o.toString());
                }
                // TODO check this hard-coding of indices
                RDFList objList = o.as(RDFList.class);
                List<RDFNode> nodes = objList.asJavaList();
                for (int i = 0; i < nodes.size(); i += 1) {
                    // TODO see release/0.1 for ottr:none support
                    Resource param = canonical.createResource();
                    canonical.add(s, m.getValue().get(0), param);
                    canonical.add(param, m.getValue().get(1), nodes.get(i));
                    canonical.addLiteral(param, WOTTR.index, i + 1);
                }
                objList.removeList();
                t.remove();
            }
        }
        canonical.setNsPrefixes(model);
        return canonical;
    }

    public static List<Resource> getInstances(Model model) {
        return ModelSelector.listResourcesWithProperty(model, WOTTR.templateRef);
    }

    public static boolean isTemplateDefinition(Model model) {
        return model.contains(null, RDF.type, WOTTR.Template);
    }

    public static Model getTemplateHeadWParam(Model model, Resource template, List<Resource> parameters) {

        Model head = Models.empty();
        head.add(getNeighbourhood(model, template));
        parameters.forEach(r -> head.add(getNeighbourhood(model, r)));
        head.setNsPrefixes(model);
        return head;
    }

    public static Model getTemplateHeadWVars(Model model, Resource template) {

        Model head = Models.empty();
        head.add(getNeighbourhood(model, template));
        head.setNsPrefixes(model);
        return head;
    }

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
                if (s.getPredicate().equals(WOTTR.hasArgument)) {
                    RDFNode o = s.getObject();
                    modelCopy.remove(getNeighbourhood(model, o.asResource()));
                }
            }
        }
        modelCopy.setNsPrefixes(model);
        return modelCopy;
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
        }
        return neighbourhood;
    }
}
