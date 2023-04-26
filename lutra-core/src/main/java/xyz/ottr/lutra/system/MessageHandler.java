package xyz.ottr.lutra.system;

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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import lombok.Setter;

public class MessageHandler {

    private static int count;

    private final Set<Trace> traces;
    private final PrintStream printStream;
    private final Set<String> printedMsgs;

    @Setter
    private boolean quiet;

    public MessageHandler(PrintStream printStream) {
        this.printStream = printStream;
        this.traces = new LinkedHashSet<>();
        this.printedMsgs = new LinkedHashSet<>();
    }

    public MessageHandler() {
        this(System.err);
    }

    public void add(Message msg) {
        Trace t = new Trace();
        t.addMessage(msg);
        add(t);
    }

    public void add(Trace trace) {
        if (trace != null
            && !(trace.getMessages().isEmpty() && trace.getTrace().isEmpty())) { // Avoid adding empty Trace

            this.traces.add(trace);
        }
    }

    public void add(Result<?> result) {
        if (result != null) {
            add(result.getTrace());
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
     * Applies a fresh ResultConumer with the argument consumer to the argument 
     * element, and prints messages using this' PrintStream. 
     *
     * @param element
     *     Element to applied the consumer to
     */
    public <T> Message.Severity use(Result<T> element, Consumer<T> consumer) {
        // TODO: This only depends on this.output and it not so natural,  perhaps move
        ResultConsumer<T> resConsumer = new ResultConsumer<>(consumer, this.printStream);
        resConsumer.accept(element);
        return resConsumer.getMessageHandler().printMessages();
    }

    /**
     * Returns a list of all Messages on any accepted Result,
     * and all Traces reachable via traces-pointers from
     * accepted Results. That is, it returns all Message-s
     * relevant for accepted Result-s.
     */
    public List<Message> getMessages() {
        List<Message> msgs = new LinkedList<>();
        Trace.visitTraces(this.traces, trace -> msgs.addAll(trace.getMessages()));
        return msgs;
    }

    public Message.Severity getMostSevere() {
        return visitMessagesAndTraces(_ignore -> { }, _ignore -> { });
    }

    /**
     * Visits all Messages on all Traces contained in this, and feeds them to the
     * Message consumer, and in case a Message on a Trace was fed to the Message consumer,
     * it also feeds the Trace to the Trace consumer.
     * @param msgConsumer
     *      Consumer that accepts all Messages on all Traces in this MessageHandler
     * @param traceConsumer
     *      Consumer that accepts all Traces containing at least one Message in this MessageHandler
     */
    private Message.Severity visitMessagesAndTraces(Consumer<Message> msgConsumer, Consumer<Trace> traceConsumer) {

        Message.Severity[] mostSevere = { Message.Severity.least() };

        Trace.visitTraces(this.traces, trace -> {
            for (Message msg : trace.getMessages()) {
                msgConsumer.accept(msg);
                if (msg.getSeverity().isGreaterEqualThan(mostSevere[0])) {
                    mostSevere[0] = msg.getSeverity();
                }
            }
            if (!trace.getMessages().isEmpty()) {
                traceConsumer.accept(trace);
            }
        });
        return mostSevere[0];
    }
    
    /**
     * Prints all Message-s from all Result-s
     * as described in #getMessages() together
     * with a location derived from each Trace's
     * identifier, and returns an int representing
     * the level of the most severe Message.
     */
    public Message.Severity printMessages() {
        var severity = visitMessagesAndTraces(this::printMessage, this::printLocation);
        return severity;
    }


    public void printMessage(Message msg) {
        if (!this.quiet && !this.printedMsgs.contains(msg.toString())) {
            this.printStream.println("\n" + msg);
            this.printedMsgs.add(msg.toString());
        }
    }

    private static String getLocation(Trace trace) {
        StringBuilder context = new StringBuilder();
        count = 0;
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
            context.append(toReferenceString(curRef, refs.get(trace)));
            return;
        } 
        refs.put(trace, makeReference(curRef));
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

    private static String makeReference(String ref) {
        count++;
        return "[" + count + ": " + ref + "]";
    }
    
    private static String toReferenceString(String curRef, String eqRef) {
        return "# >>> at " + makeReference(curRef) + " = " + eqRef + "\n";
    }
    
    private static String toLocationString(Trace trace, String enumStr) {
        return "# >>> at " + enumStr + " " + trace.getLocation() + "\n";
    }
    
    public void printLocation(Trace trace) {
        if (!this.quiet) {
            this.printStream.print(getLocation(trace));
        }
    }
    
    public Optional<Message> toSingleMessage(String initialMessage) {
        StringBuilder str = new StringBuilder();
        var severity = visitMessagesAndTraces(
            msg -> str.append(msg).append("\n"),
            trace -> str.append(getLocation(trace)));

        if (str.length() == 0) { // No messages added
            return Optional.empty();
        }
        str.insert(0, initialMessage + "\n");
        return Optional.of(new Message(severity, str.toString()));
    }

}
