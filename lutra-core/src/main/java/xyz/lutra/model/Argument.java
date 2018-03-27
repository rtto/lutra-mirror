package xyz.lutra.model;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import osl.util.rdf.ModelIO;

public class Argument extends IndexedValueResource<RDFNode> {
	
	public Argument (Resource uri, int index, RDFNode value) {
		super(uri, index, value);
	}
	
	public int getValueCount () {
		return 1;
	}
	
	public RDFNode getValue (int i) {
		if (i != 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		return getValue();
	}
	
	public String toString() {
		return ModelIO.shortForm(value);
	}
}