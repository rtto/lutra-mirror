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

import java.util.Objects;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;
import xyz.ottr.lutra.wottr.writer.RDFFactory;
import xyz.ottr.lutra.writer.InstanceWriter;

public class WInstanceWriter implements InstanceWriter {

    private final Model model;
    private final RDFFactory rdfFactory;

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
    public synchronized void accept(Instance instance) { // Cannot write in parallel, Jena breaks
        if (RDFFactory.isTriple(instance)) {
            this.model.add(this.rdfFactory.createTriple(this.model, instance));
        } else {
            createInstanceNode(this.model, instance);
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

    public Resource createInstanceNode(Model model, Instance instance) {
        Resource templateIRI = model.createResource(instance.getIri());
        Resource instanceNode = model.createResource();
        model.add(instanceNode, WOTTR.of, templateIRI);

        ArgumentList arguments = instance.getArguments();
        addArguments(arguments, instanceNode, model);
        if (arguments.hasListExpander()) {
            model.add(instanceNode, WOTTR.modifier, WOTTR.listExpanders.getKey(arguments.getListExpander()));
        }
        return instanceNode;
    }

    private void addArguments(ArgumentList arguments, Resource iri, Model model) {

        Objects.requireNonNull(arguments, "Cannot add arguments with no argument list.");

        RDFList argsLst = model.createList();

        for (Term arg : arguments.asList()) {
            RDFNode val = this.rdfFactory.createRDFNode(model, arg);

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
