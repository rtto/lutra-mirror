package xyz.ottr.lutra.docttr;

/*-
 * #%L
 * xyz.ottr.lutra:lutra-docttr
 * %%
 * Copyright (C) 2018 - 2020 University of Oslo
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

import static j2html.TagCreator.details;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.span;
import static j2html.TagCreator.summary;

import j2html.tags.ContainerTag;
import java.util.Collection;
import java.util.List;

public abstract class TreeViewWriter<T> implements Tree.Action<T, ContainerTag> {

    protected abstract ContainerTag writeRoot(Tree<T> root);

    protected abstract Collection<Tree<T>> prepareChildren(List<Tree<T>> children);

    @Override
    public ContainerTag perform(Tree<T> tree) {

        var instance = writeRoot(tree);
        var children = prepareChildren(tree.getChildren());

        return tree.hasChildren()
            ? details(summary(instance), each(children, this::perform))
            : span(instance).withClass("leaf");
    }

    ContainerTag write(Tree<T> tree) {
        return div(tree.apply(this))
            .withClasses("treeview", "toggle-all", "click-first");
    }

}
