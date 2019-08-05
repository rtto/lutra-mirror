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

import xyz.ottr.lutra.model.NoneTerm;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.ModelSelectorException;
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

    public Result<Term> apply(RDFNode argNode) {

        if (!argNode.isResource()) {
            return Result.empty(Message.error(
                "Error parsing argument, expected resource for argument node, but got " + argNode.toString() + "."));
        }

        Resource arg = argNode.asResource();
        Result<Term> resultTerm;

        try {

            RDFNode value = ModelSelector.getRequiredObjectOfProperty(this.model, arg, WOTTR.value);
            resultTerm = value != null
                ? this.rdfTermFactory.apply(value)
                : Result.of(new NoneTerm());

            Resource expand = ModelSelector.getOptionalResourceOfProperty(this.model, arg, WOTTR.modifier);

            if (expand != null) {
                if (!expand.equals(WOTTR.listExpand)) {
                    resultTerm.addMessage(Message.error(
                        "Error parsing argument, expected " + WOTTR.listExpand.toString() + " as argument modifier, "
                            + "but got " + expand.toString() + "."));
                } else if (resultTerm.isPresent()) {
                    this.expanderValues.add(resultTerm.get());
                }
            }
        } catch (ModelSelectorException ex) {
            // TODO: Correct lvl and good message?
            resultTerm = Result.empty(Message.error("Error parsing argument: " + ex.getMessage()));
        }

        return resultTerm;
    }

    public Set<Term> getExpanderValues() {
        return Collections.unmodifiableSet(this.expanderValues);
    }
}
