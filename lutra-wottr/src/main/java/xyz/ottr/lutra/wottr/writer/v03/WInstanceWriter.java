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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
//import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.wottr.parser.v03.WOTTR;
import xyz.ottr.lutra.wottr.util.ModelIO;

public class WInstanceWriter extends AbstractWriter implements InstanceWriter {

    private Model model;

    public WInstanceWriter() {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(PrefixMapping.Standard);
        this.model.setNsPrefix("ottr", WOTTR.namespace);
    }

    @Override
    public synchronized void accept(Instance i) { // Cannot write in parallel, Jena breaks
        if (isTriple(i)) {
            this.model.add(getTriple(model, i));
        } else {
            this.model.add(makeWottrInstance(i));
        }
    }

    @Override
    public String write() {
        return ModelIO.writeModel(this.model);
    }

    public Model writeToModel() {
        return this.model;
    }

    public Model makeWottrTripleOrInstance(Instance i) {
        if (isTriple(i)) {
            Model m = ModelFactory.createDefaultModel();
            m.add(getTriple(m, i));
            return m;
        } else {
            return makeWottrInstance(i);
        }
    }

    public Model makeWottrInstance(Instance i) {
        Model m = ModelFactory.createDefaultModel();
        Resource templateIRI = m.createResource(i.getIRI());
        Resource instance = m.createResource();
        //m.add(m.createStatement(instance, RDF.type, WOTTR.Instance));
        m.add(m.createStatement(instance, WOTTR.templateRef, templateIRI));

        ArgumentList arguments = i.getArguments();
        addArguments(arguments, instance, m);
        return m;
    }
}
