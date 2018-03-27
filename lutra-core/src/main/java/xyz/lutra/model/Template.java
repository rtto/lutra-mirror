package xyz.lutra.model;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import osl.util.rdf.Models;
import osl.util.rdf.RDFLists;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.stOTTRWriter;

public class Template extends xyz.lutra.model.RDFResource {

	private Model source, head, body;
	private List<Parameter> parameters;
	
	public Template (Model model, Resource iri, List<Parameter> parameters) {
		super(iri);
		this.source = model;
		this.parameters = parameters;
	}
	
	public Model getSourceModel () {
		return source;
	}

	public Model getHead () {
		// compute head on demand
		if (head == null) {
			head = Models.empty(source);
			head.add(super.getNeighbourhood());
			for (Parameter p : parameters) {
				head.add(p.getNeighbourhood());
			}
		}
		return head;
	}

	public Model getBody () {
		if (body == null) {
			body = Models.duplicate(source, Models.BlankCopy.KEEP); // need to KEEP blank nodes in order to remove head
			body.remove(getHead());
		}
		return body;
	}
	
	/**
	 * Gets an independent model of *one* typical instance of the template.
	 * @return
	 */
	public Model getInstance () {
		Model model = Models.empty(source);
		
		Resource instance = ResourceFactory.createResource();
		model.add(instance, Templates.templateRef, this.getIRI());
		
		int index = 1;
		for (Parameter p : parameters) { 
			Resource arg = ResourceFactory.createResource();
			model.add(instance, Templates.hasArgument, arg);
			model.addLiteral(arg, Templates.index, index++);
			
			// TODO support correct subproperties of value
			// TODO: Maybe add new method for generating example instance with missing optional values
			if (!p.isOptional()) {
				RDFNode value = p.getValue();
				model.add(arg, Templates.value, value);
				if (value.canAs(RDFList.class)) {
					model.add(RDFLists.getAllListStatements(value.as(RDFList.class)));
				}
			}
		}
		return model;
	}
	
	public List<Parameter> getParameters () {
		return parameters;
	}

	public Substitution getVariableSubstitution () {
		List<VariableArgument> args = new ArrayList<>();
		for (Parameter p : parameters) {
			Resource res = ResourceFactory.createResource();
			Node var = NodeFactory.createVariable("param" + p.getIndex());
			args.add(new VariableArgument(res, p.getIndex(), var));
		}
		return new Substitution(getParameters(), args);
	}
	
	public String toString() {
		stOTTRWriter w = new stOTTRWriter();
		return w.print(this);
	}

}
