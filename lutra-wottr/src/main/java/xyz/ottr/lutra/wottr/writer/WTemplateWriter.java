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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.io.RDFIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.writer.TemplateWriter;

public class WTemplateWriter implements TemplateWriter {

    private final Map<String, Model> models; // TODO: Decide on representation
    private final WInstanceWriter instanceWriter;
    private final PrefixMapping prefixes;
    private final RDFFactory rdfFactory;

    public WTemplateWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WTemplateWriter(PrefixMapping prefixes) {
        this(prefixes, new RDFFactory());
    }

    WTemplateWriter(PrefixMapping prefixes, RDFFactory rdfFactory) {
        this.models = new HashMap<>();
        this.rdfFactory = rdfFactory;
        this.instanceWriter = new WInstanceWriter(prefixes, rdfFactory);
        this.prefixes = prefixes;
    }

    @Override
    public Set<String> getIRIs() {
        return this.models.keySet();
    }

    @Override
    public void accept(Signature template) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(this.prefixes);
        
        Resource signatureNode = createSignature(model, template);
        if (template instanceof Template) {
            for (Instance instance : ((Template) template).getPattern()) {
                Resource instanceNode = this.instanceWriter.createInstanceNode(model, instance);
                model.add(signatureNode, WOTTR.pattern, instanceNode);
            }
        }

        PrefixMappings.trim(model);
        this.models.put(template.getIri(), model);
    }
    
    @Override
    public String write(String iri) {
        return RDFIO.writeToString(this.models.get(iri));
    }

    private Resource createSignature(Model model, Signature template) {

        Resource templateType;
        if (template instanceof Template) {
            templateType = WOTTR.Template;
        } else if (template instanceof BaseTemplate) {
            templateType = WOTTR.BaseTemplate;
        } else {
            templateType = WOTTR.Signature;
        }

        Resource templateIRI = model.createResource(template.getIri());
        model.add(templateIRI, RDF.type, templateType);
        addParameters(template.getParameters(), templateIRI, model);
        return templateIRI;
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

        RDFNode variable = this.rdfFactory.createRDFNode(model, param.getTerm());
        Resource type = TypeFactory.createRDFType(model, param.getTerm().getType());

        model.add(paramNode, WOTTR.variable, variable);
        model.add(paramNode, WOTTR.type, type);

        if (param.isOptional()) {
            model.add(paramNode, WOTTR.modifier, WOTTR.optional);
        }
        if (param.isNonBlank()) {
            model.add(paramNode, WOTTR.modifier, WOTTR.nonBlank);
        }
        if (param.hasDefaultValue()) {
            RDFNode def = this.rdfFactory.createRDFNode(model, param.getDefaultValue());
            model.add(paramNode, WOTTR.defaultVal, def);
        }
        return paramNode;
    }


}
