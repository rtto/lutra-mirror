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

    private final Set<Result<?>> results;

    public MessageHandler() {
        this.results = new HashSet<>();
    }

    public void add(Result<?> result) {
        if (result == null) {
            return;
        }

        Result<?> res = result.deriveContext();
        while (res != null && !this.results.contains(res)) {
            this.results.add(res);
            res = res.getParsedFrom();
        }
    }

    /**
     * Combines the results from argument MessageHandler into this,
     * and returns this (for chaining).
     */
    public MessageHandler combine(MessageHandler other) {
        other.results.forEach(this::add);
        return this;
    }

    /**
     * Returns a list of all Messages on any accepted Result,
     * and all Results reachable via parsedFrom-pointers from
     * accepted Results. That is, it returns all Message-s
     * relevant for accepted Result-s.
     */
    public List<Message> getMessages() {
        List<Message> msgs = new LinkedList<>();
        for (Result<?> res : results) {
            msgs.addAll(res.getMessages());
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
        return iterateMessages(!quiet);
    }
    
    public int getMostSevere() {
        return iterateMessages(false);
    }
    
    private int iterateMessages(boolean printMsgs) {
        int mostSevere = Integer.MAX_VALUE;
        for (Result<?> res : results) {
            for (Message msg : res.getMessages()) {
                if (printMsgs) {
                    printMessage(msg);
                }
                if (Message.moreSevere(msg.getLevel(), mostSevere)) {
                    mostSevere = msg.getLevel();
                }
            }
            if (!res.getMessages().isEmpty() && !quiet) {
                printContext(res);
            }
        }
        return mostSevere;
    }

    public static void printMessage(Message msg) {
        if (!quiet) {
            System.err.println("\n" + msg);
        }
    }

    private static String getContext(Result<?> res) {
        StringBuilder context = new StringBuilder();
        getContextRecur(context, res);
        return context.toString();
    }

    private static void getContextRecur(StringBuilder context, Result<?> res) {
        if (res.isPresent()) {
            context.append(" >>> in context: " + res.getContext() + "\n");
        }
        if (res.getParsedFrom() != null) {
            getContextRecur(context, res.getParsedFrom());
        }
    }

    public static void printContext(Result<?> res) {
        if (!quiet) {
            System.err.print(getContext(res));
        }
    }
    
    // TODO: Implement toSingleMessage that makes a single Message containing same
    // info as what would be printed with a call to iterateMessages(true)

    public Message toSingleMessage(String initialMessage) {
        // TODO Auto-generated method stub
        return null;
    }
}
