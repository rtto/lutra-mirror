package xyz.lutra.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import osl.util.CartesianProduct;
import osl.util.rdf.Models;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.stOTTRWriter;
import xyz.lutra.parser.TemplateLoader;

public class TemplateInstance extends xyz.lutra.model.RDFResource {

	private Resource templateRef;
	private List<Argument> arguments;
	private Collection<Substitution> substs;
	
	private Resource status; // TODO: convert to enum

	private Model proposition, body, head;

	public TemplateInstance (Resource iri, Resource templateRef, Resource status, List<Argument> arguments) {
		super(iri);
		this.templateRef = templateRef;
		this.arguments = arguments;
		this.status = status;
		if (! this.isIncomplete()) {
			buildSubstitutions ();
		}
	}

	private void buildSubstitutions () {
		int[] lengths = new int[arguments.size()];
		
		// build lengths for cartesian product calculation
		for (int i = 0; i < lengths.length; i += 1) {
			lengths[i] = arguments.get(i).getValueCount();
		}
		
		this.substs = new ArrayList<>();
		for (int[] indices : new CartesianProduct(lengths)) {
			List<Argument> substArgs = new ArrayList<>();
			for (int i = 0; i < indices.length; i += 1) {
				Argument arg = new Argument(ResourceFactory.createResource(), i+1, arguments.get(i).getValue(indices[i]));
				substArgs.add(arg);
			}
			try {
				Substitution subst = new Substitution(getTemplate().getParameters(), substArgs);
				this.substs.add(subst);
			}
			catch (IllegalSubstitutionException ex) {
				throw new IllegalSubstitutionException ("The substitution induced by template instance " +
						iri + " of template " + templateRef + " is illegal. " + ex.getMessage());
			}
		}
	}
	
	public boolean isIncomplete () {
		return Templates.incomplete.equals(this.status);
	}

	public Resource getTemplateRef () {
		return templateRef;
	}
	public Template getTemplate () {
		return TemplateLoader.getTemplate(templateRef.getURI());
	}
	public List<Argument> getArguments () {
		return arguments;
	}

	public Model getProposition (Model model) {
		if (proposition == null) {
			proposition = Models.empty();
			proposition.add(this.getNeighbourhood(model));
			for (Argument arg : arguments) {
				proposition.add(arg.getNeighbourhood(model));
			}
		}
		return proposition;
	}

    private boolean shouldBeExpanded() {
        for (int i = 0; i < arguments.size(); i++) {
            if (arguments.get(i).isNullValued() && !getTemplate().getParameters().get(i).isOptional()) {
                return false; // Contains null-valued non-optional value, should not be expanded
            }
        }
        return true;
    }   

	public Model getBodyInstance () {
		if (body == null) {
			body = Models.empty();
            if (shouldBeExpanded()) {
                for (Substitution subst : substs) {
                    Model temp = Models.duplicate(getTemplate().getBody(), Models.BlankCopy.FRESH);
                    subst.apply(temp);
                    body.add(temp);
                }
            }
		}
		return body;
	}

	public Model getHeadInstance () {
		if (head == null) {
			head = Models.empty();
			for (Substitution subst : substs) {
				Model temp = Models.duplicate(getTemplate().getHead(), Models.BlankCopy.FRESH);
				subst.apply(temp);
				head.add(temp);
			}
		}
		return head;
	}
	
	public String toString() {
		stOTTRWriter w = new stOTTRWriter();
		return w.print(this);
	}



	/*
	// TODO use this to get owl:imports statement.
	private Resource getTemplateInstanceURI () {
		StringBuffer instanceURI = new StringBuffer();
		instanceURI.append(getTemplateURI().getURI()).append("?");
		for (Argument arg : arguments) {
			instanceURI.append(arg.getName().getLocalName()).append("=").append(arg.getValue());
		}
		return ResourceFactory.createResource(instanceURI.toString());
	}
	 */
}
