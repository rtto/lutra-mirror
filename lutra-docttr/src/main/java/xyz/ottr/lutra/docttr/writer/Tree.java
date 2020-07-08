package xyz.ottr.lutra.docttr.writer;

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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import xyz.ottr.lutra.Space;

// TODO this is a generic class which should be either
//  1. placed elsewhere,
//  2. replaced by standard API,
//  3. functionality moved into TemplateStore

@Getter
public class Tree<T> {

    private final T root;
    private final List<Tree<T>> children;

    private Tree(T root, List<Tree<T>> children) {
        this.root = root;
        this.children = children;
    }

    public Tree(T root, Function<T, List<T>> buildFunction) {
        this(root,
            buildFunction.apply(root).stream()
            //.sorted()
            .map(t -> new Tree<>(t, buildFunction))
            .collect(Collectors.toList()));
    }

    public <O> Tree<O> map(Function<Tree<T>, O> applyFunction) {
        return new Tree<>(
            applyFunction.apply(this),
            this.children.stream()
                .map(c -> c.map(applyFunction))
                .collect(Collectors.toList()));
    }

    public void preorderForEach(Consumer<Tree<T>> consumer) {
        consumer.accept(this);
        this.children.forEach(c -> c.preorderForEach(consumer));
    }

    public String toString() {
        return toString("  ");
    }

    private String toString(String indent) {
        return indent
            + Objects.toString(root, "null")
            + this.children.stream().map(c -> c.toString(indent + indent)).collect(Collectors.joining(Space.LINEBR));
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }



    // https://stackoverflow.com/questions/26158082/how-to-convert-a-tree-structure-to-a-stream-of-nodes-in-java/37484430
    public Stream<Tree<T>> preorderStream() {
        return Stream.concat(Stream.of(this), this.getChildren().stream().flatMap(Tree::preorderStream));
    }

    public Stream<Tree<T>> postorderStream() {
        return Stream.concat(this.getChildren().stream().flatMap(Tree::postorderStream), Stream.of(this));
    }


    public <O> O apply(Action<T,O> action) {
        return action.perform(this);
    }

    @FunctionalInterface
    interface Action<T, O> {
        O perform(Tree<T> tree);
    }
}
