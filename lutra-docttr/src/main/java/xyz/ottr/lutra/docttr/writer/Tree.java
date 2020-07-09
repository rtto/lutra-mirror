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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import xyz.ottr.lutra.Space;

// TODO this is a generic class which should be either
//  1. placed elsewhere,
//  2. replaced by standard API,
//  3. functionality moved into TemplateStore

@Getter
public class Tree<T> {

    @Setter(AccessLevel.PRIVATE) private Tree<T> parent;
    private final T root;
    private final List<Tree<T>> children;

    private Tree(Tree<T> parent, T root, List<Tree<T>> children) {
        this.parent = parent;
        this.root = root;
        this.children = children;
    }

    private Tree(Tree<T> parent, T root, Function<T, List<T>> buildFunction) {
        this(parent,
            root,
            buildFunction.apply(root).stream()
                .map(t -> new Tree<>(t, buildFunction))
                .collect(Collectors.toList()));
        // update parent pointer
        var that = this;
        this.children.forEach(c -> c.setParent(that));
    }

    public Tree(T root, Function<T, List<T>> buildFunction) {
        this(null, root, buildFunction);
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
            + Space.LINEBR
            + " | " + this.children.stream().map(c -> c.toString(indent + indent)).collect(Collectors.joining(Space.LINEBR));
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public boolean isRoot() {
        return this.parent == null;
    }

    public boolean isLeaf() {
        return !hasChildren();
    }

    public int getMaxDepth() {
        return isLeaf() ? 0 : 1 + this.children.stream().mapToInt(Tree::getMaxDepth).max().getAsInt();
    }

    public int getMinDepth() {
        return isLeaf() ? 0 : 1 + this.children.stream().mapToInt(Tree::getMinDepth).min().getAsInt();
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
    public interface Action<T, O> {
        O perform(Tree<T> tree);
    }
}
