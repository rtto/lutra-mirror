package xyz.ottr.lutra.result;

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

    // Used to construct a printable context, which is stored within a Result
    // object in the parsedFrom-pointer. This is used by a ResultConsumer
    // to give a context to the Messages printed. The default is just the
    // toString-representation of the original object, but one can override
    // this to get a different context object.
    private static Function<Object, ?> toLocation = obj -> { // TODO: Rename to identifier
        if (obj == null) {
            return null;
        }
        String str = obj.toString();
        String prefix = "(" + obj.getClass().getName() + ") ";
        if (str.length() <= 60) {
            return prefix + str;
        } else {
            return prefix + str.substring(0, 60) + "...";
        }
    };

    /** 
     * Sets the argument function to be the method to construct a printable context, which is
     * stored within a Result object in the parsedFrom-pointer.
     * This context is then used by a ResultConsumer to give a context to the Messages printed. 
     * The default is just Object#toString()
     */
    public static void setToLocationFunction(Function<Object, ?> fun) {
        toLocation = fun;
    }

    private final Optional<?> location;
    private final Set<Trace> trace;
    private final List<Message> messages;
   
    protected Trace(Optional<?> value) {
        this.location = value.map(toLocation);
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
    
    public boolean hasLocation() {
        return this.location.isPresent();
    }
    
    public Object getLocation() {
        return this.location.get();
    }

    public Set<Trace> getTrace() {
        return this.trace;
    }

    public List<Message> getMessages() {
        return this.messages;
    }
    
    protected void addTrace(Trace elem) {
        Set<Trace> subTree = new HashSet<>();
        visitTraces(Arrays.asList(elem), t -> subTree.add(t));
        addTrace(elem, subTree);
    }
    
    protected void addTrace(Trace elem, Set<Trace> subTree) {
        
        // TODO: Fix this, not all nodes gets added.
        //       Maybe merge subtrees, so result contains all nodes
        if (subTree.contains(this)) {
            return;
        }
        
        if (this.trace.isEmpty()) {
            this.trace.add(elem);
        } else {
            this.trace.forEach(t -> t.addTrace(elem, subTree));
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
