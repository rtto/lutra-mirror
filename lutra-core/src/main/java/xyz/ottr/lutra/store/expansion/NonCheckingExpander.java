package xyz.ottr.lutra.store.expansion;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-core
 * %%
 * Copyright (C) 2018 - 2021 University of Oslo
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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.StandardTemplateStore;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Message;
import xyz.ottr.lutra.system.MessageHandler;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

public class NonCheckingExpander implements Expander {

    private final TemplateStore templateStore;

    public NonCheckingExpander(TemplateStore templateStore) {
        this.templateStore = templateStore;
    }

    @Override
    public ResultStream<Instance> expandInstanceFetch(Instance instance) {
        Result<Signature> result = templateStore.getSignature(instance.getIri());

        if (isTemplate(result) || isBaseTemplate(result)) {
            return expandInstance(instance);
        }

        // Need to fetch missing template
        MessageHandler messages = templateStore.fetchMissingDependencies(List.of(instance.getIri()));
        Result<Instance> insWithMsgs = Result.of(instance);
        messages.toSingleMessage("Fetch missing template: " + instance.getIri())
                .ifPresent(insWithMsgs::addMessage);
        return insWithMsgs.mapToStream(this::expandInstance);
    }

    @Override
    public Result<? extends TemplateStore> expandAll() {
        TemplateStore newStore = new StandardTemplateStore(templateStore.getFormatManager());
        templateStore.getAllBaseTemplates().innerForEach(newStore::addSignature);

        ResultConsumer<Template> consumer = new ResultConsumer<>(t -> newStore.addTemplate(t));
        templateStore.getAllTemplates().mapFlatMap(this::expandTemplate).forEach(consumer);

        Result<TemplateStore> result = Result.of(newStore);
        result.addMessages(consumer.getMessageHandler().getMessages());
        return result;
    }

    @Override
    public Result<Template> expandTemplate(Template template) {
        Set<Instance> pattern = template.getPattern();
        Result<Set<Instance>> patternStream = ResultStream.innerOf(pattern)
                .innerFlatMap(i -> expandTemplateInstance(i, template))
                .aggregate()
                .map(s -> s.collect(Collectors.toSet()));

        return patternStream.map(p -> Template.builder()
                .iri(template.getIri())
                .instances(p)
                .parameters(template.getParameters())
                .build());
    }

    // for some reason PMD does not recognize that the method IS used above
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    protected ResultStream<Instance> expandTemplateInstance(Instance instance, Signature from) {
        Result<Signature> toResult = templateStore.getSignature(instance.getIri());
        if (toResult.isEmpty()) {
            return ResultStream.of(toResult.map(s -> null));
        }
        if (cannotExpandTemplateInstance(instance, from, toResult.get())) {
            return ResultStream.innerOf(instance);
        }

        return expandInstance(instance).innerFlatMap(i -> expandTemplateInstance(i, toResult.get()));
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        Result<Signature> result = templateStore.getSignature(instance.getIri());

        if (!isBaseTemplate(result) && !isTemplate(result)) {
            return ResultStream.of(Result.error("Missing definition for " + instance.getIri()));
        }
        if (shouldDiscard(instance, result.get())) {
            return ResultStream.of(Result.empty(Message.warning("Discarded instance with none at non-optional position: " + instance)));
        }
        if (cannotExpand(instance)) {
            return ResultStream.innerOf(instance);
        }

        if (instance.hasListExpander()) {
            // TODO move to Instance class later
            Stream<Result<Instance>> expanded = instance.getListExpander().expand(instance.getArguments()).stream()
                    .map(args -> Instance.builder().iri(instance.getIri()).arguments(args).build())
                    .map(Result::of);
            return new ResultStream<>(expanded).innerFlatMap(this::expandInstance);
        } else {
            Result<Template> templateResult = result.map(x -> (Template) x);
            Result<Substitution> subst = templateResult.flatMap(t -> Substitution.resultOf(instance.getArguments(), t.getParameters()));
            Result<Stream<Instance>> combination = Result.zip(templateResult, subst,
                    (t, s) -> t.getPattern().stream().map(i -> i.apply(s)));

            return combination.mapToStream(ResultStream::innerOf).innerFlatMap(this::expandInstance);
        }
    }

    protected boolean shouldDiscard(Instance instance, Signature signature) {
        // Should discard this instance if it contains none at a non-optional position
        for (int i = 0; i < instance.getArguments().size(); i++) {
            if (instance.getArguments().get(i).getTerm() instanceof NoneTerm
                    && !signature.isOptional(i)
                    && !signature.getParameters().get(i).hasDefaultValue()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this edge can be expanded (i.e. not base and no optional variables),
     * but does not check for missing definitions.
     */
    protected boolean cannotExpandTemplateInstance(Instance instance, Signature from, Signature to) {

        if (cannotExpand(instance)) {
            return true;
        }
        for (int i = 0; i < instance.getArguments().size(); i++) {
            Term arg = instance.getArguments().get(i).getTerm();
            if (arg.isVariable() && !to.isOptional(i) && from.isOptional(arg)) {
                return true;
            }
        }
        return false;
    }

    protected boolean cannotExpand(Instance instance) {
        if (templateStore.containsBase(instance.getIri()) && !instance.hasListExpander()) {
            return true;
        }
        if (instance.hasListExpander() && cannotExpandExpander(instance)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if this edge's listExpander can be expanded (i.e. no variable or blank marked for expansion),
     * but does not check for missing definitions.
     */
    protected boolean cannotExpandExpander(Instance instance) {
        for (Argument arg : instance.getArguments()) {
            if (arg.isListExpander()
                    && (arg.getTerm().isVariable()
                    || arg.getTerm() instanceof BlankNodeTerm)) {
                return true;
            }
        }
        return false;
    }

    protected TemplateStore getTemplateStore() {
        return templateStore;
    }

    private boolean isBaseTemplate(Result<Signature> result) {
        return result.isPresent() && result.get() instanceof BaseTemplate;
    }

    private boolean isTemplate(Result<Signature> result) {
        return result.isPresent() && result.get() instanceof Template;
    }
}
