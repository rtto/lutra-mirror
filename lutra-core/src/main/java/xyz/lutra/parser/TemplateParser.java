package xyz.lutra.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import osl.util.Strings;
import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelSelector;
import osl.util.rdf.ModelSelectorException;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.model.IndexedValueResource;
import xyz.lutra.model.Parameter;
import xyz.lutra.model.Template;

public class TemplateParser {

	private String uri;
	private Model source, canonical;

	public TemplateParser (String uri, Model model) {
		this.uri = uri;
		this.source = model;
		this.canonical = ParserUtils.getCanonicalModel(model);
	}

	public Template parse () throws ParserException {
		Template parsedTemplate;
		try {
			ParserUtils.isValidTemplate(source, canonical);
			Resource template = ModelSelector.getRequiredInstanceOfClass(canonical, Templates.Template);
			parsedTemplate = parseTemplate(template);
		} catch (ParserException | ModelSelectorException ex) {
			throw new ParserException("Error parsing template in " + canonical.shortForm(uri) + ". " + ex.getMessage());
		}
		return parsedTemplate;
	}

	private Template parseTemplate (Resource template) throws ParserException {
		List<Resource> parameters = ModelSelector.listResourcesOfProperty(canonical, template, Templates.hasParameter);
		int noParameters = parameters.size();
		List<Parameter> parsedParameters = new ArrayList<>();
		for (Resource r : parameters) {
			parsedParameters.add(parseParameter(r, noParameters));
		}
		Collections.sort(parsedParameters, IndexedValueResource.indexComparator);

		// Parameter indices must be 1, 2, 3, ...
		{for (int i = 0; i < noParameters; i += 1) {
			int indexValue = parsedParameters.get(i).getIndex();
			if (indexValue != i+1) {
				// Collect all indices. 
				String indices = Strings.toString(parsedParameters, p -> p.getIndex(), ", ");

				throw new ParserException("Error parsing parameters; indices are not consecutively numbered: [",
						indices, "]. Expected index ", (i+1), ", but found index: ", indexValue, ".");
			}
		}}

		// all parameter variables must be unique
		{TreeSet<Parameter> uniques = new TreeSet<>(IndexedValueResource.stringValueComparator);
		uniques.addAll(parsedParameters);
		if (uniques.size() != parsedParameters.size()) {
			TreeSet<Parameter> duplicates = new TreeSet<>(IndexedValueResource.indexComparator);
			duplicates.addAll(parsedParameters);
			duplicates.removeAll(uniques);
			throw new ParserException("Error parsing parameters; variables are not unique. ",
					"The following parameters have variables used by other parameters: ",
					duplicates);
		}}

		return new Template(canonical, template, parsedParameters);
	}

	private Parameter parseParameter (Resource parameter, int maxValue) throws ParserException {
		Literal index;
		Property type;
		RDFNode variable;
		Literal optional;
		try {
			// Must have one index, where 0 < index <= maxValue:
			index = ModelSelector.getRequiredLiteralOfProperty(canonical, parameter, Templates.index);
			optional = ModelSelector.getOptionalLiteralOfProperty(canonical, parameter, Templates.optional);
			// Must have a variable:
			Statement varAssignment = ModelSelector.getOptionalStatementWithProperties(canonical, parameter, Templates.ALL_variable);
			type = varAssignment.getPredicate();
			variable = varAssignment.getObject();
		} catch (ModelSelectorException ex) {
			throw new ParserException("Error parsing parameter. " + ex.getMessage()); 
		}

		int indexValue = NumberUtils.toInt(index.getLexicalForm());
		if (1 > indexValue || indexValue > maxValue) {
			throw new ParserException("Error parsing parameter index for parameter " + ModelIO.shortForm(parameter) 
			+ ". Expected index value = 0 < [index] < " + (maxValue + 1) 
			+ ", but found index value = " + indexValue + ".");
		}

		try {
			ParserUtils.checkParameterType(type, variable);
		} catch (IllegalArgumentException ex) {
			throw new ParserException("Error parsing parameter " + ModelIO.shortForm(parameter), ". ", ex.getMessage()); 
		}

		if (optional == null) {
			return new Parameter(parameter, indexValue, variable, type);
		} else {
			return new Parameter(parameter, indexValue, variable, type, optional.getBoolean());
		}
	}

	
}

