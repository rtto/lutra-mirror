package xyz.lutra;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.lutra.parser.TemplateLoader;

@RunWith(Parameterized.class)
public class AllCandidateTemplatesTest extends TemplateAllTestsBase {

	private static Logger log = LoggerFactory.getLogger(AllCandidateTemplatesTest.class);

	private static String ROOT = "http://candidate.ottr.xyz/",
			ATOM =  ROOT + "owl/atom/",
			AXIOM = ROOT + "owl/axiom/",
			MACRO = ROOT + "owl/macro/",
			REST =  ROOT + "owl/restriction/";

	private static final String[] tests = {
			ATOM + "Cardinality", 
			ATOM + "DataCardinality", 
			ATOM + "ListRelation", 
			ATOM + "ObjectCardinality", 
			ATOM + "TypedListRelation", 
			ATOM + "ValueRestriction", 

			AXIOM + "DifferentIndividuals",
			AXIOM + "DisjointClasses",
			AXIOM + "DisjointProperties",
			AXIOM + "DisjointUnion",
			AXIOM + "EquivAllValuesFrom",
			AXIOM + "EquivDataAllValuesFrom",
			AXIOM + "EquivDataExactCardinality",
			AXIOM + "EquivDataHasValue",
			AXIOM + "EquivDataMaxCardinality",
			AXIOM + "EquivDataMinCardinality",
			AXIOM + "EquivDataSomeValuesFrom",
			AXIOM + "EquivExactCardinality",
			AXIOM + "EquivHasValue",
			AXIOM + "EquivMaxCardinality",
			AXIOM + "EquivMinCardinality",
			AXIOM + "EquivObjectAllValuesFrom",
			AXIOM + "EquivObjectExactCardinality",
			AXIOM + "EquivObjectHasValue",
			AXIOM + "EquivObjectIntersectionOf",
			AXIOM + "EquivObjectMaxCardinality",
			AXIOM + "EquivObjectMinCardinality",
			AXIOM + "EquivObjectOneOf",
			AXIOM + "EquivObjectSomeValuesFrom",
			AXIOM + "EquivObjectUnionOf",
			AXIOM + "EquivSomeValuesFrom",
			AXIOM + "HasKey",
			AXIOM + "NegativeDataPropertyAssertion",
			AXIOM + "NegativeObjectPropertyAssertion",
			AXIOM + "SubAllValuesFrom",
			AXIOM + "SubDataAllValuesFrom",
			AXIOM + "SubDataExactCardinality",
			AXIOM + "SubDataHasValue",
			AXIOM + "SubDataMaxCardinality",
			AXIOM + "SubDataMinCardinality",
			AXIOM + "SubDataSomeValuesFrom",
			AXIOM + "SubExactCardinality",
			AXIOM + "SubHasValue",
			AXIOM + "SubMaxCardinality",
			AXIOM + "SubMinCardinality",
			AXIOM + "SubObjectAllValuesFrom",
			AXIOM + "SubObjectExactCardinality",
			AXIOM + "SubObjectHasValue",
			AXIOM + "SubObjectIntersectionOf",
			AXIOM + "SubObjectMaxCardinality",
			AXIOM + "SubObjectMinCardinality",
			AXIOM + "SubObjectOneOf",
			AXIOM + "SubObjectPropertyOfChain",
			AXIOM + "SubObjectSomeValuesFrom",
			AXIOM + "SubObjectUnionOf",
			AXIOM + "SubSomeValuesFrom",
			AXIOM + "SuperAllValuesFrom",
			AXIOM + "SuperDataAllValuesFrom",
			AXIOM + "SuperDataExactCardinality",
			AXIOM + "SuperDataHasValue",
			AXIOM + "SuperDataMaxCardinality",
			AXIOM + "SuperDataMinCardinality",
			AXIOM + "SuperDataSomeValuesFrom",
			AXIOM + "SuperExactCardinality",
			AXIOM + "SuperHasValue",
			AXIOM + "SuperMaxCardinality",
			AXIOM + "SuperMinCardinality",
			AXIOM + "SuperObjectAllValuesFrom",
			AXIOM + "SuperObjectExactCardinality",
			AXIOM + "SuperObjectHasValue",
			AXIOM + "SuperObjectIntersectionOf",
			AXIOM + "SuperObjectMaxCardinality",
			AXIOM + "SuperObjectMinCardinality",
			AXIOM + "SuperObjectOneOf",
			AXIOM + "SuperObjectSomeValuesFrom",
			AXIOM + "SuperObjectUnionOf",
			AXIOM + "SuperSomeValuesFrom",

			MACRO + "DomainRange",
			MACRO + "ScopedDomain",
			MACRO + "ScopedDomainRange",
			MACRO + "ScopedRange",

			REST + "AllValuesFrom",
			REST + "DataAllValuesFrom",
			REST + "DataExactCardinality",
			REST + "DataHasValue",
			REST + "DataIntersectionOf",
			REST + "DataMaxCardinality",
			REST + "DataMinCardinality",
			REST + "DataOneOf",
			REST + "DataSomeValuesFrom",
			REST + "DataUnionOf",
			REST + "ExactCardinality",
			REST + "HasValue",
			REST + "MaxCardinality",
			REST + "MinCardinality",
			REST + "ObjectAllValuesFrom",
			REST + "ObjectExactCardinality",
			REST + "ObjectHasValue",
			REST + "ObjectIntersectionOf",
			REST + "ObjectMaxCardinality",
			REST + "ObjectMinCardinality",
			REST + "ObjectOneOf",
			REST + "ObjectSomeValuesFrom",
			REST + "ObjectUnionOf",
			REST + "SomeValuesFrom" };

	private static final String inFolderRoot = "src/test/resources/";

	@Parameters(name = "{index}: {0}")
	public static Collection<String[]> data () throws IOException {
		if (!Utils.isLocal) {
			return Utils.toArgCollection(tests);
		} else {
			Predicate<String> filter = name -> !name.endsWith(".out")
					&& !name.endsWith("README")
					&& !name.endsWith("LICENSE")
					&& !name.endsWith(".sh"); 
			List<String[]> params = Utils.getParameterisedTestInput(inFolderRoot + "test", name -> !name.startsWith(inFolderRoot + "test/error") && filter.test(name));
			params.addAll(Utils.getParameterisedTestInput(inFolderRoot + "draft", filter));
			params.addAll(Utils.getParameterisedTestInput(inFolderRoot + "candidate", filter));
			return params;
		}
	}
	@BeforeClass
	public static void load () throws IOException {
		log.info("BeforeClass: loading templates");
		for (String[] arg : data()) {
			TemplateLoader.load(arg[0]);
		}
		log.info("BeforeClass: DONE Loading templates");
	}

	public AllCandidateTemplatesTest (String file) {
		super(file);
	}

	
}
