package xyz.ottr.lutra.util;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2023 University of Oslo
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public class PrefixValidator {

    private static Map<String, String> stdPrefixMap;

    static {
        stdPrefixMap = new HashMap<>(PrefixMapping.Extended.getNsPrefixMap());
        stdPrefixMap.put(OTTR.prefix, OTTR.namespace);
    }

    /**
     * Checks if the prefix pairs do not contain duplicate prefix declarations.
     * @param pairs
     * @return The input prefix pairs in the form of a PrefixMapping wrapped in a Result with
     * additional error messages.
     */
    public static Result<PrefixMapping> buildPrefixMapping(Collection<Map.Entry<String,String>> pairs) {

        List<Message> errors = new ArrayList<>();
        // build a map of the pairs, while collecting conflicts if there are any
        Map<String, String> pxMap = pairs.stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (ns1, ns2) -> {  // run merge function on values if identical keys
                            if (!ns1.equals(ns2)) {
                                errors.add(Message.error("Conflicting prefix declarations. Namespaces '"
                                        + ns1 + "' and '" + ns2 + "' share the same prefix."));
                            }
                            return ns1; // if both are equal, then the first one will do.
                        }));

        if (errors.isEmpty()) {
            return Result.of(PrefixMapping.Factory.create().setNsPrefixes(pxMap));
        } else {
            return Result.empty(errors);
        }
    }

    /**
     * Checks if the given prefixMap is inconsistent with standard prefix declarations. Inconsistencies are reported
     * as Warnings.
     * @param prefixMap prefixMap to check
     * @return The input prefixMap unchanged in a Result, with messages attached if applicable.
     */
    public static <X extends PrefixMapping> Result<X> check(X prefixMap) {

        var result = Result.of(prefixMap);

        // check for namespaces with multiple prefixes
        var duplicates = getDuplicates(prefixMap.getNsPrefixMap().values());
        if (!duplicates.isEmpty()) {
            result.addWarning("Prefix declaration namespaces with multiple prefix declarations: " + duplicates);
        }

        for (var entry : prefixMap.getNsPrefixMap().entrySet()) {
            var prefix = entry.getKey();
            var namespace = entry.getValue();

            for (var stdEntry : stdPrefixMap.entrySet()) {
                var stdPrefix = stdEntry.getKey();
                var stdNamespace = stdEntry.getValue();

                if (prefix.equalsIgnoreCase(stdPrefix) && !namespace.equals(stdNamespace)) {
                    result.addWarning("Standard prefix declared with unusual namespace: "
                            + "The prefix '" + prefix + "' declared with the namespace '" + namespace
                            + "', but the standard namespace is '" + stdNamespace + "'");
                }

                if (namespace.equals(stdNamespace) && !prefix.equals(stdPrefix)) {
                    result.addWarning("Standard namespace declared with unusual prefix: "
                            + "The namespace '" + namespace + "' is declared with the prefix '" + prefix
                            + "', but the common prefix is '" + stdPrefix + "'");
                }


            }
        }
        return result;
    }

    private static <T> Set<T> getDuplicates(Collection<T> collection) {

        Set<T> duplicates = new HashSet<>();
        Set<T> uniques = new HashSet<>();

        for(T t : collection) {
            if(!uniques.add(t)) {
                duplicates.add(t);
            }
        }

        return duplicates;
    }
}
