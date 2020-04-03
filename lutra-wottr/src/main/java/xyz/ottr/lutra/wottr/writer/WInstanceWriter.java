package xyz.ottr.lutra.wottr.writer;

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

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.io.Models;
import xyz.ottr.lutra.writer.InstanceWriter;

public class WInstanceWriter implements InstanceWriter {

    private final Model model;

    public WInstanceWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WInstanceWriter(PrefixMapping prefixes) {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(prefixes); // Will trim unused before write
    }

    @Override
    public synchronized void accept(Instance instance) { // Cannot write in parallel, Jena breaks
        if (WTripleWriter.isTriple(instance)) {
            this.model.add(WTripleWriter.write(this.model, instance));
        } else {
            createInstanceNode(this.model, instance);
        }
    }

    @Override
    public String write() {
        return Models.writeModel(writeToModel());
    }

    public Model writeToModel() {
        Models.trimPrefixes(this.model);
        return this.model;
    }

    Resource createInstanceNode(Model model, Instance instance) {

        Resource instanceNode = model.createResource();
        Resource templateIRI = model.createResource(instance.getIri());
        model.add(instanceNode, WOTTR.of, templateIRI);

        if (instance.hasListExpander()) {
            model.add(instanceNode, WOTTR.modifier, WOTTR.listExpanders.getKey(instance.getListExpander()));
            addArgumentsList(instance.getArguments(), instanceNode, model);
        } else {
            addValuesList(instance.getArguments(), instanceNode, model);
        }

        return instanceNode;
    }

    private void addValuesList(List<Argument> arguments, Resource instanceNode, Model model) {

        RDFList argsLst = model.createList();

        for (Argument arg : arguments) {
            RDFNode val = WTermWriter.term(model, arg.getTerm());
            argsLst = argsLst.with(val);
        }
        model.add(instanceNode, WOTTR.values, argsLst);

    }

    private void addArgumentsList(List<Argument> arguments, Resource instanceNode, Model model) {

        RDFList argsLst = model.createList();

        for (Argument arg : arguments) {
            RDFNode val = WTermWriter.term(model, arg.getTerm());

            Resource argNode = model.createResource();
            model.add(argNode, WOTTR.value, val);

            if (arg.isListExpander()) {
                model.add(argNode, WOTTR.modifier, WOTTR.listExpand);
            }
            argsLst = argsLst.with(argNode);
        }
        model.add(instanceNode, WOTTR.arguments, argsLst);
    }
}
