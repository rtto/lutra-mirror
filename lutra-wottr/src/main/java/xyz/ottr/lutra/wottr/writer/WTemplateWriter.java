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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.writer.TemplateWriter;

public class WTemplateWriter implements TemplateWriter {

    private final PrefixMapping prefixes;
    
    private MessageHandler msgs;
    private BiFunction<String, String, Optional<Message>> stringConsumer;

    public WTemplateWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WTemplateWriter(PrefixMapping prefixes) {
        this.prefixes = prefixes;
        this.msgs = new MessageHandler();
    }

    @Override
    public void accept(Signature template) {
        var iri = template.getIri();
                
        String content = buildStringRep(template);
        stringConsumer.apply(iri, content).ifPresent(msgs::add); //write template to file or console
    }
       
    public Model getModel(Signature signature) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(this.prefixes);

        Resource signatureNode = createSignature(model, signature);

        if (signature instanceof Template) {
            addInstances(((Template) signature).getPattern(), signatureNode, WOTTR.pattern, model);
        }
        PrefixMappings.trim(model);
        
        return model;        
    }
    
    
    public String buildStringRep(Signature template) {
        return RDFIO.writeToString(getModel(template));
    }
    

    private Resource createSignature(Model model, Signature signature) {

        Resource templateType;
        if (signature instanceof Template) {
            templateType = WOTTR.Template;
        } else if (signature instanceof BaseTemplate) {
            templateType = WOTTR.BaseTemplate;
        } else {
            templateType = WOTTR.Signature;
        }

        Resource signatureNode = model.createResource(signature.getIri());
        model.add(signatureNode, RDF.type, templateType);
        addParameters(signature.getParameters(), signatureNode, model);
        addInstances(signature.getAnnotations(), signatureNode, WOTTR.annotation, model);
        return signatureNode;
    }

    private void addParameters(List<Parameter> parameters, Resource iri, Model model) {

        RDFList paramLst = model.createList();

        for (Parameter param : parameters) {
            paramLst = paramLst.with(addParameter(param, model));
        }
        model.add(iri, WOTTR.parameters, paramLst);
    }

    private Resource addParameter(Parameter param, Model model) {

        Resource paramNode = model.createResource();

        RDFNode variable = WTermWriter.term(model, param.getTerm());
        Resource type = WTypeWriter.type(model, param.getTerm().getType());

        model.add(paramNode, WOTTR.variable, variable);
        model.add(paramNode, WOTTR.type, type);

        if (param.getName() != null) {
            model.add(paramNode, WOTTR.name, param.getName());
        }
        if (param.isOptional()) {
            model.add(paramNode, WOTTR.modifier, WOTTR.optional);
        }
        if (param.isNonBlank()) {
            model.add(paramNode, WOTTR.modifier, WOTTR.nonBlank);
        }
        if (param.hasDefaultValue()) {
            RDFNode def = WTermWriter.term(model, param.getDefaultValue());
            model.add(paramNode, WOTTR.defaultVal, def);
        }
        return paramNode;
    }

    private void addInstances(Collection<Instance> instances, Resource signature, Property property, Model model) {
        for (Instance instance : instances) {
            Resource instanceNode = WriterUtils.createInstanceNode(model, instance);
            model.add(signature, property, instanceNode);
        }
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
