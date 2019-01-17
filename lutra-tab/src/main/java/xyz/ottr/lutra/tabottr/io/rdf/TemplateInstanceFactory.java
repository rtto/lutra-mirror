package xyz.ottr.lutra.tabottr.io.rdf;

/*-
 * #%L
 * lutra-tab
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

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.WTermFactory;

public class TemplateInstanceFactory {
    
    private RDFNodeFactory dataFactory;
    private Resource templateIRI;
    private List<String> types;
    private WTermFactory termFactory;
    
    public TemplateInstanceFactory(PrefixMapping prefixes, String templateIRI, List<String> types) {
        this.dataFactory = new RDFNodeFactory(prefixes);
        this.templateIRI = dataFactory.toResource(templateIRI);
        this.types = types;
        this.termFactory = new WTermFactory();
    }
    
    public Result<Instance> createTemplateInstance(List<String> arguments) {
        List<Result<Term>> members = new LinkedList<>();
        for (int i = 0; i < arguments.size(); i += 1) {
            Result<RDFNode> rdfNode = dataFactory.toRDFNode(arguments.get(i), types.get(i));
            members.add(rdfNode.flatMap(termFactory));
        }
        Result<List<Term>> rsArguments = Result.aggregate(members);
        return rsArguments.map(args ->
                new Instance(this.templateIRI.toString(), new ArgumentList(args)));
    }
}
