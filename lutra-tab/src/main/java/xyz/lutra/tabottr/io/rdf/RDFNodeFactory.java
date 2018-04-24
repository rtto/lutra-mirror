package xyz.lutra.tabottr.io.rdf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.XSD;

import osl.util.rdf.vocab.Templates;
import xyz.lutra.tabottr.TabOTTR;

public class RDFNodeFactory {

	private Model model;
	private DataValidator validator;

	public RDFNodeFactory (Model model) {
		this.model = model;
		this.validator = new DataValidator(model);
	}

	public RDFNode toRDFNode(String value, String type) {
		// if is a list, split in to values and parse into RDF nodes with recursive call.
		if (type.endsWith(TabOTTR.TYPE_LIST_POSTFIX)) {
			type = type.substring(0, type.length() - 1).trim(); // remove list operator from type
			List<RDFNode> nodes = new ArrayList<>();
			for (String item : value.split(Pattern.quote(TabOTTR.VALUE_LIST_SEPARATOR))) {
				item = item.trim();
				nodes.add(toRDFNode(item, type));
			}
			return toList(nodes);
		}
		// if value == empty -> ottr:none
		else if (DataValidator.isEmpty(value)) {
			return Templates.none;	
		}  
		else if (TabOTTR.TYPE_IRI.equals(type)) {
			return toResource(value);
		} 
		else if (TabOTTR.TYPE_BLANK.equals(type)) {
			return toBlank(value);
		} 
		// string, e.g, untyped literal
		else if (TabOTTR.TYPE_TEXT.equals(type)) {
			return toUntypedLiteral(value);
		} 
		// auto, guess type
		else if (TabOTTR.TYPE_AUTO.equals(type)) {
			return toRDFNode(value, getType(value));
		}
		// literal
		else {
			if (!validator.isIRI(type)) {
				throw new IllegalArgumentException("Type " + type + " is not a recognised type.");
			}
			return toTypedLiteral(value, type);
		}
	}

	private String getType(String value) {
		if(DataValidator.isBoolean(value)) {
			return XSD.xboolean.toString();
		}
		else if(DataValidator.isInteger(value)) {
			return XSD.integer.toString();
		}
		else if(DataValidator.isDecimal(value)) {
			return XSD.decimal.toString();
		}
		else if (DataValidator.isBlank(value)) {
			return TabOTTR.TYPE_BLANK;
		} 
		else if (validator.isIRI(value)) {
			return TabOTTR.TYPE_IRI;
		} 
		// default
		else {
			return TabOTTR.TYPE_TEXT;
		}
	}
	
	public RDFList toList(List<RDFNode> nodes) {
		return model.createList(nodes.iterator());
	}

	public Resource toResource(String qname) {
		return model.createResource(model.expandPrefix(qname));
	}

	private Resource toBlank(String value) {
		if (DataValidator.isFreshBlank(value)) {
			return model.createResource();
		} else {
			// remove trailing "_:"
			if (value.startsWith(TabOTTR.VALUE_BLANK_NODE_PREFIX)) {
				value = value.substring(TabOTTR.VALUE_BLANK_NODE_PREFIX.length());
			}
			return model.createResource(AnonId.create(value));
		}
	}

	private Literal toTypedLiteral(String value, String type) {
		// default:
		Literal literal = model.createTypedLiteral(value, model.expandPrefix(type));

		// overwrite default if ...
		// xsd:boolean and value is "1" or any capitalisation of "TRUE" (and similar for false):
		if (XSD.xboolean.toString().equals(model.expandPrefix(type))) {
			if (value.equals("1") || Boolean.parseBoolean(value) == true) {
				literal = model.createTypedLiteral(true);
			} else if (value.equals("0") || Boolean.parseBoolean(value) == false) {
				literal = model.createTypedLiteral(false);
			}
		}
		return literal;
	}
	
	private Literal toUntypedLiteral(String value) {
		// default:
		Literal literal = model.createLiteral(value);
		
		// overwrite default if ...
		// has language tag:
		int langTagIndex = value.lastIndexOf(TabOTTR.VALUE_LANGUAGE_TAG_PREFIX);
		if (langTagIndex != -1) {
			String lang = value.substring(langTagIndex + TabOTTR.VALUE_LANGUAGE_TAG_PREFIX.length());
			if (DataValidator.isLanguageTag(lang)) {
				literal = model.createLiteral(value.substring(0, langTagIndex), lang);
			}
		}
		return literal;
	}
}
