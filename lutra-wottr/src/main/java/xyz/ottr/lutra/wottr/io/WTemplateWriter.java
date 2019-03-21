package xyz.ottr.lutra.wottr.io;

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
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import xyz.ottr.lutra.io.TemplateWriter;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.TemplateSignature;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.ModelIO;
import xyz.ottr.lutra.wottr.util.ModelIOException;

public class WTemplateWriter extends AbstractWWriter implements TemplateWriter {

    private Map<String, Model> models; // TODO: Decide on representation
    private WInstanceWriter instanceWriter;

    public WTemplateWriter() {
        this.models = new HashMap<String, Model>();
        this.instanceWriter = new WInstanceWriter();
    }

    @Override
    public Set<String> getIRIs() {
        return this.models.keySet();
    }

    @Override
    public void accept(TemplateSignature template) {
        Model model = ModelFactory.createDefaultModel();
        addPrefixes(model);
        
        Resource tempNode = makeWottrHead(model, template);
        if (template instanceof Template) {
            for (Instance ins : ((Template) template).getBody()) {
                Resource insNode = instanceWriter.makeWottrInstance(model, ins);
                model.add(model.createStatement(tempNode, WOTTR.pattern, insNode));
            }
        }
        models.put(template.getIRI(), model);
    }
    
    @Override
    public void printDefinitions() {
        for (Model model : models.values()) {
            System.out.println("==========\nModel\n==========");
            model.write(System.out, "TTL");
        }
    }

    @Override
    public String write(String iri) {
        String out = "";
        try {
            out = ModelIO.writeModel(this.models.get(iri), ModelIO.Format.TURTLE);
        } catch (ModelIOException e) {
            e.printStackTrace();
        }
        return out;
    }

    private Resource makeWottrHead(Model model, TemplateSignature template) {

        Resource templateIRI = model.createResource(template.getIRI());
        if (template instanceof Template) {
            model.add(model.createStatement(templateIRI, RDF.type, WOTTR.Template));
        } else if (template.isBaseTemplate()) {
            model.add(model.createStatement(templateIRI, RDF.type, WOTTR.BaseTemplate));
        } else {
            model.add(model.createStatement(templateIRI, RDF.type, WOTTR.TemplateSignature));
        }

        ParameterList parameters = template.getParameters();
        addParameters(parameters, templateIRI, model);
        return templateIRI;
    }
}
