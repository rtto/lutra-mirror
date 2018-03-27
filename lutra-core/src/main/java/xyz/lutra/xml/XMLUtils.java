package xyz.lutra.xml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class XMLUtils {
	
	public static String writeDocument (Document doc) throws TransformerException {
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		streamDocument(doc, result);
		return writer.toString();
	}
	
	public static void printDocument (Document doc) throws TransformerException {
		StreamResult result = new StreamResult(System.out);
		streamDocument(doc, result);
	}
	
	public static Source getSourceFromString (String xmlData) {
		return new StreamSource(new StringReader(xmlData));
	}
	
	public static Source getSourceFromFile (String xmlFile) {
		return new StreamSource(xmlFile);
	}
	
	
	private static void streamDocument (Document doc, StreamResult result) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		//transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
	}

	protected static Document getDocument () {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		//TODO docFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = docBuilder.newDocument();
		//doc.setXmlStandalone(true);
		return doc;		 
	}
	
	protected static Element appendNewChild(Document doc, Element parent, String elementName) {
		Element element = doc.createElement(elementName);
		parent.appendChild(element);
		return element;
	}
	
	/*
	private static String applyXSLT (String xsltPath, String sourcePath) throws TransformerException {
		TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl();

		Transformer transformer = tfactory.newTransformer(new StreamSource(xsltPath));

		StreamSource inputSource = new StreamSource(sourcePath);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		transformer.transform(inputSource, new StreamResult(output));
		
		return output.toString();
	}
	*/
}