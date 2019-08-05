package xyz.ottr.lutra.wottr.writer.v04;

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

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;
import xyz.ottr.lutra.wottr.writer.RDFFactory;

public class WInstanceWriter implements InstanceWriter {

    private Model model;
    private RDFFactory rdfFactory;

    public WInstanceWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WInstanceWriter(PrefixMapping prefixes) {
        this(prefixes, new RDFFactory(WOTTR.theInstance));
    }

    public WInstanceWriter(PrefixMapping prefixes, RDFFactory rdfFactory) {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(prefixes); // Will trim unused before write
        this.rdfFactory = rdfFactory;
    }

    @Override
    public synchronized void accept(Instance i) { // Cannot write in parallel, Jena breaks
        if (RDFFactory.isTriple(i)) {
            this.model.add(rdfFactory.createTriple(this.model, i));
        } else {
            makeWottrInstance(this.model, i);
        }
    }

    @Override
    public String write() {
        PrefixMappings.trim(this.model);
        return ModelIO.writeModel(this.model);
    }

    public Model writeToModel() {
        PrefixMappings.trim(this.model);
        return this.model;
    }

    public Resource makeWottrInstance(Model model, Instance i) {
        Resource templateIRI = model.createResource(i.getIRI());
        Resource instance = model.createResource();
        model.add(instance, WOTTR.of, templateIRI);

        ArgumentList arguments = i.getArguments();
        addArguments(arguments, instance, model);
        if (arguments.hasListExpander()) {
            model.add(instance, WOTTR.modifier, WOTTR.listExpanders.getKey(arguments.getListExpander()));
        }
        return instance;
    }

    private void addArguments(ArgumentList arguments, Resource iri, Model model) {

        if (arguments == null) {
            return; // TODO: Perhaps throw exception(?)
        }

        RDFList argsLst = model.createList();

        for (Term arg : arguments.asList()) {
            RDFNode val = rdfFactory.createRDFNode(model, arg);

            Resource argNode = model.createResource();
            model.add(argNode, WOTTR.value, val);

            if (arguments.hasListExpander(arg)) {
                model.add(argNode, WOTTR.modifier, WOTTR.listExpand);
            }
            argsLst = argsLst.with(argNode);
        }
        model.add(iri, WOTTR.arguments, argsLst);
    }
}
