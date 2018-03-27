package xyz.lutra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;

import osl.util.rdf.ModelIO;
import osl.util.rdf.Models;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.model.Argument;
import xyz.lutra.model.MulitValueArgument;
import xyz.lutra.model.Parameter;
import xyz.lutra.model.Template;
import xyz.lutra.model.TemplateInstance;
import xyz.lutra.parser.TemplateInstanceParser;

public class stOTTRWriter {

	private final static String empty = "";

	private final static String indent = "  ";
	private final static String br = "\n";
	private final static String ibr = br + indent;

	private final static String headBodySep = "::";

	private final static String paramTypeSep = " : ";

	private final static String argStart = "( ";
	private final static String argEnd = " )";
	private final static String argSep = " , ";

	private final static String cardOptional = "?";
	private final static String cardMandatory = "1";
	private final static String cardManyMandatory = "+";
	private final static String cardManyOptional = "*";

	private final static String modeSep = " | ";
	private final static String modeCross = "X ";
	//private final static String modeZip = "Z ";

	private final static String listStart = "<";
	private final static String listEnd = ">";
	private final static String listSep = ", ";

	/*
	private final static String command = "$";
	private final static String commandArgStart = "(";
	private final static String commandArgEnd = ")";
	private final static String commandArgSep = ", ";
	*/
	
	private final static String prefix = "@prefix ";
	private final static String prefixSep = ": ";
	private final static String prefixNsStart = "<";
	private final static String prefixNsEnd = ">";

	private final static String instuctionEnd = " .";

	private final static String pxRDF = "t-rdf";
	private final static String nsRDF = "http://candidate.ottr.xyz/rdf/";
	private final static String tripleTemplate = pxRDF + ":Triple";
	
	private final static PrefixMapping prefixes = PrefixMapping.Factory.create();

	static {
		prefixes.setNsPrefix(pxRDF, nsRDF);
		prefixes.setNsPrefix(Templates.prefix, Templates.namespace);
		prefixes.setNsPrefix("t-rdfs",      "http://candidate.ottr.xyz/rdfs/");
		prefixes.setNsPrefix("t-owl-axiom", "http://candidate.ottr.xyz/owl/axiom/");
		prefixes.setNsPrefix("t-owl-rstr",  "http://candidate.ottr.xyz/owl/restriction/");
		prefixes.setNsPrefix("t-owl-atom",  "http://candidate.ottr.xyz/owl/atom/");
	} 

	private BlankNodeMinter bMinter;
	private PrefixCollector pxCollector;

	public stOTTRWriter() {
		bMinter = new BlankNodeMinter("_:b", 1);
		pxCollector = new PrefixCollector();
	}

	public String print(PrefixMapping px) {
		return px.getNsPrefixMap().entrySet().stream()
				.sorted((a,b) -> a.getKey().compareTo(b.getKey()))
				.filter(e -> pxCollector.getNamespaces().contains(e.getValue()))
				.map(e -> String.join("", 
						prefix,
						e.getKey(),
						prefixSep,
						prefixNsStart,
						e.getValue(),
						prefixNsEnd,
						instuctionEnd
						))
				.collect(Collectors.joining(br, empty, br + br));
	}
	
	private void appendPrefixes(Model model) {
		model.withDefaultMappings(prefixes);
		model.withDefaultMappings(PrefixMapping.Extended);
	}

	public String print(Template template) {

		Model context = template.getSourceModel();
		appendPrefixes(context);
		
		StringBuilder str = new StringBuilder();

		// template head
		str
		.append(print(template.getIRI(), context)) // name
		.append(template.getParameters().stream().map(p -> print(p)).collect(Collectors.joining(argSep, argStart, argEnd))) // parameters
		.append(ibr).append(headBodySep);

		// prepare body instances
		Set<TemplateInstance> instances = TemplateInstanceParser.getTemplateInstances(context);
		// get triple template instances by removing template instance triples from body:
		Model triples = Models.duplicate(template.getBody(), Models.BlankCopy.KEEP);
		instances.stream().forEach(i -> triples.remove(i.getProposition(context)));
		// print instances
		str.append(
				Stream.concat(
						// non-triple instances
						instances.stream()
						.sorted((a,b) -> a.getTemplateRef().getURI().compareTo(b.getTemplateRef().getURI())) // sort after templateRef
						.map(i -> print(i)),
						// triple instances
						triples.listStatements().toList().stream()
						.map(n -> print(n, context)))
				.collect(Collectors.joining(ibr, ibr, empty)));

		// end template
		str.append(instuctionEnd);

		return print((PrefixMapping)context) 
				+ str.toString();
	}

