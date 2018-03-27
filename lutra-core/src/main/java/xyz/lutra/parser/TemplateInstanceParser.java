package xyz.lutra.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import osl.util.rdf.ModelIO;
import osl.util.rdf.ModelSelector;
import osl.util.rdf.ModelSelectorException;
import osl.util.rdf.vocab.Templates;
import xyz.lutra.model.Argument;
import xyz.lutra.model.MulitValueArgument;
import xyz.lutra.model.Template;
import xyz.lutra.model.TemplateInstance;

public class TemplateInstanceParser {

	private static Logger log = LoggerFactory.getLogger(TemplateInstanceParser.class);

	private Model source, canonical;

	public TemplateInstanceParser (Model model) {
		this.source = model;
		this.canonical= ParserUtils.getCanonicalModel(model);
	}

	public static Set<TemplateInstance> getTemplateInstances (Model model) throws ParserException {
		return new TemplateInstanceParser(model).parse();
	}

	public Set<TemplateInstance> parse () throws ParserException {
		ParserUtils.isValidTemplate(source, canonical);
		return parseTemplateInstances();
	}

	private Set<TemplateInstance> parseTemplateInstances () throws ParserException {

		// EDIT: We no longer require template instances to be typed as such, instead they are located by ottr:templateRef.
		// List<Resource> templateInstances = ModelSelector.listInstancesOfClass(canonical, Templates.TemplateInstance);
		List<Resource> templateInstances = ModelSelector.listResourcesWithProperty(canonical, Templates.templateRef);

		Set<TemplateInstance> parsedTemplateInstances = new HashSet<>();
		for (Resource r: templateInstances) {
			TemplateInstance parsedTemplateInstance;
			try {
				parsedTemplateInstance = parseTemplateInstance(r);
			}
			catch (ParserException ex) {
				String errorMessage = "Error parsing template instance '" + r.toString() + "'. " + ex.getMessage();
				log.error(errorMessage);
				throw new ParserException(errorMessage);
			}
			parsedTemplateInstances.add(parsedTemplateInstance);
		}
		return parsedTemplateInstances;
	}

	private TemplateInstance parseTemplateInstance (Resource templateInstance) throws ParserException {
		Resource templateRef;
		try {
			// Must have one templateRef:
			templateRef = ModelSelector.getRequiredResourceOfProperty(canonical, templateInstance, Templates.templateRef);
		} catch (ModelSelectorException ex) {
			throw new ParserException ("Error parsing template reference: " + ex.getMessage());
		}
		Template template = null;
		try {
			template = TemplateLoader.getTemplate(templateRef.getURI());
		} catch (Exception ex) {
			throw new ParserException ("Error parsing template reference: '" 
					+ canonical.shortForm(templateRef.getURI()) + "' " + ex.getMessage());
		}
		
		Resource status = ModelSelector.getOptionalResourceOfProperty(canonical, templateInstance, Templates.status);

		// Get arguments:
		List<Resource> arguments = ModelSelector.listResourcesOfProperty(canonical, templateInstance, Templates.hasArgument);

		// TODO check that indices are correctly numbered. DRY code from TemplateParser. perhaps supertype of Argument and Parameter?
		int noParameters = template.getParameters().size();
		Argument[] _parsedArguments = new Argument[noParameters];
		for (Resource r : arguments) {
			Argument arg = parseArgument(r);
			int index = arg.getIndex();

			if (_parsedArguments[index-1] != null) {
				throw new ParserException("Error: ill-indexed arguments for template '"
						+ canonical.shortForm(template.getIRI().getURI()) 
						+ "', contains multiple parameters with index " + (index));
			}
			if (index < 1 || index > noParameters) {
				throw new ParserException("Error: argument index out of bounds. Template '"
						+ canonical.shortForm(template.getIRI().getURI()) 
						+ "' specifies " + noParameters 
						+ ", but instance contains argument with index " + index);
			}
			_parsedArguments[index-1] = arg;
		}

		List<Argument> parsedArguments = Arrays.asList(_parsedArguments);

		// check if all indexes are filled:
		int nullIndex = parsedArguments.indexOf(null);
		if (nullIndex != -1) {
			throw new ParserException("Error: missing argument. Template '"
					+ canonical.shortForm(template.getIRI().getURI()) 
					+ "' specifies " + noParameters 
					+ ", but instance contains no argument with index " + (nullIndex+1));
		}
		return new TemplateInstance(templateInstance, templateRef, status, parsedArguments);
	}

	// TODO change return type to Set<Argument> or let Argument be a set of values.
	private Argument parseArgument (Resource argument) throws ParserException {
		Literal index;
		RDFNode value = null;
		Property valueType = null;
		Literal optional;
		try {
			// TODO: check that argument is of correct RDF resource type.
			// ParserUtils.isNonListRDFResource(argument);

			// Must have an index:
			index = ModelSelector.getRequiredLiteralOfProperty(canonical, argument, Templates.index);
			optional = ModelSelector.getOptionalLiteralOfProperty(canonical, argument, Templates.optional);
			// Might be an optional, thus might not have a value:
			Collection<Property> valueProperties = Arrays.asList(new Property [] { Templates.value, Templates.eachValue });
			Statement valueStatement = ModelSelector.getOptionalStatementWithProperties(canonical, argument, valueProperties);
			if (valueStatement != null) {
				valueType = valueStatement.getPredicate();
				value = valueStatement.getObject();
			} else {
                value = Templates.none;
            }
		} catch (ModelSelectorException ex) {
			throw new ParserException("Error parsing parameter. " + ex.getMessage());
		}

		// Index must be > 0:
		int indexValue = NumberUtils.toInt(index.getLexicalForm());
		if (1 > indexValue) {
			throw new ParserException("Error parsing parameter index. Expected 0 < index for parameter " 
					+ canonical.shortForm(argument.getURI()) + ", but found index: " + indexValue + ".");
		}

		Argument arg;
		if (valueType != null && valueType.equals(Templates.eachValue)) {
			if (! value.canAs(RDFList.class)) {
				throw new ParserException("Error parsing parameter index. Expected list value for " + Templates.eachValue.getLocalName() + " parameter " 
						+ canonical.shortForm(argument.getURI()) + ", but found value: " + ModelIO.shortForm(value) + ".");
			}
			arg = new MulitValueArgument (argument, indexValue, value.as(RDFList.class));
		} else {
			arg = new Argument(argument, indexValue, value);
		}
		
		if (optional != null) {
			arg.setOptional(optional.getBoolean());
		}

		return arg;
	}
}
