package xyz.lutra.parser;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.owl.Ontologies;
import osl.util.rdf.ModelIOException;
import osl.util.rdf.ModelSelector;
import osl.util.rdf.Models;
import osl.util.rdf.RDFLists;
import osl.util.rdf.RDFNodes;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.Settings;

public abstract class ParserUtils {

	private static Logger log = LoggerFactory.getLogger(ParserUtils.class);

	// TODO: ontology is loaded from net, should it be a local copy? Put in 
	//public static final Model TemplateModel = ModelIO.readModel(Templates.namespace);
	private static OWLOntology TemplateOntology;

	public static boolean isValidTemplate (Model syntax, Model semantics) {
		return isValidTemplateSyntax(syntax) && isValidTemplateSemantics(semantics);
	}	

	public static boolean isValidTemplateSyntax (Model model) {
		boolean isOK = true;
		if (Settings.enableSyntaxTemplateVocabularyCheck) {
			isOK = isTemplateVocabularySyntaxValid(model);
		}
		return isOK;

	}

	public static boolean isValidTemplateSemantics (Model model) {
		boolean isOK = true;
		if (Settings.enableSemanticTemplateVocabularyCheck) {
			isOK = isTemplateVocabularySemanticsValid(model);
		}
		return isOK;
	}

	public static boolean maybeOntology (Model model) {
		return model.contains(null, RDF.type, OWL.Ontology);
	}

	/**
	 * Lightweight method what does not parse a template, but checks if 
	 * the model contains any instances of Template. Useful for avoiding
	 * full parsing.
	 * @param model
	 * @return
	 */
	public static boolean maybeTemplate (Model model) {
		return model.contains(null, RDF.type, Templates.Template);
	}
	public static boolean maybeTemplate (String uri) {
		return maybeTemplate(TemplateLoader.getTemplateModel(uri));
	}

	private static Predicate<RDFNode> isInTemplateVocabNS = r -> r.isURIResource() 
			&& r.asResource().getNameSpace().equals(Templates.namespace);

	protected static boolean isTemplateVocabularySyntaxValid (Model model) throws ParserException {
		log.info("Checking syntactic template vocabulary validity");
		for (RDFNode node : ModelSelector.getRDFNodes(model)) {
			if (isInTemplateVocabNS.test(node) && 
					!Templates.ALL.contains(node.asResource())) {
				throw new ParserException("Error parsing templates vocabulary: " + model.shortForm(node.toString()) 
				+ " is not a recognised vocabulary URI.");
			}
		}
		return true;
	}


	protected static boolean isTemplateVocabularySemanticsValid (Model model) throws ParserException {
		log.info("Checking semantic template vocabulary validity");

		OWLOntology ontology = getTemplateVocabOntology(model);

		OWLReasoner reasoner = Ontologies.getReasoner(ontology);

		Set<OWLClass> unsat = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		boolean isConsistent = reasoner.isConsistent();

		boolean isValid = unsat.isEmpty() && isConsistent; 
		if (!isValid) {
			String errorMessage = "Error parsing templates vocabulary. "
					+ "The use of the templates vocabulary is not semantically valid. ";		
			if (!unsat.isEmpty()) {
				errorMessage += "The following classes are unsatisfiable: " + unsat.toString();
				// TODO show explanation?
			}
			if (!isConsistent) {
				errorMessage += "The ontology is inconsistent.";
				// TODO show explanation?
			}

			throw new ParserException(errorMessage);
		}
		return isValid;
	}


	public static Model getCanonicalModel (Model model) throws ParserException {
		Model canonical = Models.duplicate(model, Models.BlankCopy.KEEP);

		/*
		// replacing template instance vocabulary with template vocabulary:
		for (Entry<Property, Property> m : TemplateInstances.templateVocabMap.entrySet()) {
			ModelEditor.substituteNode(canonical, m.getKey(), m.getValue());
		}
		 */

		// replacing t:with... list properties with indexed lists:
		for (Entry<Property, List<Property>> m : Templates.listPropertiesMap.entrySet())
			for (Statement listTriple : canonical.listStatements(null, m.getKey(), (RDFNode)null).toList()) {
				Resource subject = listTriple.getSubject();
				RDFNode listObject = listTriple.getObject(); 
				if(!listObject.canAs(RDFList.class) || (listObject.canAs(RDFList.class) && listObject.as(RDFList.class).isEmpty())) {
					throw new ParserException("Error parsing value of " + m.getKey().getLocalName() 
							+ ". Expecting a non-empty rdf:List, but found: " + listObject.toString());
				}
				// TODO check this hard-coding of indices
				RDFList list = listObject.as(RDFList.class);
				List<RDFNode> nodes = list.asJavaList();
				for (int i = 0; i < nodes.size(); i += 1) {
					Resource param = canonical.createResource();
					canonical.add(subject, m.getValue().get(0), param); // insert hasParameter/hasArgument
					canonical.addLiteral(param, Templates.index, i+1);
					
					RDFNode val = nodes.get(i);
					if (Templates.none.equals(val)) {
						if (Templates.hasParameter.equals(m.getValue().get(0))) {
							throw new ParserException("Error parsing value of " + m.getKey().getLocalName()
									+ ". " + Templates.none.getLocalName() + " is not permissible in this context.");
						}
						canonical.addLiteral(param, Templates.optional, true); // flag optional
					} else {
						canonical.add(param, m.getValue().get(1), val); // insert variable/value
					}
				}
				list.removeList();
				listTriple.remove();
			}
		// TODO replace OWL list with RDF list

		return canonical;
	}

