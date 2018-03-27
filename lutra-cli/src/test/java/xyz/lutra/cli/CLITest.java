package xyz.lutra.cli;

import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import osl.util.rdf.ModelIOException;
import xyz.lutra.cli.CLI;
import xyz.lutra.cli.Utils;

import static org.junit.Assume.assumeTrue;

public class CLITest {

	private String resources = "../osl-templates-core/src/test/resources/";

	@Test
	public void testNonsense() throws IOException, ModelIOException, InvalidFormatException {
		CLI.main("nonsense".split(" "));
	}

	@Test
	public void shouldExpandSingleLocal() throws IOException, ModelIOException, InvalidFormatException {
		assumeTrue(Utils.isFolder(resources));
		String file = resources+ "test/test1/templatePipe.ttl";
		assumeTrue(Utils.isFile(file));
		
		String command = "-expand -in " + file + " -out templatePipe.test";
		CLI.main(command.split(" "));
	}
	
	@Test
	public void shouldExpandSingleRemote() throws IOException, ModelIOException, InvalidFormatException {
		String command = "-expand -in http://candidate.ottr.xyz/owl/axiom/EquivAllValuesFrom";
		CLI.main(command.split(" "));
	}
	
	@Test
	public void shouldSTOTTRSingleRemote() throws IOException, ModelIOException, InvalidFormatException {
		String command = "-stottr -in http://candidate.ottr.xyz/owl/axiom/EquivAllValuesFrom";
		CLI.main(command.split(" "));
	}
	
	@Test
	public void shouldExpandSingleRemoteOFF() throws IOException, ModelIOException, InvalidFormatException {
		String command = "-expand -in http://candidate.ottr.xyz/owl/axiom/EquivAllValuesFrom -noCheck";
		CLI.main(command.split(" "));
	}
	
	@Test
	public void shouldTestGeneratedInstances () throws IOException, ModelIOException, InvalidFormatException {
		String command = "-expand -in src/test/resources/generated-test.ttl -noCheck";
		CLI.main(command.split(" "));
	}
	
	/*
	@Test
	public void shouldLiftSingleRemote() throws IOException, ModelIOException {
		String command = "-lift https://ottr.gitlab.io/template/owl/axiom/EquivAllValuesFrom";
		CLI.main(command.split(" "));
	}
	
	@Test
	public void shouldLowerSingleRemote() throws IOException, ModelIOException {
		String command = "-lower https://ottr.gitlab.io/template/owl/axiom/EquivAllValuesFrom";
		CLI.main(command.split(" "));
	}*/

	@Test
	public void shouldExpandWithLibrary() throws IOException, ModelIOException, InvalidFormatException {
		String folder = resources + "draft/p12/";
		String file = folder + "HammerExample.ttl";
		assumeTrue(Utils.isFolder(folder));
		assumeTrue(Utils.isFile(file));
		
		String command = "-expand -in " +file+ " -lib " +folder+ " -out "+folder+"HammerExample.test";
		CLI.main(command.split(" "));
	}
	
}
