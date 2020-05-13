package xyz.ottr.lutra.wottr.parser;

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

import java.util.function.Function;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.parser.ArgumentBuilder;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.util.RDFNodes;

public class WArgumentParser implements Function<RDFNode, Result<Argument>> {

    private final Model model;
    private final TermSerializer termSerializer;

    WArgumentParser(Model model) {
        this.model = model;
        this.termSerializer = new TermSerializer();
    }

    public Result<Argument> apply(RDFNode argumentNode) {

        var argumentResource = RDFNodes.cast(argumentNode, Resource.class);

        return ArgumentBuilder.builder()
            .term(argumentResource.flatMap(this::parseArgumentValue))
            .listExpander(argumentResource.flatMap(this::parseListExpander))
            .build();
    }

    private Result<Term> parseArgumentValue(Resource argument) {
        return ModelSelector.getRequiredObject(this.model, argument, WOTTR.value)
            .flatMap(this.termSerializer);
    }

    private Result<Boolean> parseListExpander(Resource argument) {
        return ModelSelector.getOptionalResourceObject(this.model, argument, WOTTR.modifier)
            .flatMap(r -> r.equals(WOTTR.listExpand)
                ? Result.of(Boolean.TRUE)
                : Result.error("Error parsing argument modifier, expected " + RDFNodes.toString(WOTTR.listExpand)
                + ", but got " + RDFNodes.toString(r) + "."));
    }

}
