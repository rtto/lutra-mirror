package xyz.ottr.lutra.parser;

/*-
 * #%L
 * lutra-core
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
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Builder;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public enum SignatureBuilder {
    ;

    @Builder
    public static Result<Signature> create(Result<String> iri, Result<List<Parameter>> parameters) {

        iri = Result.nullToEmpty(iri, Message.error("Missing IRI. A signature must have an IRI."));
        parameters = Result.nullToEmpty(parameters, Message.error("Missing parameter list. A signature must have "
            + "a (possibly empty) list of parameters."));

        Result<Signature.SignatureBuilder> builder = Result.of(Signature.superbuilder());
        builder.addResult(iri, Signature.SignatureBuilder::iri);
        builder.addResult(parameters, Signature.SignatureBuilder::parameters);

        if (Result.allIsPresent(iri, parameters)) {
            var signature = builder.map(Signature.SignatureBuilder::build);
            validate(signature);
            return signature;
        } else {
            return Result.empty(builder);
        }
    }

    private static void validate(Result<Signature> signature) {
        checkDuplicateVariables(signature);
    }

    /**
     * Check that parameter list does not contain multiple parameters that share the same variable name.
     */
    private static void checkDuplicateVariables(Result<Signature> signature) {
        signature.ifPresent(s -> {
            var duplicateVarNames =
                s.getParameters().stream()
                    .collect(Collectors.groupingBy(p -> p.getTerm().getIdentifier())) // group parameters by variable name
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().size() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!duplicateVarNames.isEmpty()) {
                signature.addError("Parameter variables must be unique. Signature contains multiple occurrences "
                    + "of the same variable name: " + duplicateVarNames);
            }
        });
    }


}

