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

import java.util.Set;
import lombok.Builder;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

public enum TemplateBuilder {
    ;

    @Builder
    public static Result<Template> create(Result<Signature> signature, Result<Set<Instance>> instances) {

        signature = Result.nullToEmpty(signature, Message.error("Missing signature. A template must have "
            + "a signature."));
        instances = Result.nullToEmpty(instances, Message.error("Missing pattern. A template pattern must be "
            + "a (possibly empty) set of instances."));

        var builder = Result.of(Template.builder());
        builder.addResult(signature, (bldr, sign) -> {
            bldr.iri(sign.getIri());
            bldr.parameters(sign.getParameters());
        });
        builder.addResult(instances, (bldr, insts) -> {
            bldr.instances(insts);
            bldr.isEmptyPattern(insts.isEmpty());
        });

        if (Result.allIsPresent(signature, instances)) {
            return builder.map(Template.TemplateBuilder::build)
                .flatMap(Template::validate);
        } else {
            return Result.empty(builder);
        }
    }

}

