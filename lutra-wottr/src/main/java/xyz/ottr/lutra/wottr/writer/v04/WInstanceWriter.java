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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.wottr.parser.v04.WOTTR;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;

public class WInstanceWriter extends AbstractWWriter implements InstanceWriter {

    private static Map<ArgumentList.Expander, Resource> expanders;

    static {
        expanders = new HashMap<>();
        expanders.put(ArgumentList.Expander.CROSS, WOTTR.cross);
        expanders.put(ArgumentList.Expander.ZIPMIN, WOTTR.zipMin);
        expanders.put(ArgumentList.Expander.ZIPMAX, WOTTR.zipMax);
    }

    private Model model;

    public WInstanceWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WInstanceWriter(PrefixMapping prefixes) {
        this.model = ModelFactory.createDefaultModel();
        this.model.setNsPrefixes(prefixes); // Will trim unused before write
    }

    @Override
    public synchronized void accept(Instance i) { // Cannot write in parallel, Jena breaks
        if (isTriple(i)) {
            this.model.add(getTriple(model, i));
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
        //m.add(m.createStatement(instance, RDF.type, WOTTR.Instance));
        model.add(model.createStatement(instance, WOTTR.of, templateIRI));

        ArgumentList arguments = i.getArguments();
        addArguments(arguments, instance, model);
        if (arguments.hasListExpander()) {
            model.add(model.createStatement(
                    instance, WOTTR.modifier, expanders.get(arguments.getListExpander())));
        }
        return instance;
    }
}
