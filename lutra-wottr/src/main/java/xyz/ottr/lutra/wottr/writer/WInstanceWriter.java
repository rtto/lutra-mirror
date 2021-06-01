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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.writer.BufferWriter;
import xyz.ottr.lutra.writer.InstanceWriter;

public class WInstanceWriter extends BufferWriter implements InstanceWriter {

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
            WriterUtils.createInstanceNode(this.model, instance);
        }
    }
    
    @Override
    public MessageHandler flush() {
        MessageHandler msg = new MessageHandler();
        msg.combine(super.write(RDFIO.writeToString(writeToModel()))); //write model to file
        msg.combine(super.flush());
        return msg;
    }

    public Model writeToModel() {
        PrefixMappings.trim(this.model);
        return this.model;
    }
}
