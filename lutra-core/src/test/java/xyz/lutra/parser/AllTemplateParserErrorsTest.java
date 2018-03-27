package xyz.lutra.parser;

import java.io.IOException;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import xyz.lutra.Utils;
import xyz.lutra.parser.ParserException;
import xyz.lutra.parser.TemplateLoader;

@RunWith(Parameterized.class)
public class AllTemplateParserErrorsTest {

	private static final String inFolderRoot = "src/test/resources/test/error/parser";

	private static final String ROOT = "http://test.ottr.xyz/error/parser/";

	private static final String[] tests = {
			ROOT + "p1-1.ttl",
			ROOT + "p2-1.ttl",
			ROOT + "p2-2.ttl",
			ROOT + "p3-1.ttl",
			ROOT + "p3-3.ttl",
			ROOT + "p3-4.ttl",
			ROOT + "p3-5.ttl",
			ROOT + "p3-6.ttl",
			ROOT + "p4-1.ttl",
			ROOT + "p4-2.ttl",
			ROOT + "p4-3.ttl",
			ROOT + "p4-4.ttl",
			ROOT + "p4-5.ttl",
			ROOT + "p5-1.ttl",
			ROOT + "p5-2.ttl" };

	@Parameters(name = "{index}: {0}")
	public static Collection<String[]> data () throws IOException {
		if (!Utils.isLocal) {
		return Utils.toArgCollection(tests);
		} else {
			return Utils.getParameterisedTestInput(inFolderRoot);
		}
	}

	private String file;

	public AllTemplateParserErrorsTest (String file) {
		this.file = file;
	}

	@AfterClass
	public static void clear () throws IOException {
		TemplateLoader.clearCache();
	}

	@Test(expected = ParserException.class)
	public void shouldNotParse () throws ParserException, IOException {
		TemplateLoader.getTemplate(file);
	}
}
