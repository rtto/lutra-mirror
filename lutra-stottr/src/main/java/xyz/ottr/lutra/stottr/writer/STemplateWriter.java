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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.writer.TemplateWriter;

public class STemplateWriter implements TemplateWriter {

    private final Map<String, Signature> templates;
    private final PrefixMapping prefixes;

    public STemplateWriter() {
        this(OTTR.getDefaultPrefixes());
    }

    public STemplateWriter(PrefixMapping prefixes) {
        this.templates = new HashMap<>();
        this.prefixes = prefixes;
    }

    @Override
    public Set<String> getIRIs() {
        return this.templates.keySet();
    }

    @Override
    public void accept(Signature template) {
        this.templates.put(template.getIri(), template);
    }

    public String write(String iri) {

        StringBuilder builder = new StringBuilder();
        Signature template = this.templates.get(iri);

        if  (template == null) {
            return null;
        }

        var parameterVariables = template.getParameters().stream()
            .map(Parameter::getTerm)
            .collect(Collectors.toSet());

        STermWriter termWriter = new STermWriter(this.prefixes, parameterVariables);

        builder.append(writeSignature(template, termWriter));

        if (template instanceof BaseTemplate) {
            builder.append(" " + STOTTR.Statements.signatureSep + " " + STOTTR.Statements.baseBody);
        } else if (template instanceof Template) {
            builder.append(" " + STOTTR.Statements.signatureSep + " " + STOTTR.Statements.bodyStart + "\n");
            builder.append(writePattern((Template) template, termWriter));
            builder.append("\n" + STOTTR.Statements.bodyEnd);
        }

        builder.append(STOTTR.Statements.statementEnd);

        // Write used prefixes at start of String
        builder.insert(0, writeUsedPrefixes(termWriter.getUsedPrefixes()) + "\n\n");

        return builder.toString();
    }

    private String writeUsedPrefixes(Set<String> usedPrefixes) {

        PrefixMapping usedPrefixMap = PrefixMapping.Factory.create();
        for (Map.Entry<String, String> nsln : this.prefixes.getNsPrefixMap().entrySet()) {
            if (usedPrefixes.contains(nsln.getKey())) {
                usedPrefixMap.setNsPrefix(nsln.getKey(), nsln.getValue());
            }
        }
        return SPrefixWriter.write(usedPrefixMap);
    }

    private StringBuilder writeSignature(Signature template, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();

        builder.append(termWriter.writeIRI(template.getIri()));
        builder.append(STOTTR.Parameters.sigParamsStart);

        List<Parameter> params = template.getParameters();
        String sep = "";
        
        for (Parameter param : params) {

            builder.append(sep);
            builder.append(writeModes(param.isNonBlank(), param.isOptional()));
            builder.append(writeType(param.getTerm().getType(), termWriter));
            builder.append(" ");
            builder.append(termWriter.write(param.getTerm()));

            if (param.hasDefaultValue()) {
                builder.append(STOTTR.Parameters.defaultValSep).append(termWriter.write(param.getDefaultValue()));
            }

            sep = STOTTR.Parameters.paramSep + " ";
        }
        builder.append(STOTTR.Parameters.sigParamsEnd);
        return builder;
    }

    private StringBuilder writeModes(boolean isNonBlank, boolean isOptional) {

        StringBuilder builder = new StringBuilder();

        boolean written = false;
        if (isNonBlank) {
            builder.append(STOTTR.Parameters.nonBlank);
            written = true;
        }

        if (isOptional) {
            builder.append(STOTTR.Parameters.optional);
            written = true;
        }

        if (written) {
            builder.append(" ");
        }
        return builder;
    }

    private StringBuilder writeType(Type type, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();

        if (type instanceof BasicType) {
            builder.append(termWriter.writeIRI(((BasicType) type).getIri()));
        } else {

            String typeStr;
            Type innerType;

            if (type instanceof LUBType) {
                typeStr = STOTTR.Types.lub;
                innerType = ((LUBType) type).getInner();
            } else if (type instanceof ListType) {
                typeStr = STOTTR.Types.list;
                innerType = ((ListType) type).getInner();
            } else { // instanceof NEListType
                typeStr = STOTTR.Types.neList;
                innerType = ((NEListType) type).getInner();
            } 

            builder.append(typeStr).append(STOTTR.Types.innerTypeStart);
            builder.append(writeType(innerType, termWriter));
            builder.append(STOTTR.Types.innerTypeEnd);
        }
        return builder;
    }

    private String writePattern(Template template, STermWriter termWriter) {
        SInstanceWriter instanceWriter = new SPatternInstanceWriter(termWriter);
        template.getPattern().forEach(instanceWriter);
        return instanceWriter.write();
    }

    public void printDefinitions() {
        //TODO
    }
}
