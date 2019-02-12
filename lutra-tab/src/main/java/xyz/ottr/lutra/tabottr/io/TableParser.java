package xyz.ottr.lutra.tabottr.io;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.shared.PrefixMapping;

import xyz.ottr.lutra.ROTTR;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.result.ResultStream;
import xyz.ottr.lutra.tabottr.io.rdf.TemplateInstanceFactory;
import xyz.ottr.lutra.tabottr.model.Instruction;
import xyz.ottr.lutra.tabottr.model.PrefixInstruction;
import xyz.ottr.lutra.tabottr.model.Table;
import xyz.ottr.lutra.tabottr.model.TemplateInstruction;
import xyz.ottr.lutra.wottr.WOTTR;

public class TableParser {

    /**
     * Parses the list of tables into a ResultStream of Instances, but returns
     * empty Results if conflicts of defined prefixes or errors in terms occur.
     */
    public static ResultStream<Instance> processInstructions(List<Table> tables) {

        // collect all instructions
        List<Instruction> instructions = tables.stream()
            .flatMap(table -> table.getInstructions().stream())
            .collect(Collectors.toList());

        List<PrefixInstruction> prefixInstructions = instructions.stream()
            .filter(ins -> ins instanceof PrefixInstruction)
            .map(ins -> (PrefixInstruction) ins)
            .collect(Collectors.toList());

        List<TemplateInstruction> templateInstructions = instructions.stream()
            .filter(ins -> ins instanceof TemplateInstruction)
            .map(ins -> (TemplateInstruction) ins)
            .collect(Collectors.toList());

        // process all prefixes first
        Result<PrefixMapping> prefixes = processPrefixes(prefixInstructions);

        // process template instances
        return prefixes.mapToStream(pfs -> processInstanceInstructions(templateInstructions, pfs));
    }

    private static Result<PrefixMapping> processPrefixes(List<PrefixInstruction> instructions) {

        PrefixMapping prefixes = PrefixMapping.Factory.create();
        Map<String, List<String[]>> definedPrefixes = new HashMap<>(); // Used for conflict messages

        for (PrefixInstruction instruction : instructions) {

            int[] coord = instruction.getStartCoordinates();
            int row = coord[1] + 2; // Rows start at 1 and instruction is 1 row
            for (String[] pf : instruction.getPrefixes()) {

                definedPrefixes.putIfAbsent(pf[0], new LinkedList<>());
                // Store a definition of prefix with URI, table index and row
                definedPrefixes.get(pf[0]).add(new String[]{pf[1], coord[0] + "", row + ""});
                prefixes.setNsPrefix(pf[0], pf[1]);
                row++;
            }
        }

        // TODO move this into a more generic package
        PrefixMapping stdPrefixes = PrefixMapping.Factory.create();
        stdPrefixes.setNsPrefixes(PrefixMapping.Standard);
        stdPrefixes.setNsPrefix("ottr", WOTTR.namespace);
        stdPrefixes.setNsPrefix("ottt", ROTTR.namespace);

        List<Message> conflicts = checkForConflicts(definedPrefixes, stdPrefixes);

        prefixes.setNsPrefixes(stdPrefixes);
        return conflicts.isEmpty() ? Result.of(prefixes) : Result.empty(conflicts);
    }

    /**
     * Checks for conflicts in prefix definitions between definitions in definedPrefixes as well
     * as conflicts with the standards definitions from stdPrefixes.
     */
    private static List<Message> checkForConflicts(Map<String, List<String[]>> definedPrefixes,
            PrefixMapping stdPrefixes) {
        List<Message> conflicts = new LinkedList<>();

        for (Map.Entry<String, List<String[]>> defs : definedPrefixes.entrySet()) {

            String standardNs = stdPrefixes.getNsPrefixURI(defs.getKey());

            if (defs.getValue().size() > 1) { // Conflicting definitions

                Set<String> differentDefs = new HashSet<String>();
                StringBuilder msg = new StringBuilder("Conflicting definition of prefix "
                        + defs.getKey() + ":\n");
                for (String[] def : defs.getValue()) {
                    differentDefs.add(def[0]);
                    msg.append(" - " + def[0] + " at row " + def[2]
                            + " in table " + def[1] + "\n");
                }
                if (differentDefs.size() > 1) {
                    conflicts.add(Message.error(msg.toString()));
                }
            }
            if (standardNs != null) { // Definition conflicting with standard prefix

                StringBuilder msg = new StringBuilder("Conflicting definition of prefix " + defs.getKey()
                        + ": standard definition " + standardNs + " conflicts with the following:\n");
                boolean actualConflict = false;
                
                for (String[] def : defs.getValue()) {
                    if (!def[0].equals(standardNs)) {
                        msg.append(" - " + def[0] + " at row " + def[2]
                            + " in table " + def[1] + "\n");
                        actualConflict = true;
                    }
                }
                if (actualConflict) {
                    conflicts.add(Message.error(msg.toString()));
                }
            } 
        }
        return conflicts;
    }

    private static ResultStream<Instance> processInstanceInstructions(
            List<TemplateInstruction> instructions,
            PrefixMapping prefixes) {
        return new ResultStream<Instance>(instructions.stream()
                .flatMap(ins -> processInstanceInstruction(ins, prefixes)));
    }

    private static Stream<Result<Instance>> processInstanceInstruction(
            TemplateInstruction instruction,
            PrefixMapping prefixes) {
        TemplateInstanceFactory builder = new TemplateInstanceFactory(
                prefixes,
                instruction.getTemplateIRI(),
                instruction.getArgumentTypes());
        
        Stream.Builder<Result<Instance>> ins = Stream.builder();
        for (List<String> arguments : instruction.getTemplateInstances()) {
            ins.add(builder.createTemplateInstance(arguments));
        }
        return ins.build();
    }
}
