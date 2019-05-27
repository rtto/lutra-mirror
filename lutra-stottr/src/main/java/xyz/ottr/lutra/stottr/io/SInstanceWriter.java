package xyz.ottr.lutra.stottr.io;

/*-
 * #%L
 * lutra-stottr
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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.stottr.STOTTR;

public class SInstanceWriter implements InstanceWriter {

    private static Map<ArgumentList.Expander, String> expanders;

    static {
        expanders = new HashMap<>();
        expanders.put(ArgumentList.Expander.CROSS, STOTTR.Expanders.cross);
        expanders.put(ArgumentList.Expander.ZIPMIN, STOTTR.Expanders.zipMin);
        expanders.put(ArgumentList.Expander.ZIPMAX, STOTTR.Expanders.zipMax);
    }

    private final Writer writer;
    private final STermWriter termWriter;
    private final boolean inDefinition;
       
    private SInstanceWriter(Writer writer, Map<String, String> prefixes, Set<Term> variables, boolean inDefinition) {
        this.writer = writer;
        this.termWriter = variables == null ? new STermWriter(prefixes) : new STermWriter(prefixes, variables);
        this.inDefinition = inDefinition;
        writePrefixes(prefixes);
    }

    public SInstanceWriter(Map<String, String> prefixes) {
        this(new StringWriter(), prefixes, null, false);
    }

    public SInstanceWriter(Writer writer, Map<String, String> prefixes) {
        this(writer, prefixes, null, false);
    }

    public SInstanceWriter(Writer writer, Map<String, String> prefixes, Set<Term> variables) {
        this(writer, prefixes, variables, true);
    }

    @Override
    public void accept(Instance instance) {
        try {
            this.writer.write(write(instance) + "\n");
        } catch (IOException ex) {
            System.err.println(ex.toString()); // TODO
        }
    }

    public void writePrefixes(Map<String, String> prefixes) {

        try {
            for (Map.Entry<String, String> nsln : prefixes.entrySet()) {
                this.writer.write("@prefix " + nsln.getKey() + ": " + nsln.getValue() + " .\n");
            }
            this.writer.write("\n");
        } catch (IOException ex) {
            System.err.println(ex.toString()); // TODO
        }
    }

    @Override
    public String write() {
        return this.writer.toString();
    }

    public String write(Instance instance) {

        StringBuilder out = new StringBuilder();
        if (this.inDefinition) {
            out.append(STOTTR.indent);
        }

        ArgumentList args = instance.getArguments();
        if (args.hasListExpander()) {
            String expander = expanders.get(args.getListExpander());
            out.append(expander + " " + STOTTR.Expanders.expanderSep + " ");
        }
        out.append(this.termWriter.writeIRI(instance.getIRI()));
        out.append("(");
        out.append(writeArguments(args));
        out.append(") .");

        return out.toString();
    }

    public String writeArguments(ArgumentList args) {

        StringBuilder out = new StringBuilder();
        String sep = "";

        for (Term arg : args.asList()) {
            out.append(sep);
            if (args.getExpanderValues().contains(arg)) {
                out.append(STOTTR.Expanders.expander);
            }
            out.append(this.termWriter.write(arg));
            sep = ", ";
        }
        return out.toString();
    }
}
