package xyz.lutra.model;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Resource;

public class VariableArgument extends IndexedValueResource<Node> {

	public VariableArgument (Resource uri, int index, Node value) {
		super(uri, index, value);
	}
}