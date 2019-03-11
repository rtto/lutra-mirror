package xyz.ottr.lutra.wottr;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import xyz.ottr.lutra.ROTTR;

public class WOTTR {

    //private static final String ns = "http://spec.ottr.xyz/0/rottr/types#"; // TODO: Decide on prefix
    private static final String ns = "http://spec.ottr.xyz/wottr/0.4/core#";
    public static final String namespace = ns;

    // Classes
    public static final Resource Template = getResource(ns + "Template");
    public static final Resource TemplateSignature = getResource(ns + "Signature");
    public static final Resource BaseTemplate = getResource(ns + "BaseTemplate");
    public static final Resource TemplateInstance = getResource(ns + "TemplateInstance");

    // Individuals
    public static final Resource incomplete = getResource(ns + "incomplete");
    public static final Resource triple = getResource(ROTTR.triple);
    public static final Resource none = getResource(ns + "none");
    public static final Resource optional = getResource(ns + "optional");
    public static final Resource nonBlank = getResource(ns + "nonBlank");

    public static final Resource listExpand = getResource(ns + "listExpand");
    public static final Resource zipMin = getResource(ns + "zipMin");
    public static final Resource zipMax = getResource(ns + "zipMax");
    public static final Resource cross = getResource(ns + "cross");

    // Properties
    public static final Property parameters = getProperty(ns + "parameters");
    public static final Property variable = getProperty(ns + "variable");
    public static final Property type = getProperty(ns + "type");
    public static final Property defaultVal = getProperty(ns + "default");
    public static final Property pattern = getProperty(ns + "pattern");
    public static final Property arguments = getProperty(ns + "arguments");
    public static final Property modifier = getProperty(ns + "modifier");
    public static final Property of = getProperty(ns + "of");
    public static final Property values = getProperty(ns + "values");
    public static final Property value = getProperty(ns + "value");
    public static final Property annotation = getProperty(ns + "annotation");

    public static final Property status = getProperty(ns + "status");

    // TODO put these in public inner class?
    public static final Resource TermType = getResource(ns + "Type");
    public static final Property moreSpecificThan = getProperty(ns + "moreSpecificThan");
    
    // All vocabulary elements in the OTTR 
    public static final List<Resource> ALL = Collections.unmodifiableList(Arrays.asList(
        Template,
        TemplateSignature,
        BaseTemplate,
        TemplateInstance,
        incomplete,
        triple,
        none,
        optional,
        nonBlank,
        listExpand,
        zipMin,
        zipMax,
        cross,
        parameters,
        variable,
        type,
        defaultVal,
        pattern,
        arguments,
        modifier,
        of,
        arguments,
        values,
        value,
        status,
        TermType,
        moreSpecificThan
    ));

    //public static final Map<Property, List<Property>> listPropertiesMap;
   
    //static {
    //    Map<Property, List<Property>> tempMap = new HashMap<>();
    //    tempMap.put(withVariables, Arrays.asList(WOTTR.hasParameter, WOTTR.variable));
    //    tempMap.put(withValues, Arrays.asList(WOTTR.hasArgument, WOTTR.value));
    //    listPropertiesMap = Collections.unmodifiableMap(tempMap);
    //}

    private static Resource getResource(String uri) {
        return ResourceFactory.createResource(uri);
    }

    private static Property getProperty(String uri) {
        return ResourceFactory.createProperty(uri);
    }
}
