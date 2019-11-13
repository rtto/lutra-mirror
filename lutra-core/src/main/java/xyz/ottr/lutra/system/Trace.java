package xyz.ottr.lutra.system;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

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

    // Used to construct a printable identifier, which is stored within the Trace
    // object to provide in the trace. This is used by a MessageHandler
    // to give a location to the Messages printed. The default is just a slightly modified
    // toString-representation of the original object, prefixed with the object's Class,
    // but one can override this to get a different identifier.
    private static Function<Object, Optional<String>> toIdentifier = obj -> { 
        if (obj == null) {
            return Optional.empty();
        }

        int maxLength = 60;
        String prefix = "(" + obj.getClass().getName() + ") ";
        String content = obj.toString();
        String id = prefix
            + (content.length() <= maxLength ? content : content.substring(0, maxLength) + " ...");
        return Optional.of(id);
    };

    /** 
     * Sets the argument function to be the method to construct a printable identifier, which is
     * stored within a Trace object.
     * This identifier is then used by a MessageHandler to give a context to the Messages printed. 
     * The default is just a slightly modifier Object#toString() prefixed with the Class-name
     */
    public static void setToIdentifierFunction(Function<Object, Optional<String>> fun) {
        toIdentifier = fun;
    }

    private final Optional<String> identifier;
    private final Set<Trace> trace;
    private final List<Message> messages;
   
    protected Trace(Optional<?> value) {
        this.identifier = value.map(o -> (Object) o).flatMap(toIdentifier);
        this.trace = new HashSet<>();
        this.messages = new LinkedList<>();
    }
    
    protected Trace() {
        this(Optional.empty());
    }
    
    protected static Trace fork(Collection<Trace> fs) {
        Trace fork = new Trace();
        fork.trace.addAll(fs);
        return fork;
    }
    
    protected static Trace fork(Trace... fs) {
        return fork(Arrays.asList(fs));
    }
    
    public boolean hasIdentifier() {
        return this.identifier.isPresent();
    }
    
    public String getIdentifier() {
        return this.identifier.get();
    }

    public Set<Trace> getTrace() {
        return this.trace;
    }

    public List<Message> getMessages() {
        return this.messages;
    }
    
    /**
     * Adds the argument Trace at the end of the trace, so all children depend on that Trace
     * @param elem
     *      Trace element to add to this' trace
     */
    protected void addTrace(Trace elem) {
        Set<Trace> visited = new HashSet<>();
        addTrace(elem, visited);
    }
    
    protected void addTrace(Trace elem, Set<Trace> visited) {
        
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
            this.trace.add(elem);
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
            trace.getTrace().stream()
                .filter(t -> !visited.contains(t))
                .forEach(t -> toVisit.add(t));
        }
    }
}
