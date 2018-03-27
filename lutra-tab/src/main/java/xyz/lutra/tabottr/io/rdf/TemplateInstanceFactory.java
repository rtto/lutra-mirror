package xyz.lutra.tabottr.io.rdf;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import osl.util.rdf.vocab.Templates;

public class TemplateInstanceFactory {
	
	private Model model;
	private RDFNodeFactory dataFactory;
	private Resource templateIRI;
	private List<String> types;
	
	public TemplateInstanceFactory (Model model, String templateIRI, List<String> types) {
		this.model = model;
		this.dataFactory = new RDFNodeFactory(model);
		this.templateIRI = dataFactory.toResource(templateIRI);
		this.types = types;
	}
	
	public void addTemplateInstance (List<String> arguments) {
		// fresh blank node for template instance:
		Resource instance = ResourceFactory.createResource();
		model.add(instance, Templates.templateRef, templateIRI);
		
		// add arguments to template instance as ottr:withValues-list:
		RDFNode[] members = new RDFNode[arguments.size()];
		for (int i = 0; i < arguments.size(); i += 1) {
			members[i] = dataFactory.toRDFNode(arguments.get(i), types.get(i));
		}
		RDFList list = model.createList(members);
		model.add(instance, Templates.withValues, list);	
	}
}
