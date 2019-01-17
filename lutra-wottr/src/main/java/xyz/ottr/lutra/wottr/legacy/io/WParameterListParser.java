package xyz.ottr.lutra.wottr.legacy.io;

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

import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;

import org.dyreriket.gaupa.rdf.ModelSelector;
import org.dyreriket.gaupa.rdf.ModelSelectorException;

import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.Term;
import xyz.ottr.lutra.model.types.TypeFactory;
import xyz.ottr.lutra.result.Message;
import xyz.ottr.lutra.result.Result;
import xyz.ottr.lutra.wottr.legacy.WOTTR;
import xyz.ottr.lutra.wottr.legacy.WTermFactory;

public class WParameterListParser {

    private final Model model;

    public WParameterListParser(Model model) {
        this.model = model;
    }

    private Result<List<Term>> parseIndexedTerms(List<Resource> toParse,
            Function<Resource, Result<Term>> parser) {

        Vector<Result<Term>> parsed = new Vector<>(toParse.size());
        Vector<Result<Term>> errors = new Vector<>(toParse.size());
        parsed.setSize(toParse.size());

        for (Resource p : toParse) {

            Result<Term> resultTerm = parser.apply(p);
            int indexValue = 0;
            try {
                Literal index = ModelSelector.getRequiredLiteralOfProperty(model, p, WOTTR.index);
                indexValue = NumberUtils.toInt(index.getLexicalForm());
                parsed.set(indexValue - 1, resultTerm);
            } catch (ModelSelectorException ex) {
                // TODO: Correct lvl and good message?
                errors.add(Result.empty(new Message(Message.ERROR,
                                "Error parsing index of term. " + ex.getMessage()), resultTerm));
            } catch (ArrayIndexOutOfBoundsException ex) {
                String msg = "Index " + indexValue + " too large, number of terms is " + toParse.size()
                    + (resultTerm.isPresent() ? ", for indexed element " + resultTerm.get().toString() : "");
                errors.add(Result.empty(new Message(Message.ERROR,msg), resultTerm));
            }
        }
        for (int i = 0; i < toParse.size(); i++) {
            if (parsed.get(i) == null) {
                parsed.set(i, Result.empty(new Message(Message.ERROR, "Missing term for index "
                                + (i + 1) + ".")));
            }
        }

        parsed.addAll(errors);
        return Result.aggregate(parsed);
    }

    public Result<ArgumentList> parseArguments(List<Resource> toParse) {

        WArgumentParser rdfArgumentParser = new WArgumentParser(model);
        return parseIndexedTerms(toParse, (Function<Resource, Result<Term>>) rdfArgumentParser)
            .map(terms -> new ArgumentList(terms, rdfArgumentParser.getExpanderValues(),
                        rdfArgumentParser.getListExpander()));
    }

    public Result<ArgumentList> parseValues(Resource argsList) {
        return parseTermList(argsList, false).map(terms -> new ArgumentList(terms));
    }

    public Result<ParameterList> parseVariables(Resource varsList) {
        return parseTermList(varsList, true).map(terms -> new ParameterList(terms));
    }

    public Result<List<Term>> parseTermList(Resource argsList, boolean isVariables) {
        if (!argsList.canAs(RDFList.class)) {
            return Result.empty(Message.error("Expected ottr:withValues-related element to be an RDF-list "
                    + "but found " + argsList.toString()));
        }
        WTermFactory termFactory = new WTermFactory();
        List<Result<Term>> arguments = argsList
            .as(RDFList.class)
            .asJavaList()
            .stream()
            .map(termFactory)
            .map(trmRes -> trmRes.map(trm -> { 
                if (isVariables) {
                    trm.setType(TypeFactory.getConstantType(trm)); // Set variable type for variables
                }                                                  // as the constant type, as no type is
                return trm;                                        // given (this will give it a weak type),
            })).collect(Collectors.toList());                      // (e.g. an IRI will get type LUB<IRI>)
        return Result.aggregate(arguments);
    }

    public Result<ParameterList> parseParameters(List<Resource> toParse) {
        // TODO (When nonBlank-flag added): Parse and add nonBlank-flags
        WParameterParser rdfParameterParser = new WParameterParser(model);
        return parseIndexedTerms(toParse, (Function<Resource, Result<Term>>) rdfParameterParser)
            .map(terms -> new ParameterList(terms, null, rdfParameterParser.getOptionals(),
                        rdfParameterParser.getDefaultValues()));
    }
}
