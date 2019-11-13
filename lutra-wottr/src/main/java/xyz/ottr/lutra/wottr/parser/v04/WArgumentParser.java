package xyz.ottr.lutra.wottr.parser.v04;

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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v04.WOTTR;

public class WArgumentParser implements Function<RDFNode, Result<Term>> {

    private final Model model;
    private final TermFactory rdfTermFactory;
    private final Set<Term> expanderValues;

    public WArgumentParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new TermFactory(WOTTR.theInstance);
        this.expanderValues = new HashSet<>();
    }

    public Result<Term> apply(RDFNode argumentNode) {

        Result<Resource> argumentResource = RDFNodes.cast(argumentNode, Resource.class);

        Result<Term> term = argumentResource.flatMap(this::getArgumentTerm);
        Result<Resource> listExpanderResource = argumentResource.flatMap(this::getListExpander);

        // if present, addResult term to listexpander, and copy any messages to term system
        term.addResult(listExpanderResource, (t, l) -> this.expanderValues.add(t));

        return term;
    }

    private Result<Term> getArgumentTerm(Resource argument) {
        return ModelSelector.getRequiredObject(this.model, argument, WOTTR.value)
            .flatMap(this.rdfTermFactory);
    }

    private Result<Resource> getListExpander(Resource argument) {
        return ModelSelector.getOptionalResourceObject(this.model, argument, WOTTR.modifier)
            .flatMap(r -> r.equals(WOTTR.listExpand)
                ? Result.of(r)
                : Result.error("Error parsing argument modifier, expected " + RDFNodes.toString(WOTTR.listExpand)
                + ", but got " + RDFNodes.toString(r) + "."));
    }

    public Set<Term> getExpanderValues() {
        return Collections.unmodifiableSet(this.expanderValues);
    }
}
