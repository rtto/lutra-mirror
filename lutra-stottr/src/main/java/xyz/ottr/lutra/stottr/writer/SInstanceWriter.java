package xyz.ottr.lutra.stottr.writer;

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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import xyz.ottr.lutra.io.InstanceWriter;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.stottr.STOTTR;

public class SInstanceWriter implements InstanceWriter {

    private static final Map<ArgumentList.Expander, String> expanders;

    static {
        expanders = new HashMap<>();
        expanders.put(ArgumentList.Expander.CROSS, STOTTR.Expanders.cross);
        expanders.put(ArgumentList.Expander.ZIPMIN, STOTTR.Expanders.zipMin);
        expanders.put(ArgumentList.Expander.ZIPMAX, STOTTR.Expanders.zipMax);
    }

    private final List<Instance> instances;
    private final STermWriter termWriter;
    private final boolean inDefinition;
       
    private SInstanceWriter(STermWriter termWriter, boolean inDefinition) {
        this.instances = new LinkedList<>();
        this.termWriter = termWriter;
        this.inDefinition = inDefinition;
    }

    public static SInstanceWriter makeOuterInstanceWriter(Map<String, String> prefixes) {
        return new SInstanceWriter(new STermWriter(prefixes), false);
    }

    public static SInstanceWriter makeBodyInstanceWriter(STermWriter termWriter) {
        return new SInstanceWriter(termWriter, true);
    }

    @Override
    public void accept(Instance instance) {
        this.instances.add(instance);
    }

    @Override
    public String write() {

        StringBuilder builder = new StringBuilder();
        boolean firstInstance = true;

        for (Instance instance : this.instances) {

            if (this.inDefinition) {

                if (!firstInstance) {
                    builder
                        .append(STOTTR.Statements.bodyInsSep)
                        .append("\n");
                }
                builder
                    .append(STOTTR.Statements.indent)
                    .append(writeInstance(instance));
            } else {

                if (firstInstance) {
                    builder.append(SPrefixWriter.write(this.termWriter.getPrefixes()));
                }
                builder
                    .append(writeInstance(instance))
                    .append(STOTTR.Statements.statementEnd)
                    .append("\n");

            }
            firstInstance = false;
        }

        return builder.toString();
    }

    private StringBuilder writeInstance(Instance instance) {

        StringBuilder builder = new StringBuilder();

        ArgumentList args = instance.getArguments();
        if (args.hasListExpander()) {
            builder
                .append(expanders.get(args.getListExpander()))
                .append(" ")
                .append(STOTTR.Expanders.expanderSep)
                .append(" ");
        }

        builder.append(this.termWriter.writeIRI(instance.getIRI()));
        builder.append(STOTTR.Terms.insArgStart)
            .append(writeArguments(args))
            .append(STOTTR.Terms.insArgEnd);

        return builder;
    }

    private StringBuilder writeArguments(ArgumentList args) {

        StringBuilder builder = new StringBuilder();
        String sep = "";

        for (Term arg : args.asList()) {
            builder.append(sep);
            if (args.getExpanderValues().contains(arg)) {
                builder.append(STOTTR.Expanders.expander);
            }
            builder.append(this.termWriter.write(arg));
            sep = STOTTR.Terms.insArgSep + " ";
        }
        return builder;
    }
}
