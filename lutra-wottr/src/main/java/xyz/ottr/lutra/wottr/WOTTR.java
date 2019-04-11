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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import xyz.ottr.lutra.OTTR;

public class WOTTR {

    // Classes
    public static final Resource Template = getResource("Template");
    public static final Resource TemplateSignature = getResource("Signature");
    public static final Resource BaseTemplate = getResource("BaseTemplate");
    public static final Resource TemplateInstance = getResource("TemplateInstance");

    // Individuals
    public static final Resource triple = ResourceFactory.createResource(OTTR.Bases.Triple);
    public static final Resource none = getResource("none");
    public static final Resource optional = getResource("optional");
    public static final Resource nonBlank = getResource("nonBlank");

    public static final Resource listExpand = getResource("listExpand");
    public static final Resource zipMin = getResource("zipMin");
    public static final Resource zipMax = getResource("zipMax");
    public static final Resource cross = getResource("cross");

    // Properties
    public static final Property parameters = getProperty("parameters");
    public static final Property variable = getProperty("variable");
    public static final Property type = getProperty("type");
    public static final Property defaultVal = getProperty("default");
    public static final Property pattern = getProperty("pattern");
    public static final Property arguments = getProperty("arguments");
    public static final Property modifier = getProperty("modifier");
    public static final Property of = getProperty("of");
    public static final Property values = getProperty("values");
    public static final Property value = getProperty("value");
    public static final Property annotation = getProperty("annotation");
 
    
    // Utility methods
    protected static Resource getResource(String localname) {
        return ResourceFactory.createResource(OTTR.namespace + localname);
    }

    protected static Property getProperty(String localname) {
        return ResourceFactory.createProperty(OTTR.namespace + localname);
    }
}
