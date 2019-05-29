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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.LUBType;
import xyz.ottr.lutra.model.types.ListType;
import xyz.ottr.lutra.model.types.NEListType;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.stottr.STOTTR;

public class STemplateWriter implements TemplateWriter {

    private Map<String, TemplateSignature> templates;
    private Map<String, String> prefixes;

    public STemplateWriter(Map<String, String> prefixes) {
        this.templates = new HashMap<>();
        this.prefixes = prefixes;
    }

    @Override
    public Set<String> getIRIs() {
        return this.templates.keySet();
    }

    @Override
    public void accept(TemplateSignature template) {
        this.templates.put(template.getIRI(), template);
    }

    public String write(String iri) {

        StringBuilder writer = new StringBuilder();
        TemplateSignature template = this.templates.get(iri);

        if  (template == null) {
            return null;
        }

        Set<Term> variables = new HashSet<>(template.getParameters().asList());
        STermWriter termWriter = new STermWriter(this.prefixes, variables);

        writeSignature(template, writer, termWriter);

        if (template.isBaseTemplate()) {
            writer.append(" " + STOTTR.Statements.signatureSep + " " + STOTTR.Statements.baseBody);
        } else if (template instanceof Template) {
            writer.append(" " + STOTTR.Statements.signatureSep + " " + STOTTR.Statements.bodyStart + "\n");
            writeBody((Template) template, writer, termWriter);
            writer.append("\n" + STOTTR.Statements.bodyEnd);
        }

        writer.append(STOTTR.Statements.statementEnd);

        // Write used prefixes at start of String
        writer.insert(0, writeUsedPrefixes(termWriter.getUsedPrefixes()));

        return writer.toString();
    }

    private String writeUsedPrefixes(Set<String> usedPrefixes) {

        Map<String, String> usedPrefixMap = new HashMap<>();
        for (Map.Entry<String, String> nsln : this.prefixes.entrySet()) {
            if (usedPrefixes.contains(nsln.getKey())) {
                usedPrefixMap.put(nsln.getKey(), nsln.getValue());
            }
        }

        StringWriter strWriter = new StringWriter();
        SPrefixWriter.write(usedPrefixMap, strWriter);
        return strWriter.toString();
    }

    private void writeSignature(TemplateSignature template, StringBuilder writer, STermWriter termWriter) {

        writer.append(termWriter.writeIRI(template.getIRI()));
        writer.append(STOTTR.Parameters.sigParamsStart);

        ParameterList params = template.getParameters();
        String sep = "";
        
        for (Term param : params.asList()) {

            writer.append(sep);
            writeModes(params.isNonBlank(param), params.isOptional(param), writer);
            writeType(param.getType(), writer, termWriter);
            writer.append(" ");
            writer.append(termWriter.write(param));

            if (params.hasDefaultValue(param)) {
                writer.append(STOTTR.Parameters.defaultValSep).append(termWriter.write(params.getDefaultValue(param)));
            }

            sep = STOTTR.Parameters.paramSep + " ";
        }
        writer.append(STOTTR.Parameters.sigParamsEnd);
    }

    private void writeModes(boolean isNonBlank, boolean isOptional, StringBuilder writer) {

        boolean written = false;
        if (isNonBlank) {
            writer.append(STOTTR.Parameters.nonBlank);
            written = true;
        }

        if (isOptional) {
            writer.append(STOTTR.Parameters.optional);
            written = true;
        }

        if (written) {
            writer.append(" ");
        }
    }

    private void writeType(TermType type, StringBuilder writer, STermWriter termWriter) {

        if (type instanceof BasicType) {
            writer.append(termWriter.writeIRI(((BasicType) type).getIRI()));
        } else {

            String typeStr;
            TermType innerType;

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

            writer.append(typeStr).append(STOTTR.Types.innerTypeStart);
            writeType(innerType, writer, termWriter);
            writer.append(STOTTR.Types.innerTypeEnd);
        }
    }

    private void writeBody(Template template, StringBuilder writer, STermWriter termWriter) {

        StringWriter strWriter = new StringWriter();
        SInstanceWriter instanceWriter = SInstanceWriter.makeBodyInstanceWriter(strWriter, termWriter);
        template.getBody().forEach(instanceWriter);
        writer.append(strWriter.toString());
    }

    public void printDefinitions() {
        //TODO
    }
}
