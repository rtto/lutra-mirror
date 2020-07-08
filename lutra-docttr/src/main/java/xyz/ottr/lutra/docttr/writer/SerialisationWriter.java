package xyz.ottr.lutra.docttr.writer;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.RDFTurtle;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.stottr.writer.SInstanceWriter;
import xyz.ottr.lutra.stottr.writer.STemplateWriter;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.wottr.writer.WInstanceWriter;
import xyz.ottr.lutra.wottr.writer.WTemplateWriter;

class SerialisationWriter {

    private final PrefixMapping prefixMapping;

    SerialisationWriter(PrefixMapping prefixMapping) {
        this.prefixMapping = prefixMapping;
    }

    String writeStottr(Signature signature) {
        var writer = new STemplateWriter(this.prefixMapping);
        writer.accept(signature);
        return writer.writeSignature(signature, false);
    }

    String writeStottr(Instance instance) {
        var writer = new SInstanceWriter(this.prefixMapping);
        writer.accept(instance);
        return writer.writeInstance(instance);
    }

    String writeWottr(Signature signature) {
        var writer = new WTemplateWriter(this.prefixMapping);
        writer.accept(signature);
        return removePrefixes(writer.write(signature.getIri()));
    }

    Model writeWottrModel(Instance instance) {
        var writer = new WInstanceWriter(this.prefixMapping);
        writer.accept(instance);
        return writer.writeToModel();
    }

    String writeRDF(Model graph) {
        return removePrefixes(RDFIO.writeToString(PrefixMappings.trim(graph)));
    }

    private String removePrefixes(String turtleRDFModel) {
        return Arrays.asList(turtleRDFModel.split(Space.LINEBR)).stream()
            .filter(s -> !s.startsWith(RDFTurtle.prefixInit))
            .collect(Collectors.joining("\n"));
    }

}
