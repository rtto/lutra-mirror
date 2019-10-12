package xyz.ottr.lutra.tabottr.parser;

/*-
 * #%L
 * lutra-tab
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.tabottr.model.Instruction;
import xyz.ottr.lutra.tabottr.model.PrefixInstruction;


public class PrefixInstructionParser {

    private static final PrefixMapping stdPrefixes = OTTR.getDefaultPrefixes();

    private static Set<Map.Entry<String, String>> getPrefixPairs(PrefixMapping prefixes) {
        return prefixes.getNsPrefixMap().entrySet();
    }

    // Tip: Think of Map.Entry as Java's Pair implementation. Map.Entry<String, String> is here one prefix declaration.
    private static Result<PrefixMapping> mergePrefixResults(Result<PrefixMapping> base, Result<PrefixMapping> add) {
        Result<Set<Map.Entry<String, String>>> pairs =
            Result.zip(base, add, (px1, px2) -> SetUtils.union(getPrefixPairs(px1), getPrefixPairs(px2)));
        return pairs.flatMap(PrefixInstructionParser::buildPrefixMapping);
    }

    /**
     * Builds a PrefixMapping containing the given prefix pairs, returns an empty Result if there is a
     * conflict in the prefix pairs, i.e,. if one prefix has two different namespaces.
     * @param pairs a list of pairs of strings (prefix, namespace)
     * @return a Result containing the PrefixMapping or an empty Result with an error message.
     */
    private static Result<PrefixMapping> buildPrefixMapping(Collection<Map.Entry<String,String>> pairs) {

        List<Message> errors = new ArrayList<>();
        // build a map of the pairs, while collecting conflicts if there are any
        Map<String, String> pxMap = pairs.stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                    (ns1, ns2) -> {  // run merge function on values if identical keys
                        if (!ns1.equals(ns2)) {
                            errors.add(Message.error("Conflicting prefix instruction: "
                                + ns1 + " and " + ns2 + " share the same prefix."));
                        }
                        return ns1; // if both are equal, then the first one will do.
                    }));

        // NOTE: we keep the result even though there are errors in other to collect more possible errors.
        Result<PrefixMapping> prefixes = Result.of(PrefixMapping.Factory.create().setNsPrefixes(pxMap));
        prefixes.addMessages(errors);
        return prefixes;
    }

    Result<PrefixMapping> processPrefixInstructions(Collection<Instruction> instructions) {
        return instructions.stream()
                .filter(ins -> ins instanceof PrefixInstruction)
                .map(ins -> (PrefixInstruction) ins)
                .map(PrefixInstruction::getPrefixPairs)
                .map(PrefixInstructionParser::buildPrefixMapping) // checks for local conflicts
                // check for standard prefix conflicts:
                .map(prefixes -> mergePrefixResults(Result.of(stdPrefixes), prefixes))
                // check for conflicts when merging prefix instructions:
                .reduce(Result.of(stdPrefixes), PrefixInstructionParser::mergePrefixResults);
    }
}
