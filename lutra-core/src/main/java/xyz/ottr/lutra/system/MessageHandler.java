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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MessageHandler {

    private final Set<Trace> traces;
    private final PrintStream initPrintStream;
    private PrintStream printStream;
    private boolean quiet;
    private String linePrefix = "";

    public MessageHandler(PrintStream printStream) {
        this.initPrintStream = printStream;
        this.printStream = printStream;
        this.traces = new LinkedHashSet<>();
    }

    public MessageHandler() {
        this(System.err);
    }

    public void setLinePrefix(String linePrefix) {
        this.linePrefix = linePrefix;
    }

    public void setQuiet(boolean quiet) {

        this.quiet = quiet;
        if (this.quiet) {
            // Ignore printing
            this.printStream = new PrintStream(OutputStream.nullOutputStream());
        } else {
            // reset to original
            this.printStream = this.initPrintStream;
        }
    }

    public void add(Message msg) {
        Trace t = new Trace();
        t.addMessage(msg);
        add(t);
    }

    public void add(Trace trace) {
        if (trace != null) {
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
        List<Trace> current = new LinkedList<>(this.traces);
        Set<Trace> visited = new HashSet<>(this.traces);

        while (!current.isEmpty()) {
            Trace t = current.remove(0);
            msgs.addAll(t.getMessages());
            for (Trace nt : t.getTrace()) {
                if (!visited.contains(nt)) {
                    current.add(nt);
                    visited.add(nt);
                }
            }
        }
        return msgs;
    }

    public Message.Severity getMostSevere() {
        return getMessages().stream()
            .max(Message.severityComparator)
            .map(m -> m.getSeverity())
            .orElse(Message.Severity.least());
    }

    /**
     * Prints all Message-s from all Result-s
     * as described in #getMessages() together
     * with a location derived from each Trace's
     * identifier, and returns an int representing
     * the level of the most severe Message.
     */
    public Message.Severity printMessages() {
        return printMessagesTo(s -> this.printStream.println(s));
    }

    public Message.Severity printMessagesTo(Consumer<String> output) {

        Message.Severity[] mostSevere = { Message.Severity.least() };

        Trace.visitPaths(this.traces, path -> {
            String indent = "";
            for (Trace trace : path) {
                for (Message msg : trace.getMessages()) {
                    this.printMessage(msg, indent, output);
                    if (msg.getSeverity().isGreaterEqualThan(mostSevere[0])) {
                        mostSevere[0] = msg.getSeverity();
                    }
                }
                if (!trace.getMessages().isEmpty()) {
                    indent = indent + "  ";
                }
            }
            if (path.stream().anyMatch(t -> t.hasLocation())) {
                this.printLocations(path, output);
            }
            output.accept("");
        });
        return mostSevere[0];
    }

    private void printLocations(Collection<Trace> traces, Consumer<String> output) {

        output.accept(addLinePrefix("Location(s):"));
        traces.stream()
            .filter(t -> t.hasLocation())
            .map(t -> "- " + t.getLocation())
            .map(this::addLinePrefix)
            .forEach(output);
    }

    public void printMessage(Message msg) {
        printMessage(msg, "", s -> this.printStream.println(s));
    }

    public void printMessage(Message msg, String indent, Consumer<String> output) {

        String prefixed = addLinePrefix(msg.toString(), indent);
        output.accept(prefixed);
    }

    public Optional<Message> toSingleMessage(String initialMessage) {
        StringBuilder str = new StringBuilder();
        var severity = printMessagesTo(s -> str.append(s));

        if (str.length() == 0) { // No messages added
            return Optional.empty();
        }
        str.insert(0, initialMessage + "\n");
        return Optional.of(new Message(severity, str.toString()));
    }

    private String addLinePrefix(String str) {
        return addLinePrefix(str, "");
    }

    private String addLinePrefix(String str, String indent) {
        return str.lines()
                .map(l -> this.linePrefix + indent + l)
                .collect(Collectors.joining("\n"));
    }
}
