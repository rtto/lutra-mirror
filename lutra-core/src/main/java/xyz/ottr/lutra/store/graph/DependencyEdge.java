package xyz.ottr.lutra.store.graph;

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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import xyz.ottr.lutra.model.Argument;
import xyz.ottr.lutra.model.ListExpander;
import xyz.ottr.lutra.model.terms.BlankNodeTerm;
import xyz.ottr.lutra.model.terms.NoneTerm;
import xyz.ottr.lutra.model.terms.Term;
import xyz.ottr.lutra.system.Result;

@EqualsAndHashCode
@AllArgsConstructor
public class DependencyEdge {

    // TODO I have added an listExpander, as this is no longer available from the deleted class ArgumentList,
    // but it seems a more thorough refactoring is necessary which takes in to account that Instance contains
    // the listExpander and the argument list and the to-template.
    public final TemplateNode from;
    public final @NonNull List<Argument> argumentList;
    public final ListExpander listExpander;
    public final @NonNull TemplateNode to;

    boolean hasListExpander() {
        return Objects.nonNull(listExpander);
    }

    public boolean shouldDiscard() {

        // Should discard this instance if it contains none at a non-optional position
        for (int i = 0; i < this.argumentList.size(); i++) {
            if (this.argumentList.get(i).getTerm() instanceof NoneTerm
                && !this.to.isOptional(i)
                && !this.to.getParameters().get(i).hasDefaultValue()) {
                return true;
            }
        }
        return false;
    }

    public boolean isInstance() {
        return Objects.isNull(this.from);
    }

    /**
     * Checks if this edge can be expanded (i.e. not base and no optional variables),
     * but does not check for missing definitions.
     */
    public boolean canExpand() {

        if (this.to.isBase()) {
            return false;
        }
        if (this.isInstance()) {
            return true;
        }
        for (int i = 0; i < this.argumentList.size(); i++) {
            Term arg = this.argumentList.get(i).getTerm();
            if (arg.isVariable() && !this.to.isOptional(i) && this.from.isOptional(arg)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this edge's listExpander can be expanded (i.e. no variable or blank marked for expansion),
     * but does not check for missing definitions.
     */
    public boolean canExpandExpander() {
        for (Argument arg : this.argumentList) {
            if (arg.isListExpander()
                && (arg.getTerm().isVariable()
                    || arg.getTerm() instanceof BlankNodeTerm)) {
                return false;
            }
        }
        return true;
    }

    public Set<Result<DependencyEdge>> expandListExpander() {
        return this.listExpander.expand(this.argumentList).stream()
            .map(args -> new DependencyEdge(this.from, args, null, this.to))
            .map(Result::of)
            .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return Objects.toString(this.from, "")
            + Objects.toString(this.argumentList, "")
            + " --> " + Objects.toString(this.to, "");
    }

    /* TODO: remove if lombok annotations work

        @Override
        public int hashCode() {
            return Objects.hash(this.from, this.argumentList, this.to);
        }

        @Override
        public boolean equals(Object o) {
            return this == o
                || o != null
                && this.getClass().equals(o.getClass())
                && Objects.equals(this.from, ((DependencyEdge) o).from)
                && Objects.equals(this.argumentList, ((DependencyEdge) o).argumentList)
                && Objects.equals(this.to, ((DependencyEdge) o).to);
        }
    */

}



