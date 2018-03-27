package xyz.lutra.xml;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import xyz.lutra.Cache;
import xyz.lutra.Settings;
import xyz.lutra.model.Parameter;
import xyz.lutra.model.Template;
import xyz.lutra.model.VariableArgument;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.TemplateLoader;

public abstract class TemplateXMLSample {

	public final static String XSI_NS = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
	public final static String XSI_prefix = "xsi";

	private final static String EX = "http://example.com#";
	private final static String EXPANDED_XML_BODY_URL_PREFIX = "http://sws.ifi.uio.no/ottrs/expansion/body/?fmt=xml&tpl=";

	// TODO-s:
	// - add ref to XSD instance
	// - V datatypes

	private static Cache<String> xmlStringCache = new Cache<String> (
			Settings.enableTempalteXMLSampleCache, 
			iri -> {
				String xml;
				try {
					Template template = TemplateLoader.getTemplate(iri);
					xml = XMLUtils.writeDocument(getXMLDocument(template));
				} catch (ParserException | TransformerException e) {
					throw new ParserException ("Error generating XML sample. " + e.getMessage());
				}
				return xml;
			});

	public static String getXMLString (String iri) throws TransformerException {
		String xml = xmlStringCache.get(iri);
		if (Settings.enableXMLSampleValidation) {
			TemplateXSD.validateXML(iri, xml);
		}
		return xml;
	}

	private static Document getXMLDocument (Template template) {
		Document doc = XMLUtils.getDocument();
		
		//ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"" + Settings.servletRoot + "/xsl/lifting-template-instance.xsl\"");
		
		Element root = doc.createElement(template.getIRI().getLocalName());
		
		root.setAttribute("xmlns:" + XSI_prefix, XSI_NS);

		String templateNS = template.getIRI().getURI();
		//root.setAttribute("xmlns", templateNS);
		root.setAttribute(XSI_prefix + ":noNamespaceSchemaLocation", Settings.servletRoot + Settings.servletFormatServiceHeadXML + "/?" +Settings.servletParamTemplate+ "=" + templateNS);  
		root.setAttribute(TemplateXSD.TEMPLATE_REF_ATTRIBUTE, EXPANDED_XML_BODY_URL_PREFIX+templateNS);  

		doc.appendChild(root);
		//doc.insertBefore(pi, root);

		Element instance = doc.createElement(TemplateXSD.TEMPLATE_INSTANCE_ELEMENT);
		root.appendChild(instance);

		List<Parameter> parameters = template.getParameters();
		for (Parameter param : parameters) {
			Element argName = doc.createElement(TemplateXSD.getParameneterElementName(param));
			argName.setAttribute("variable", TemplateXSD.getVariableValue(param.getValue()));
			argName.appendChild(doc.createTextNode(getParameterValue(param.getValue())));
			instance.appendChild(argName);
		}
		return doc;
	}

	public static String getParameterValue (RDFNode variable) {
		String value;
		if (variable.canAs(RDFList.class)) {
			// convert to space separated list
			value = variable.as(RDFList.class).asJavaList().stream()
					.map(i -> getParameterValue(i))
					.collect(Collectors.joining(" "));
		} else if (variable.isAnon()) {
			value = EX + TemplateXSD.ParamNameBase + variable.hashCode(); 
		} else if (variable.isLiteral()) {
			value = variable.asLiteral().getLexicalForm(); 
		} else if (variable.isURIResource()) {
			value = EX + variable.asResource().getLocalName();
		} else { // unreachable 
			value = variable.toString();
		}
		return value;
	}
}
