package xyz.lutra;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.jena.rdf.model.Model;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;
import xyz.lutra.parser.ParserUtils;		

public class Utils {

	public final static boolean isLocal = false;

	public final static String outFileSuffix = ".out";

	public static void printModel (Model model, String file, String suffix) throws ModelIOException {
		ModelIO.format format = ParserUtils.maybeOntology(model) ? ModelIO.format.OWL : ModelIO.format.TURTLE; 
		printFile(ModelIO.writeModel(model, format), file, suffix);
	}

	public static void printFile (String content, String file, String suffix) {
		if (isLocal) {
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file + suffix), "utf-8"))) {
				writer.write(content);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//System.out.println(content);
		}
	}

	// not . or # 
	private static IOFileFilter hiddenFiles = new NotFileFilter(FileFilterUtils.or(new PrefixFileFilter("."),new PrefixFileFilter("#"))); 

	public final static Collection<File> getFolderContents (String folder) throws IOException {
		return FileUtils.listFiles( 
				new File(folder), 
				hiddenFiles,
				hiddenFiles
				);
	}
	public static Collection<String[]> getParameterisedTestInput (String folder) throws IOException {
		return getParameterisedTestInput(folder, s -> true);
	}

	public static List<String[]> getParameterisedTestInput (String folder, Predicate<String> test) throws IOException {
		List<String[]> params = new ArrayList<>();
		for (File file : new TreeSet<File>(Utils.getFolderContents(folder))) {
			String path = file.toString();
			if (!path.endsWith(outFileSuffix) && test.test(path)) {
				params.add(new String[] { path });
			}
		}
		return params;
	}

	public static Collection<String[]> toArgCollection (String[] args) throws IOException {
		Collection<String[]> data = new ArrayList<>();
		for (String s : args) {
			data.add(new String[] {s});
		}
		return data;
	}

	/*
	private final static void createFolder (String folder) {
		new File(folder).mkdirs();
	}
	private final static boolean isFolder (String folder) {
		return new File(folder).isDirectory();
	}
	*/
}
