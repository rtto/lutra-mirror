package xyz.ottr.lutra.system;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Setter;

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
    private final Collection<Message> messages;
    @Setter private static boolean deepTrace;
   
    protected Trace(Optional<String> location) {
        this.location = location;
        this.trace = new HashSet<>();
        if (deepTrace) {
            this.messages = new LinkedList<>();
        } else {
            this.messages = new HashSet<>();
        }
    }
    
    protected Trace() {
        this(Optional.empty());
    }

    public void setLocation(String location) {
        this.location = Optional.of(location);
    }
    
    protected static Trace fork(Collection<Trace> fs) {
        Trace fork = new Trace();
        if (fork.deepTrace) {
            for (Trace f : fs) {
                if (f.hasLocation() || f.hasMessages()) {
                    fork.trace.add(f);
                }
            }
        } else {
            fs.stream().forEach(f -> fork.addMessages(f.getMessages()));
        }
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
        if (deepTrace) {
            Set<Trace> visited = new HashSet<>();
            addTrace(elem, visited);
        } else {
            this.addMessages(elem.getMessages());
        }
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
            if (deepTrace) {
                if (elem.hasLocation() || elem.hasMessages()) {
                    this.trace.add(elem);
                } else {
                    elem.getTrace().forEach(this::addDirectTrace);
                }
            } else {
                this.addMessages(elem.getMessages());
            }
        }
    }

    protected void addMessage(Message msg) {
        this.messages.add(msg);
    }

    protected void addMessages(Collection<Message> msgs) {
        this.messages.addAll(msgs);
    }

    protected static void visitTraces(Collection<Trace> traces, Consumer<Trace> traceConsumer) {

        Set<Trace> visited = new HashSet<>();
        LinkedList<Trace> toVisit = new LinkedList<>(traces);
        while (!toVisit.isEmpty()) {
            Trace trace = toVisit.poll();
            traceConsumer.accept(trace);
            visited.add(trace);
            trace.trace.stream()
                .filter(t -> !visited.contains(t))
                .forEach(toVisit::add);
        }
    }
}
