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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.model.Instruction;
import xyz.ottr.lutra.tabottr.model.PrefixInstruction;
import xyz.ottr.lutra.tabottr.model.Table;
import xyz.ottr.lutra.tabottr.model.TemplateInstruction;

public class TableParser {

    private static final PrefixMapping stdPrefixes = PrefixMapping.Standard; // TODO use OTTR standard

    /**
     * Parses the list of tables into a ResultStream of Instances, but returns
     * empty Results if conflicts of defined prefixes or errors in terms occur.
     */
    public static ResultStream<Instance> processInstructions(List<Table> tables) {

        // collect all instructions
        List<Instruction> instructions = tables.stream()
            .flatMap(table -> table.getInstructions().stream())
            .collect(Collectors.toList());

        // process all prefixes first
        Result<PrefixMapping> prefixes = instructions.stream()
            .filter(ins -> ins instanceof PrefixInstruction)
            .map(ins -> (PrefixInstruction) ins)
            .map(TableParser::processPrefixInstruction)
            .reduce(Result.of(PrefixMapping.Factory.create()), (sum, part) -> sum); // TODO BUG!

        // process instances, with prefixes as input
        ResultStream<Instance> instances = prefixes.mapToStream(pfs ->
            new ResultStream(
                instructions.stream()
                        .filter(ins -> ins instanceof TemplateInstruction)
                        .map(ins -> (TemplateInstruction) ins)
                        .flatMap(ins -> processTemplateInstruction(ins, pfs))));

        return instances;
    }

    private static List<Message> getMergeConflicts(PrefixMapping base, PrefixMapping addition) {
        return addition.getNsPrefixMap().keySet().stream()
                .filter(key -> !addition.getNsPrefixURI(key).equalsIgnoreCase(base.getNsPrefixURI(key)))
                .map(key -> "Conflicting prefix declaration: prefix "
                        + key + " has values "
                        + base.getNsPrefixURI(key) + " and "
                        + addition.getNsPrefixURI(key))
                .map(Message::error)
                .collect(Collectors.toList());
    }

    private static Result<PrefixMapping> processPrefixInstruction(PrefixInstruction instruction) {
        PrefixMapping prefixes = PrefixMapping.Factory.create();
        int[] coord = instruction.getStartCoordinates();
        for (String[] pf : instruction.getPrefixes()) {
            prefixes.setNsPrefix(pf[0], pf[1]);
        }

        // check conflicts against standard prefixes
        List<Message> mergeConflicts = getMergeConflicts(stdPrefixes, prefixes);
        return mergeConflicts.isEmpty() ? Result.of(prefixes) : Result.empty(mergeConflicts);
    }

    private static Stream<Result<Instance>> processTemplateInstruction(TemplateInstruction instruction, PrefixMapping prefixes) {
        TemplateInstanceFactory factory = new TemplateInstanceFactory(
                prefixes,
                instruction.getTemplateIRI(),
                instruction.getArgumentTypes());

        return instruction.getTemplateInstanceRows().stream()
                .map(factory::createTemplateInstance);
    }

}
