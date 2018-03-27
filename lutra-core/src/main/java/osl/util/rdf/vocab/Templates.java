package osl.util.rdf.vocab;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class Templates extends Vocabulary {

	public static final String prefix = "ottr";
	private static final String ns = "http://ns.ottr.xyz/templates#";
	public static final String namespace = ns;
	
	// Classes
	public static final Resource Template = getResource(ns + "Template");
	public static final Resource TemplateInstance = getResource(ns + "TemplateInstance");
	
	// Individuals
	public static final Resource none = getResource(ns + "none");
	// - statuses
	public static final Resource incomplete = getResource(ns + "incomplete");

	// Properties
	public static final Property hasParameter   = getProperty(ns + "hasParameter");
	public static final Property hasArgument    = getProperty(ns + "hasArgument");
	public static final Property templateRef    = getProperty(ns + "templateRef");
	//public static final Property parameterRef   = getProperty(ns + "parameterRef");
	public static final Property index          = getProperty(ns + "index");
	public static final Property optional       = getProperty(ns + "optional");
	public static final Property status         = getProperty(ns + "status");
	public static final Property value          = getProperty(ns + "value");
	public static final Property eachValue      = getProperty(ns + "eachValue");
	public static final Property variable       = getProperty(ns + "variable");
	public static final Property withValues     = getProperty(ns + "withValues");
	public static final Property withVariables  = getProperty(ns + "withVariables");
	
	public static final Property literalVariable             = getProperty(ns + "literalVariable");
	public static final Property nonLiteralVariable          = getProperty(ns + "nonLiteralVariable");
	public static final Property classVariable               = getProperty(ns + "classVariable");
	public static final Property listVariable                = getProperty(ns + "listVariable");
	public static final Property individualVariable          = getProperty(ns + "individualVariable");
	public static final Property datatypeVariable            = getProperty(ns + "datatypeVariable");
	public static final Property propertyVariable            = getProperty(ns + "propertyVariable");
	public static final Property objectPropertyVariable      = getProperty(ns + "objectPropertyVariable");
	public static final Property dataPropertyVariable        = getProperty(ns + "dataPropertyVariable");
	public static final Property annotationPropertyVariable  = getProperty(ns + "annotationPropertyVariable");
	
	public static final List<Property> ALL_variable = Arrays
			.asList(new Property[] {
					variable,
					  literalVariable,
					  nonLiteralVariable,
					    classVariable,
					    listVariable,
					    individualVariable,  
					    datatypeVariable,
					    propertyVariable,
					      objectPropertyVariable, 
					      dataPropertyVariable, 
					      annotationPropertyVariable, 
					});
	
	// All vocabulary elements in the OTTR vocabulary.
	public static final List<Resource> ALL = Arrays.asList(
			Template, 
			TemplateInstance, 
			hasParameter, 
			hasArgument, 
			templateRef, 
			//parameterRef, 
			index,
			optional,
			none,
			status,
			incomplete,
			value,
			eachValue,
			variable,
			withValues,
			withVariables,
			literalVariable,
			nonLiteralVariable,
			  classVariable,
			  listVariable,
			  individualVariable,  
			  datatypeVariable,
			  propertyVariable,
			    objectPropertyVariable, 
			    dataPropertyVariable, 
			    annotationPropertyVariable 
			);
	
	public static final Map<Property, List<Property>> listPropertiesMap;
	
    static {
        Map<Property, List<Property>> aMap = new HashMap<>();
        aMap.put(withVariables,  Arrays.asList(Templates.hasParameter, Templates.variable));
        aMap.put(withValues,  Arrays.asList(Templates.hasArgument, Templates.value));
        listPropertiesMap = Collections.unmodifiableMap(aMap);
    }
}