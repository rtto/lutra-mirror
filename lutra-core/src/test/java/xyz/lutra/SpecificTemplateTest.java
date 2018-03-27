package xyz.lutra;

import java.io.IOException;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SpecificTemplateTest extends TemplateAllTestsBase {

	public SpecificTemplateTest(String file) {
		super(file);
	}

	private static String root = "../../"; // Root for gitlab repos

	@Parameters(name = "{index}: {0}")
	public static Collection<String[]> data () throws IOException {
		return Utils.toArgCollection(new String[] { 
				//root + "draft/pizza/NamedPizza" 
				//root + "candidate/owl/axiom/DisjointClasses"				
		} );
	}

}
