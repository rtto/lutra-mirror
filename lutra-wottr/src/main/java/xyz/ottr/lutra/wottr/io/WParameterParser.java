package xyz.ottr.lutra.wottr.io;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.types.TermType;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.WOTTR;
import xyz.ottr.lutra.wottr.WTermFactory;
import xyz.ottr.lutra.wottr.util.ModelSelector;
import xyz.ottr.lutra.wottr.util.ModelSelectorException;

public class WParameterParser implements Function<RDFNode, Result<Term>> {

    private final Model model;
    private final WTermFactory rdfTermFactory;
    private final Set<Term> optionals;
    private final Set<Term> nonBlanks;
    private final Map<Term, Term> defaultValues;
    private final List<Message> msgs;

    public WParameterParser(Model model) {
        this.model = model;
        this.rdfTermFactory = new WTermFactory();
        this.optionals = new HashSet<>();
        this.nonBlanks = new HashSet<>();
        this.defaultValues = new HashMap<>();
        this.msgs = new LinkedList<>();
    }

    public Result<Term> apply(RDFNode paramNode) {

        if (!paramNode.isResource()) {
            return Result.empty(Message.error(
                "Parameter node cannot be non-resource node, for parameter " + paramNode.toString() + "."));
        }

        Resource param = paramNode.asResource();
        Result<Term> resultTerm;

        try {
            // Must have a variable/value:
            RDFNode variable = ModelSelector.getRequiredObjectOfProperty(model, param, WOTTR.variable);
            resultTerm = rdfTermFactory.apply(variable);

            resultTerm.ifPresent(term -> {
                setType(term, param);
                setModifiers(term, param);
                setDefaultValue(term, param);
            });
        } catch (ModelSelectorException ex) {
            resultTerm = Result.empty(Message.error("Error parsing parameter: " + ex.getMessage()));
        }

        resultTerm.addMessages(this.msgs);
        return resultTerm;
    }

    private void setType(Term term, Resource param) {

        Resource type = ModelSelector.getOptionalResourceOfProperty(this.model, param, WOTTR.type);

        if (type != null) {
            Result<TermType> termType = new WTypeParser().apply(type);
            termType.ifPresent(tt -> term.setType(tt));
            this.msgs.addAll(termType.getAllMessages());
        } else {
            term.setType(TypeFactory.getVariableType(term));
        }
    }

    private void setModifiers(Term term, Resource param) {

        List<Resource> modifiers = ModelSelector.listResourcesOfProperty(this.model, param, WOTTR.modifier);

        for (Resource modifier : modifiers) {
            if (modifier.equals(WOTTR.optional)) {
                this.optionals.add(term);
            } else if (modifier.equals(WOTTR.nonBlank)) {
                this.nonBlanks.add(term);
            } else {
                this.msgs.add(Message.error(
                    "Unknown modifier " + modifier.toString() + " in parameter " + param.toString() + "."));
            }
        }
    }

    private void setDefaultValue(Term term, Resource param) {
        
        RDFNode defRes = ModelSelector.getOptionalObjectOfProperty(this.model, param, WOTTR.defaultVal);
        if (defRes != null) {
            Result<Term> defVal = this.rdfTermFactory.apply(defRes);
            defVal.ifPresent(val -> this.defaultValues.put(term, val));
            this.msgs.addAll(defVal.getAllMessages());
        }
    }

    public Set<Term> getOptionals() {
        return this.optionals;
    }

    public Set<Term> getNonBlanks() {
        return this.nonBlanks;
    }

    public Map<Term, Term> getDefaultValues() {
        return this.defaultValues;
    }
}
