package osl.util.rdf;

import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;

public abstract class Models {

	//private static Logger log = LoggerFactory.getLogger(Models.class);
	
	public final static Model EMPTY = empty();
	
	public static enum BlankCopy { KEEP, FRESH };

	/**
	 * Get a fresh empty model.
	 * @param mapping
	 * @return an empty model
	 */
	public static Model empty () {
		return ModelFactory.createDefaultModel();
	}
	
	/**
	 * Get an empty model with given prefix mapping.
	 * @param mapping
	 * @return the empty model
	 */
	public static Model empty (PrefixMapping mapping) {
		Model empty = empty();
		empty.setNsPrefixes(mapping);
		return empty;
	}
	
	/**
	 * Create a detached copy of the model, possibly keeping blank nodes as is, and including the model's prefix mapping.
	 * @param model the model to copy
	 * @param refreshBlanks true if blank nodes are to be replaced with fresh blanks.
	 * @return the copy of the model
	 */
	public static Model duplicate (Model model, BlankCopy blankCopyStrategy) {
		Model copy = empty();
		addStatements(model, copy);
		if (blankCopyStrategy.equals(BlankCopy.FRESH)) {
			ModelEditor.substituteBlanks(copy);
		}
		return copy;
	}
	
	/**
	 * Add all statements from source to target. Add also prefix mappings from source not in target.
	 * @param target
	 * @param source
	 * @return
	 */
	public static void addStatements (Model source, Model target) {
		// copy triples:
		// this overwrites prefixes: target.add(source);
		target.add(source.listStatements());
		// copy prefix mapping:
		PrefixMappings.addPrefixes(target, source);
	}

	public static Set<Triple> toTripleSet (Model model) {
		return Graphs.toTripleSet(model.getGraph());
	}
	
}
