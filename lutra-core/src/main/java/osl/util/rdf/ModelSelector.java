package osl.util.rdf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.Strings;

public abstract class ModelSelector {

	private static Logger log = LoggerFactory.getLogger(ModelSelector.class);

	@SuppressWarnings("unchecked")
	public static Set<RDFNode> getRDFNodes (Model model) {
		return (Set<RDFNode>) getRDFNodes(model.listStatements().toList());
	}

	public static Set<? extends RDFNode> getRDFNodes (Collection<Statement> statements) {
		Set<RDFNode> nodes = new HashSet<>();
		for (Statement t : statements) {
			nodes.add(t.getSubject());
			nodes.add(t.getPredicate());
			nodes.add(t.getObject());
		}
		return nodes;
	}

	@SuppressWarnings("unchecked")
	public static Set<Resource> getURIResources (Model model) {
		Set<? extends RDFNode> nodes = getRDFNodes(model);
		nodes.removeIf(node -> !node.isURIResource());
		return (Set<Resource>) nodes;
	}

	public static Set<String> getNamespaces (Model model) {
		Set<String> namespaces = new HashSet<>();
		for (Resource uri : ModelSelector.getURIResources(model)) {
			namespaces.add(uri.getNameSpace());
		}
		return namespaces;
	}

	/////////////////////////////////////
	// wrapper methods

	public static List<Resource> listResourcesWithProperty (Model model, Property property) {
		return model.listResourcesWithProperty(property).toList();	
	}

	////////////////////////////////////
	// class instances

	public static List<Resource> listInstancesOfClass (Model model, Resource cls) {
		List<Resource> instances = model.listResourcesWithProperty(RDF.type, cls).toList();
		log.trace("Found " + instances.size() + " instance(s) of class " + qname(model, cls) + ": " + instances.toString());
		return instances;
	}

	public static Resource getRequiredInstanceOfClass(Model model, Resource cls) throws ModelSelectorException {
		List<Resource> individuals = listInstancesOfClass(model, cls);
		int size = individuals.size();
		if (size != 1) {
			throw new ModelSelectorException("Error getting instance of class " + qname(model, cls) + ". Expected exactly 1 instance, but found " + individuals.size() + ": " + qname(model, individuals));
		}
		return individuals.get(0);
	}

	////////////////////////////////////
	// lists of subject-property objects

	public static  List<RDFNode> listObjectsOfProperty (Model model, Resource subject, Property property) {
		List<RDFNode> objects = subject.getModel().listObjectsOfProperty(subject, property).toList();
		log.trace("Found " + objects.size() + " resource object(s) of subject " + qname(model, subject) + " and property " + qname(model, property)  + ": " + qname(model, objects));
		return objects;
	}
	public static  List<Resource> listResourcesOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		List<Resource> resources = new ArrayList<>();
		for (RDFNode object : listObjectsOfProperty(model, subject, property)) {
			if (!object.isResource()) {
				throw new ModelSelectorException("Error getting resource objects of subject " 
						+ qname(model, subject) + " and property " 
						+ qname(model, property) 
						+ ". Excepted resource node, but found: " + qname(model, object));
			}
			resources.add(object.asResource());
		}
		return resources;
	}

	////////////////////////////////////
	// singletons of subject-property objects

	// required-s

	public static RDFNode getRequiredObjectOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		List<RDFNode> objects = listObjectsOfProperty (model, subject, property);
		int size = objects.size();
		if (size != 1) {
			throw new ModelSelectorException("Error getting required property value '" + qname(model, property)
			+ "' for subject '" + qname(model, subject) + "'. Expected exactly 1 instance, but found " 
			+ objects.size() + ": " + qname(model, objects));
		}
		return objects.get(0);
	}
	public static Resource getRequiredResourceOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		RDFNode object = getRequiredObjectOfProperty (model, subject, property);
		if (!object.isResource()) {
			throw new ModelSelectorException("Error getting required property literal value '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'. Expected resource, but found " + qname(model, object));
		}
		return object.asResource();
	}
	public static Literal getRequiredLiteralOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		RDFNode object = getRequiredObjectOfProperty (model, subject, property);
		if (!object.isLiteral()) {
			throw new ModelSelectorException("Error getting required property literal value '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'. Expected literal, but found " + qname(model, object));
		}
		return object.asLiteral();
	}

	// special

	public static Statement getRequiredStatementWithProperties (Model model, Resource subject, Collection<Property> properties) throws ModelSelectorException {
		Set<Statement> statements = new HashSet<>();
		for (Property p : properties) {
			statements.addAll(model.listStatements(subject, p, (RDFNode)null).toSet());
		}
		if (statements.size() != 1) {
			throw new ModelSelectorException("Error getting exactly one required value for properties: '" ,
					qname(model, properties), "' and subject '", qname(model, subject) ,
					"'. Expected resource, but found ", 
					Strings.toString(statements, s -> qname(model, s.getObject()), ", "));
		}
		return statements.iterator().next();
	}

	// optionals

	public static Statement getOptionalStatementWithProperties (Model model, Resource subject, Collection<Property> properties) throws ModelSelectorException {
		Set<Statement> statements = new HashSet<>();
		for (Property p : properties) {
			statements.addAll(model.listStatements(subject, p, (RDFNode)null).toSet());
		}
		if (statements.size() > 1) {
			throw new ModelSelectorException("Error getting optionally one value for properties: '" ,
					qname(model, properties), "' and subject '", qname(model, subject) ,
					"'. Expected resource, but found ", 
					Strings.toString(statements, s -> qname(model, s.getObject()), ", "));
		}
		return (!statements.isEmpty()) ? statements.iterator().next() : null;
	}


	public static RDFNode getOptionalObjectOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		List<RDFNode> objects = listObjectsOfProperty (model, subject, property);
		int size = objects.size();
		if (size > 1) {
			throw new ModelSelectorException("Error getting optional property value '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'. Expected optionally 1 instance, but found " + objects.size() + ": " + qname(model, objects));
		}
		log.trace("Found " + objects.size() + " property value(s) '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'");
		return objects.isEmpty() ? null : objects.get(0);
	}
	public static Resource getOptionalResourceOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		RDFNode object = getOptionalObjectOfProperty (model, subject, property);
		if (object != null && !object.isResource()) {
			throw new ModelSelectorException("Error getting optional property resource value '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'. Expected resource, but found " + qname(model, object));
		}
		log.trace("Found " + object == null ? "0" : "1" + " property resource value(s) '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'");
		return object == null ? null : object.asResource();
	}
	public static Literal getOptionalLiteralOfProperty (Model model, Resource subject, Property property) throws ModelSelectorException {
		RDFNode object = getOptionalObjectOfProperty (model, subject, property);
		if (object != null && !object.isLiteral()) {
			throw new ModelSelectorException("Error getting optional property literal value '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'. Expected literal, but found " + object.toString());
		}
		log.trace("Found " + object == null ? "0" : "1" + " property literal value(s) '" + qname(model, property) + "' for subject '" + qname(model, subject) + "'");
		return object == null ? null : object.asLiteral();
	}


	//////////////////////////////////////////////
	// privates

	private static String qname (Model model, RDFNode node) {
		return ModelIO.shortForm(model, node);
	}
	private static String qname (Model model, Collection<? extends RDFNode> nodes) {
		String list = Strings.toString(nodes, node -> ModelIO.shortForm(model, node), ", ");
		return "[" + list + "]";
	}
	private static String qname (Model model, List<? extends RDFNode> nodes) {
		return ModelIO.shortForm(model, nodes);
	}
}
