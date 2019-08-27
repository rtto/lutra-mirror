package xyz.ottr.lutra.result;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class MessageHandler {

    private static boolean quiet = false;

    public static void setQuiet(boolean shouldBeQuiet) {
        quiet = shouldBeQuiet;
    }

    private final Set<Trace> traces;

    public MessageHandler() {
        this.traces = new HashSet<>();
    }

    public void add(Trace trace) {
        if (trace != null) {
            this.traces.add(trace);
        }
    }

    public void add(Result<?> result) {
        if (result != null) {
            this.traces.add(Trace.from(result));
        }
    }

    /**
     * Combines the results from argument MessageHandler into this,
     * and returns this (for chaining).
     */
    public MessageHandler combine(MessageHandler other) {
        other.traces.forEach(this::add);
        return this;
    }

    /**
     * Returns a list of all Messages on any accepted Result,
     * and all Traces reachable via traces-pointers from
     * accepted Results. That is, it returns all Message-s
     * relevant for accepted Result-s.
     */
    public List<Message> getMessages() {
        List<Message> msgs = new LinkedList<>();
        visitTraces(trace -> msgs.addAll(trace.getMessages()));
        return msgs;
    }
    
    /**
     * Prints all Message-s from all Result-s
     * as described in #getMessages() together
     * with a context derived from each Result's
     * parsedFrom, and returns an int representing
     * the level of the most severe Message.
     */
    public int printMessages() {

        // int is wrapped in an array as it needs to be final as used in closure below
        int[] mostSevere = new int[] {Integer.MAX_VALUE}; 

        visitTraces(trace -> {
            for (Message msg : trace.getMessages()) {
                printMessage(msg);
                if (Message.moreSevere(msg.getLevel(), mostSevere[0])) {
                    mostSevere[0] = msg.getLevel();
                }
            }
            if (!trace.getMessages().isEmpty()) {
                printLocation(trace);
            }
        });
        return mostSevere[0];
    }

    private void visitTraces(Consumer<Trace> traceConsumer) {

        Set<Trace> visited = new HashSet<>();
        LinkedList<Trace> toVisit = new LinkedList<>(this.traces);
        while (!toVisit.isEmpty()) {
            Trace trace = toVisit.poll();
            traceConsumer.accept(trace);
            visited.add(trace);
            trace.getTrace().stream()
                .filter(t -> !visited.contains(t))
                .forEach(t -> toVisit.add(t));
        }
    }

    public static void printMessage(Message msg) {
        if (!quiet) {
            System.err.println("\n" + msg);
        }
    }

    private static String getLocation(Trace trace) {
        StringBuilder context = new StringBuilder();
        getLocationRecur(context, trace, "1", new HashMap<>());
        return context.toString();
    }

    /**
     * Writes out a stack trace and enumerates the elements of the stack
     * trace with an enumeration (dot separated number sequence, e.g. 1.3.2)
     * such that siblings get their parents enumeration appended with a number
     * denoting its sibling number (e.g. a node with enumeration 1.2 and three
     * children will make the three children get enumeration 1.2.1, 1.2.2, and 1.2.3).
     * This enumeration is then used to reference already visited trace elements.
     */
    private static void getLocationRecur(StringBuilder context, Trace trace,
            String curRef, Map<Trace, String> refs) {
        
        if (refs.containsKey(trace)) {
            // Already printed subtrace, just reference to its enumeration and returns
            context.append(toReferenceString(refs.get(trace)));
            return;
        } 
        refs.put(trace, curRef);
        if (trace.hasLocation()) {
            // Assign enumeration to trace element, and append to trace
            context.append(toLocationString(trace, refs.get(trace)));
        }

        int c = 1;
        for (Trace child : trace.getTrace()) {
            getLocationRecur(context, child, curRef + "." + c, refs); 
            c++;
        }
    }
    
    private static String toReferenceString(String enumStr) {
        return " >>> at [" + enumStr + "]\n";
    }
    
    private static String toLocationString(Trace trace, String enumStr) {
        return " >>> at [" + enumStr + "] " + trace.getLocation().toString() + "\n";
    }
    
    public static void printLocation(Trace trace) {
        if (!quiet) {
            System.err.print(getLocation(trace));
        }
    }
}
