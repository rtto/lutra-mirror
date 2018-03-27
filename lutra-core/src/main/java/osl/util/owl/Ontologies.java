package osl.util.owl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;

public class Ontologies {

	/*
	public static OWLOntology getEmptyOntology () {
		try {
			return OWLManager.createOWLOntologyManager().createOntology();
		} catch (OWLOntologyCreationException e) {
			System.err.println("Error creating empty ontology: ");
			e.printStackTrace();
		}
		return null;
	}
	 */

	public static OWLOntology readOntology (String owlfile) throws OWLOntologyCreationException {
		return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(IRI.create(owlfile));
	}

	public static OWLOntology toOntology (Model model) throws OWLOntologyCreationException, ModelIOException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		return manager.loadOntologyFromOntologyDocument(new StringDocumentSource(ModelIO.writeModel(model, ModelIO.format.RDFXML)));
	}

	public static String writeOntology (OWLOntology ontology) throws OWLOntologyStorageException, IOException {
		return writeOntology(ontology, new TurtleDocumentFormat());
	}

	public static String writeAsOntology (Model model) throws OWLOntologyStorageException, IOException, OWLOntologyCreationException, ModelIOException {
		TurtleDocumentFormat format = new TurtleDocumentFormat();
		format.copyPrefixesFrom(model.getNsPrefixMap());
		return writeOntology(toOntology(model), format);
	}

	private static String writeOntology (OWLOntology ontology, TurtleDocumentFormat format) throws OWLOntologyStorageException, IOException {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		manager.saveOntology(ontology, format, buffer);
		String output = buffer.toString();
		buffer.flush();
		buffer.close();
		return output;
	}

	public static OWLReasoner getReasoner (OWLOntology ontology) {
		// OWL API
		//OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		//OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);

		// Hermit
		ReasonerFactory factory = new ReasonerFactory();
		Configuration config = new Configuration();
		config.throwInconsistentOntologyException = false;
		OWLReasoner reasoner = factory.createReasoner(ontology, config);

		reasoner.precomputeInferences();

		return reasoner;
	}

	/*
	public static void addTriplesToOntology (final OWLOntology ontology, final Model model) {

		OWLRDFConsumer consumer = new OWLRDFConsumer(ontology, new OWLOntologyLoaderConfiguration());

		for (Triple t : Models.toTripleSet(model)) {
			Node subject = t.getSubject();
			Node predicate = t.getPredicate();
			Node object = t.getObject();
			if (object.isLiteral()) {
				String oValue = object.toString();
				String oDatatype = object.getLiteralDatatypeURI();
				String oLanguage = object.getLiteralLanguage();
				consumer.statementWithLiteralValue(getURI(subject), getURI(predicate), oValue, oLanguage, oDatatype);
			} else {
				consumer.statementWithResourceValue(getURI(subject), getURI(predicate), getURI(object));
			}
		}
	}

	private static String getURI (Node resourceNode) {
		return resourceNode.toString();
	}
	 */

}
