package xyz.lutra.cli;

import java.io.IOException;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Level;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import xyz.lutra.Expander;
import xyz.lutra.Settings;
import xyz.lutra.stOTTRWriter;
import xyz.lutra.model.Template;
import xyz.lutra.parser.ParserUtils;
import xyz.lutra.parser.TemplateLoader;
import xyz.lutra.parser.TemplateParser;
import xyz.lutra.tabottr.Table;
import xyz.lutra.tabottr.io.TableReader;
import xyz.lutra.tabottr.io.excel.ExcelReader;
import xyz.lutra.tabottr.io.rdf.RDFWriter;
import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIO.format;
import osl.util.rdf.ModelIOException;
import osl.util.rdf.Models;

public class CLI {

	private final static char valueSep = ',';

	private final static String libExtDefaults = String.join(valueSep+"", "owl", "rdf", "ttl");
	
	private final static String name = "lutra";
	
	private final static String 
	version = "version",
	libFolder = "lib", 
	libExt = "libExt",
	expand = "expand",
	stottr = "stottr",
	tabottr = "tabottr",
	//lift = "lift",
	//lower = "lower",
	out = "out",
	in = "in",	
	quiet = "quiet",
	noValidation = "noCheck",
	noOWLOutput = "noOWLOutput",
	noCache = "noCache";

	private final static String[] operations = { version, expand, stottr, tabottr }; //, lift, lower }; 

	public static void main (String... args) throws IOException, ModelIOException, InvalidFormatException {
		CommandLineParser parser = new DefaultParser();
		Options options = buildOptions();

		// Handle all options
		try {
			CommandLine line = parser.parse(options, args);
			
			if (line.hasOption(version)) {
				System.out.println(name + " " + CLI.class.getPackage().getImplementationVersion());
				System.exit(0);
			}

			// PREPARATIONS
			
			if (line.hasOption(noValidation)) {
				Settings.enableValidation(false);
			}

			if (line.hasOption(noCache)) {
				Settings.enableCache(false);
			}

			if (line.hasOption(quiet)) {
				org.apache.log4j.Logger rootLogger = org.apache.log4j.Logger.getRootLogger();
				rootLogger.setLevel(Level.OFF);
			}

			// READ INPUT
			
			if (line.hasOption(libFolder)) {
				String path = line.getOptionValue(libFolder);
				String ext = line.getOptionValue(libExt, libExtDefaults);
				loadLibrary (path, ext.split(","), new String[] {});				
			}
			
			Model inModel;
			String inString;
			if (line.hasOption(in)) {
				inString = line.getOptionValue(in);
				inModel = ModelIO.readModel(inString);
			} else {
				inString = "stdin";
				inModel = Models.empty();
				inModel.read(System.in, null, ModelIO.format.TURTLE.toString());
			}
			
			// PROCESS INPUT

			String output = "";
			if (line.hasOption(expand)) {
				Model model = Expander.expand(inModel);
				//String format = line.getOptionValue(libFolder);
				
				output = ModelIO.writeModel(model, getModelFormat(line, model));	
			} 
			else if (line.hasOption(stottr)) {
				TemplateParser tParser = new TemplateParser(inString, inModel);
				Template template = tParser.parse();
				stOTTRWriter writer = new stOTTRWriter();
				output = writer.print(template);
			} 
			else if (line.hasOption(tabottr)) {
				TableReader tabparser = new ExcelReader(inString);
				List<Table> tables = tabparser.getTables();
				RDFWriter writer = new RDFWriter();
				writer.process(tables);
				Model model = writer.write();
				model = Expander.expand(model);
				output = ModelIO.writeModel(model, getModelFormat(line, model));
			}
			
			
			// OUTPUT
			
			if (line.hasOption(out)) {
				Utils.printFile(output,line.getOptionValue(out)); 
			} else {
				System.out.println(output);
			}

		}
		catch( ParseException exp ) {
			System.out.println( "Unexpected exception: " + exp.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(name + " -" +String.join("|",  operations)+ " [-in <file|IRI>] [-" +out+ " <file>] [-" +libFolder+ " <path> [-" +libExt+ " <ext,..>]] [(flags)]", options );
		}
	}
	
	private static ModelIO.format getModelFormat (CommandLine line, Model model) {
		format modelFormat = (!line.hasOption(noOWLOutput) && ParserUtils.maybeOntology(model)) ? ModelIO.format.OWL : ModelIO.format.TURTLE;
		return modelFormat;
	}

	private static Options buildOptions () {
		Options options = new Options();
		options.addOption(buildOption(version, false, 0, "Prints version number."));
		options.addOption(buildOption(libFolder, false, 1, "Path to optional local template library root, read recursively."));
		options.addOption(buildOption(libExt, false, -1, "Comma-separated list of template file extensions to be read from '" +libFolder+ "' (default: " +libExtDefaults+ ")."));
		options.addOption(buildOption(out, false, 1, "Path to optional output file. Defaults to stout."));
		options.addOption(buildOption(in, false, 1, "Path to optional input file or IRI. Defaults to stdin."));
		options.addOption(buildOption(noValidation, false, 0, "Disable various validation services, gives faster processing."));
		options.addOption(buildOption(noOWLOutput, false, 0, "Disable OWL API rendering of output."));
		options.addOption(buildOption(noCache, false, 0, "Disable caching."));
		options.addOption(buildOption(quiet, false, 0, "Disable logging."));


		OptionGroup operationsGroup = new OptionGroup();
		for (String op : operations) {
			operationsGroup.addOption(buildOption(op, false, 0, "Enable " + op + " mode."));
		}
		operationsGroup.setRequired(true);
		options.addOptionGroup(operationsGroup);
		return options;
	}

	private static Option buildOption (String argName, boolean required, int noArgs, String description) {
		Builder ob = Option.builder(argName).required(required).desc(description);
		if (noArgs < 0) {
			ob.hasArgs();
		} else {
			ob.numberOfArgs(noArgs);
			ob.valueSeparator(valueSep);
		}
		return ob.build();
	}

	private static void loadLibrary (String folder, String[] includeExtensions, String[] excludeExtensions) throws IOException {
		TemplateLoader.loadFolder(folder, includeExtensions, excludeExtensions);
	}
}

