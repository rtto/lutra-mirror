package xyz.ottr.lutra.system;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetUtils;

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

public class Trace {

    private Optional<String> location;
    private final Set<Trace> trace;
    private final List<Message> messages;
   
    protected Trace(Optional<String> location) {
        this.location = location;
        this.trace = new HashSet<>();
        this.messages = new LinkedList<>();
    }
    
    protected Trace() {
        this(Optional.empty());
    }

    public void setLocation(String location) {
        this.location = Optional.of(location);
    }
    
    protected static Trace fork(Collection<Trace> fs) {
        Trace fork = new Trace();
        fs.forEach(fork.trace::add);
        return fork;
    }
    
    protected static Trace fork(Trace... fs) {
        return fork(List.of(fs));
    }
    
    public boolean hasLocation() {
        return this.location.isPresent();
    }
    
    public String getLocation() {
        return this.location.get();
    }

    public Set<Trace> getTrace() {
        return this.trace;
    }

    public Collection<Message> getMessages() {
        return this.messages;
    }

    public boolean hasMessages() {
        return !this.messages.isEmpty();
    }

    /**
     * Adds the argument Trace at the end of the trace, so all children depend on that Trace
     * @param elem
     *      Trace element to add to this' trace
     */
    protected void addTrace(Trace elem) {
        addTrace(elem, new HashSet<>());
    }
    
    private void addTrace(Trace elem, Set<Trace> visited) {
        
        if (visited.contains(this)) {
            addDirectTrace(elem);
            return;
        }
        visited.add(this);
        if (this.trace.isEmpty()) {
            addDirectTrace(elem);
        } else {
            new HashSet<>(this.trace) // To not get ConcurrentModificationException
                .forEach(t -> t.addTrace(elem, visited));
        }
    }
    
    /**
     * Adds the argument Trace as a direct child in this' trace
     * @param elem
     *      Trace element to add to this' trace
     */
    protected void addDirectTrace(Trace elem) {

        if (!this.equals(elem)) {
            if (elem.hasLocation() || elem.hasMessages()) {
                this.trace.add(elem);
            } else {
                elem.getTrace().forEach(this::addDirectTrace);
            }
        }
    }

    protected void addMessage(Message msg) {
        this.messages.add(msg);
    }

    protected void addMessages(Collection<Message> msgs) {
        this.messages.addAll(msgs);
    }

    /**
     * Visits all paths trough the Trace-graph depth-first, starting from the roots,
     * feeding each path to the argument consumer.
     */
    protected static void visitPaths(Set<Trace> traces, Consumer<List<Trace>> pathConsumer) {
        
        Set<Trace> nonRoots = traces.stream()
            .flatMap(t -> t.trace.stream())
            .collect(Collectors.toSet());

        Set<Trace> roots = SetUtils.difference(traces, nonRoots);
        visitPathsFromNodes(roots, pathConsumer, new Stack<>());
    }

    private static void visitPathsFromNodes(Set<Trace> nodes,
            Consumer<List<Trace>> pathConsumer, Stack<Trace> currentPath) {

        if (nodes.isEmpty()) { // Reached end of path

            // If the path contains at least one Message, send it to consumer
            if (currentPath.stream().anyMatch(t -> !t.messages.isEmpty())) {
                pathConsumer.accept(new LinkedList<>(currentPath));
            }
        } else { // Traverse further

            for (Trace node : nodes) { // Make one path per node
                if (!currentPath.contains(node)) { //Avoid cycles
                    currentPath.push(node);
                    visitPathsFromNodes(node.trace, pathConsumer, currentPath);
                } else {
                    pathConsumer.accept(new LinkedList<>(currentPath));
                }
            }
        }
        if (!currentPath.empty()) {
            currentPath.pop();
        }
    }
}
