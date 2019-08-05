package xyz.ottr.lutra.wottr.writer.v03;

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

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.vocabulary.v03.WOTTR;
import xyz.ottr.lutra.wottr.writer.RDFFactory;

public class WInstanceWriter implements InstanceWriter {

    private Model model;
    private RDFFactory rdfFactory;

    public WInstanceWriter() {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(PrefixMapping.Standard);
        this.model.setNsPrefix(WOTTR.prefix, WOTTR.namespace);
        this.rdfFactory = new RDFFactory(WOTTR.theInstance);
    }

    @Override
    public synchronized void accept(Instance i) { // Cannot write in parallel, Jena breaks
        if (this.rdfFactory.isTriple(i)) {
            this.model.add(this.rdfFactory.createTriple(this.model, i));
        } else {
            addInstance(i, this.model);
        }
    }

    @Override
    public String write() {
        return ModelIO.writeModel(this.model);
    }

    public Model writeToModel() {
        return this.model;
    }

    private void addInstance(Instance instance, Model model) {

        Resource templateNode = model.createResource(instance.getIRI());
        Resource instanceNode = model.createResource();

        model.add(instanceNode, WOTTR.templateRef, templateNode);

        ArgumentList arguments = instance.getArguments();
        addArguments(arguments, instanceNode, model);
    }

    private void addArguments(ArgumentList arguments, Resource iri, Model model) {

        if (arguments == null) {
            return; // TODO: Perhaps throw exception(?)
        }

        int index = 1; // Start index count on 1

        for (Term arg : arguments.asList()) {
            Resource argumentNode = model.createResource();
            RDFNode valueNode = rdfFactory.createRDFNode(model, arg);

            model.add(iri, WOTTR.hasArgument, argumentNode);
            if (arguments.hasListExpander(arg) && arguments.hasCrossExpander()) {
                model.add(argumentNode, WOTTR.eachValue, valueNode);
            } else {
                model.add(argumentNode, WOTTR.value, valueNode);
            }
            model.add(argumentNode, WOTTR.index, model.createTypedLiteral(index, XSDDatatype.XSDint));
            index++;
        }
    }
}
