package osl.util.rdf;

import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

public abstract class ModelEditor {

	//private static Logger log = LoggerFactory.getLogger(ModelEditor.class);
	
	public static void substituteNode (Graph graph, Node old, Node fresh) throws ModelEditorException {
		// subjects
		Set<Triple> subjects = graph.find(old, Node.ANY, Node.ANY).toSet();
		if (!subjects.isEmpty() && fresh.isLiteral()) {
			throw new ModelEditorException("Cannot put literal + '" + fresh + "' + in subject position.");
		}
		for (Triple t : subjects) {
			graph.delete(t);
			graph.add(new Triple(fresh, t.getPredicate(), t.getObject()));

		}
		// predicate
		Set<Triple> predicates = graph.find(Node.ANY, old, Node.ANY).toSet();
		if (!predicates.isEmpty() && (fresh.isBlank() || fresh.isLiteral())) {
			throw new ModelEditorException("Cannot put literal or blank node '"+ fresh +"' in predicate position.");
		}
		for (Triple t : predicates) {
			graph.delete(t);
			graph.add(new Triple(t.getSubject(), fresh, t.getObject()));
		}
		// object, anything goes
		for (Triple t : graph.find(Node.ANY, Node.ANY, old).toList()) {
			graph.delete(t);
			graph.add(new Triple(t.getSubject(), t.getPredicate(), fresh));
		}
	}

	public static void substituteNode (Model model, Node old, Node fresh) throws ModelEditorException {
		try {
			substituteNode(model.getGraph(), old, fresh);
		} catch (ModelEditorException e) {
			throw new ModelEditorException(
					"Error replacing " + old.toString() + " with " + fresh.toString() + ": " + e.getMessage());
		}
	}

	public static void substituteNode (Model model, RDFNode old, RDFNode fresh) throws ModelEditorException {
		substituteNode(model, old.asNode(), fresh.asNode());
	}
		
	public static void substituteBlanks (Model model) {
		for (RDFNode node : ModelSelector.getRDFNodes(model)) {
			if (node.isAnon()) {
				ModelEditor.substituteNode(model, node, ResourceFactory.createResource());
			}
		}
	}

	/*
	public static void removeResource (Model model, Resource resource) {
		model.removeAll(resource, null, (RDFNode) null);
		model.removeAll(null, ResourceFactory.createProperty(resource.getURI()), (RDFNode) null);
		model.removeAll(null, null, resource);
	}

	public static void removeResources (Model model, List<Resource> resources) {
		resources.forEach(r -> removeResource(model, r));
	}
	*/

}
