package xyz.lutra.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import osl.util.rdf.vocab.Templates;
import xyz.lutra.Cache;
import xyz.lutra.Settings;
import xyz.lutra.model.IndexedValueResource;
import xyz.lutra.model.Parameter;
import xyz.lutra.model.Template;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.TemplateLoader;

public abstract class TemplateXSD {

	private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	private static final String XSD_prefix = "xs";
	
	private static final String SAWSDL_NS = "http://www.w3.org/ns/sawsdl";
	private static final String SAWSDL_prefix = "sawsdl";

	public final static String TEMPLATE_INSTANCE_ELEMENT = "instance";
	public final static String TEMPLATE_REF_ATTRIBUTE = "template";
	public final static String TEMPLATE_VARIABLE_ATTRIBUTE = "variable";
	
	final static String parameterElementBase = "p";
	final static String ParamNameBase = "param_";

	private final static String xsdParamtypeBase = "type_";

	// TODO-s:
	// - schema namespace
	// - X template URI
	// - X parameter types
	// - X parse document to schema: SchemaFactory.newSchema(Source source)

	private static Cache<String> xsdStringCache = new Cache<String> (
			Settings.enableTempalteXSDCache, 
			iri -> {
				String xsd;
				try {
					Template template = TemplateLoader.getTemplate(iri);
					Document doc = getXSDDocument(template);
					xsd = XMLUtils.writeDocument(doc);
				} catch (ParserException | TransformerException e) {
					throw new ParserException ("Error generating XSD. " + e.getMessage());
				}
				return xsd;
			});

	public static String getXSDString (String iri) throws TransformerException, SAXException {
		String xsdString = xsdStringCache.get(iri);
		if (Settings.enableXSDStingValidation) {
			convertToXSDSchema(xsdString);
		}
		return xsdString;
	}

	
	public static Schema getXSDSchema (String iri) throws SAXException, TransformerException {
		return convertToXSDSchema(getXSDString(iri));
	}
	
	private static Schema convertToXSDSchema (String xsdString) throws SAXException, TransformerException {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		return factory.newSchema(XMLUtils.getSourceFromString(xsdString));
	}

	/**
	 * Validate {{xml}} string against the template XSD identified by {{iri}}.
	 * @param iri
	 * @param xml
	 * @throws ParserException
	 */
	public static void validateXML (String iri, String xml) throws ParserException {
		try {
			Schema xsd = getXSDSchema(iri);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			xsd.newValidator().validate(XMLUtils.getSourceFromString(xml), result);
		} catch (SAXException | TransformerException | IOException e) {
			throw new ParserException ("Error validating XML. " + e.getMessage());
		}
	}

	private static Document getXSDDocument (Template template) {

		List<Parameter> parameters = template.getParameters();

		Document doc = XMLUtils.getDocument();

		// TODO doc.setDocumentURI(template.getIRI().getURI()+".xsd");

		// root element: XSD schema
		Element schema = doc.createElement(XSD_prefix + ":schema");
		schema.setAttribute("xmlns:" + XSD_prefix, XSD_NS);
		schema.setAttribute("xmlns:" + SAWSDL_prefix, SAWSDL_NS);
		
		//schema.setAttribute("xmlns:" + thisPrefix , template.getIRI()+"#");
		//schema.setAttribute("targetNamespace", template.getIRI()+"#");
		//schema.setAttribute("elementFormDefault", "qualified");
		doc.appendChild(schema);
		
		//Element rootSeq = getXSDComplexTypeSequenceElement(doc, schema, rootAttrs);
		
		Element elTemplate = XMLUtils.appendNewChild(doc, schema, XSD_prefix + ":element");
		elTemplate.setAttribute("name", template.getIRI().getLocalName());

		Element elTemplateType = XMLUtils.appendNewChild(doc, elTemplate, XSD_prefix + ":complexType");
			
		Element elTemplateSequence = XMLUtils.appendNewChild(doc, elTemplateType, XSD_prefix + ":sequence");
		
		Element elTemplateAttr = XMLUtils.appendNewChild(doc, elTemplateType, XSD_prefix + ":attribute");
		elTemplateAttr.setAttribute("name", TEMPLATE_REF_ATTRIBUTE);
		
		Element elInstance = XMLUtils.appendNewChild(doc, elTemplateSequence, XSD_prefix + ":element");
		elInstance.setAttribute("name", TEMPLATE_INSTANCE_ELEMENT); 
		elInstance.setAttribute("maxOccurs", "unbounded");
		elInstance.setAttribute(SAWSDL_prefix +":modelReference", template.getIRI().getURI());
		
		Element elInstanceType = XMLUtils.appendNewChild(doc, elInstance, XSD_prefix + ":complexType");
		Element elInstanceSequence = XMLUtils.appendNewChild(doc, elInstanceType, XSD_prefix + ":sequence");

		// add parameter refs to sequence
		for (Parameter p : parameters) {
			elInstanceSequence.appendChild(getParameterElement(doc, p));
		}

		// add parameter types as separate simple types
		for (Parameter p : parameters) {
			if (hasComplexType(p)) {
				schema.appendChild(getParameterType(doc, p));
			}
		}
		return doc;
	}

