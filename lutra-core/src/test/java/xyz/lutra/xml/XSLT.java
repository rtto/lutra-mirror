package xyz.lutra.xml;

import java.io.ByteArrayOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

import xyz.lutra.Settings;

public class XSLT {
	
	String root = "src/test/resources/xslt/";
	String webroot = "../lutra-servlet/src/main/webapp/xsl/";
	
	private void print (String s) {
		// disable printing for now
		//System.out.println(s);
	}
	
	@Test public void shouldCopyBody () throws TransformerException {
		print(applyXSLT(root + "copy.xsl", root + "body.xml"));
	}

	@Test public void shouldCopyInstance () throws TransformerException {
		print(applyXSLT(root + "copy.xsl", root + "instance.xml"));
	}

	@Test public void shouldInstantiateBody () throws TransformerException {
		print(applyXSLT(root + "copy2.xsl", root + "instance.xml"));
	}

	/*
	@Test public void shouldGiveRDF () throws TransformerException {
		String result = applyXSLT(webroot + "lifting-template-instance.xsl", root + "agentrole.xml");
		System.out.println(result);
	}*/
	
	@Test public void shouldGiveRDF2 () throws TransformerException {
		String w = Settings.servletRoot + Settings.servletFormatServiceHeadXML 
				+ "/?" + Settings.servletParamTemplate + "=" 
				+ "http://candidate.ottr.xyz/owl/axiom/EquivDataMaxCardinality";
		print(applyXSLT(webroot + "lifting-template-instance.xsl", w));
	}
	
	/*
	@Test public void shouldGiveRDF3 () throws TransformerException {
		String result = applyXSLT(webroot + "lifting-template-instance.xsl", root + "EquivDataMaxCardinality.xml");
		System.out.println(result);
	}*/
	
	private static String applyXSLT(String xsltPath, String sourcePath) throws TransformerException {
		TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl();

		Transformer transformer = tfactory.newTransformer(new StreamSource(xsltPath));

		StreamSource inputSource = new StreamSource(sourcePath);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		transformer.transform(inputSource, new StreamResult(output));
		
		return output.toString();
	}
	
}
