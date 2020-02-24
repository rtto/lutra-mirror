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
import java.util.Set;
import java.util.function.Function;

import lombok.Builder;
import lombok.NonNull;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Parameter;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

// TODO should we split this into different parsers and introduce a new class TemplateSource which orchestrates the different parsers?

public abstract class TemplateParser<E> implements Function<E, ResultStream<Signature>> {

    public abstract Map<String, String> getPrefixes();

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @Builder(builderMethodName = "signatureBuilder", builderClassName = "SignatureBuilder")
    private static Result<Signature> createSignature(@NonNull Result<String> iri, @NonNull Result<List<Parameter>> parameters) {
        var builder = Result.of(Signature.superbuilder());
        builder.addResult(iri, Signature.SignatureBuilder::iri);
        builder.addResult(parameters, Signature.SignatureBuilder::parameters);
        return builder.map(Signature.SignatureBuilder::build);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @Builder(builderMethodName = "templateBuilder", builderClassName = "TemplateBuilder")
    private static Result<Template> createTemplate(@NonNull Result<Signature> signature, @NonNull Result<Set<Instance>> instances) {
        var builder = Result.of(Template.builder());
        builder.addResult(signature, (bldr, sign) -> {
            bldr.iri(sign.getIri());
            bldr.parameters(sign.getParameters());
        });
        builder.addResult(instances, Template.TemplateBuilder::instances);
        return builder.map(Template.TemplateBuilder::build);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @Builder(builderMethodName = "baseTemplateBuilder", builderClassName = "BaseTemplateBuilder")
    private static Result<BaseTemplate> createBaseTemplate(@NonNull Result<Signature> signature) {
        var builder = Result.of(BaseTemplate.builder());
        builder.addResult(signature, (bldr, sign) -> {
            bldr.iri(sign.getIri());
            bldr.parameters(sign.getParameters());
        });
        return builder.map(BaseTemplate.BaseTemplateBuilder::build);
    }
}

