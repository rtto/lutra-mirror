package xyz.lutra;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.owl.Ontologies;
import osl.util.rdf.ModelIO;
import osl.util.rdf.Models;
import osl.util.rdf.PrefixMappings;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.model.Template;
import xyz.lutra.model.TemplateInstance;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.ParserUtils;
import xyz.lutra.parser.TemplateInstanceParser;
import xyz.lutra.parser.TemplateLoader;

public class Expander {

	private static Logger log = LoggerFactory.getLogger(Expander.class);

	private static Cache<Model> cache = new Cache<>(
			Settings.enableExpanderCache,
			iri -> Expander.expand(TemplateLoader.getTemplateModel(iri))
			);

	public static Model expandTemplate (String iri) {
		log.info("Expanding template: " + iri);
		return cache.get(iri);
	}

	public static Model expand (String file) {
		log.info("Expanding model in file: " + file);
		return expand(ModelIO.readModel(file));
	}

	public static Model expand (Model model) {
		if (Settings.enableSemanticTemplateVocabularyExpansionCheck) {
			Model fullExpand = expand(model, ExpanderSettings.ALL);
			boolean consistent = ParserUtils.isValidTemplateSemantics(fullExpand);
			if (!consistent) {
				throw new ParserException ("Expansion is inconsistent");
			}
		}
		return expand(model, ExpanderSettings.BODY);
	}

	public static Model expand (Model model, ExpanderSettings settings) {
		Model copy = Models.duplicate(model, Models.BlankCopy.KEEP); // Need to expand a copy
		Model expanded = expand(copy, settings, new LinkedList<Resource>(), settings.getDepth());
        if (!settings.isIncludeHead() && !settings.isIncludeCall()) { // Should not remove null-valued arguments/parameters from head/instance
            removeTriplesWithNull(expanded);
        }
        
		if (Settings.enableSemanticExpansionCheck) {
			log.info("Checking semantic consistency of expanded model: " + model.hashCode());

			OWLOntology ontModel = ParserUtils.getTemplateVocabOntology(expanded);
			OWLReasoner reasoner = Ontologies.getReasoner(ontModel);

			Set<OWLClass> unsat = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			boolean isConsistent = reasoner.isConsistent();

			boolean isValid = unsat.isEmpty() && isConsistent; 
			if (!isValid) {
				String errorMessage = "Error expanding model. "
						+ "The expanded ontology model is not semantically valid. ";		
				if (!unsat.isEmpty()) {
					errorMessage += "The following classes are unsatisfiable: " + unsat.toString();
					// TODO show explanation?
				}
				if (!isConsistent) {
					errorMessage += "The ontology is inconsistent.";
					// TODO show explanation?
				}
				throw new ExpanderException(errorMessage);
			}
		}
		PrefixMappings.trim(expanded);
		return expanded;
	}

	private static Model expand (Model model, ExpanderSettings settings, List<Resource> callerTemplates, int depth) {
		log.info("Expanding model: " + model.hashCode() + ", depth: " + depth);
		for (TemplateInstance instance : TemplateInstanceParser.getTemplateInstances(model)) {

			if (instance.isIncomplete()) {
				model.remove(instance.getProposition(model));
			}
			else {

				Template template = instance.getTemplate();
				log.info("Model: " + model.hashCode() + " - Expanding instance of template: " + template.getIRI());

				// check for cycles
				if (callerTemplates.contains(template.getIRI())) {
					throw new ExpanderException("Cyclic template expansion calls, '" + template.getIRI()
					+ "' is already called. List of expansion calls: " + callerTemplates.toString());
				}

				// TODO Mark instance call?
				if (settings.isIncludeCall()) {

				} else {
					log.info("Model: " + model.hashCode() + " - Removing instance call: " + instance);
					model.remove(instance.getProposition(model));
				}

				if (settings.isIncludeHead()) {
					Model instanceHead = instance.getHeadInstance();
					log.info("Model: " + model.hashCode() + " - Adding instance head");//: " + ModelIO.writeModel(instanceHead, ModelIO.format.TURTLE));
					Models.addStatements(instanceHead, model);
				}

				// recurse on instance's body
				if (depth != 0) {
					List<Resource> nextCallers = new LinkedList<>(callerTemplates);
					nextCallers.add(template.getIRI());
					log.info("Model: " + model.hashCode() + " - Expanding body instance");
					Model instanceBody = expand(instance.getBodyInstance(), settings, nextCallers, depth - 1);

					if (settings.isIncludeBody()) {
						Models.addStatements(instanceBody, model);
					}
				}
			}
		}
		return model;
	}

    private static void removeTriplesWithNull(Model model) {

		// Remove triples containing null-valued arguments from body
		StmtIterator stmtIter = model.listStatements();
		while (stmtIter.hasNext()) {
			Statement s = stmtIter.nextStatement();
			if (Templates.none.equals(s.getSubject()) ||
				Templates.none.equals(s.getPredicate()) ||
				Templates.none.equals(s.getObject())) {

				stmtIter.remove();
			}
		}
    }
}
