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
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.Result;

// TODO should we split this into different parsers and introduce a new class TemplateSource which orchestrates the different parsers?

public abstract class TemplateBuilder {

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @Builder(builderMethodName = "signatureBuilder", builderClassName = "InnerSignatureBuilder")
    private static Result<Signature> createSignature(Result<String> iri, Result<List<Parameter>> parameters) {

        iri = Result.nullToEmpty(iri, Message.error("Missing IRI. A signature must have an IRI."));
        parameters = Result.nullToEmpty(parameters, Message.error("Missing parameter list. A signature must have "
            + "a (possibly empty) list of parameters."));

        Result<Signature.SignatureBuilder> builder = Result.of(Signature.superbuilder());
        builder.addResult(iri, Signature.SignatureBuilder::iri);
        builder.addResult(parameters, Signature.SignatureBuilder::parameters);

        return Result.allIsPresent(iri, parameters)
            ? builder.map(Signature.SignatureBuilder::build)
            : Result.empty(builder);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @Builder(builderMethodName = "templateBuilder", builderClassName = "InnerTemplateBuilder")
    private static Result<Template> createTemplate(Result<Signature> signature, Result<Set<Instance>> instances) {

        signature = Result.nullToEmpty(signature, Message.error("Missing signature. A template must have "
            + "a signature."));
        instances = Result.nullToEmpty(instances, Message.error("Missing pattern. A template pattern must be "
            + "a (possibly empty) set of instances."));

        var builder = Result.of(Template.builder());
        builder.addResult(signature, (bldr, sign) -> {
            bldr.iri(sign.getIri());
            bldr.parameters(sign.getParameters());
        });
        builder.addResult(instances, Template.TemplateBuilder::instances);

        return Result.allIsPresent(signature, instances)
            ? builder.map(Template.TemplateBuilder::build)
            : Result.empty(builder);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @Builder(builderMethodName = "baseTemplateBuilder", builderClassName = "InnerBaseTemplateBuilder")
    private static Result<BaseTemplate> createBaseTemplate(Result<Signature> signature) {

        signature = Result.nullToEmpty(signature, Message.error("Missing signature. A base template must have "
            + "a signature."));

        var builder = Result.of(BaseTemplate.builder());
        builder.addResult(signature, (bldr, sign) -> {
            bldr.iri(sign.getIri());
            bldr.parameters(sign.getParameters());
        });

        return Result.allIsPresent(signature)
            ? builder.map(BaseTemplate.BaseTemplateBuilder::build)
            : Result.empty(builder);
    }
}

