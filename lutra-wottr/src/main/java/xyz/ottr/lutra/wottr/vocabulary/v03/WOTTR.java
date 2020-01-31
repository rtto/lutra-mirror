package xyz.ottr.lutra.wottr.vocabulary.v03;

/*-
 * #%L
 * lutra-wottr
 * %%
 * Copyright (C) 2018 - 2019 University of Oslo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import xyz.ottr.lutra.wottr.vocabulary.WOTTRVocabulary;

public class WOTTR implements WOTTRVocabulary {

    private static final String ns = "http://ns.ottr.xyz/templates#";
    public static final String namespace = ns;
    public static final String prefix = "ottr";

    // Classes
    public static final Resource Template = getResource(ns + "Template");
    public static final Resource TemplateInstance = getResource(ns + "TemplateInstance");

    // Individuals
    public static final Resource incomplete = getResource(ns + "incomplete");
    public static final Resource triple = getResource("http://candidate.ottr.xyz/rdf/Triple");
    public static final Resource none = getResource(ns + "none");

    // Properties
    public static final Property hasPattern = getProperty(ns + "hasPattern");
    public static final Property hasParameter = getProperty(ns + "hasParameter");
    public static final Property hasArgument = getProperty(ns + "hasArgument");
    public static final Property templateRef = getProperty(ns + "templateRef");
    public static final Property index = getProperty(ns + "index");
    public static final Property optional = getProperty(ns + "optional");
    public static final Property status = getProperty(ns + "status");
    public static final Property value = getProperty(ns + "value");
    public static final Property eachValue = getProperty(ns + "eachValue");
    public static final Property variable = getProperty(ns + "variable");
    public static final Property withValues = getProperty(ns + "withValues");
    public static final Property withVariables = getProperty(ns + "withVariables");

    public static final Property literalVariable = getProperty(ns + "literalVariable");
    public static final Property nonLiteralVariable = getProperty(ns + "nonLiteralVariable");
    public static final Property classVariable = getProperty(ns + "classVariable");
    public static final Property listVariable = getProperty(ns + "listVariable");
    public static final Property individualVariable = getProperty(ns + "individualVariable");
    public static final Property datatypeVariable = getProperty(ns + "datatypeVariable");
    public static final Property propertyVariable = getProperty(ns + "propertyVariable");
    public static final Property objectPropertyVariable = getProperty(ns + "objectPropertyVariable");
    public static final Property dataPropertyVariable = getProperty(ns + "dataPropertyVariable");
    public static final Property annotationPropertyVariable = getProperty(ns + "annotationPropertyVariable");
    
    public static final List<Property> ALL_variable = List.of(
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
        annotationPropertyVariable);

    public static final Map<Property, List<Property>> listPropertiesMap =
        Map.of(
            withVariables, List.of(hasParameter, variable),
            withValues, List.of(hasArgument, value));
    
    private static Resource getResource(String uri) {
        return ResourceFactory.createResource(uri);
    }
    
    private static Property getProperty(String uri) {
        return ResourceFactory.createProperty(uri);
    }

    public static final WOTTRVocabulary theInstance = new WOTTR();

    private WOTTR() {
        // hide constructor, use theInstance.
    }

    @Override
    public Resource getNoneResource() {

        return none;
    }


}
