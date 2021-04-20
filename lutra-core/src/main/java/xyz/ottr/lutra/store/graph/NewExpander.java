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

import java.util.stream.Stream;
import xyz.ottr.lutra.model.BaseTemplate;
import xyz.ottr.lutra.model.Instance;
import xyz.ottr.lutra.model.Signature;
import xyz.ottr.lutra.model.Substitution;
import xyz.ottr.lutra.model.Template;
import xyz.ottr.lutra.store.TemplateStore;
import xyz.ottr.lutra.system.Result;
import xyz.ottr.lutra.system.ResultStream;

public class NewExpander implements Expander {

    private final TemplateStore templateStore;

    public NewExpander(TemplateStore templateStore) {
        this.templateStore = templateStore;
    }

    @Override
    public Result<? extends TemplateStore> expandAll() {
        // if(canExpand(instance, template))
        // performExpansion(instance).innerFlatMap(this::expandInstance);
        return null;
    }

    @Override
    public ResultStream<Instance> expandInstance(Instance instance) {
        if (isInstanceOfBaseTemplate(instance)) {
            return ResultStream.innerOf(instance);
        }

        return performExpansion(instance).innerFlatMap(this::expandInstance);
    }

    private ResultStream<Instance> performExpansion(Instance instance) {
        Result<Template> result = templateStore.getTemplate(instance.getIri());
        Result<Substitution> subst = result.flatMap(t -> Substitution.resultOf(instance.getArguments(), t.getParameters()));
        Result<Stream<Instance>> combination = Result.zip(result, subst, (t, s) -> t.getPattern().stream().map(i -> i.apply(s)));
        // TODO list expanders
        return combination.mapToStream(ResultStream::innerOf);
    }

    private boolean isInstanceOfBaseTemplate(Instance instance) {
        return templateStore.containsBase(instance.getIri());
    }

    /**
     * Checks if this edge can be expanded (i.e. not base and no optional variables),
     * but does not check for missing definitions.
     */
    public boolean canExpand(Instance instance, Signature signature) {

        if (signature instanceof BaseTemplate) {
            return false;
        }
        if (isInstanceOfBaseTemplate(instance)) {
            return true;
        }
        /*for (int i = 0; i < this.argumentList.size(); i++) {
            Term arg = this.argumentList.get(i).getTerm();
            if (arg.isVariable() && !this.to.isOptional(i) && this.from.isOptional(arg)) {
                return false;
            }
        }*/
        return true;
    }

    /**
     * Checks if this edge's listExpander can be expanded (i.e. no variable or blank marked for expansion),
     * but does not check for missing definitions.
     */
    public boolean canExpandExpander() {
        /*for (Argument arg : this.argumentList) {
            if (arg.isListExpander()
                    && (arg.getTerm().isVariable()
                    || arg.getTerm() instanceof BlankNodeTerm)) {
                return false;
            }
        }*/
        return true;
    }

}
