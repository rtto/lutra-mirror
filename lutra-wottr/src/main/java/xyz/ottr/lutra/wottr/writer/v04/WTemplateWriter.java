package xyz.ottr.lutra.wottr.writer.v04;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.io.FormatName;
import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.util.PrefixMappings;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;
import xyz.ottr.lutra.wottr.writer.RDFFactory;
import xyz.ottr.lutra.wottr.writer.TypeFactory;

public class WTemplateWriter implements TemplateWriter {

    private final Map<String, Model> models; // TODO: Decide on representation
    private final WInstanceWriter instanceWriter;
    private final PrefixMapping prefixes;
    private final RDFFactory rdfFactory;

    public WTemplateWriter() {
        this(PrefixMapping.Factory.create());
    }

    public WTemplateWriter(PrefixMapping prefixes) {
        this(prefixes, new RDFFactory(WOTTR.theInstance));
    }

    public WTemplateWriter(PrefixMapping prefixes, RDFFactory rdfFactory) {
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
    public void accept(TemplateSignature template) {
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(this.prefixes);
        
        Resource signatureNode = createSignature(model, template);
        if (template instanceof Template) {
            for (Instance instance : ((Template) template).getBody()) {
                Resource instanceNode = this.instanceWriter.createInstanceNode(model, instance);
                model.add(signatureNode, WOTTR.pattern, instanceNode);
            }
        }

        PrefixMappings.trim(model);
        this.models.put(template.getIRI(), model);
    }
    
    @Override
    public String write(String iri) {
        return ModelIO.writeModel(this.models.get(iri));
    }

    private Resource createSignature(Model model, TemplateSignature template) {

        Resource templateType;
        if (template instanceof Template) {
            templateType = WOTTR.Template;
        } else if (template.isBaseTemplate()) {
            templateType = WOTTR.BaseTemplate;
        } else {
            templateType = WOTTR.TemplateSignature;
        }

        Resource templateIRI = model.createResource(template.getIRI());
        model.add(templateIRI, RDF.type, templateType);
        ParameterList parameters = template.getParameters();
        addParameters(parameters, templateIRI, model);
        return templateIRI;
    }

    private void addParameters(ParameterList parameters, Resource iri, Model model) {

        Objects.requireNonNull(parameters, "Cannot add parameters with no parameter list.");

        RDFList paramLst = model.createList();

        for (Term param : parameters.asList()) {
            RDFNode variable = this.rdfFactory.createRDFNode(model, param);
            Resource type = TypeFactory.createRDFType(model, param.getType());

            Resource paramNode = model.createResource();
            model.add(paramNode, WOTTR.variable, variable);
            model.add(paramNode, WOTTR.type, type);

            if (parameters.isOptional(param)) {
                model.add(paramNode, WOTTR.modifier, WOTTR.optional);
            }
            if (parameters.isNonBlank(param)) {
                model.add(paramNode, WOTTR.modifier, WOTTR.nonBlank);
            }
            if (parameters.hasDefaultValue(param)) {
                RDFNode def = this.rdfFactory.createRDFNode(model, parameters.getDefaultValue(param));
                model.add(paramNode, WOTTR.defaultVal, def);
            }
            paramLst = paramLst.with(paramNode);
        }
        model.add(iri, WOTTR.parameters, paramLst);
    }

    @Override
    public FormatName getFormat() {
        return FormatName.wottr;
    }
}
