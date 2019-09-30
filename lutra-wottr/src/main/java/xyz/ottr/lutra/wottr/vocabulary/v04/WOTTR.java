package xyz.ottr.lutra.wottr.vocabulary.v04;

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

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.bidimap.UnmodifiableBidiMap;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.wottr.vocabulary.WOTTRVocabulary;

public class WOTTR implements WOTTRVocabulary {

    private static final String ns = OTTR.namespace;
    public static final String prefix = "ottr";
   
    // Classes
    public static final Resource Template = getResource(ns + "Template");
    public static final Resource TemplateSignature = getResource(ns + "Signature");
    public static final Resource BaseTemplate = getResource(ns + "BaseTemplate");
    public static final Resource TemplateInstance = getResource(ns + "TemplateInstance");

    // Individuals
    public static final Resource triple = getResource(OTTR.BaseURI.Triple);
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

    public static final BidiMap<Resource, ArgumentList.Expander> listExpanders;

    static {
        BidiMap<Resource, ArgumentList.Expander> map = new DualHashBidiMap<>();
        map.put(cross, ArgumentList.Expander.CROSS);
        map.put(zipMin, ArgumentList.Expander.ZIPMIN);
        map.put(zipMax, ArgumentList.Expander.ZIPMAX);
        listExpanders = UnmodifiableBidiMap.unmodifiableBidiMap(map);
    }

    public static final List<Resource> argumentModifiers = getList(optional, nonBlank);

    // Utility methods
    private static Resource getResource(String uri) {
        return ResourceFactory.createResource(uri);
    }

    private static Property getProperty(String uri) {
        return ResourceFactory.createProperty(uri);
    }

    private static <X> List<X> getList(X... objects) {
        return Collections.unmodifiableList(Arrays.asList(objects));
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