package xyz.ottr.lutra.wottr.util;

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

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// TODO evolve this into a util class for checking conflicting prefixes?

public enum PrefixMappings {

    ; // singleton enum

    private static final Logger log = LoggerFactory.getLogger(PrefixMappings.class);

    private static final String NS = "ns";
    private static final String defaultNSPrefix = "";

    /**
     * Add a namespace to mapping, giving it some fresh prefix.
     *
     * @param mapping
     * @param namespace
     */
    public static void addNamespace(PrefixMapping mapping, String namespace) {
        if (!containsNamespace(mapping, namespace)) {
            int i = 1;
            // find an unused prefix:
            while (containsPrefix(mapping, NS + i)) {
                i += 1;
            }
            mapping.setNsPrefix(NS + i, namespace);
            log.info("Adding ns: " + NS + i + " - " + namespace);
        }
    }

    public static void addPrefixes(PrefixMapping target, PrefixMapping source) {
        if (target.samePrefixMappingAs(source)) {
            return;
        } else if (getPrefixes(target).isEmpty()) {
            target.setNsPrefixes(source);
        } else { // copy only nonexistent
            for (String ns : getNamespaces(source)) {
                // only add new namespaces
                if (!containsNamespace(target, ns)) {
                    String prefix = getPrefix(source, ns);
                    // check that prefix is not already in target, or if default ns
                    if (!containsPrefix(target, prefix) && !prefix.equals(defaultNSPrefix)) {
                        target.setNsPrefix(prefix, ns);
                        log.info("Setting ns: " + prefix + " - " + ns);
                    } else {
                        addNamespace(target, ns);
                    }
                }
            }
        }
    }

    public static boolean containsNamespace(PrefixMapping mapping, String namespace) {
        return getPrefix(mapping, namespace) != null;
    }

    public static boolean containsPrefix(PrefixMapping mapping, String prefix) {
        return getNamespace(mapping, prefix) != null;
    }

    public static String getNamespace(PrefixMapping mapping, String prefix) {
        return mapping.getNsPrefixURI(prefix);
    }

    public static Collection<String> getNamespaces(PrefixMapping mapping) {
        return mapping.getNsPrefixMap().values();
    }

    public static String getPrefix(PrefixMapping mapping, String namespace) {
        return mapping.getNsURIPrefix(namespace);
    }

    public static Collection<String> getPrefixes(PrefixMapping mapping) {
        return mapping.getNsPrefixMap().keySet();
    }

    public static PrefixMapping merge(PrefixMapping... maps) {
        PrefixMapping pmap = PrefixMapping.Factory.create();
        for (PrefixMapping map : maps) {
            replacePrefix(map, defaultNSPrefix);
            addPrefixes(pmap, map);
        }
        return pmap;
    }

    private static void replacePrefix(PrefixMapping mapping, String prefix) {
        if (containsPrefix(mapping, prefix)) {
            String namespace = getNamespace(mapping, prefix);
            mapping.removeNsPrefix(prefix);
            addNamespace(mapping, namespace);
        }
    }

    public static void trim(Model model) {
        Set<String> namespaces = getAllURIsNamespaces(model);
        for (String prefixNamespace : model.getNsPrefixMap().values()) {
            if (!namespaces.contains(prefixNamespace)) {
                model.removeNsPrefix(model.getNsURIPrefix(prefixNamespace));
            }
        }
    }

    // TODO This might be too heavy for big data, should be possible to disable.
    private static Set<String> getAllURIsNamespaces(Model model) {
        return model.listObjects()
            .andThen(model.listSubjects())
            .andThen(model.listStatements()
                .mapWith(Statement::getPredicate))
            .filterKeep(RDFNode::isURIResource)
            .mapWith(RDFNode::asResource)
            .mapWith(Resource::getNameSpace)
            .toSet();
    }

}
