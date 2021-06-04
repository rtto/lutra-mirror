package xyz.ottr.lutra.store.graph;

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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.store.Expander;
import xyz.ottr.lutra.store.TemplateStoreNew;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultConsumer;
import xyz.ottr.lutra.system.ResultStream;

public class NewExpander implements Expander {

    private final TemplateStoreNew templateStore;

    public NewExpander(TemplateStoreNew templateStore) {
        this.templateStore = templateStore;
    }

    @Override
    public Result<? extends TemplateStoreNew> expandAll() {
        TemplateStoreNew newStore = new TemplateManager(templateStore.getFormatManager());
        templateStore.getAllBaseTemplates().innerForEach(newStore::addSignature);

        ResultConsumer<Template> consumer = new ResultConsumer<>(t -> newStore.addTemplate(t));
        templateStore.getAllTemplates().mapFlatMap(this::expandTemplate).forEach(consumer);

        Result<TemplateStoreNew> result = Result.of(newStore);
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

    // for somme reason PMD does not recognize that the method IS used above
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private ResultStream<Instance> expandTemplateInstance(Instance instance, Signature from) {
        Result<Signature> toResult = templateStore.getSignature(instance.getIri());
        if (toResult.isEmpty()) {
            return ResultStream.of(toResult.map(s -> null));
        }
        if (cannotExpandTemplateInstance(instance, from, toResult.get())) {
            return ResultStream.innerOf(instance);
        }

        return performExpansion(instance).innerFlatMap(i -> expandTemplateInstance(i, toResult.get()));
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        if (cannotExpand(instance)) {
            return ResultStream.innerOf(instance);
        }

        return performExpansion(instance).innerFlatMap(this::expandInstance);
    }

    private ResultStream<Instance> performExpansion(Instance instance) {
        Result<Template> result = templateStore.getTemplate(instance.getIri());

        if (instance.hasListExpander()) {
            // TODO move to Instance class later
            Stream<Result<Instance>> expanded = instance.getListExpander().expand(instance.getArguments()).stream()
                    .map(args -> Instance.builder().iri(instance.getIri()).arguments(args).build())
                    .map(Result::of);
            return new ResultStream<>(expanded);
        } else {
            Result<Substitution> subst = result.flatMap(t -> Substitution.resultOf(instance.getArguments(), t.getParameters()));
            Result<Stream<Instance>> combination = Result.zip(result, subst, (t, s) -> t.getPattern().stream().map(i -> i.apply(s)));

            return combination.mapToStream(ResultStream::innerOf);
        }
    }

    private boolean cannotExpand(Instance instance) {
        return templateStore.containsBase(instance.getIri()) && !instance.hasListExpander();
    }

    /**
     * Checks if this edge can be expanded (i.e. not base and no optional variables),
     * but does not check for missing definitions.
     */
    private boolean cannotExpandTemplateInstance(Instance instance, Signature from, Signature to) {

        if (cannotExpand(instance)) {
            return true;
        }
        if (instance.hasListExpander() && cannotExpandExpander(instance)) {
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

    /**
     * Checks if this edge's listExpander can be expanded (i.e. no variable or blank marked for expansion),
     * but does not check for missing definitions.
     */
    private boolean cannotExpandExpander(Instance instance) {
        for (Argument arg : instance.getArguments()) {
            if (arg.isListExpander()
                    && (arg.getTerm().isVariable()
                    || arg.getTerm() instanceof BlankNodeTerm)) {
                return true;
            }
        }
        return false;
    }

}
