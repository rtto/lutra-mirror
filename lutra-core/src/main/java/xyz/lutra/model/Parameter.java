package xyz.lutra.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class Parameter extends IndexedValueResource<RDFNode> {
	
	private Property type;
	
	public Parameter (Resource uri, int index, RDFNode variable) {
		super(uri, index, variable);
	}

	public Parameter (Resource uri, int index, RDFNode variable, boolean optional) {
		super(uri, index, variable, optional);
	}

	public Parameter (Resource uri, int index, RDFNode variable, Property type) {
		this(uri, index, variable);
		this.type = type;
	}

	public Parameter (Resource uri, int index, RDFNode variable, Property type, boolean optional) {
		this(uri, index, variable, optional);
		this.type = type;
	}

	public Property getType () {
		return type;
	}
	
	public boolean hasType () {
		return type != null;
	}
	
	/*
	public Argument toArgument () {
		return new Argument (ResourceFactory.createResource(), this.index, this.value);
	}
	*/
	
	public String toString () {
		return index + ": " + super.toString(); 
	}
	
}
