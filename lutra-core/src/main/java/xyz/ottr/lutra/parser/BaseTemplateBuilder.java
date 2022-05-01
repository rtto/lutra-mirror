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

import lombok.Builder;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public enum BaseTemplateBuilder {
    ;

    @Builder
    public static Result<BaseTemplate> create(Result<Signature> signature) {

        signature = Result.nullToEmpty(signature, Message.error("Missing signature. A base template must have "
            + "a signature."));

        // Must copy signature fields. @SuperBuilder does not work when annotating static methods
        var builder = Result.of(BaseTemplate.builder());
        builder.addResult(signature, (bldr, sign) -> {
            bldr.iri(sign.getIri());
            bldr.parameters(sign.getParameters());
            bldr.annotations(sign.getAnnotations());
        });

        return Result.allIsPresent(signature)
            ? builder.map(BaseTemplate.BaseTemplateBuilder::build)
                .flatMap(BaseTemplate::validate)
                .map(b -> (BaseTemplate)b)
            : Result.empty(builder);
    }
}

