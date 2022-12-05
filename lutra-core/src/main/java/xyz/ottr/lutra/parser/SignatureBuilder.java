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
import java.util.Set;
import lombok.Builder;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public enum SignatureBuilder {
    ;

    @Builder
    public static Result<Signature> create(Result<String> iri, Result<List<Parameter>> parameters, Result<Set<Instance>> annotations) {

        iri = Result.nullToEmpty(iri, Message.error("Missing IRI. A signature must have an IRI."));
        parameters = Result.nullToEmpty(parameters, Message.error("Missing parameter list. A signature must have "
            + "a (possibly empty) list of parameters."));
        annotations = Result.nullToEmpty(annotations);

        Result<Signature.SignatureBuilder> builder = Result.of(Signature.superbuilder());
        builder.addResult(iri, Signature.SignatureBuilder::iri);
        builder.addResult(parameters, Signature.SignatureBuilder::parameters);
        builder.addResult(annotations, Signature.SignatureBuilder::annotations);

        if (Result.allIsPresent(iri, parameters)) {
            return builder.map(Signature.SignatureBuilder::build)
                .flatMap(Signature::validate)
                .map(s -> (Signature)s);
        } else {
            return Result.empty(Message.error("Error building signature with IRI '" + iri.orElse("[IRI missing]'.")), builder);
        }
    }
}

