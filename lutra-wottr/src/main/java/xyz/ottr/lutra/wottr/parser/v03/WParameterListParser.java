package xyz.ottr.lutra.wottr.parser.v03;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import xyz.ottr.lutra.model.ArgumentList;
import xyz.ottr.lutra.model.ParameterList;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.wottr.parser.TermFactory;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelector;
import xyz.ottr.lutra.wottr.parser.v03.util.ModelSelectorException;
import xyz.ottr.lutra.wottr.util.RDFNodes;
import xyz.ottr.lutra.wottr.vocabulary.v03.WOTTR;

public class WParameterListParser {

    private final Model model;

    public WParameterListParser(Model model) {
        this.model = model;
    }

    private Result<List<Term>> parseIndexedTerms(List<Resource> toParse,
            Function<Resource, Result<Term>> parser) {

        // use a fixed size list that supports .set(). NB, this does not support add()/addAll()
        List<Result<Term>> parsed = Arrays.asList(new Result[toParse.size()]);
        List<Result<Term>> errors = new ArrayList<>();

        for (Resource p : toParse) {

            Result<Term> resultTerm = parser.apply(p);
            int indexValue = 0;
            try {
                Literal index = ModelSelector.getRequiredLiteralOfProperty(this.model, p, WOTTR.index);
                indexValue = NumberUtils.toInt(index.getLexicalForm());
                parsed.set(indexValue - 1, resultTerm);
            } catch (ModelSelectorException ex) {
                // TODO: Correct lvl and good message?
                errors.add(Result.empty(Message.error(
                                "Error parsing index of term. " + ex.getMessage()), resultTerm));
            } catch (ArrayIndexOutOfBoundsException ex) {
                String msg = "Index " + indexValue + " too large, number of terms is " + toParse.size()
                    + (resultTerm.isPresent() ? ", for indexed element " + resultTerm.get() : "");
                errors.add(Result.empty(new Message(Message.ERROR, msg), resultTerm));
            }
        }
        for (int i = 0; i < toParse.size(); i++) {
            if (parsed.get(i) == null) {
                parsed.set(i, Result.error("Missing term for index " + (i + 1) + "."));
            }
        }

        // move to different list type that supports addAll()
        List<Result<Term>> all = new ArrayList<>(parsed);
        all.addAll(errors);
        return Result.aggregate(all);

    }

    public Result<ArgumentList> parseArguments(List<Resource> toParse) {

        WArgumentParser argumentParser = new WArgumentParser(this.model);
        return parseIndexedTerms(toParse, (Function<Resource, Result<Term>>) argumentParser)
            .map(terms -> new ArgumentList(terms, argumentParser.getExpanderValues(),
                        argumentParser.getListExpander()));
    }

    public Result<ArgumentList> parseValues(Resource argsList) {
        return parseTermList(argsList).map(ArgumentList::new);
    }

    public Result<ParameterList> parseVariables(Resource varsList) {
        return parseTermList(varsList).map(ParameterList::new);
    }

    private Result<List<Term>> parseTermList(Resource argsList) {
        if (!argsList.canAs(RDFList.class)) {
            return Result.error("Expected " + RDFNodes.toString(WOTTR.withValues) + " element to be an RDF-list, "
                    + "but found " + RDFNodes.toString(argsList));
        }
        TermFactory termFactory = new TermFactory(WOTTR.theInstance);
        List<Result<Term>> arguments = argsList
            .as(RDFList.class)
            .asJavaList()
            .stream()
            .map(termFactory)
            /*
            TODO: NB! check that removing this is correct.
            .map(trmRes -> trmRes.map(trm -> {
                if (isVariables) {
                    trm.setType(TypeRegistry.getConstantType(trm)); // Set variable type for variables
                }                                                  // as the constant type, as no type is
                return trm;                                        // given (this will give it a weak type),
            }))                                                    // (e.g. an IRI will get type LUB<IRI>)
            */
            .collect(Collectors.toList());
        return Result.aggregate(arguments);
    }

    public Result<ParameterList> parseParameters(List<Resource> toParse) {
        // TODO (When nonBlank-flag added): Parse and add nonBlank-flags
        WParameterParser parameterParser = new WParameterParser(this.model);
        return parseIndexedTerms(toParse, (Function<Resource, Result<Term>>) parameterParser)
            .map(terms -> new ParameterList(terms, null, parameterParser.getOptionals(),
                        parameterParser.getDefaultValues()));
    }
}
