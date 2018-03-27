package xyz.lutra.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

import osl.util.rdf.Models;
import osl.util.rdf.RDFLists;

public abstract class RDFResource {

	protected org.apache.jena.rdf.model.Resource iri;
	private List<Statement> theNeighbourhood;

	public RDFResource (Resource iri) {
		this.iri = iri;
	}

	public Resource getIRI () {
		return iri;
	}

	public String toString () {
		Model m = iri.getModel(); // not all resources are contained in a model.
		if (m == null) {
			m = Models.EMPTY;
		}
		return m.shortForm(iri.toString());
	}

	/**
	 * The neighbourhood of a Resource is the set of
	 * triples where the Resource's uri is the subject---including complete lists, 
	 * if the object of the triple is a list.
	 * @return
	 */
	public List<Statement> getNeighbourhood () {
		if (theNeighbourhood == null) {
			theNeighbourhood = getNeighbourhood(iri.getModel());
		}
		return theNeighbourhood;
	}

	public List<Statement> getNeighbourhood (Model model) {
		List<Statement> neighbourhood = new ArrayList<>();
		for (StmtIterator it = model.listStatements(iri, null, (RDFNode)null); it.hasNext(); ) {
			Statement t = it.next();
			neighbourhood.add(t);
			// if object is a list, we add the whole list:
			RDFNode object = t.getObject();
			if (object.canAs(RDFList.class)) {
				neighbourhood.addAll(RDFLists.getAllListStatements(object.as(RDFList.class)));
			}
		}
		return neighbourhood;
	}
}
