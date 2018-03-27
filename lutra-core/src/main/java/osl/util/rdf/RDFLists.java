package osl.util.rdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDFLists {
	
	private static Logger log = LoggerFactory.getLogger(RDFLists.class);

	public static List<RDFList> getNonEmptyRDFLists (Model model) {
		List<RDFList> lists = new ArrayList<>();
		for (ResIterator itList = model.listSubjectsWithProperty(RDF.first); itList.hasNext(); ) {
			Resource head = itList.next();
			if (!model.listResourcesWithProperty(RDF.rest, head).hasNext() && // make sure that we are at the first element
					head.canAs(RDFList.class)) {
				lists.add(head.as(RDFList.class));
			}
		}
		log.info("Found " + lists.size() + " non-empty RDF lists.");
		return lists;
	}

	// TODO check that they really are lists.
	public static void substituteNonEmptyRDFList (Model model, RDFList old, RDFList fresh) {
		List<RDFNode> oldContent = old.asJavaList();
		
		for (RDFList l : getNonEmptyRDFLists(model)) {
			if (oldContent.equals(l.asJavaList())){
				log.info("Found matching list");
				l.removeList();
				//RDFList freshCopy = copy(fresh);
				Model copy = getFreshListCopy(fresh);
				model.add(copy);
				// TODO Change this: get single head:
				RDFList copyHead = getNonEmptyRDFLists(copy).get(0);
				for (Statement s: model.listStatements(null, null, l).toList()) {
					s.changeObject(copyHead);
				}
			}
		}
	}
	
	// replace lists with the same content as old with a single RDFNode fresh
	public static void substituteNonEmptyRDFList (Model model, RDFList old, Node fresh) {
		List<RDFNode> oldContent = old.asJavaList();
		for (RDFList l : getNonEmptyRDFLists(model)) {
			if (oldContent.equals(l.asJavaList())){
				l.removeList();
				ModelEditor.substituteNode(model, l.asNode(), fresh);
			}
		}
	}
	
	public static Model getFreshListCopy (RDFList head) {
		Model empty = Models.empty();
		empty.add(getMinimalListStatements(head));
		
		// ERROR: must not replace blank nodes that are the contents of the list
		// *** ModelEditor.substituteBlanks(empty);
		// Instead we only substitute the subjects of rdf:first:
		for (Resource blank : ModelSelector.listResourcesWithProperty(empty, RDF.first)) {
			ModelEditor.substituteNode(empty, blank, ResourceFactory.createResource());
		}
		return empty;
	}
		
	/**
	 * Selects only rdf:first and rdf:rest statements that make up the list, recursively including lists in the list.
	 * @SeeAlso: getAllListStatements.
	 */
	public static List<Statement> getMinimalListStatements (RDFList head) {
		List<Statement> list = new ArrayList<>();
		Model model = head.getModel();

		Resource first = head;
		Resource rest;

		for (ExtendedIterator<RDFNode> it = head.iterator(); it.hasNext(); ){
			// first, the content element
			RDFNode content = it.next();
			list.addAll(model.listStatements(first, RDF.first, content).toList());
			// recursive call, if content is a list
			if (content.canAs(RDFList.class)) {
				list.addAll(getMinimalListStatements(content.as(RDFList.class)));
			}
			// rest: either more lists elements, or nil
			if (it.hasNext()) {
				rest = model.getRequiredProperty(first, RDF.rest).getObject().asResource();
			} else {
				rest = RDF.nil;
			}
			list.addAll(model.listStatements(first, RDF.rest, rest).toList());
			// prepare for next list element
			first = rest;
		}
		return list;
	}

	/**
	 * Selects all triples where the triple subject is part of the linked list structure.
	 * @param head
	 * @return
	 */
	public static List<Statement> getAllListStatements (RDFList head) {
		List<Statement> list = new ArrayList<>();
		Model model = head.getModel();
		Resource first = head;

		for (ExtendedIterator<RDFNode> it = head.iterator(); it.hasNext(); ){
			list.addAll(model.listStatements(first, null, (RDFNode)null).toList());

			// recursive call, if content is a list
			RDFNode content = it.next();
			if (content.canAs(RDFList.class)) {
				list.addAll(getAllListStatements(content.as(RDFList.class)));
			}
			// prepare for next list element
			if (it.hasNext()) {
				first = model.getRequiredProperty(first, RDF.rest).getObject().asResource();
			}
		}
		return list;
	}
	
	/*
	public static List<Statement> copyList (RDFList head) {
		List<Statement> copy = new ArrayList<>();

		Resource first = head;
		Resource rest;
		for (ExtendedIterator<RDFNode> it = head.iterator(); it.hasNext(); ){
			// first, the content element
			RDFNode content = it.next();
			copy.add(ResourceFactory.createStatement(first, RDF.first, content));
			// recursive call, if content is a list
			if (content.canAs(RDFList.class)) {
				copy.addAll(copyList(content.as(RDFList.class)));
			}
			// rest: either more lists elements, or nil
			if (it.hasNext()) {
				rest = ResourceFactory.createResource();
			} else {
				rest = RDF.nil;
			}
			copy.add(ResourceFactory.createStatement(first, RDF.rest, rest));
			// prepare for next list element
			first = rest;
		}
		return copy;
	}*/
	
	/**
	 * This is a almost verbatim copy of Jena's RDFlistImpl.copy method, but 
	 * there the list items are not typed as lists as the native list syntax 
	 * (a b c) fails to kick in.
	 * Note that the copy is performed inplace. 
	 * @param inlist
	 * @return
	 */

	public static RDFList copy (RDFList inlist) {
		Resource list = null;
		Resource start = null;

		Property head = RDF.first;
		Property tail = RDF.rest;

		ExtendedIterator<RDFNode> i = inlist.iterator();

		if (i.hasNext())
		{
			while (i.hasNext()){
				// create a list cell to hold the next value from the existing list
				Resource cell = inlist.getModel().createResource();
				cell.addProperty( head, i.next() );

				// point the previous list cell to this one
				if (list != null) {
					list.addProperty( tail, cell ); 
				}
				else {
					// must be the first cell we're adding
					start = cell;
				}

				list = cell;
			}

			// finally close the list
			list.addProperty( tail, RDF.nil );
		}
		else
		{
			// create an empty list
			start = inlist.getModel().createList();
		}

		return start.as( RDFList.class );
	}
}