	public String print(TemplateInstance instance) {

		String mode = (instance.getArguments().stream().anyMatch(a -> a instanceof MulitValueArgument)) 
				? modeSep + modeCross 
						: empty;

		return print(instance.getTemplateRef())
				+ mode 
				+ instance.getArguments().stream().map(a -> print(a)).collect(Collectors.joining(argSep, argStart, argEnd));
	}

	private String print(Parameter p) {
		String cardinality = getParameterCardinality(p);
		String type = getParameterType(p);
		return print(p.getValue()) 
				+ paramTypeSep
				+ cardinality
				+ (!type.isEmpty() ? " " + type : empty)
				;
	}

	private String getParameterType(Parameter p) {
		return p.getType().getLocalName()
				.replace("Variable", empty).replace("variable", empty)
				.replace("listVariable", empty);
	}

	private String getParameterCardinality(Parameter p) {
		RDFNode value = p.getValue();
		if (value.canAs(RDFList.class) && p.isOptional()) {
			return cardManyOptional;
		} else if (value.canAs(RDFList.class) && !p.isOptional()) {
			return cardManyMandatory;
		} else if (p.isOptional()) {
			return cardOptional;
		} else {
			return cardMandatory;
		}
	}

	private String print(Argument p) {
		RDFNode value = p.getValue();
		return value != null ? print(value) : empty;
	}

	private String print(Statement s, Model context) {
		pxCollector.register(nsRDF);
		return tripleTemplate
				+ argStart
				+ print(s.getSubject(), context) + argSep
				+ print(s.getPredicate(), context) + argSep
				+ print(s.getObject(), context) 
				+ argEnd;
	}

	private String print(RDFNode node, Model context) {
		// mint new blank nodes
		if (node.isAnon() && !node.canAs(RDFList.class)) {
			return bMinter.get(node);
		}
		// list
		else if (node.canAs(RDFList.class)) {
			return node.as(RDFList.class).asJavaList().stream()
					.map(n -> print(n, context))
					.collect(Collectors.joining(listSep, listStart, listEnd));
		} 
		else {
			pxCollector.register(node);
			return context == null ? node.toString() : ModelIO.shortForm(context, node);
		}
	}

	private String print(RDFNode node) {
		return print(node, node.getModel());
	}


	private class PrefixCollector {
		private Set<String> namespaces;

		public PrefixCollector() {
			namespaces = new HashSet<>();
		}

		public void register(String ns) {
			if (!ns.isEmpty()) {
				namespaces.add(ns);
			}
		}
		
		public void register(RDFNode node) {

			// Collect uri:
			String uri = "";
			if (node.isURIResource()) {
				uri = node.asResource().getURI();
			}
			else if (node.isLiteral() && node.asLiteral().getDatatypeURI() != null) {
				uri = node.asLiteral().getDatatypeURI();
			}

			if (!uri.isEmpty()) {
				// Get namespace from uri:
				String ns;
				ns = uri.substring(0, uri.indexOf('#') + 1);

				if (ns.isEmpty()) {
					ns = uri.substring(0, uri.lastIndexOf('/') + 1);
				}	
				register(ns);
			}
		}

		public Set<String> getNamespaces() {
			return namespaces;
		}
	}

	private class BlankNodeMinter {
		private Map<RDFNode, String> map = new HashMap<>();
		private String prefix;
		private int index;

		public BlankNodeMinter(String prefix, int index) {
			map = new HashMap<>();
			this.prefix = prefix;
			this.index = index;
		}

		public String get(RDFNode node) {
			return map.computeIfAbsent(node, x -> prefix + index++);
		}
	}
}
