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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.Space;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.types.BasicType;
import xyz.ottr.lutra.model.types.ComplexType;
import xyz.ottr.lutra.model.types.Type;
import xyz.ottr.lutra.stottr.STOTTR;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.writer.TemplateWriter;

public class STemplateWriter implements TemplateWriter {

    /*
    private static final Comparator<Signature> signatureComparator =
        Comparator.comparing(sign -> sign.getClass().getSimpleName() + sign.getIri(), String::compareToIgnoreCase);
    */
    
    //private final Map<String, Signature> templates;
    private final PrefixMapping prefixes;
    
    private MessageHandler msgs;
    private BiFunction<String, String, Optional<Message>> stringConsumer;

    public STemplateWriter(PrefixMapping prefixes) {
        //this.templates = new HashMap<>();
        this.prefixes = prefixes;
        this.msgs = new MessageHandler();
        this.stringConsumer = null;
    }

    /*
    @Override
    public Set<String> getIRIs() {
        return this.templates.keySet();
    }
    */    

    @Override
    public void accept(Signature template) {
        //this.templates.put(template.getIri(), template);
        
        String iri = template.getIri();
        if (iri != null) {
            String content = writeSignature(template, true);
            this.stringConsumer.apply(iri, content).ifPresent(this.msgs::add); //write template to file or console
        }
    }
    
    /*
    public String write() {
        return SPrefixWriter.write(this.prefixes)
            + Space.LINEBR2
            + this.templates.values().stream()
                .sorted(signatureComparator)
                .map(signature -> writeSignature(signature, false))
                .collect(Collectors.joining(Space.LINEBR2));
    }
    */

    /*
    public String write(String iri) {

        Signature template = this.templates.get(iri);
        return template == null
            ? null
            : writeSignature(template, true);
    }
    */

    public String writeSignature(Signature signature, boolean includePrefixes) {

        var parameterVariables = signature.getParameters().stream()
            .map(Parameter::getTerm)
            .collect(Collectors.toSet());

        STermWriter termWriter = new STermWriter(this.prefixes, parameterVariables);

        StringBuilder builder = new StringBuilder();

        if (signature instanceof BaseTemplate) {
            builder.append(writeBaseTemplate((BaseTemplate)signature, termWriter));
        } else if (signature instanceof Template) {
            builder.append(writeTemplate((Template)signature, termWriter));
        } else {
            builder.append(writeSignature(signature, termWriter));
        }

        builder.append(STOTTR.Statements.statementEnd);

        // Write prefixes at start of String
        if (includePrefixes) {
            builder.insert(0, SPrefixWriter.write(termWriter.getUsedPrefixes()) + Space.LINEBR2);
        }

        return builder.toString();
    }


    protected StringBuilder writeSignature(Signature signature, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();
        builder.append(termWriter.writeIRI(signature.getIri()));
        builder.append(this.writeParameters(signature, termWriter));
        builder.append(this.writeAnnotations(signature, termWriter));
        return builder;
    }

    private StringBuilder writeBaseTemplate(BaseTemplate baseTemplate, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();
        builder.append(writeSignature(baseTemplate, termWriter));
        builder.append(STOTTR.Statements.signatureSep);
        builder.append(STOTTR.Statements.baseBody);
        return builder;
    }

    private StringBuilder writeTemplate(Template template, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();
        builder.append(writeSignature(template, termWriter));
        builder.append(STOTTR.Statements.signatureSep);
        builder.append(STOTTR.Statements.bodyStart);
        builder.append(writePattern(template, termWriter));
        builder.append(Space.LINEBR);
        builder.append(STOTTR.Statements.bodyEnd);
        return builder;
    }

    protected StringBuilder writeParameters(Signature signature, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();
        builder.append(signature.getParameters().stream()
            .map(parameter -> writeParameter(parameter, termWriter))
            .map(p -> Space.LINEBR + Space.INDENT + p)
            .collect(Collectors.joining(
                STOTTR.Parameters.paramSep,
                STOTTR.Parameters.sigParamsStart,
                Space.LINEBR + STOTTR.Parameters.sigParamsEnd)));
        return builder;
    }

    private StringBuilder writeParameter(Parameter parameter, STermWriter termWriter) {

        StringBuilder builder = new StringBuilder();

        builder.append(writeModes(parameter.isNonBlank(), parameter.isOptional()));
        builder.append(writeType(parameter.getTerm().getType(), termWriter));
        builder.append(Space.SPACE);
        builder.append(termWriter.write(parameter.getTerm()));

        if (parameter.hasDefaultValue()) {
            builder.append(STOTTR.Parameters.defaultValSep).append(termWriter.write(parameter.getDefaultValue()));
        }

        return builder;
    }

    private StringBuilder writeModes(boolean isNonBlank, boolean isOptional) {

        StringBuilder builder = new StringBuilder();

        if (isNonBlank) {
            builder.append(STOTTR.Parameters.nonBlank);
        }

        if (isOptional) {
            builder.append(STOTTR.Parameters.optional);
        }

        if (builder.length() != 0) {
            builder.append(Space.SPACE);
        }
        return builder;
    }

    private StringBuilder writeType(Type type, STermWriter termWriter) {
        return type instanceof BasicType
            ? new StringBuilder(termWriter.writeIRI(((BasicType) type).getIri()))
            : writeComplexType((ComplexType)type, termWriter);
    }

    private StringBuilder writeComplexType(ComplexType type, STermWriter termWriter) {

        String typeStr = STOTTR.Types.map.get(type.getClass());
        Type innerType = type.getInner();

        StringBuilder builder = new StringBuilder();

        builder.append(typeStr).append(STOTTR.Types.innerTypeStart);
        builder.append(writeType(innerType, termWriter));
        builder.append(STOTTR.Types.innerTypeEnd);
        return builder;
    }

    private String writePattern(Template template, STermWriter termWriter) {

        SInstanceWriter instanceWriter = new SInstanceWriter(termWriter);
        var pattern = template.getPattern();

        if (pattern.isEmpty()) {
            return Space.LINEBR + Space.INDENT + STOTTR.Statements.commentStart + "Empty pattern";
        } else {
            return pattern.stream()
                .sorted(instanceWriter.instanceSorter)
                .map(instanceWriter::writeInstance)
                .map(i -> Space.LINEBR + Space.INDENT + i)
                .collect(Collectors.joining(STOTTR.Statements.bodyInsSep));
        }
    }

    protected String writeAnnotations(Signature signature, STermWriter termWriter) {

        SInstanceWriter instanceWriter = new SAnnotationInstanceWriter(termWriter);

        var annotations = signature.getAnnotations().stream()
                .sorted(instanceWriter.instanceSorter)
                .map(instanceWriter::writeInstance)
                .map(i -> Space.LINEBR + STOTTR.Statements.annotationStart +  i)
                .collect(Collectors.joining(STOTTR.Statements.bodyInsSep));

        if (!annotations.isEmpty()) {
            annotations += Space.LINEBR;
        }

        return annotations;
    }
    
    /**
     * Set writer function which will write to file
     * 
     * @param stringConsumer
     *      A function to which the written string are applied
     */
    @Override
    public void setWriterFunction(BiFunction<String, String, Optional<Message>> stringConsumer) {
        this.stringConsumer = stringConsumer;
    }
    
    /**
     * @return MessageHandler
     */
    @Override
    public MessageHandler getMessages() {
        return this.msgs;
    }
}
