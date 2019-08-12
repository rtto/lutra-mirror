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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        for (Trace trace : traces) { // TODO: Traverse trace's traces
            msgs.addAll(trace.getMessages());
        }
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
        int mostSevere = Integer.MAX_VALUE;
        for (Trace trace : traces) {
            for (Message msg : trace.getMessages()) { // TODO: Traverse trace's traces
                if (!quiet) {
                    printMessage(msg);
                }
                if (Message.moreSevere(msg.getLevel(), mostSevere)) {
                    mostSevere = msg.getLevel();
                }
            }
            if (!trace.getMessages().isEmpty() && !quiet) {
                printLocation(trace);
            }
        }
        return mostSevere;
    }

    public static void printMessage(Message msg) {
        if (!quiet) {
            System.err.println("\n" + msg);
        }
    }

    private static String getLocation(Trace trace) {
        StringBuilder context = new StringBuilder();
        getLocationRecur(context, trace);
        return context.toString();
    }

    private static void getLocationRecur(StringBuilder context, Trace trace) {
        if (trace.hasLocation()) {
            context.append(" >>> at " + trace.getLocation().toString() + "\n");
        }
        if (trace.getTraces() != null) {
            getLocationRecur(context, trace.getTraces()); // TODO: Fix recur call (need branching)
        }
    }

    public static void printLocation(Trace trace) {
        if (!quiet) {
            System.err.print(getLocation(trace));
        }
    }
}
