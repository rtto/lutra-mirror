package xyz.lutra.model;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class MulitValueArgument extends Argument {
	public MulitValueArgument (Resource uri, int index, RDFList value) {
		super(uri, index, value);
	}
	public RDFList getValue () {
		return super.getValue().as(RDFList.class);
	}
	
	public int getValueCount () {
		return getValue().size();
	}
	public RDFNode getValue (int i) {
		return getValue().get(i);
	}
	
}