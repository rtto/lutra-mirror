package xyz.lutra.cli;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelIOException;

public class Utils {
	
	/*
	public static void printModel (Model model, String file) throws ModelIOException {
		try {
			FileUtils.writeStringToFile(new File(file), ModelIO.writeModel(model, ModelIO.format.TURTLE), "utf-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	public static void printFile (String content, String file) {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"))) {
			writer.write(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isFolder (String folder) {
		return new File(folder).isDirectory();
	}
	
	public static boolean isFile(String folder) {
		return new File(folder).isFile();
	}
}
