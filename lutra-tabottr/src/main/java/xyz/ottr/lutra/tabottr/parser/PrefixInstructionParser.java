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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.SetUtils;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.OTTR;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.tabottr.model.Instruction;
import xyz.ottr.lutra.tabottr.model.PrefixInstruction;
import xyz.ottr.lutra.util.PrefixValidator;


public class PrefixInstructionParser {

    private static final PrefixMapping stdPrefixes = OTTR.getDefaultPrefixes();

    private static Set<Map.Entry<String, String>> getPrefixPairs(PrefixMapping prefixes) {
        return prefixes.getNsPrefixMap().entrySet();
    }

    // Tip: Think of Map.Entry as Java's Pair implementation. Map.Entry<String, String> is here one prefix declaration.
    private static Result<PrefixMapping> mergePrefixResults(Result<PrefixMapping> base, Result<PrefixMapping> add) {
        Result<Set<Map.Entry<String, String>>> pairs =
            Result.zip(base, add, (px1, px2) -> SetUtils.union(getPrefixPairs(px1), getPrefixPairs(px2)));
        return pairs.flatMap(PrefixValidator::buildPrefixMapping);
    }

    Result<PrefixMapping> processPrefixInstructions(Collection<Instruction> instructions) {
        return instructions.stream()
                .filter(ins -> ins instanceof PrefixInstruction)
                .map(ins -> (PrefixInstruction) ins)
                .map(PrefixInstruction::getPrefixPairs)
                .map(PrefixValidator::buildPrefixMapping)
                .reduce(Result.of(stdPrefixes), PrefixInstructionParser::mergePrefixResults)
                .flatMap(PrefixValidator::check);
    }
}
