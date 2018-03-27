package xyz.lutra;

import java.io.IOException;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xyz.lutra.Expander;
import xyz.lutra.ExpanderException;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.TemplateLoader;

@RunWith(Parameterized.class)
public class AllTemplateExpansionErrorsTest {

	private static final String inFolderRoot = "src/test/resources/test/error/expansion";

	private static final String ROOT = "http://test.ottr.xyz/error/expansion/";

	private static final String[] tests = {
			ROOT + "cycletest1.ttl",
			ROOT + "cycletest2a.ttl",
			ROOT + "cycletest2b.ttl",
			ROOT + "cycletest3.ttl" } ;

	@Parameters(name = "{index}: {0}")
	public static Collection<String[]> data () throws IOException {
		if (!Utils.isLocal) {
			return Utils.toArgCollection(tests);
		} else {
			return Utils.getParameterisedTestInput(inFolderRoot);
		}
	}

	@BeforeClass	
	public static void load () throws IOException {
		if (Utils.isLocal) {
			TemplateLoader.loadFolder(inFolderRoot, new String []{"ttl"}, new String []{});
		}
	}

	private String file; 

	public AllTemplateExpansionErrorsTest (String file) {
		this.file = file;
	}


	@AfterClass
	public static void clear () throws IOException {
		TemplateLoader.clearCache();
	}

	@Test
	public void shouldParse () throws ParserException {
		TemplateLoader.getTemplate(file);
	}

	@Test(expected = ExpanderException.class)
	public void shouldNotExpand () throws ParserException, ExpanderException, IOException {
		Expander.expandTemplate(file);
	}
}
