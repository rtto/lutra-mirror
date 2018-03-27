package xyz.lutra.parser;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelSelector;
import osl.util.rdf.ModelSelectorException;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.Cache;
import xyz.lutra.Settings;
import xyz.lutra.model.Template;

public class TemplateLoader {

	private static Logger log = LoggerFactory.getLogger(TemplateLoader.class);

	private static Cache<Model> loaderCache = new Cache<Model>(
			iri -> ModelIO.readModel(iri));
	
	private static Cache<Template> parserCache = new Cache<Template> (
			Settings.enableTempalteParserCache, 
			iri -> new TemplateParser(iri, TemplateLoader.getTemplateModel(iri)).parse()
			);

	public static void clearCache () {
		loaderCache.clear();
		parserCache.clear();
	}
	
	public static Template getTemplate (String iri) throws ParserException {
		return parserCache.get(iri);
	}	

	public static Model getTemplateModel (String iri) { 
		return loaderCache.get(iri);
	}

	public static void load (String... templates) {
		for (String path : templates) {
			try {
				log.info("Reading model: " + path);
				Model model = ModelIO.readModel(path);
				FilenameUtils.getName(path);
				Resource template = ModelSelector.getRequiredInstanceOfClass(model, Templates.Template);
				if (!FilenameUtils.getName(path).equals(FilenameUtils.getName(template.getURI()))) {
					log.warn("Template filename: '" + path + "' is different from template name: '" + template.getURI() + '"');
				}
				loaderCache.put(template.getURI(), model);
				log.info("Loaded template: " + template.getURI());
			} catch (ModelSelectorException ex) {
				log.warn("Error loading template from " + path + ". " + ex.getMessage());
			}
		}
	}

	public static void loadFolder (String folder, String[] includeExtensions, String[] excludeExtensions) throws IOException {
		for (File file : getFolderContents(folder, includeExtensions, excludeExtensions)) {
			load(file.getPath());
		}
	}

	// TODO is this correct
	private static IOFileFilter hiddenFiles = new NotFileFilter(FileFilterUtils.or(new PrefixFileFilter("."),new PrefixFileFilter("#"))); 
	private static Function<String, IOFileFilter> extFilter = string -> FileFilterUtils.suffixFileFilter(string, IOCase.INSENSITIVE);

	private static Collection<File> getFolderContents (String folder, String[] includeExtensions, String[] excludeExtensions) throws IOException {

		IOFileFilter ext = null;

		for (int i = 0; i < includeExtensions.length; i += 1) {
			if (i == 0 && ext == null) {
				ext = extFilter.apply(includeExtensions[i]);		
			}
			ext = FileFilterUtils.or(ext, extFilter.apply(includeExtensions[i]));
		}

		for (int i = 0; i < excludeExtensions.length; i += 1) {
			if (i == 0 && ext == null) {
				ext = FileFilterUtils.notFileFilter(extFilter.apply(includeExtensions[i]));		
			} else {
				ext = FileFilterUtils.and(ext, FileFilterUtils.notFileFilter(extFilter.apply(excludeExtensions[i])));
			}
		}

		if (ext == null) {
			ext = FileFilterUtils.trueFileFilter();
		}

		return FileUtils.listFiles( 
				new File(folder), 
				FileFilterUtils.and(hiddenFiles, ext),
				hiddenFiles
				);
	}
}