	public static OWLOntology getTemplateVocabOntology(Model model) throws ParserException {
		if (TemplateOntology == null) {
			try {
				TemplateOntology = Ontologies.readOntology(Templates.namespace);
			} catch (OWLOntologyCreationException e) {
				throw new ParserException ("Error loading the Templates ontology. " + e.getMessage());
			}
		}

		OWLOntology ontology;
		try {
			ontology = Ontologies.toOntology(getTemplateVocabModel(model));
		} catch (OWLOntologyCreationException | ModelIOException e) {
			throw new ParserException ("Error converting and loading template model as OWL ontology. " + e.getMessage());
		}
		// include template ontology:
		ontology.getOWLOntologyManager().addAxioms(ontology, TemplateOntology.getAxioms());

		return ontology;
	}

	private static Model getTemplateVocabModel (Model model) {
		Model vocabModel = Models.empty(model);
		for (StmtIterator it = model.listStatements(); it.hasNext(); ) {
			Statement t = it.next();
			if (isInTemplateVocabNS.test(t.getPredicate())) {
				vocabModel.add(t);
				// if object is a list, include the complete list, in a recursive manner.
				RDFNode object = t.getObject();
				if (object.canAs(RDFList.class)) {
					vocabModel.add(RDFLists.getAllListStatements(object.as(RDFList.class)));
				} 
			} else if (isInTemplateVocabNS.test(t.getObject()) || isInTemplateVocabNS.test(t.getSubject())) {
				vocabModel.add(t);
			}
		}
		return vocabModel;
	}

	public static void checkParameterType (Property type, RDFNode value) throws IllegalArgumentException {

		boolean isVar = value.asNode().isVariable();

		String expectingType = null;
		String propertyType = null;

		if (type.equals(Templates.variable)) {
			// anything goes
		}
		else if (type.equals(Templates.nonLiteralVariable)) {
			if (value.isLiteral()) {
				expectingType = "non-literal";
				propertyType = "non-literal";
			}
		}
		else if (type.equals(Templates.literalVariable)) {
			if (!value.isLiteral() && !isVar) {
				expectingType = "literal";
				propertyType = "literal";
			}
		} else if (type.equals(Templates.propertyVariable) 
				|| type.equals(Templates.objectPropertyVariable)
				|| type.equals(Templates.dataPropertyVariable)
				|| type.equals(Templates.annotationPropertyVariable)) {
			if (value.isLiteral() && !isVar) {
				expectingType = "non-literal";
				propertyType = "property";
			}
		} 
		else if (type.equals(Templates.classVariable)) {
			if(value.isLiteral() || value.canAs(RDFList.class)) {
				expectingType = "non literal";
				propertyType = "class";
			}
		}
		else if (type.equals(Templates.datatypeVariable)) {
			if(!value.isURIResource() && !isVar) {
				expectingType = "concrete IRI";
				propertyType = "datatype";
			}
		} 
		else if (type.equals(Templates.individualVariable)){ 
			if(value.isLiteral() || value.canAs(RDFList.class)) {
				expectingType = "non literal";
				propertyType = "individual";
			}
		} else if (type.equals(Templates.listVariable)) {
			if (!value.canAs(RDFList.class) || value.as(RDFList.class).size() == 0) { 
				expectingType = "non-empty list";
				propertyType = "list";
			}
		}
		else {
			String msg = "Type check error, unreckognised type: " + type.toString();
			log.error(msg);
			throw new ParserException(msg); 
		}


		if (expectingType != null) {
			String msg = "Type error, expecting " + expectingType + 
					" for " + propertyType +
					" variable, but found " +
					RDFNodes.getType(value) + ": " +
					value.getModel().shortForm(value.toString());
			log.error(msg);
			throw new ParserException(msg);
		}
	}

	public static void isNonListRDFResource (Resource resource) {
		if (RDFNodes.isOfType(resource, RDFNodes.Type.RDFList)) {
			throw new ParserException("Type error; expecting URI resource or single blank node, but found RDF list resource: " + 
					resource.getModel().shortForm(resource.toString()));
		}
	}

}