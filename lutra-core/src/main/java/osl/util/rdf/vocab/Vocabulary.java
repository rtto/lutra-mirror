package osl.util.rdf.vocab;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public abstract class Vocabulary {
	
	protected static Resource getResource (String url) {
		return ResourceFactory.createResource(url);
	}

	protected static Property getProperty (String url) {
		return ResourceFactory.createProperty(url);
	}

}
