package osl.util.rdf;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.StringJoiner;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.FileUtils;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import osl.util.owl.Ontologies;

public abstract class ModelIO {

	public enum format {
		RDFXML("RDF/XML"), TURTLE("TTL"), N3("N3"), NTRIPLES("N-TRIPLES"), OWL ("OWL");
		private final String lang;
		private format (final String lang) {
			this.lang = lang;
		}
	}
	
	public static Model readModel (String file) {
		return readModel(file, FileUtils.guessLang(file, ModelIO.format.TURTLE.lang));
	}

	public static Model readModel (String file, ModelIO.format serialisation) {
		return readModel(file, serialisation.lang);
	}

	private static Model readModel (String file, String serialisation) {
		return FileManager.get().loadModel(file, serialisation);
	}

	public static String writeModel (Model model, ModelIO.format format) throws ModelIOException {
		String serialisation = "";
		if (format.equals(ModelIO.format.OWL)) {
			try {
				serialisation = Ontologies.writeAsOntology(model);
			} catch (OWLOntologyStorageException | OWLOntologyCreationException | IOException e) {
				throw new ModelIOException ("Error writing model. " + e.getMessage());
			}
		} else {
			serialisation = writeRDFModel(model, format);
		}
		return serialisation;
	}

	private static String writeRDFModel (Model model, ModelIO.format format) {
		StringWriter str = new StringWriter();
		model.write(str, format.lang);
		String modelString = str.toString();
		str.flush();
		try {
			str.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modelString;
	}

	public static void printModel (Model model, ModelIO.format format) throws ModelIOException {
		System.out.println(writeModel(model, format));
	}

	public static String shortForm (RDFNode node) {
		Model model = node.getModel();
		return (model == null) ? node.toString() : shortForm(model, node.asNode());
	}

	public static String shortForm (Model model, Node node) {
		return shortForm(model, model.asRDFNode(node));
	}
	
	public static String shortForm (Model model, RDFNode node) {
		if (node.asNode().isVariable()) {
			return node.toString();
		}
		else if (node.canAs(RDFList.class)) {
			return shortForm(model, node.as(RDFList.class).asJavaList());
		} 
		else if (node.isLiteral() && node.asLiteral().getDatatypeURI() != null) {
			Literal l = node.asLiteral();
			return "\"" + l.getLexicalForm() + "\"^^" + model.qnameFor(l.getDatatypeURI());
		}
		return model.shortForm(node.toString());
	}

	public static String shortForm (Model model, List<? extends RDFNode> nodes) {
		StringJoiner sj = new StringJoiner(", ", "[", "]");
		for (RDFNode node : nodes) {
			sj.add(shortForm(model, node)); 
		}
		return sj.toString();
	}

}