	private static Element getParameterRef (Document doc, Parameter parameter) {
		Element element = doc.createElement(XSD_prefix + ":element");
		element.setAttribute("ref", getParameneterElementName(parameter));
		return element;
	}

	private static Element getParameterElement (Document doc, Parameter parameter) {
		// <xs:element name="NAME" type="TYPE"/>
		Element element = doc.createElement(XSD_prefix + ":element");
		String name = getParameneterElementName(parameter);
		element.setAttribute("name", name);
		element.setAttribute(SAWSDL_prefix +":modelReference", parameter.getIRI().toString());

		Element elementType = XMLUtils.appendNewChild(doc, element, XSD_prefix + ":complexType");
		Element elementContent = XMLUtils.appendNewChild(doc, elementType, XSD_prefix + ":simpleContent");
		Element elementExtension = XMLUtils.appendNewChild(doc, elementContent, XSD_prefix + ":extension");
		elementExtension.setAttribute("base", XSD_prefix + ":string");
		
		Element elementAttr = XMLUtils.appendNewChild(doc, elementExtension, XSD_prefix + ":attribute");
		elementAttr.setAttribute("name", TEMPLATE_VARIABLE_ATTRIBUTE);
		elementAttr.setAttribute("default", getVariableValue(parameter.getValue()));

		if (hasType(parameter)) {
			if (hasComplexType(parameter)) {
				elementAttr.setAttribute("type", xsdParamtypeBase + getParameterName(parameter));
			} else {
				elementAttr.setAttribute("type", getXSDDatatype(parameter));
			}
		}
		return element;
	}

	private final static List<Property> URI_types = Arrays
			.asList(new Property[] {
					//Templates.variable,
					//Templates.literalVariable,
					Templates.nonLiteralVariable,
					Templates.classVariable,
					//Templates.listVariable,
					Templates.individualVariable,  
					Templates.datatypeVariable,
					Templates.propertyVariable,
					Templates.objectPropertyVariable, 
					Templates.dataPropertyVariable, 
					Templates.annotationPropertyVariable, 
			});

	private final static boolean hasType (Parameter parameter) {
		return !parameter.getType().equals(Templates.variable);
	}

	private final static boolean hasComplexType (Parameter parameter) {
		return parameter.getType().equals(Templates.listVariable);
	}

	private final static String getXSDDatatype (Parameter parameter) {
		String XSD_type = XSD_prefix + ":" + "anySimpleType";
		Property parameterType = parameter.getType();
		if (URI_types.contains(parameterType)) {
			XSD_type = XSD_prefix + ":" + "anyURI";
		} else if (parameterType.equals(Templates.literalVariable)) {
			RDFNode value = parameter.getValue();
			XSD_type = value.asLiteral().getDatatypeURI();
			XSD_type = XSD_type.replaceAll(XSD_NS+"#", XSD_prefix+":");
		}
		return XSD_type;
	}

	// https://www.w3.org/TR/xmlschema-2/
	private static Element getParameterType (Document doc, Parameter parameter) {

		Property parameterType = parameter.getType();

		Element element = doc.createElement(XSD_prefix + ":simpleType");
		element.setAttribute("name", xsdParamtypeBase + getParameterName(parameter));

		if (parameterType.equals(Templates.listVariable)) {
			Element list = doc.createElement(XSD_prefix + ":list");
			list.setAttribute("itemType", getXSDDatatype(parameter));
			element.appendChild(list);
		} else {
			Element restriction = doc.createElement(XSD_prefix + ":restriction");
			restriction.setAttribute("base", getXSDDatatype(parameter));
			element.appendChild(restriction);
		}
		return element;
	}

	public static String getParameterName (IndexedValueResource param) {
		String name;
		Resource paramIRI = param.getIRI();
		if (paramIRI.isAnon()) {
			name = ParamNameBase + param.getIndex(); 
		} else { 
			name = paramIRI.getLocalName();
		}
		return name;
	}

	public static String getVariableValue (RDFNode variable) {
		String value;
		if (variable.canAs(RDFList.class)) {
			// convert to space separated list
			value = variable.as(RDFList.class).asJavaList().stream()
					.map(i -> getVariableValue(i))
					.collect(Collectors.joining(" "));
		} else if (variable.isLiteral()) {
			value = variable.asLiteral().getLexicalForm(); 
		} else {
			value = variable.toString(); 
		}
		return value;
	}

	public static String getParameneterElementName (Parameter param) {
		return parameterElementBase + param.getIndex();
	}
}
