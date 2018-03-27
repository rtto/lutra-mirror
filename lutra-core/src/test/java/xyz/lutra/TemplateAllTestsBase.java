package xyz.lutra;

import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.util.function.Function;

import javax.xml.transform.TransformerException;

import org.apache.jena.rdf.model.Model;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import osl.util.rdf.ModelIOException;
import xyz.lutra.Expander;
import xyz.lutra.ExpanderSettings;
import xyz.lutra.TemplateQueries;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.ParserUtils;
import xyz.lutra.parser.TemplateLoader;
import xyz.lutra.xml.TemplateXMLSample;
import xyz.lutra.xml.TemplateXSD;

public abstract class TemplateAllTestsBase {

	private static Logger log = LoggerFactory.getLogger(TemplateAllTestsBase.class);

	protected String file;

	public TemplateAllTestsBase (String file) {
		this.file = file;
	}
	
	// Helper test functions

	public static void templateTestPrintString (Function<String, String> templateFunc, String templatePath, String suffix) {
		assumeTrue(ParserUtils.maybeTemplate(templatePath));
		String fileContent = templateFunc.apply(templatePath);
		Utils.printFile(fileContent, templatePath, suffix + Utils.outFileSuffix);
	}
	public static void templateTestPrintModel (Function<String, Model> templateFunc, String templatePath, String suffix) throws ModelIOException {
		assumeTrue(ParserUtils.maybeTemplate(templatePath));
		Model model = templateFunc.apply(templatePath);
		Utils.printModel(model, templatePath, suffix + Utils.outFileSuffix);
	}
	public static void modelTestPrintModel (Function<String, Model> templateFunc, String templatePath, String suffix) throws ModelIOException {
		Model model = templateFunc.apply(templatePath);
		Utils.printModel(model, templatePath, suffix + Utils.outFileSuffix);
	}

	// Query tests
	
	@Test public void shouldQueryUpdateLowering () throws ParserException, IOException {
		templateTestPrintString(s -> TemplateQueries.getLoweringUpdateQuery(s).toString(), file, ".update-lowering"); }
	@Test public void shouldQueryUpdateLifting () throws ParserException, IOException {
		templateTestPrintString(s -> TemplateQueries.getLiftingUpdateQuery(s).toString(), file, ".update-Lifting"); }

	@Test public void shouldQueryConstructLowering () throws ParserException, IOException {
		templateTestPrintString(s -> TemplateQueries.getLoweringConstructQuery(s).serialize(), file, ".construct-lowering"); }
	@Test public void shouldQueryConstructLifting () throws ParserException, IOException {
		templateTestPrintString(s -> TemplateQueries.getLiftingConstructQuery(s).serialize(), file, ".construct-Lifting"); }

	@Test public void shouldQuerySelectBody () throws ParserException, IOException {
		templateTestPrintString(s -> TemplateQueries.getBodySelectQuery(s).serialize(), file, ".select-body"); }
	@Test public void shouldQuerySelectHead () throws ParserException, IOException {
		templateTestPrintString(s -> TemplateQueries.getHeadSelectQuery(s).serialize(), file, ".select-head"); }

	// Expansion tests
	
	@Test public void shouldExpandTemplate () throws ParserException, ModelIOException {
		templateTestPrintModel(s -> Expander.expandTemplate(s), file, ".expandTemplate"); }
	@Test public void shouldTemplateInstance () throws ParserException, ModelIOException {
		templateTestPrintModel(s -> TemplateLoader.getTemplate(s).getInstance(), file, ".instance"); }
	@Test public void shouldExpand () throws ParserException, ModelIOException {
		templateTestPrintModel(s -> Expander.expand(s), file, ".expandModel"); }
	@Test public void shouldTemplateBody () throws ParserException, ModelIOException {
		templateTestPrintModel(s -> Expander.expand(TemplateLoader.getTemplate(s).getBody(), ExpanderSettings.BODY), file, ".body"); }
	@Test public void shouldTemplateHead () throws ParserException, ModelIOException {
		templateTestPrintModel(s -> Expander.expand(TemplateLoader.getTemplate(s).getHead(), ExpanderSettings.HEAD), file, ".head"); }

	// XML tests
	
	@Test public void shouldXSDInstanceFormat () throws ParserException, IOException, TransformerException, SAXException {
		templateTestPrintString(s -> {
			try { return TemplateXSD.getXSDString(s); } 
			catch (TransformerException | SAXException e) { e.printStackTrace(); }
			return null;
		}, file, ".xsd-format"); }
	
	@Test public void shouldXMLInstanceSample () throws ParserException, IOException, TransformerException {
		templateTestPrintString(s -> {
			try { return TemplateXMLSample.getXMLString(s); } 
			catch (TransformerException e) { e.printStackTrace(); }
			return null;
		}, file, ".xml-sample"); }


	@AfterClass
	public static void clear () throws IOException {
		log.info("AfterClass: clearing template loader cache");
		TemplateLoader.clearCache();
		log.info("AfterClass: DONE clearing template loader cache");
	}
}
