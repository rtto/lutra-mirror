package osl.util.rdf;

import java.util.Arrays;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;

public class RDFNodes {
	
	public enum Type {
		Resource ("RDF resource"), RDFList("RDF list"), NonLiteral ("non-literal"), Literal("literal"), Blank("blank node"), IRI("concrete IRI"), Variable("variable");
		private String name;
		private Type(String name) { this.name = name; }
		public String toString () { return name; }
	}
	
	public static RDFNodes.Type getType (RDFNode node) {
		if (node.canAs(RDFList.class)) {
			return Type.RDFList;
		} else if (node.asNode().isVariable()) {
			return Type.Variable;
		} else if (node.isURIResource()) {
			return Type.IRI;
		} else if (node.isAnon()) {
			return Type.Blank;
		} else if (node.isLiteral()) {
			return Type.Literal;
		} 
		// unreachable:
		else if (!node.isLiteral()) {
			return Type.NonLiteral;
		} else {
			return Type.Resource;
		}
	}
	
	public static boolean isOfType(RDFNode node, Type... types) {
		return Arrays.asList(types).contains(getType(node));
	}
}
