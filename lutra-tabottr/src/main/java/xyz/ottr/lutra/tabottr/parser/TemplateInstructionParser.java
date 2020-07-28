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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.jena.shared.PrefixMapping;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.ArgumentBuilder;
import xyz.ottr.lutra.parser.InstanceBuilder;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.tabottr.model.TemplateInstruction;
import xyz.ottr.lutra.wottr.parser.WTermParser;

public class TemplateInstructionParser {

    private final RDFNodeFactory dataFactory;
    
    public TemplateInstructionParser(PrefixMapping prefixes) {
        this.dataFactory = new RDFNodeFactory(prefixes);
    }
    
    private Result<Instance> createTemplateInstance(String templateIRI, List<String> arguments, List<String> argumentTypes) {

        List<Result<Argument>> listArguments = new LinkedList<>();
        for (int i = 0; i < arguments.size(); i += 1) {
            Result<Term> term = this.dataFactory.toRDFNode(arguments.get(i), argumentTypes.get(i))
                .flatMap(WTermParser::toTerm);
            Result<Argument> argument = ArgumentBuilder.builder().term(term).build();
            listArguments.add(argument);
        }

        return InstanceBuilder.builder()
            .iri(Result.of(templateIRI))
            .arguments(Result.aggregate(listArguments))
            .build();
    }

    Stream<Result<Instance>> processTemplateInstruction(TemplateInstruction instruction) {

        String templateIRI = this.dataFactory.toResource(instruction.getTemplateIRI()).toString();
        List<String> argumentTypes = instruction.getArgumentTypes();

        return instruction.getTemplateInstanceRows().stream()
                .map(argList -> createTemplateInstance(templateIRI, argList, argumentTypes));
    }
}
