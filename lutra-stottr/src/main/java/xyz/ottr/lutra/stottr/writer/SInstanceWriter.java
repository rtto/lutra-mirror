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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.writer.InstanceWriter;

public class SInstanceWriter implements InstanceWriter {

    protected static final Comparator<Instance> instanceSorter = Comparator.comparing(Instance::getIri)
        .thenComparing(i -> Objects.toString(i.getListExpander(), ""), String::compareToIgnoreCase)
        .thenComparing(i -> i.getArguments().toString(), String::compareToIgnoreCase);

    protected final List<Instance> instances;
    private final STermWriter termWriter;

    SInstanceWriter(STermWriter termWriter) {
        this.instances = new LinkedList<>();
        this.termWriter = termWriter;
    }

    public SInstanceWriter(PrefixMapping prefixes) {
        this(new STermWriter(prefixes));
    }

    @Override
    public void accept(Instance instance) {
        this.instances.add(instance);
    }

    @Override
    public String write() {

        StringBuilder builder = new StringBuilder();

        builder
            .append(SPrefixWriter.write(this.termWriter.getPrefixes()))
            .append(STOTTR.Space.br2);

        this.instances.stream()
            .sorted(instanceSorter)
            .forEach(instance ->
                builder
                    .append(writeInstance(instance))
                    .append(STOTTR.Statements.statementEnd)
                    .append(STOTTR.Space.br));

        return builder.toString();
    }

    StringBuilder writeInstance(Instance instance) {

        StringBuilder builder = new StringBuilder();

        if (instance.hasListExpander()) {
            builder
                .append(STOTTR.Expanders.map.get(instance.getListExpander()))
                .append(STOTTR.Expanders.expanderSep);
        }

        builder.append(this.termWriter.writeIRI(instance.getIri()));
        builder.append(STOTTR.Terms.insArgStart);
        builder.append(instance.getArguments().stream()
            .map(this::writeArgument)
            .collect(Collectors.joining(STOTTR.Terms.insArgSep)));
        builder.append(STOTTR.Terms.insArgEnd);

        return builder;
    }

    private StringBuilder writeArgument(Argument arg) {

        StringBuilder builder = new StringBuilder();

        if (arg.isListExpander()) {
            builder.append(STOTTR.Expanders.expander);
        }
        builder.append(this.termWriter.write(arg.getTerm()));

        return builder;
    }
}
